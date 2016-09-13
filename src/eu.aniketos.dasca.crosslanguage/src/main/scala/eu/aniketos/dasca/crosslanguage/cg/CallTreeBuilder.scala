/*
 * (C) Copyright 2016   The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.cg;

import com.ibm.wala.ipa.callgraph.CGNode
import eu.aniketos.dasca.crosslanguage.builder.MergedCallGraph
import scala.collection.JavaConversions._
import com.ibm.wala.classLoader.CallSiteReference

object CallTreeBuilder {

  def buildCallForest(cg: MergedCallGraph, sources: List[CGNode], sinks: List[CGNode]): List[CallTree] = {
    buildCallForest(cg, 0, sources, sinks)
  }

  def buildCallForest(cg: MergedCallGraph, bound: Integer, sources: List[CGNode], sinks: List[CGNode]): List[CallTree] = {
    return sources.map { src => buildCallTree(cg, bound, src, sinks) }.filterNot { x => null == x }
  }

  def buildCallTree(cg: MergedCallGraph, root: CGNode, sinks: List[CGNode]): CallTree = {
    buildCallTree(cg, 0, root, sinks)
  }

  def buildCallTree(cg: MergedCallGraph, bound: Integer, root: CGNode, sinks: List[CGNode]): CallTree = {
    build(cg, List[CGNode](), sinks, bound, 0, root)
  }

  private def build(cg: MergedCallGraph, visited: List[CGNode], sinks: List[CGNode], bound: Integer,
    depth: Integer, root: CGNode): CallTree = {
    if (bound > 0 && bound < depth) {
      if (null == sinks) {
        return new CallTree(root)
      } else {
        return null
      }
    }

    // Base case
    // root is one of the sinks (i.e., root is part of the analysed project and, thus, the 
    // call graph
    if (sinks.contains(root))
        return new CallTree(root)
    
    // root is only part of the call sites iterator (i.e., defined outside of the analysis 
    // scope
    val it = root.iterateCallSites()
    var m = null: CallSiteReference
    var children = List[CGNode]()
    while (it.hasNext()) {
      m = it.next()
      children = children.union(sinks.filter { x => x.getMethod.getName().equals(m.getDeclaredTarget().getName()) })
    }
    if (!children.isEmpty) {
      return new CallTree(root, children.map { x => new CallTree(x) })
    }

    // Recursive case
    val targetNodes = cg.getAllPossibleTargetNodes(root).filterNot { n => visited.contains(n) }
    if (targetNodes.isEmpty) {
      return null
    } else {
      val children = targetNodes.toList.map { x => build(cg, root :: (visited ++ targetNodes), sinks, bound, depth + 1, x) }
        .filterNot { x => (null == x) }
      if (children.isEmpty || null == children) {
        return null
        //return new CallTree(root, children)
      } else {
        return new CallTree(root, children)
      }
    }
  }
}

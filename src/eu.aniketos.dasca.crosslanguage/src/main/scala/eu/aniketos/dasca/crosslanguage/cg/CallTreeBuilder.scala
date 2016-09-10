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

object CallTreeBuilder {
  def buildCallForest(cg: MergedCallGraph, sources: List[CGNode], sinks: List[CGNode]): List[CallTree] = {
    return sources.map { src => buildCallTree(cg, src, sinks) }.filter { x => null != x }
  }

  def buildCallTree(cg: MergedCallGraph, root: CGNode, sinks: List[CGNode]): CallTree = {
    build(cg, List[CGNode](), sinks, root)
  }

  private def build(cg: MergedCallGraph, visited: List[CGNode], sinks: List[CGNode], root: CGNode): CallTree = {
    if (sinks.contains(root)) {
      return new CallTree(root)
    } else {
      val targetNodes = cg.getAllPossibleTargetNodes(root).filterNot { n => visited.contains(n) }
      if (targetNodes.isEmpty) {
        return null
      } else {
        val children = targetNodes.toList.map { x => build(cg, visited ++ targetNodes, sinks, x) }
          .filterNot { x => (null == x) }
        return new CallTree(root, children)
      }

    }
  }
}

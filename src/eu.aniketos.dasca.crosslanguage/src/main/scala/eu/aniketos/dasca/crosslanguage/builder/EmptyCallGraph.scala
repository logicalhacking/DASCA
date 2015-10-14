/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.builder

import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.types.MethodReference
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.util.intset.IntSet
import com.ibm.wala.ipa.cha.IClassHierarchy
import java.util.Collection
import scala.collection.JavaConverters._

class EmptyCallGraph extends CallGraph {
  def addEdge(x$1: CGNode, x$2: CGNode): Unit = {}

  def addNode(x$1: CGNode): Unit = {}

  def containsNode(x$1: CGNode): Boolean = false

  def getClassHierarchy(): IClassHierarchy = null

  def getEntrypointNodes(): Collection[CGNode] = List().asJava

  def getFakeRootNode(): CGNode = null

  def getMaxNumber(): Int = 0

  def getNode(x$1: IMethod, x$2: Context): CGNode = null

  def getNode(x$1: Int): CGNode = null

  def getNodes(x$1: MethodReference): java.util.Set[CGNode] = Set().asJava

  def getNumber(x$1: CGNode): Int = 0

  def getNumberOfNodes(): Int = 0

  def getNumberOfTargets(x$1: CGNode, x$2: CallSiteReference): Int = 0

  def getPossibleSites(x$1: CGNode, x$2: CGNode): java.util.Iterator[CallSiteReference] = Iterator.empty.asJava

  def getPossibleTargets(x$1: CGNode, x$2: CallSiteReference): java.util.Set[CGNode] = Set().asJava

  def getPredNodeCount(x$1: CGNode): Int = 0

  def getPredNodeNumbers(x$1: CGNode): IntSet = null

  def getPredNodes(x$1: CGNode): java.util.Iterator[CGNode] = Iterator.empty.asJava

  def getSuccNodeCount(x$1: CGNode): Int = 0

  def getSuccNodeNumbers(x$1: CGNode): IntSet = null

  def getSuccNodes(x$1: CGNode): java.util.Iterator[CGNode] = Iterator.empty.asJava

  def hasEdge(x$1: CGNode, x$2: CGNode): Boolean = false

  def iterateNodes(x$1: IntSet): java.util.Iterator[CGNode] = Iterator.empty.asJava

  def iterator(): java.util.Iterator[CGNode] = Iterator.empty.asJava

  def removeAllIncidentEdges(x$1: CGNode): Unit = {}

  def removeEdge(x$1: CGNode, x$2: CGNode): Unit = {}

  def removeIncomingEdges(x$1: CGNode): Unit = {}

  def removeNode(x$1: CGNode): Unit = {}

  def removeNodeAndEdges(x$1: CGNode): Unit = {}

  def removeOutgoingEdges(x$1: CGNode): Unit = {}

  def getFakeWorldClinitNode(): CGNode = {
    ???
  }
}

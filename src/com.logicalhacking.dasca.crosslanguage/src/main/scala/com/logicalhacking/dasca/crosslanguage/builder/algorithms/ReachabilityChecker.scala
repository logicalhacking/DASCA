/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.crosslanguage.builder.algorithms

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.mutable.Queue
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.scalalogging.Logger
import scala.collection.mutable.ListBuffer
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction

abstract class ReachabilityChecker(private val cg: CallGraph, private val keep: CGNode => Boolean) {
  val logger = Logger(LoggerFactory.getLogger(getClass.toString))
  val cache = scala.collection.mutable.Map[(CGNode, CGNode, SSAInstruction), Boolean]()

  def isReachable(fromNode: CGNode, toNode: CGNode, inst: SSAInstruction): Boolean = {
    if (cache.contains((fromNode, toNode, inst))) return cache((fromNode, toNode, inst))

    val instructions = if (fromNode == toNode) {
      List(inst)
    } else {
      getReachableInstructions(fromNode, toNode)
    }

    val ecfg = ExplodedControlFlowGraph.make(fromNode.getIR)

    val res = instructions.exists { inst => isReachable(ecfg, inst) }
    cache((fromNode, toNode, inst)) = res
    res
  }

  def getReachableInstructions(fromNode: CGNode, toNode: CGNode): List[SSAInstruction] = {
    val lb = ListBuffer[SSAInstruction]()

    if (!keep(fromNode) || !keep(toNode)) return List()

    val queue = Queue[CGNode](toNode)
    val visited = scala.collection.mutable.Set[CGNode]()

    while (!queue.isEmpty) {
      val cur = queue.dequeue()

      if (!visited.contains(cur)) {
        visited += cur

        for (pred <- cg.getPredNodes(cur).asScala ++ extraPredNodes(cur) if keep(pred)) {
          if (pred == fromNode) {
            for (csr <- cg.getPossibleSites(pred, cur).asScala;
              invoke <- pred.getIR.getCalls(csr)) {
              lb += invoke
            }
          } else {
            queue += pred
          }
        }
      }
    }

    lb.toList
  }

  private def isReachable(ecfg: ExplodedControlFlowGraph, inst: SSAInstruction): Boolean = {
    ecfg.getBlockForInstruction(inst.iindex)

    val queue = Queue[IExplodedBasicBlock](ecfg.getBlockForInstruction(inst.iindex))
    val visited = scala.collection.mutable.Set[IExplodedBasicBlock]()

    while (!queue.isEmpty) {
      val cur = queue.dequeue()

      if (!visited.contains(cur)) {
        visited += cur

        if (cur == ecfg.entry())
          return true

        for (pred <- ecfg.getPredNodes(cur).asScala if canPassThrough(cur, pred)) {
          queue += pred
        }
      }
    }

    return false
  }

  def extraPredNodes(node: CGNode): List[CGNode]

  def canPassThrough(toBB: IExplodedBasicBlock, predBB: IExplodedBasicBlock): Boolean
}

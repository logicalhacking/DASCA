/*
 * Copyright (c) 2010-2015 SAP SE.
 *               2016-2018 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.logicalhacking.dasca.crosslanguage.builder.algorithms

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import org.slf4j.LoggerFactory

import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction
import com.ibm.wala.ssa.SSAConditionalBranchInstruction
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.scalalogging.Logger

import scala.{ Option => ? }

class ExecuteActionBasedChecker(val cg: CallGraph,
                                val keep: CGNode => Boolean,
                                val action: String,
                                val execNode: CGNode) extends ReachabilityChecker(cg, keep) {
  val symbolTable = execNode.getIR.getSymbolTable
  override val logger = Logger(LoggerFactory.getLogger(getClass.toString))

  override def canPassThrough(toBB: IExplodedBasicBlock,
                              predBB: IExplodedBasicBlock): Boolean = {
    // Asuming both basic blocks are from the execNode
    predBB.getLastInstruction match {
      case cond: SSAConditionalBranchInstruction if cond.getOperator == IConditionalBranchInstruction.Operator.EQ => {
        val vl = cond.getUse(0)
        val vr = cond.getUse(1)
        // The right side (vr) is apparently always either one or zero in conditional eq instructions

        // We are only looking for equal calls on the second method parameter of the cordova execute method,
        // which is "action"
        val nonActionUseInEquals = isEqualsAndDependsOnActionParam(vl)

        if (nonActionUseInEquals != -1 && symbolTable.isConstant(nonActionUseInEquals) &&
          symbolTable.isStringConstant(nonActionUseInEquals)) {
          val actionString = symbolTable.getStringValue(nonActionUseInEquals)
          if (actionString.equals(action) && !isTrueBranch(vr, toBB, predBB)) {
            return false
          } else if (!actionString.equals(action) && isTrueBranch(vr, toBB, predBB)) {
            return false
          } else {
            return true
          }
        } else {
          return true
        }
      }
      case _ => true
    }
  }

  def isEqualsAndDependsOnActionParam(v: Int): Int = {
    for (
      invoke <- ?(execNode.getDU.getDef(v)).collect({ case i: SSAAbstractInvokeInstruction => i });
      if (invoke.getDeclaredTarget.toString().contains("equal"))
    ) {
      if (invoke.getUse(0) == 2) {
        return invoke.getUse(1)
      } else if (invoke.getUse(1) == 2) {
        return invoke.getUse(0)
      }
    }

    return -1
  }

  def isTrueBranch(v: Int,
                   toBB: IExplodedBasicBlock,
                   predBB: IExplodedBasicBlock): Boolean = {
    if (symbolTable.isOneOrTrue(v) && predBB.getLastInstructionIndex + 1 != toBB.getLastInstructionIndex) {
      return true
    } else if (symbolTable.isZeroOrFalse(v) && predBB.getLastInstructionIndex + 1 == toBB.getLastInstructionIndex) {
      return true
    } else {
      return false
    }
  }

  def extraPredNodes(node: CGNode): List[CGNode] = {
    if (node.getMethod.getName.toString().equals("run")) {
      for (
        interf <- node.getMethod.getDeclaringClass.getAllImplementedInterfaces.asScala;
        if (interf.getName().toString().equals("Ljava/lang/Runnable"));
        n <- cg.iterator().asScala;
        if (n.getMethod.getDeclaringClass == node.getMethod.getDeclaringClass);
        if (n.getMethod.getName.toString() == "<init>")
      ) {
        return List(n)
      }
    }
    return List.empty
  }
}

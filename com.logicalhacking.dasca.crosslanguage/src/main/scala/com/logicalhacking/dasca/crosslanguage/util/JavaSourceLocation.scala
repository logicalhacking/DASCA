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

package com.logicalhacking.dasca.crosslanguage.util

import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ssa.SSAInstruction

class JavaSourceLocation(val line: Int, val filePath: String) extends SourceLocation with Equals {

  override def toString = s"$filePath:$line"

  override def equals(other: Any) = other match {
    case o: JavaSourceLocation => line == o.line && filePath == o.filePath
    case _ => false
  }

  def canEqual(other: Any) = {
    other.isInstanceOf[com.logicalhacking.dasca.crosslanguage.util.JavaSourceLocation]
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime + line.hashCode) + filePath.hashCode
  }

}

object JavaSourceLocation {
  def apply(node: CGNode, inst: SSAInstruction): JavaSourceLocation = {
    val (line, path, className, method) = Util.getJavaSourceInfo(node, inst)
    new JavaSourceLocation(line, path)
  }

  def apply(node: CGNode, csr: CallSiteReference): JavaSourceLocation = apply(node, node.getIR.getCalls(csr)(0))

  def apply(node: CGNode): JavaSourceLocation = apply(node, node.getIR.getInstructions.find({ i => i != null }).get)
}

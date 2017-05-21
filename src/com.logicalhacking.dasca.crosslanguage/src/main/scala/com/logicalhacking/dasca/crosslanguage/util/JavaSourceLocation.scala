/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
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

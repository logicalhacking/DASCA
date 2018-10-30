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

import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.cast.js.html.IncludedPosition
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.cast.ir.ssa.AstIRFactory.AstIR

class JavaScriptSourceLocation(val line: Int, val column: Int, val filePath: String) extends SourceLocation with Equals {

  override def toString = s"$filePath:$line:$column"

  override def equals(other: Any) = other match {
    case o: JavaScriptSourceLocation => line == o.line && column == o.column && filePath == o.filePath
    case _ => false
  }

  def canEqual(other: Any) = {
    other.isInstanceOf[com.logicalhacking.dasca.crosslanguage.util.JavaScriptSourceLocation]
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime * (prime + line.hashCode) + column.hashCode) + filePath.hashCode
  }
}

object JavaScriptSourceLocation {
  val JavaScriptPathRegex = """.+/assets/(.+)""".r

  def apply(ir: AstIR, inst: SSAInstruction): JavaScriptSourceLocation = {
    val (line:Int, col:Int, _, _, relPath:String) = Util.getJavaScriptSourceInfo(ir, inst)
    new JavaScriptSourceLocation(line, col, relPath)
  }

  def apply(ir: AstIR, csr: CallSiteReference): JavaScriptSourceLocation = apply(ir, ir.getCalls(csr)(0))

  def apply(ir: AstIR): JavaScriptSourceLocation = apply(ir, ir.getInstructions.find({ i => i != null }).get)
}

/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.util

import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.cast.js.html.IncludedPosition

class JavaScriptSourceLocation(val line: Int, val column: Int, val filePath: String) extends SourceLocation with Equals {

  override def toString = s"$filePath:$line:$column"

  override def equals(other: Any) = other match {
    case o: JavaScriptSourceLocation => line == o.line && column == o.column && filePath == o.filePath
    case _ => false
  }

  def canEqual(other: Any) = {
    other.isInstanceOf[eu.aniketos.dasca.crosslanguage.util.JavaScriptSourceLocation]
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime * (prime + line.hashCode) + column.hashCode) + filePath.hashCode
  }
}

object JavaScriptSourceLocation {
  val JavaScriptPathRegex = """.+/assets/(.+)""".r

  def apply(ir: AstIRFactory#AstIR, inst: SSAInstruction): JavaScriptSourceLocation = {
    val (line, col, _, _, relPath) = Util.getJavaScriptSourceInfo(ir, inst)
    new JavaScriptSourceLocation(line, col, relPath)
  }

  def apply(ir: AstIRFactory#AstIR, csr: CallSiteReference): JavaScriptSourceLocation = apply(ir, ir.getCalls(csr)(0))

  def apply(ir: AstIRFactory#AstIR): JavaScriptSourceLocation = apply(ir, ir.getInstructions.find({ i => i != null }).get)
}

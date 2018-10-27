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

import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.classLoader.Language
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.classLoader.CallSiteReference
import scala.collection.mutable.ListBuffer
import java.io.File
import com.ibm.wala.cast.js.html.IncludedPosition
import com.typesafe.scalalogging.Logger
import java.util.concurrent.TimeUnit
import com.ibm.wala.classLoader.IClass
import scala.collection.mutable.LinkedHashSet
import com.logicalhacking.dasca.crosslanguage.builder.CrossBuilderOption
import com.logicalhacking.dasca.crosslanguage.builder.FilterJavaCallSites
import com.logicalhacking.dasca.crosslanguage.builder.MockCordovaExec
import com.logicalhacking.dasca.crosslanguage.builder.ReplacePluginDefinesAndRequires
import com.logicalhacking.dasca.crosslanguage.builder.FilterJSFrameworks
import com.logicalhacking.dasca.crosslanguage.builder.PreciseJS
import com.logicalhacking.dasca.crosslanguage.builder.RunBuildersInParallel
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.cast.ir.ssa.AstIRFactory.AstIR

object Util {
  val cachedDalvikLines = Map[(CGNode, SSAInstruction), Int]()

  def derivesFromCordovaPlugin(cls: IClass): Boolean = cls != null &&
    (cls.getName.toString == "Lorg/apache/cordova/CordovaPlugin" || derivesFromCordovaPlugin(cls.getSuperclass))

  def getJavaSourceInfo(node: CGNode, inst: SSAInstruction) = {
    val path = node.getMethod.getReference.getDeclaringClass.getName.toString().substring(1)
    val className = path.substring(path.lastIndexOf('/') + 1)
    val method = node.getMethod.getName.toString()
    val line = node.getMethod.getLineNumber(inst.iindex)
    (line, path, className, method)
  }

  def hasSomeParentWithName(name: String, file: File): Boolean = {
    val parent = file.getParentFile
    parent != null && (parent.getName == name || hasSomeParentWithName(name, parent))
  }

  val JavaScriptPathRegex = """.+/assets/(.+)""".r
  def getJavaScriptSourceInfo(ir:AstIR, inst: SSAInstruction) = {
    val sourcePos = ir.getMethod.getSourcePosition(inst.iindex) match {
      case iPos: IncludedPosition => iPos.getIncludePosition
      case sp => sp
    }
    val relPath = sourcePos.getURL.getFile match {
      case JavaScriptPathRegex(p) => p
      case _ => sourcePos.getURL.toString()
    }
    val (line, col, start, end) = FileMappingStore.map.find(_._1.toURI().toURL() == sourcePos.getURL).map(_._2) match {
      case Some(rec) => rec.translate(sourcePos)
      case None => (sourcePos.getFirstLine, sourcePos.getFirstCol, sourcePos.getFirstOffset, sourcePos.getLastOffset)
    }
    (line, col, start, end, relPath)
  }

  def prettyPrintInstruction(node: CGNode, inst: SSAInstruction): String = {
    if (isJavaNode(node)) {
      val (line, path, className, method) = getJavaSourceInfo(node, inst)
      s"$className:$line, Method: $method, Path: $path"
    } else {
      node.getIR match {
        case ir:AstIR => {
          val (line, col, start, end, relPath:String) = getJavaScriptSourceInfo(ir, inst)
          val fileName = relPath.substring(relPath.lastIndexOf('/') + 1)
          s"$fileName:$line:$col ($start -> $end), Path: $relPath"
        }
        case _ => s"$inst, $node"
      }
    }
  }

  def argsToOptions(arg: String) = if (arg.charAt(0) == '-') {
      val lb = new ListBuffer[CrossBuilderOption]
      if (arg.contains('j')) lb += FilterJavaCallSites
      if (arg.contains('m')) lb += MockCordovaExec
      if (arg.contains('r')) lb += ReplacePluginDefinesAndRequires
      if (arg.contains('f')) lb += FilterJSFrameworks
      if (arg.contains('p')) lb += PreciseJS
      if (arg.contains('x')) lb += RunBuildersInParallel
      lb.toList
    } else List()

  def prettyPrintCrossTargets(targets: Map[(CGNode, CallSiteReference), LinkedHashSet[CGNode]]): List[String] = {
    val (javaJsCalls, jsJavaCalls) = targets.partition(p => Util.isJavaNode(p._1._1))

    val lb = new ListBuffer[String]()
    lb += s"Java -> JavaScript (${javaJsCalls.values.map({ set => set.size }).sum} calls)"
    lb ++= prettyPrintCrossCalls(javaJsCalls)
    lb += s"JavaScript -> Java (${jsJavaCalls.values.map({ set => set.size }).sum} calls)"
    lb ++= prettyPrintCrossCalls(jsJavaCalls)
    lb.toList
  }

  private def prettyPrintCrossCalls(targets: Map[(CGNode, CallSiteReference), LinkedHashSet[CGNode]]): List[String] = {
    val lb = new ListBuffer[String]()
    for ((node, csr) <- targets.keys) {
      lb += "\t" + prettyPrintInstruction(node, node.getIR.getCalls(csr)(0))
      for (target <- targets.get((node, csr)).get) {
        lb += "\t\t-> " + prettyPrintInstruction(target, target.getIR.getInstructions.find({ i => i != null }).get)
      }
    }
    lb.toList
  }

  def walkTree(file: File): Iterable[File] = {
    val children = new Iterable[File] {
      def iterator = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
    }
    Seq(file) ++: children.flatMap(walkTree(_))
  }

  def isJavaNode(node: CGNode): Boolean = node.getMethod.getDeclaringClass.getClassLoader.getLanguage == Language.JAVA

  def time[R](label: String, block: => R)(implicit logger: Logger): R = {
    val start = System.nanoTime()
    val result = block
    logger.info(s"$label took %.3fs".format(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) / 1000.0))
    result
  }
}

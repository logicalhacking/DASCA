/*
 * (C) Copyright 2010-2015 SAP SE.
 *               2016      The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.test

import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import eu.aniketos.dasca.crosslanguage.util.Util
import eu.aniketos.dasca.crosslanguage.builder.CordovaCGBuilder
import eu.aniketos.dasca.crosslanguage.builder.FilterJavaCallSites
import eu.aniketos.dasca.crosslanguage.builder.MockCordovaExec
import eu.aniketos.dasca.crosslanguage.builder.ReplacePluginDefinesAndRequires
import java.io.File
import eu.aniketos.dasca.crosslanguage.util.JavaScriptSourceLocation
import eu.aniketos.dasca.crosslanguage.util.JavaSourceLocation
import com.ibm.wala.cast.ir.ssa.AstIRFactory
import eu.aniketos.dasca.crosslanguage.util.SourceLocation
import eu.aniketos.dasca.crosslanguage.builder.FilterJSFrameworks
import scala.collection.mutable.LinkedHashSet
import com.ibm.wala.classLoader.IMethod

object AbstractTestDriver {
  
  def run(infos:List[AppInfo], args: Array[String]): Unit = {
    if (args.length < 1 || (args.length < 2 && args(0).charAt(0) == '-')) {
      println("You must at least provide the path to the apk folder!")
      return
    }

    val apkDir = if (args(0).charAt(0) == '-') new File(args(1)) else new File(args(0))

    if (!apkDir.exists() || !apkDir.isDirectory) {
      println("Please provide a valid folder!")
      return
    }

    val options = if (args(0).charAt(0) == '-') Util.argsToOptions(args(0)) else List()

    
    for (info <- infos if (new File(apkDir, info.getApkName)).isFile()) {
      println(s"Processing ${info.getApkName}...")
      val builder = CordovaCGBuilder(new File(apkDir, info.getApkName))
      builder.setOptions(options:_*)
      val mcg = builder.createCallGraph
      val crossTargets = mcg.getAllCrossTargets
      val convertedCrossTargets = convertToSourceLocationPairs(crossTargets)

      val (javaPairs, jsPairs) = convertedCrossTargets.partition({case (origin, target) => origin.isInstanceOf[JavaSourceLocation]})
      val javaJsCount = javaPairs.size
      val jsJavaCount = jsPairs.size
      val (found, notFound) = info.getExpectedConnections.partition(p => convertedCrossTargets.contains(p))
      val javaJsCorrectlyFound = found.count(_._1.isInstanceOf[JavaSourceLocation])
      val jsJavaCorrectlyFound = found.count(_._1.isInstanceOf[JavaScriptSourceLocation])
      val javaJsNotFound = notFound.count(_._1.isInstanceOf[JavaSourceLocation])
      val jsJavaNotFound = notFound.count(_._1.isInstanceOf[JavaScriptSourceLocation])

      for (p <- found) println(s"Correctly found ${p._1} -> ${p._2}")
      for (p <- notFound) println(s"Did not find ${p._1} -> ${p._2}")
      for (p <- convertedCrossTargets if !info.getExpectedConnections.contains(p)) println(s"Incorrectly found ${p._1} -> ${p._2}")

      println(s"\t ${info.getApkName}: Java -> JS:")
      printResult(javaJsCount, javaJsCorrectlyFound, javaJsNotFound)
      println(s"\t ${info.getApkName}: JS -> Java:")
      printResult(jsJavaCount, jsJavaCorrectlyFound, jsJavaNotFound)
    }
  }

  
  def convertToSourceLocationPairs(crossTargets: Map[(CGNode, CallSiteReference), LinkedHashSet[CGNode]]): Set[(SourceLocation, SourceLocation)] = {
    for (
      origin <- crossTargets.keys;
      target <- crossTargets.get(origin).get
    ) yield {
      if (Util.isJavaNode(origin._1)) {
        target.getIR match {
          case ir: AstIRFactory[IMethod]#AstIR => (JavaSourceLocation(origin._1, origin._2), JavaScriptSourceLocation(ir))
          case _ => (JavaSourceLocation(origin._1, origin._2), new JavaScriptSourceLocation(-1, -1, "unknown"))
        }
      } else {
        origin._1.getIR match {
          case ir: AstIRFactory[IMethod]#AstIR => (JavaScriptSourceLocation(ir, origin._2), JavaSourceLocation(target))
          case _ => (new JavaScriptSourceLocation(-1, -1, "unknown"), JavaSourceLocation(target))
        }
      }
    }
  }.toSet

  def printResult(total: Int, hits: Int, misses: Int) = {
    println(s"\t\tHits: $hits")
    println(s"\t\tMisses: $misses")
    println(s"\t\tErrors: ${total - hits}")
    println("\t\t-> Recall: %.2f%%".format(100.0 * hits / (hits + misses)))
    println("\t\t-> Precision: %.2f%%".format(100.0 * hits / total))
  }

}

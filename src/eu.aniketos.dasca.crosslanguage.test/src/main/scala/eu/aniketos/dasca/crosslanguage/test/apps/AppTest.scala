/*
 * (C) Copyright 2016 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.test.apps

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
import eu.aniketos.dasca.crosslanguage.builder.CrossBuilderOption
import eu.aniketos.dasca.crosslanguage.builder.FilterJavaCallSites
import eu.aniketos.dasca.crosslanguage.builder.MockCordovaExec
import eu.aniketos.dasca.crosslanguage.builder.ReplacePluginDefinesAndRequires
import eu.aniketos.dasca.crosslanguage.builder.FilterJSFrameworks
import eu.aniketos.dasca.crosslanguage.builder.PreciseJS
import eu.aniketos.dasca.crosslanguage.builder.RunBuildersInParallel

class AppTest {
   private var js2JavaHits      = -1;
   private var js2JavaMisses    = -1;
   private var js2JavaTotal     = -1;

   private var java2JSHits      = -1;
   private var java2JSMisses    = -1;
   private var java2JSTotal     = -1;
   
   def getJS2JavaHits() = {
     js2JavaHits
   }
   def getJS2JavaMisses() = {
     js2JavaMisses
   }

   def getJS2JavaErrors() = {
     js2JavaTotal - js2JavaHits
   }
   
   def getJS2JavaRecall() = {
     100.0 * js2JavaHits / (js2JavaHits + js2JavaMisses)
   }
   
   def getJS2JavaPrecision() = {
     100.0 * js2JavaHits / js2JavaTotal
   }
   
   def getJava2JSHits() = {
     java2JSHits
   }
   def getJava2JSMisses() = {
     java2JSMisses
   }

   def getJava2JSErrors() = {
     java2JSTotal - java2JSHits
   }
   
   def getJava2JSRecall() = {
     100.0 * java2JSHits / (java2JSHits + java2JSMisses)
   }
   
   def getJava2JSPrecision() = {
     100.0 * java2JSHits / java2JSTotal
   }
   
   private var truePositives  = Set[(SourceLocation, SourceLocation)]()
   private var falseNegatives = Set[(SourceLocation, SourceLocation)]()
   private var falsePositives = Set[(SourceLocation, SourceLocation)]()

   def getTruePositives() = {
     truePositives
   }
   
   def getFalseNegatives() = {
     falseNegatives
   }
   
   def getFalsePositives() = {
     falsePositives
   }
    
   def analyze(apk:String, options:List[CrossBuilderOption], expectedConnections:Set[(SourceLocation, SourceLocation)]):Boolean = {
      val builder = CordovaCGBuilder(new File(getClass.getResource("/"+apk).getFile));
      builder.setOptions(options:_*)
      val crossTargets = builder.createCallGraph.getAllCrossTargets
      val convertedCrossTargets = convertToSourceLocationPairs(crossTargets)
      val (javaPairs, jsPairs) = convertedCrossTargets.partition({case (origin, target) => origin.isInstanceOf[JavaSourceLocation]})
      java2JSTotal = javaPairs.size
      js2JavaTotal = jsPairs.size
      val (found, notFound) = expectedConnections.partition(p => convertedCrossTargets.contains(p))
      java2JSHits = found.count(_._1.isInstanceOf[JavaSourceLocation])
      js2JavaHits = found.count(_._1.isInstanceOf[JavaScriptSourceLocation])
      java2JSMisses = notFound.count(_._1.isInstanceOf[JavaSourceLocation])
      js2JavaMisses = notFound.count(_._1.isInstanceOf[JavaScriptSourceLocation])

      truePositives  = found
      falseNegatives = notFound
      falsePositives = convertedCrossTargets -- expectedConnections
      true
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



}

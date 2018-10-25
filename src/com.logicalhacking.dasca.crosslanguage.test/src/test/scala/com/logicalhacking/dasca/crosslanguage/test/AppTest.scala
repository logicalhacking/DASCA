/*
 * (C) Copyright 2016 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.crosslanguage.test

import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import com.logicalhacking.dasca.crosslanguage.util.Util
import com.logicalhacking.dasca.crosslanguage.builder.CordovaCGBuilder
import java.io.File
import com.logicalhacking.dasca.crosslanguage.util.JavaScriptSourceLocation
import com.logicalhacking.dasca.crosslanguage.util.JavaSourceLocation
import com.logicalhacking.dasca.crosslanguage.util.SourceLocation
import scala.collection.mutable.LinkedHashSet
import com.logicalhacking.dasca.crosslanguage.builder.CrossBuilderOption
import com.logicalhacking.dasca.crosslanguage.builder.MergedCallGraph
import com.logicalhacking.dasca.crosslanguage.builder._
import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.classLoader.IMethod

class AppTest {
   private var cg = null:MergedCallGraph;
  
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
   
   def getCallGraphSize() = cg.getNumberOfNodes
   
   def getJSCallGraphSize() = cg.jsCG.getNumberOfNodes
   
   def getJavaCallGraphSize() = cg.javaCG.getNumberOfNodes
   
   
   private var cgSize = -1
    
   def analyze(apk:String, options:List[CrossBuilderOption], expectedConnections:Set[(SourceLocation, SourceLocation)]):Boolean = {
      val builder = CordovaCGBuilder(new File(getClass.getResource("/"+apk).getFile));
      builder.setOptions(options:_*)
      cg = builder.createCallGraph
      val crossTargets = cg.getAllCrossTargets
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

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

package com.logicalhacking.dasca.crosslanguage

import java.io.File
import com.logicalhacking.dasca.crosslanguage.builder._
import com.logicalhacking.dasca.crosslanguage.cg._
import scala.collection.mutable.ListBuffer
import java.lang.management.ManagementFactory
import com.logicalhacking.dasca.crosslanguage.util.Util
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.cast.js.loader.JavaScriptLoader
import com.ibm.wala.util.strings.Atom

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length < 1 || (args.length < 2 && args(0).charAt(0) == '-')) {
      println("You must at least provide the path to the apk!")
      return
    }

    val apk = if (args(0).charAt(0) == '-') new File(args(1)) else new File(args(0))

    if (!apk.exists() || !apk.getCanonicalPath.endsWith(".apk")) {
      println("Please provide a valid apk file!")
      return
    }

    val options = if (args(0).charAt(0) == '-') Util.argsToOptions(args(0)) else List()
    implicit val logger = Logger(LoggerFactory.getLogger(getClass.toString))
    var cg = null:MergedCallGraph
    Util.time("Creation of the Cordova unified call graph", {
      val builder = CordovaCGBuilder(apk)
      builder.setOptions(options: _*)
      cg = builder.createCallGraph
    })
    
    // compute sinks (JS entry points from index.js) and 
    //         sources (execSQL statements)
    val it = cg.iterator()
    var sinks = List[CGNode]()
    var roots = List[CGNode]()
    val execSQL = Atom.findOrCreateUnicodeAtom("execSQL");
    for(n <- it){
      if (Util.isJavaNode(n)){
        if (n.getMethod().getName.equals(execSQL)) { sinks = sinks.+:(n) } 
      }else{
        try{
            if((n.getMethod().asInstanceOf[JavaScriptLoader#DynamicMethodObject]).getDeclaringClass()
                      .getSourceFileName().endsWith("index.js"))
         {roots = roots.+:(n) }
        }catch{
        case e:ClassCastException => {}
        }
      }
    }
    println ("Number of roots: "+roots.size)
    roots.map { x => println("  "+x) }
    println ("Number of sinks: "+sinks.size)
    sinks.map { x => println("  "+x) }

    Util.time("Creation of the CallForest", {
      val callForest = CallTreeBuilder.buildCallForest(cg, 15, roots, sinks)
      print ("Number of trees: "+callForest.size)
      callForest.map { x => print ("\n"+x+"\n\n") }
    })
  }
}

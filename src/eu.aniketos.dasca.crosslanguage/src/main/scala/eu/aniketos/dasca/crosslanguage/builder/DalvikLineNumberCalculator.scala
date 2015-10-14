/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.builder

import java.io.File
import scala.collection.JavaConverters._
import scala.io.Source
import scala.{ Option => ? }
import com.ibm.wala.dalvik.classLoader.DexIMethod
import com.ibm.wala.ipa.callgraph.CallGraph
import org.apache.commons.io.FileUtils

/**
 * @author D062824
 */
object DalvikLineNumberCalculator {
  val MethodRegex = """\s*\.method (.+)""".r
  val LineRegex = """\s*\.line ([0-9]+)\s*""".r
  val AnnotationRegex = """\s*\.annotation .+""".r
  val EndAnnotationRegex = """\s*\.end annotation\s*""".r
  val InstructionRegex = """\s*[a-zA-Z].*""".r

  def setLineNumbers(apkUnzipDir: File, cg: CallGraph) = {
    for (
      node <- cg.iterator().asScala;
      method <- ?(node.getMethod).collect({ case m: DexIMethod => m })
    ) {
      val path = method.getDeclaringClass.getName.toString().substring(1) + ".smali"
      val smaliFile = new File(new File(apkUnzipDir, "smali"), path)
      val methodString = method.getReference().getName().toString() + node.getMethod().getReference().getDescriptor()
      var lastLine = -1
      var inCorrectMethod = false
      var correctMethodFound = false
      var inAnnotation = false
      var instCounter = 0
      for (line <- FileUtils.readLines(smaliFile).asScala.takeWhile(_ => !correctMethodFound || inCorrectMethod)) line match {
        case MethodRegex(rest) => {
          inCorrectMethod = rest.endsWith(methodString)
          if (inCorrectMethod) correctMethodFound = true
        }
        case LineRegex(line) if inCorrectMethod => lastLine = line.toInt
        case AnnotationRegex() => inAnnotation = true
        case EndAnnotationRegex() => inAnnotation = false
        case InstructionRegex() if (inCorrectMethod && !inAnnotation) => {
          method.setLineNumber(instCounter, lastLine)
          instCounter += 1
        }
        case _ =>
      }
    }
  }
}

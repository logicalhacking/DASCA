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

import java.io.File
import scala.collection.mutable.TreeSet
import org.apache.commons.io.FileUtils
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position
import com.ibm.wala.cast.js.translator.RangePosition

class FileMapRecorder(file: File) {

  val replacements = TreeSet[(Int, Int, String)]()
  val content = FileUtils.readFileToString(file)

  var alreadyWritten = false

  def replace(start: Int, end: Int, replacement: String) = {
    replacements += ((start, end, replacement))
  }

  def writeToFile = {
    if (alreadyWritten) {
      throw new IllegalStateException("Already written to file!")
    }

    val replArray = replacements.toArray
    for (i <- 0.until(replArray.length - 1)) {
      val end = replArray(i)._2
      val nextStart = replArray(i+1)._1
      if (end > nextStart) {
        throw new IllegalStateException("Must not overlap!")
      }
    }

    var newContent = content

    for ((start, end, replacement) <- replArray.reverse) {
      newContent = newContent.substring(0, start) + replacement + newContent.substring(end)
    }

    alreadyWritten = true

    FileUtils.write(file, newContent)
  }

  def translate(pos: Position) = {
    if (pos.getURL != file.toURI().toURL()) {
      throw new IllegalArgumentException("The passed position's file does not match this one!")
    }

    val startOffset = pos.getFirstOffset
    val endOffset = pos.getLastOffset

    val translatedStartOffset = translateOffset(startOffset)

    val translatedEndOffset = translateOffset(endOffset)

    val line = 1 + content.substring(0, translatedStartOffset).count { _ == '\n' }

    val col = RangePosition.getCol(content, line, translatedStartOffset)

    (line, col, translatedStartOffset, translatedEndOffset)
  }

  private def translateOffset(offset: Int) = {
    var newOffset = offset
    val replArray = replacements.toArray
    for (i <- 0.until(replArray.size)) {
      val (start, end, repl) = replArray(i)
      if (end < newOffset) {
        newOffset += (end - start) - repl.size
      }
    }
    newOffset
  }
}

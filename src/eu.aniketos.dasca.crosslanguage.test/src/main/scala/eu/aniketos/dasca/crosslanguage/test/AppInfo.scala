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

import java.io.File
import eu.aniketos.dasca.crosslanguage.util.SourceLocation
import eu.aniketos.dasca.crosslanguage.test.AppTest
import eu.aniketos.dasca.crosslanguage.builder.CrossBuilderOption

trait AppInfo {
  def apkName: String
  def truePositives: Set[(SourceLocation, SourceLocation)]
  def falsePositives: Set[(SourceLocation, SourceLocation)]
  def falseNegatives: Set[(SourceLocation, SourceLocation)]
  def options: List[CrossBuilderOption]
}

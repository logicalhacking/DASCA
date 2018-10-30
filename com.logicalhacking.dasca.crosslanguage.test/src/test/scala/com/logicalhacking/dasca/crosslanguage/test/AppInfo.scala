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

package com.logicalhacking.dasca.crosslanguage.test

import java.io.File
import com.logicalhacking.dasca.crosslanguage.util.SourceLocation
import com.logicalhacking.dasca.crosslanguage.builder.CrossBuilderOption

trait AppInfo {
  def apkName: String
  def truePositives: Set[(SourceLocation, SourceLocation)]
  def falsePositives: Set[(SourceLocation, SourceLocation)]
  def falseNegatives: Set[(SourceLocation, SourceLocation)]
  def options: List[CrossBuilderOption]
}

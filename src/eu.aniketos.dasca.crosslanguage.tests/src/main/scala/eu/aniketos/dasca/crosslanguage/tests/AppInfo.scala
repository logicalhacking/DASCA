/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.tests

import java.io.File
import eu.aniketos.dasca.crosslanguage.util.SourceLocation

trait AppInfo {
  def getApkName: String
  def getExpectedConnections: List[(SourceLocation, SourceLocation)]
}

/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.crosslanguage.builder

import java.io.File

class PluginInfo(val fileName: String, val id: String, val clobbers: List[String], val file: File) {

  def getGlobalVar = s"window.crosswala${id.replace(".", "").replace("-", "")}"
}

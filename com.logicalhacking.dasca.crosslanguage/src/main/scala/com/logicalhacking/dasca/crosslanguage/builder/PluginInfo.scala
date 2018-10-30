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

package com.logicalhacking.dasca.crosslanguage.builder

import java.io.File

class PluginInfo(val fileName: String, val id: String, val clobbers: List[String], val file: File) {

  def getGlobalVar = s"window.crosswala${id.replace(".", "").replace("-", "")}"
}

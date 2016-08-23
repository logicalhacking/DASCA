/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.test.apps

import java.io.File

import scala.collection.mutable.ListBuffer

import eu.aniketos.dasca.crosslanguage.test.AppInfo
import eu.aniketos.dasca.crosslanguage.util.JavaScriptSourceLocation
import eu.aniketos.dasca.crosslanguage.util.JavaSourceLocation
import eu.aniketos.dasca.crosslanguage.util.SourceLocation

object FeatherweightInfo extends AppInfo {
  def getApkName: String = "de.zertapps.dvhma.featherweight.apk"

  def getExpectedConnections: List[(SourceLocation, SourceLocation)] = {
    val list = new ListBuffer[(SourceLocation, SourceLocation)]()

    list += ((new JavaSourceLocation(43, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js")))
    list += ((new JavaSourceLocation(145, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(77, 1, "www/js/index.js")))
    list += ((new JavaSourceLocation(126, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(77, 1, "www/js/index.js")))
    list += ((new JavaSourceLocation(49, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(26, 58, "www/js/index.js")))
    list += ((new JavaSourceLocation(49, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(25, 51, "www/js/index.js")))
    list += ((new JavaSourceLocation(52, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js")))
    list += ((new JavaSourceLocation(36, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js")))
    list += ((new JavaSourceLocation(85, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(77, 1, "www/js/index.js")))
    list += ((new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(46, 21, "www/js/index.js")))
    list += ((new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(68, 25, "www/js/index.js")))
    list += ((new JavaScriptSourceLocation(7, 12, "www/plugins/de.zertapps.dvhma.plugins.webintent/www/webintent.js"), new JavaSourceLocation(28, "com/borismus/webintent/WebIntent")))
    list += ((new JavaScriptSourceLocation(28, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage")))
    list += ((new JavaScriptSourceLocation(24, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage")))
    list += ((new JavaScriptSourceLocation(16, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage")))
    list += ((new JavaScriptSourceLocation(20, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage")))
    list.toList
  }
}

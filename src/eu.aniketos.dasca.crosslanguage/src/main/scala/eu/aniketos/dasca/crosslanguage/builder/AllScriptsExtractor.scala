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

import com.ibm.wala.cast.js.html.DefaultSourceExtractor
import com.ibm.wala.cast.js.html.DefaultSourceExtractor.HtmlCallBack
import com.ibm.wala.cast.js.html.IUrlResolver
import java.net.URL
import com.ibm.wala.cast.js.html.ITag
import org.apache.commons.io.input.BOMInputStream
import org.apache.commons.io.ByteOrderMark
import java.io.InputStreamReader
import scala.io.Source
import java.io.File
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import scala.collection.JavaConverters._
import com.typesafe.scalalogging.Logger
import java.io.IOException
import org.slf4j.LoggerFactory
import org.apache.commons.io.FileUtils
import scala.collection.mutable.MutableList

class AllScriptsExtractor(apkUnzipDir: File) extends DefaultSourceExtractor {
  val logger = Logger(LoggerFactory.getLogger(getClass.toString))
  val extractedScripts = MutableList[URL]()

  override def createHtmlCallback(entrypoint: URL, urlResolver: IUrlResolver) = new HtmlCallBack(entrypoint, urlResolver)

  class HtmlCallBack(entrypoint: URL, urlResolver: IUrlResolver) extends DefaultSourceExtractor.HtmlCallBack(entrypoint, urlResolver) {

    val AbsolutePathRegex = """.+android_asset/(.+)""".r
    val RemotePathRegex = """(https?://.+)""".r

    override def getScriptFromUrl(urlString: String, scriptTag: ITag) = {
      try {
        // For some reason, some people use absolute script paths in their Cordova apps...
        val scriptSrc = urlString match {
          case AbsolutePathRegex(rel) => new File(new File(apkUnzipDir, "assets"), rel).toURI().toURL()
          case RemotePathRegex(urlString) => {
            val url = new URL(urlString)
            logger.debug(s"Downloading $urlString...")
            val dest = new File(new File(apkUnzipDir, "downloaded"), url.getFile)
            dest.getParentFile.mkdirs()
            val stream = url.openStream()
            FileUtils.copyInputStreamToFile(stream, dest)
            IOUtils.closeQuietly(stream)
            dest.toURI().toURL()
          }
          case _ => new URL(entrypoint, urlString)
        }
        if (!new File(scriptSrc.getFile).isFile()) {
          // Try again with all lower-case letter, otherwise files may be missed on case-sensitive file systems
          if (!new File(scriptSrc.getFile.toLowerCase()).isFile()) {
            throw new IOException(s"$scriptSrc does not exist on the file system!")
          }
        }
        extractedScripts += scriptSrc
      } catch {
        case ioe: IOException => logger.warn(s"Could not fetch script $urlString!", ioe)
      }
    }
  }
}

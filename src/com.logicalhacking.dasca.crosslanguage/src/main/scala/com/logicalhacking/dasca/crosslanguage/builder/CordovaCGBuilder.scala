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
import java.io.FileNotFoundException
import java.lang.ref.ReferenceQueue.Null
import scala.collection.JavaConverters._
import scala.collection.immutable.StringOps
import scala.xml.XML
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dalvik.classLoader.DexIRFactory
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint
import com.ibm.wala.dalvik.ipa.callgraph.impl.DexEntryPoint
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator.LocatorFlags
import com.ibm.wala.ipa.callgraph.AnalysisCache
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.logicalhacking.dasca.crosslanguage.util.Util
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.ipa.cha.ClassHierarchy
import brut.androlib.ApkDecoder
import spray.json._
import spray.json.DefaultJsonProtocol._
import com.ibm.wala.cast.js.html.WebPageLoaderFactory
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory
import com.ibm.wala.cast.js.loader.JavaScriptLoaderFactory
import java.net.URL
import com.ibm.wala.cast.js.loader.JavaScriptLoader
import com.ibm.wala.cast.js.html.WebUtil
import com.ibm.wala.classLoader.SourceURLModule
import com.ibm.wala.classLoader.SourceModule
import com.ibm.wala.classLoader.SourceFileModule
import com.ibm.wala.classLoader.ModuleEntry
import com.ibm.wala.classLoader.Module
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.Random
import com.logicalhacking.dasca.crosslanguage.util.Util
import scala.collection.mutable.ListBuffer
import com.ibm.wala.cast.js.html.jericho.JerichoHtmlParser
import com.ibm.wala.cast.js.html.IdentityUrlResolver
import com.ibm.wala.cast.js.html.DefaultSourceExtractor
import scala.xml.Elem
import org.apache.commons.io.input.BOMInputStream
import org.apache.commons.io.ByteOrderMark
import scala.util.matching.Regex
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import com.logicalhacking.dasca.crosslanguage.builder.CordovaCGBuilder._
import com.ibm.wala.ipa.callgraph.impl.{ Util => WalaUtil }
import com.logicalhacking.dasca.crosslanguage.util.FileMappingStore
import com.logicalhacking.dasca.crosslanguage.util.FileMapRecorder
import com.ibm.wala.util.io.TemporaryFile
import org.apache.commons.io.IOUtils
import com.ibm.wala.util.io.FileUtil
import java.nio.file.Files
import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.cast.ipa.callgraph.CAstAnalysisScope
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil
import com.ibm.wala.cast.js.ipa.callgraph.JSZeroOrOneXCFABuilder
import java.util.Collections
import com.ibm.wala.cast.js.callgraph.fieldbased.OptimisticCallgraphBuilder
import com.ibm.wala.util.WalaException
import com.ibm.wala.dalvik.util.AndroidAnalysisScope
import java.util.jar.JarFile
import com.ibm.wala.types.ClassLoaderReference
import com.ibm.wala.classLoader.JarFileModule

object CordovaCGBuilder {
  val ExecuteSuffix = "walaexec"
  val ExclusionFile = new File("jsexclusions.txt")
  def apply(apk: File): CordovaCGBuilder = {
    val tmpApkDir = new File(System.getProperty("java.io.tmpdir"), s"${apk.getName}-${Random.alphanumeric.take(16).mkString}")
    tmpApkDir.deleteOnExit()
    CordovaCGBuilder(apk, tmpApkDir)
  }

  def apply(apk: File, tmpApkDir: File): CordovaCGBuilder = new CordovaCGBuilder(apk, tmpApkDir)
}

class CordovaCGBuilder(val apk: File, val apkUnzipDir: File) {
  val PluginsRegex = """(?s).*\.exports\s*=\s*(.+]).*""".r
  implicit val logger = Logger(LoggerFactory.getLogger(getClass.toString))

  var mockCordovaExec = false
  var filterJavaCallSites = false
  var replacePluginDefinesAndRequires = false
  var filterJSFrameworks = false
  var preciseJS = false
  var runBuildersInParallel = false

  def waitForFutures(fut1: Future[Any], fut2: Future[Any]) = {
    while (fut1.value.isEmpty || fut2.value.isEmpty) {
      for (value <- fut1.value if value.isFailure) {
        value.get
      }
      for (value <- fut2.value if value.isFailure) {
        value.get
      }
      Thread.sleep(500)
    }
  }

  def createCallGraph: MergedCallGraph = {
    val mcg = if (runBuildersInParallel) {
      val javaFuture = Future { Util.time("Creation of Java Call Graph", { createJavaCallGraph }) }
      val jsFuture = Future { Util.time("Creation of JavaScript Call Graph", { createJavaScriptCallGraph }) }
      waitForFutures(javaFuture, jsFuture)
      val javaCG = javaFuture.value.get.get
      val (jsCG, xml) = jsFuture.value.get.get
      new MergedCallGraph(javaCG, jsCG, xml)
      val mcgFuture = for {
        javaCG <- javaFuture
        jsCG <- jsFuture
      } yield new MergedCallGraph(javaCG, jsCG._1, jsCG._2)
      Await.result(mcgFuture, Duration.Inf)
    } else {
      val javaCG = Util.time("Creation of Java Call Graph", { createJavaCallGraph })
      val (jsCG, xml) = Util.time("Creation of JavaScript Call Graph", { createJavaScriptCallGraph })
      new MergedCallGraph(javaCG, jsCG, xml)
    }

    logger.info(s"The Java Call Graph has ${mcg.javaCG.getNumberOfNodes} nodes")
    logger.info(s"The JavaScript Call Graph has ${mcg.jsCG.getNumberOfNodes} nodes")

    DalvikLineNumberCalculator.setLineNumbers(apkUnzipDir, mcg.javaCG)

    logger.info("Connecting cross language calls...")
    mcg.connect(filterJavaCallSites, filterJSFrameworks)
    Util.time("Connecting the cross calls", { mcg.connect })(logger)

    logger.info("The following calls have been found:")
    for (line <- Util.prettyPrintCrossTargets(mcg.getAllCrossTargets)) logger.info(line)
    logger.info("End of cross calls")
    mcg
  }

  private def createJavaCallGraph = {
    val tmpAndroidJar = File.createTempFile("android", "jar")
    tmpAndroidJar.deleteOnExit()
    if (null != getClass.getClassLoader.getResource("android19.jar")){
      TemporaryFile.urlToFile(tmpAndroidJar, getClass.getClassLoader.getResource("android19.jar"))
    }else{
       logger.error("Please install android19.jar.");
       throw new FileNotFoundException("Please install android19.jar.");
    }
    val scope = AndroidAnalysisScope.setUpAndroidAnalysisScope(apk.toURI(), getClass.getClassLoader.getResource("javaexclusions.txt").getFile, CordovaCGBuilder.getClass.getClassLoader())
    scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(tmpAndroidJar)))
    val cha = ClassHierarchyFactory.make(scope)
    val cache = new AnalysisCacheImpl(new DexIRFactory())
    val eps = new AndroidEntryPointLocator(Set(
      LocatorFlags.INCLUDE_CALLBACKS, LocatorFlags.EP_HEURISTIC, LocatorFlags.CB_HEURISTIC).asJava)
    val pluginEntryPoints = getPluginEntryPoints(cha)
    for (ep <- pluginEntryPoints) logger.info(s"Found cordova plugin entry point: $ep")
    val entryPoints = eps.getEntryPoints(cha).asScala ++ pluginEntryPoints
    val options = new AnalysisOptions(scope, entryPoints.asJava);
    options.setReflectionOptions(ReflectionOptions.FULL);
    val cgb = WalaUtil.makeZeroCFABuilder(options, cache, cha, scope);
    cgb.makeCallGraph(options, null);
  }

  private def getPluginEntryPoints(cha: ClassHierarchy): List[Entrypoint] = for (
    cls <- cha.iterator.asScala.toList;
    m <- cls.getDeclaredMethods.asScala if !cls.isInterface() &&
      cls.getClassLoader.getName == AnalysisScope.APPLICATION && Util.derivesFromCordovaPlugin(cls.getSuperclass);
    if m.getName.toString.equals("execute")
  ) yield new DexEntryPoint(m, cha)

  private def createJavaScriptCallGraph: (CallGraph, Elem) = {
    decodeApk(apk);
    val configXml = XML.loadFile(new File(apkUnzipDir, "/res/xml/config.xml"))
    val entrypoint = getEntryPoint(configXml)
    if (!entrypoint.exists()) {
      logger.error(s"Could not find entrypoint $entrypoint, using an empty call graph ...")
      return (new EmptyCallGraph(), configXml)
    }
    val pluginInfos = getPluginInfos
    val loaders = new WebPageLoaderFactory(new CAstRhinoTranslatorFactory())
    val scripts = makeHtmlScope(entrypoint.toURI().toURL(), loaders, pluginInfos);
    preprocessApkDir(scripts, pluginInfos)
    val cache = new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory())
    val scope = new CAstAnalysisScope((scripts:List[Module]).toArray, loaders, Collections.singleton(JavaScriptLoader.JS))
    val cha = ClassHierarchyFactory.make(scope, loaders, JavaScriptLoader.JS)
    val roots = JSCallGraphUtil.makeScriptRoots(cha)
    val options = JSCallGraphUtil.makeOptions(scope, cha, roots)
    try {
      com.ibm.wala.cast.js.util.Util.checkForFrontEndErrors(cha);
    } catch {
      case e: WalaException => logger.warn("JavaScript front end error:", e)
    }
    val cg = if (preciseJS) {
      val builder = new JSZeroOrOneXCFABuilder(cha, options, cache, null, null, ZeroXInstanceKeys.ALLOCATIONS,
        true)
      builder.makeCallGraph(options)
    } else {
      val builder = new OptimisticCallgraphBuilder(cha, JSCallGraphUtil.makeOptions(scope, cha, roots), cache, false)
      builder.buildCallGraph(roots, null).fst
    }
    (cg, configXml)
  }

  private def makeHtmlScope(url: URL, loaders: JavaScriptLoaderFactory, pluginInfos: List[PluginInfo]): List[SourceModule] = {
    JavaScriptLoader.addBootstrapFile(WebUtil.preamble);
    val lb = new ListBuffer[SourceModule]()
    lb += new SourceURLModule(CordovaCGBuilder.getClass.getClassLoader.getResource("prologue.js"))
    lb += new SourceURLModule(CordovaCGBuilder.getClass.getClassLoader.getResource("preamble.js"))
    for (info <- pluginInfos) lb += new SourceURLModule(info.file.toURI().toURL())

    val extractor = new AllScriptsExtractor(apkUnzipDir)

    lb ++= extractor.extractSources(url, new JerichoHtmlParser, new IdentityUrlResolver).asScala

    lb ++= extractor.extractedScripts filter { !excludeJS(_) } map { toSourceModule }

    logger.debug("The following scripts are in the JavaScript scope:")
    for (scriptUrl <- lb.map { _.getURL })
      logger.debug(scriptUrl.toString())

    lb.toList
  }

  private def toSourceModule(src: URL) = new SourceURLModule(src) {
    override def getInputStream = new BOMInputStream(src.openConnection().getInputStream(), false,
      ByteOrderMark.UTF_8,
      ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
      ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)
  }

  private def decodeApk(apk: File) = {
    val decoder = new ApkDecoder();
    decoder.setFrameworkDir(new File(System.getProperty("java.io.tmpdir"), s"apktoolframework${Random.alphanumeric.take(16).mkString}").getCanonicalPath)
    decoder.setApkFile(apk);
    if (apkUnzipDir.exists()) FileUtils.deleteDirectory(apkUnzipDir);
    decoder.setOutDir(apkUnzipDir);
    decoder.decode();
  }



  private def getPluginInfos: List[PluginInfo] = {
    val lb = new ListBuffer[PluginInfo]()

    val files = Util.walkTree(apkUnzipDir).filter({ file => file.getName == "cordova_plugins.js" }).toList
    if (files.isEmpty) {
      logger.warn("Expected a cordova_plugins.js file, but found none. Assuming there are no plugins.")
      return lb.toList
    }

    val cordovaPluginJs = if (files.size > 1) {
      logger.warn(s"Found ${files.size} cordova_plugin.js files! Trying to take one in a folder called 'www'.")
      files.find { Util.hasSomeParentWithName("www", _) } getOrElse (files(0))
    } else {
      files(0)
    }

    val jsonArray = FileUtils.readFileToString(cordovaPluginJs) match {
      case PluginsRegex(jsonArray) => jsonArray
      case _ => throw new IllegalArgumentException("Could not parse plugin section of cordova_plugins.js!")
    }
    val list = jsonArray.parseJson.convertTo[List[Map[String, JsValue]]]
    for (info <- list) {
      val fileName = info.get("file").get.convertTo[String]
      val file = new File(cordovaPluginJs.getParentFile, fileName)
      val id = info.get("id").get.convertTo[String]
      val clobbers = if (info.contains("clobbers")) {
        info.get("clobbers").get.convertTo[Vector[JsValue]].map({ _.convertTo[String] }).toList
      } else {
        List[String]()
      }
      lb += new PluginInfo(fileName, id, clobbers, file)
    }

    lb.toList
  }

  private def preprocessApkDir(scripts: List[SourceModule], plugins: List[PluginInfo]) = {
    var counter = 0
    for (
      script <- scripts;
      file = new File(script.getURL.getFile) if script.getURL.getProtocol == "file"
    ) {
      val recorder = new FileMapRecorder(file)

      // Fixes WALA bug when encountering "new Function('');" statements
      for (m <- """new Function\(""\)""".r.findAllMatchIn(recorder.content)) {
        recorder.replace(m.start, m.end, """new Function("wala123")""")
      }
      for (m <- """new Function\(''\)""".r.findAllMatchIn(recorder.content)) {
        recorder.replace(m.start, m.end, """new Function('wala123')""")
      }

      for (plugin <- plugins.find { _.file == file } if replacePluginDefinesAndRequires) {
        for (m <- """cordova\.define\(".+?",\s*function\(.*?\)\s*\{""".r.findFirstMatchIn(recorder.content)) {
          recorder.replace(m.start, m.end, s"(function() {\n${plugin.getGlobalVar} = function() {};\n")
        }

        val lastSemi = recorder.content.lastIndexOf(";")
        recorder.replace(lastSemi, lastSemi, "()")

        for (m <- """module\.exports""".r.findAllMatchIn(recorder.content)) {
          recorder.replace(m.start, m.end, plugin.getGlobalVar)
        }

        //        for (m <- """exports\s*=""".r.findAllMatchIn(recorder.content)) {
        //          recorder.replace(m.start, m.end, plugin.getGlobalVar + " =")
        //        }

        for (clobber <- plugin.clobbers if !clobber.contains("-")) {
          recorder.replace(recorder.content.size, recorder.content.size, s"\n$clobber = ${plugin.getGlobalVar};")
        }

        for (m <- """require\((?:'|")(.+?)(?:'|")\)""".r.findAllMatchIn(recorder.content)) {
          recorder.replace(m.start, m.end, replaceRequire(plugin, plugins, m))
        }

      }

      if (mockCordovaExec) {
        var counterAtBeginning = counter

        for (m <- """cordova\s*\.\s*exec\s*\(""".r.findAllMatchIn(recorder.content)) {
          counter += 1
          recorder.replace(recorder.content.size, recorder.content.size,
            s"\nfunction fun${counter}$ExecuteSuffix(s, f){s();f();};")

          recorder.replace(m.start, m.end, s"fun${counter}$ExecuteSuffix(")
        }

        val VarRegex =
          for (m <- """([a-zA-Z0-9_]+)\s*=\s*[a-zA-Z0-9_]+\s*\('cordova/exec'\)""".r.unanchored.findFirstMatchIn(recorder.content)) {
            val name = m.group(1)
            for (m2 <- ("""([\s\};])""" + name + """\(""").r.findAllMatchIn(recorder.content)) {
              counter += 1
              recorder.replace(recorder.content.size, recorder.content.size,
                s"\nfunction fun${counter}$ExecuteSuffix(s, f){s();f();};")
              recorder.replace(m2.start, m2.end, m2.group(1) + s"fun${counter}$ExecuteSuffix(")
            }
          }
        logger.debug(s"Replaced ${counter - counterAtBeginning} cordova exec calls in file $file.")
      }
      recorder.writeToFile
      FileMappingStore.map += ((file, recorder))
    }
  }

  val LocalRequireRegex = """\./([a-zA-Z]+)""".r

  private def replaceRequire(plugin: PluginInfo, plugins: List[PluginInfo], m: Regex.Match): String = m.group(1) match {
    case LocalRequireRegex(name) => plugins.find({ p =>
      p.fileName.substring(0, p.fileName.lastIndexOf('/')) ==
        plugin.fileName.substring(0, plugin.fileName.lastIndexOf('/')) && p.fileName.endsWith(s"/$name.js")
    }).get.getGlobalVar
    case _ => m.group(0)
  }

  def setOptions(options: CrossBuilderOption*) = for (option <- options) option match {
    case MockCordovaExec => mockCordovaExec = true
    case ReplacePluginDefinesAndRequires => replacePluginDefinesAndRequires = true
    case FilterJavaCallSites => filterJavaCallSites = true
    case FilterJSFrameworks => filterJSFrameworks = true
    case PreciseJS => preciseJS = true
    case RunBuildersInParallel => runBuildersInParallel = true
  }

  def getEntryPoint(configXml: Elem) = {
    val RemotePathRegex = """(https?://.+)""".r
    val entryPoint = configXml \ "content" \ "@src" text match {
      case RemotePathRegex(urlString) => {
        //        val url = new URL(urlString)
        //        val dest = new File(new File(apkUnzipDir, "downloaded"), "entrypoint.html")
        //        logger.debug(s"Downloading $urlString to $dest")
        //        dest.getParentFile.mkdirs()
        //        val stream = url.openStream()
        //        FileUtils.copyInputStreamToFile(stream, dest)
        //        IOUtils.closeQuietly(stream)
        //        dest
        new File(apkUnzipDir, "assets/www/index.html")
      }
      case "" => new File(apkUnzipDir, "assets/www/index.html")
      case text => new File(new File(apkUnzipDir, "assets/www"), text)
    }
    if (!entryPoint.getCanonicalPath.startsWith(apkUnzipDir.getCanonicalPath))
      throw new RuntimeException("Entrypoint seems to be outside of the unzip dir")
    entryPoint
  }

  lazy val jsExclusions = if (ExclusionFile.isFile() && ExclusionFile.canRead()) {
    FileUtils.readLines(ExclusionFile).asScala.map { _.r.unanchored }.toList
  } else {
    List.empty
  }

  def excludeJS(url: URL) = jsExclusions.exists { _.unapplySeq(url.toString()).isDefined }
}

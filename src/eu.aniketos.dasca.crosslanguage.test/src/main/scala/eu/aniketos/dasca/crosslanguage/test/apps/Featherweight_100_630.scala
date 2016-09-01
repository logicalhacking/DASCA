/*
 * (C) Copyright 2016 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.test.apps


import collection.mutable.Stack
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import eu.aniketos.dasca.crosslanguage.util.JavaScriptSourceLocation
import eu.aniketos.dasca.crosslanguage.util.JavaSourceLocation
import eu.aniketos.dasca.crosslanguage.util.SourceLocation
import eu.aniketos.dasca.crosslanguage.builder.CrossBuilderOption
import eu.aniketos.dasca.crosslanguage.builder.FilterJavaCallSites
import eu.aniketos.dasca.crosslanguage.builder.MockCordovaExec
import eu.aniketos.dasca.crosslanguage.builder.ReplacePluginDefinesAndRequires
import eu.aniketos.dasca.crosslanguage.builder.FilterJSFrameworks
import eu.aniketos.dasca.crosslanguage.builder.PreciseJS
import eu.aniketos.dasca.crosslanguage.builder.RunBuildersInParallel
import eu.aniketos.dasca.crosslanguage.test.Tag._;


@RunWith(classOf[JUnitRunner])
class Featherweight_100_630 extends FlatSpec with Matchers with BeforeAndAfterAll {
   def ApkName: String = "de.zertapps.dvhma.featherweight_1.0.0_6.3.0_debug.apk"
   def connections = Set[(SourceLocation, SourceLocation)](
             (new JavaSourceLocation(43, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js"))
            ,(new JavaSourceLocation(145, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(79, 1, "www/js/index.js"))
            ,(new JavaSourceLocation(126, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(79, 1, "www/js/index.js"))
            ,(new JavaSourceLocation(49, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(26, 58, "www/js/index.js"))
            ,(new JavaSourceLocation(49, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(25, 51, "www/js/index.js"))
            ,(new JavaSourceLocation(52, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js"))
            ,(new JavaSourceLocation(36, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js"))
            ,(new JavaSourceLocation(85, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(79, 1, "www/js/index.js"))
            ,(new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(48, 21, "www/js/index.js"))
            ,(new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(70, 25, "www/js/index.js"))
            ,(new JavaScriptSourceLocation(8, 12, "www/plugins/de.zertapps.dvhma.plugins.webintent/www/webintent.js"), new JavaSourceLocation(28, "com/borismus/webintent/WebIntent"))
            ,(new JavaScriptSourceLocation(25, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
            ,(new JavaScriptSourceLocation(21, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
            ,(new JavaScriptSourceLocation(17, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
            ,(new JavaScriptSourceLocation(29, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
            ,(new JavaSourceLocation(43, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(37, 8, "www/js/index.js"))
            ,(new JavaSourceLocation(36, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(37, 8, "www/js/index.js"))
            ,(new JavaSourceLocation(52, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(37, 8, "www/js/index.js"))
            ,(new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(79, 1, "www/js/index.js"))
       )

   def options = List(
       FilterJavaCallSites
      ,MockCordovaExec
      ,ReplacePluginDefinesAndRequires
      ,FilterJSFrameworks
   )   
       
       
   val app = new AppTest();
   override def beforeAll(){
      app.analyze(ApkName, options, connections)
   }  
    
   "Merged CallGraph" should "contain 4.3k nodes" in {
     app.getCallGraphSize() should be (4351 +- 20)
   }
   
   "JavaScriptCallGraph" should "contain 0.9k nodes" in {
     app.getJSCallGraphSize() should be (918 +- 10)
   }

   "JavaCallGraph" should "be contain 3.4k nodes" in {
     app.getJavaCallGraphSize() should be (3433 +- 10)
   }
   
   "Java -> JavaScript" should "report fourteen hits" taggedAs (ManuallyChecked) in {
       app.getJava2JSHits() should be (14)
   }
   
   it should "report no misses" taggedAs (ManuallyChecked) in {
       app.getJava2JSMisses() should be (0)
   }
   
   it should "report no errors" taggedAs (ManuallyChecked) in {
       app.getJava2JSErrors() should be (0)
   }

   it should "have a precision of 100%" taggedAs (ManuallyChecked) in {
       app.getJava2JSPrecision() should be (100.0 +- 0.1)
   }
   
   it should "have a recall of 100%" taggedAs (ManuallyChecked) in {
       app.getJava2JSRecall() should be (100.0 +- 0.1)
   } 
   
   "JavaScript -> Java" should "report five hits" taggedAs (ManuallyChecked) in {
       app.getJS2JavaHits() should be (5)
   }
   
   it should "report no misses" taggedAs (ManuallyChecked) in {
       app.getJS2JavaMisses() should be (0)
   }
   
   it should "report no errors" taggedAs (ManuallyChecked) in {
       app.getJS2JavaErrors() should be (0)
   }

   it should "have a precision of 100%" taggedAs (ManuallyChecked) in {
       app.getJS2JavaPrecision() should be (100.0 +- 0.1)
   }
   
   it should "have a recall of 100%" taggedAs (ManuallyChecked) in {
       app.getJS2JavaRecall() should be (100.0 +- 0.1)
   }
   
   
   "Reported connections" should "contain all expected connections" taggedAs (ManuallyChecked) in {
       app.getTruePositives() should contain theSameElementsAs connections
   }
   
   it should "not contain false negatives" taggedAs (ManuallyChecked) in {
      app.getFalseNegatives() shouldBe empty
   }

   it should "not contain false positives" taggedAs (ManuallyChecked) in {
      app.getFalsePositives() shouldBe empty
   }
   
}












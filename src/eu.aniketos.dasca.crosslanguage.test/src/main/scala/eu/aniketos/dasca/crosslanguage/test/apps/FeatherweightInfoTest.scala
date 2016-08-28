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

@RunWith(classOf[JUnitRunner])
class FeatherweightInfoTest extends FlatSpec with Matchers {
   def ApkName: String = "de.zertapps.dvhma.featherweight.apk"
   def connections = Set[(SourceLocation, SourceLocation)](
            (new JavaSourceLocation(43, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js"))
           ,(new JavaSourceLocation(145, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(77, 1, "www/js/index.js"))
           ,(new JavaSourceLocation(126, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(77, 1, "www/js/index.js"))
           ,(new JavaSourceLocation(49, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(26, 58, "www/js/index.js"))
           ,(new JavaSourceLocation(49, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(25, 51, "www/js/index.js"))
           ,(new JavaSourceLocation(52, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js"))
           ,(new JavaSourceLocation(36, "com/borismus/webintent/WebIntent"), new JavaScriptSourceLocation(31, 12, "www/js/index.js"))
           ,(new JavaSourceLocation(85, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(77, 1, "www/js/index.js"))
           ,(new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(46, 21, "www/js/index.js"))
           ,(new JavaSourceLocation(62, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"), new JavaScriptSourceLocation(68, 25, "www/js/index.js"))
           ,(new JavaScriptSourceLocation(7, 12, "www/plugins/de.zertapps.dvhma.plugins.webintent/www/webintent.js"), new JavaSourceLocation(28, "com/borismus/webintent/WebIntent"))
           ,(new JavaScriptSourceLocation(28, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
           ,(new JavaScriptSourceLocation(24, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
           ,(new JavaScriptSourceLocation(16, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage"))
           ,(new JavaScriptSourceLocation(20, 12, "www/plugins/de.zertapps.dvhma.plugins.storage/www/DVHMA-Storage.js"), new JavaSourceLocation(43, "de/zertapps/dvhma/plugins/storage/DVHMAStorage")) 
       )
   
   def fixture =
    new {
      val app = new AppTest(ApkName, connections);
      app.analyze();
    }
   
   
   
   "Java -> JavaScript" should "report ten hits" in {
       fixture.app.getJS2JavaHits() should be (10)
   }
   
   it should "report no misses" in {
       fixture.app.getJS2JavaMisses() should be (0)
   }
   
   it should "report no errors" in {
       fixture.app.getJS2JavaErrors() should be (0)
   }

   it should "have a precision of 100%" in {
       fixture.app.getJS2JavaPrecision() should be (100.0 +- 0.1)
   }
   
   it should "have a recall of 100%" in {
       fixture.app.getJS2JavaRecall() should be (100.0 +- 0.1)
   }

   
   
   "JavaScript -> Java" should "report ten hits" in {
       fixture.app.getJS2JavaHits() should be (5)
   }
   
   it should "report no misses" in {
       fixture.app.getJS2JavaMisses() should be (0)
   }
   
   it should "report no errors" in {
       fixture.app.getJS2JavaErrors() should be (0)
   }

   it should "have a precision of 100%" in {
       fixture.app.getJS2JavaPrecision() should be (100.0 +- 0.1)
   }
   
   it should "have a recall of 100%" in {
       fixture.app.getJS2JavaRecall() should be (100.0 +- 0.1)
   }
   
   
   
   "Reported connections" should "contain all expected connections" in {
       fixture.app.getTruePositives() should contain theSameElementsAs connections
   }
   
   it should "not contain false negatives" in {
      fixture.app.getFalseNegatives() shouldBe empty
   }

   it should "not contain false positives" in {
      fixture.app.getFalsePositives() shouldBe empty
   }
   
}












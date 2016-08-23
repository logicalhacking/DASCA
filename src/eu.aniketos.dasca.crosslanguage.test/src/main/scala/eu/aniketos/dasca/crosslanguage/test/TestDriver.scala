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

import eu.aniketos.dasca.crosslanguage.test.apps.FeatherweightInfo
import eu.aniketos.dasca.crosslanguage.test.apps.Featherweight_100_350_Info
import eu.aniketos.dasca.crosslanguage.test.apps.Featherweight_100_630_Info

object TestDriver {
  import AbstractTestDriver._; 

  val infos = List(
      FeatherweightInfo,
      Featherweight_100_350_Info,
      Featherweight_100_630_Info
      )
  
  def main (args: Array[String]):Unit = run(infos,args);
}
 
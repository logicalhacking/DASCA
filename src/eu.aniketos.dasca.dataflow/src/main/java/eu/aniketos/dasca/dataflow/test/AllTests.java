/*
 * (C) Copyright 2016 The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.aniketos.dasca.dataflow.util.PlugInUtil;

import com.ibm.wala.cast.java.client.JavaSourceAnalysisEngine;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.CancelException;

@RunWith(Suite.class)
@SuiteClasses({ 
	Test01.class, Test02.class, Test03.class, Test04.class, 
	Test05.class, Test06.class, Test07.class, Test08.class, 
	Test09.class, Test10.class, Test11.class, Test12.class,
	Test13.class, Test14.class, Test15.class, Test16.class,
	Test17.class, Test18.class, Test19.class 
	})

public class AllTests {	
	protected static ICFGSupergraph superGraph = null;
	protected static String testDir = "../eu.aniketos.dasca.dataflow.test.data/src/main/java/eu/aniketos/dasca/dataflow/test/data/";

	@BeforeClass
	public static void setUp() throws IllegalArgumentException, CancelException, IOException {
		if (null != superGraph){
			return;
		}
		Collection<String> sources = Arrays.asList(testDir);
		List<String> libs = Arrays.asList(WalaProperties.getJ2SEJarFiles());

		JavaSourceAnalysisEngine engine = PlugInUtil.createECJJavaEngine(sources, libs);
		CallGraph cg = engine.buildDefaultCallGraph();

		AnalysisCache ac = new AnalysisCache();
		superGraph = ICFGSupergraph.make(cg, ac);
	} 
}

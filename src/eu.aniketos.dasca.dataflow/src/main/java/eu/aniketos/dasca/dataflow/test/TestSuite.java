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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.apache.log4j.Logger;

import eu.aniketos.dasca.dataflow.util.AnalysisUtil;
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

public class TestSuite {	
	private static Logger log = AnalysisUtil.getLogger(TestSuite.class);
	protected static ICFGSupergraph superGraph = null;
	protected static String testDir = "../eu.aniketos.dasca.dataflow.test.data/src/main/java/eu/aniketos/dasca/dataflow/test/data/";

	protected static List<String> sources = null;
	protected static List<String> libs = null;
	
    protected static String [] customEntryPoints = {
            "Leu/aniketos/dasca/dataflow/test/data/Test01"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test02"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test03"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test04"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test05"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test06"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test07"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test08"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test09"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test10"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test11"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test12"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test13"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test14"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test15"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test16"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test17"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test18"
           ,"Leu/aniketos/dasca/dataflow/test/data/Test19"
        };
		
    
    
	public static void logConfiguration() {
		log.info("Test Configuration:");
		log.info("===================");
	    log.info("Sources:");
	    for (String element : sources) {
	        log.info("    "+element);
	    }  
	    log.info("Libs:");
	    for (String element : libs) {
	        log.info("    "+element);
	    }  
	    log.info("J2SE:" + WalaProperties.J2SE_DIR);
	}
	
	
	public static void initTestSG(String test) throws IllegalArgumentException, CancelException, IOException{
		List<String> entryPoints = new ArrayList<String>();    
		if (null != test) {
			entryPoints.add("Leu/aniketos/dasca/dataflow/test/data/"+test);
		}else{
			entryPoints.addAll(Arrays.asList(customEntryPoints));
		}
		log.info("Generating Test  Specific SG ("+ test +"):");
	    for (String element : entryPoints) {
	        log.info("    "+element);
	    }  

	    JavaSourceAnalysisEngine engine = PlugInUtil.createECJJavaEngine(sources, libs, 
				                                                         entryPoints.stream().toArray(String[]::new));
		CallGraph cg = engine.buildDefaultCallGraph();
		AnalysisCache ac = new AnalysisCache();
		superGraph = ICFGSupergraph.make(cg, ac);
        log.info("CG size: "+cg.getNumberOfNodes());
        log.info("SG size: "+superGraph.getNumberOfNodes());
	}
	
	@BeforeClass
	public static void setUp() throws IllegalArgumentException, CancelException, IOException {
		if (null != superGraph){
			return;
		}
		sources = Arrays.asList(testDir);
		libs = Arrays.asList(WalaProperties.getJ2SEJarFiles());
		logConfiguration();
		initTestSG(null);	} 
}

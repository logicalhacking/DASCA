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

package eu.aniketos.dasca.dataflow.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

import org.junit.Assert;

import com.ibm.wala.cast.java.client.ECJJavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.client.JavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class PlugInUtil {

    public static String REGRESSION_EXCLUSIONS;

    /**
     * Private constructor to ensure no object will be created
     */
    private PlugInUtil() {
    }

    public static void populateScope(JavaSourceAnalysisEngine engine, Collection<String> sources, List<String> libs) {
    	if (null != libs ){
    		boolean foundLib = false;
    		for (String lib : libs) {
    			File libFile = new File(lib);
    			if (libFile.exists()) {
    				foundLib = true;
    				try {
    					engine.addSystemModule(new JarFileModule(new JarFile(libFile, false)));
    				} catch (IOException e) {
    					Assert.fail(e.getMessage());
    				}
    			}
    		}
    		assert foundLib : "couldn't find library file from " + libs;
    	}

    	for (String srcFilePath : sources) {
    		String srcFileName = srcFilePath.substring(srcFilePath.lastIndexOf(File.separator) + 1);
    		File f = new File(srcFilePath);
    		Assert.assertTrue("couldn't find " + srcFilePath, f.exists());
    		if (f.isDirectory()) {
    			engine.addSourceModule(new SourceDirectoryTreeModule(f));
    		} else {
    			engine.addSourceModule(new SourceFileModule(f, srcFileName, null));
    		}
    	}
    }


    

    public static JavaSourceAnalysisEngine
    createECJJavaEngine(Collection<String> sources, List<String> libs)  {
    
        JavaSourceAnalysisEngine engine = new ECJJavaSourceAnalysisEngine() {
            @Override
            protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
                String [] classes= {
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
                return Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE,cha, classes);

            }
        };
        
        engine.setExclusionsFile(REGRESSION_EXCLUSIONS);
        populateScope(engine, sources, libs);
        return engine;
    } 

}

/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.tests;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.chainsaw.Main;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.aniketos.dasca.dataflow.util.AnalysisUtil;
import eu.aniketos.dasca.dataflow.util.PlugInUtil;
import eu.aniketos.dasca.dataflow.util.SuperGraphUtil;

import com.ibm.wala.cast.java.client.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.client.JavaSourceAnalysisEngine;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

@RunWith(Suite.class)
@SuiteClasses({
    /*	Test01.class, Test02.class, Test03.class, Test04.class,
    	Test05.class, */ Test06.class /*, Test07.class, Test08.class,
	Test09.class, Test10.class, Test11.class, Test12.class,
	Test13.class, Test14.class, Test15.class, Test16.class,
	Test17.class, Test18.class, Test19.class
*/
})

public class AllTests {
    private static final Logger log = AnalysisUtil.getLogger(SuperGraphUtil.class);
    protected static ICFGSupergraph superGraph = null;
    protected static ICFGSupergraph superGraph2 = null;

    @BeforeClass
    public static void init() {
//
//		IJavaProject javaProject = null;
//		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
//		for (IProject project : projects) {
//			try {
//				project.open(null /* IProgressMonitor */);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//			IJavaProject javaProjectintern = JavaCore.create(project);
//			if(javaProjectintern.getElementName().equals("TestCases")){
//				log.warn("Found project with name 'TestCases' ("+javaProjectintern+")");
//				javaProject = javaProjectintern;
//			}
//		}
//

        try {
            /*			log.warn("javaProject = "+javaProject);
            			JDTJavaSourceAnalysisEngine engine = PlugInUtil.createEngine(javaProject);
            			log.warn("Engine = "+engine);
            			CallGraph cg = engine.buildDefaultCallGraph();
            			log.warn("CallGraph = "+cg);
            	*/		String [] classes= {
                /*    "Ltests/Test01"
                	  ,"Ltests/Test02"
                	  ,"Ltests/Test03"
                	  ,"Ltests/Test04"
                	  ,"Ltests/Test05"
                	  ,*/
                "Ltests/Test06"
                /*  ,"Ltests/Test07"
                  ,"Ltests/Test08"
                  ,"Ltests/Test09"
                  ,"Ltests/Test10"
                  ,"Ltests/Test11"
                  ,"Ltests/Test12"
                  ,"Ltests/Test13"
                  ,"Ltests/Test14"
                  ,"Ltests/Test15"
                  ,"Ltests/Test16"
                  ,"Ltests/Test17"
                  ,"Ltests/Test18"
                  ,"Ltests/Test19"
                */
            };
            CallGraph cg = com.sap.research.wala.cross.javajs.SingleLanguageBuilders.makeJavaCG("test.jar", classes).fst;
            AnalysisCache ac = new AnalysisCache();
            log.warn("AnalsyisCache = "+ac);
//			CallGraph cg = Main.makeJavaCG();
            superGraph = ICFGSupergraph.make(cg, ac);
            superGraph2 = superGraph;
            log.warn("SuperGraph = "+superGraph);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CancelException e) {
            e.printStackTrace();
        } catch (ClassHierarchyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

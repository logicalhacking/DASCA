package eu.aniketos.dasca.dataflow.test;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.aniketos.dasca.dataflow.util.PlugInUtil;

import com.ibm.wala.cast.java.client.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.client.JavaSourceAnalysisEngine;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CallGraph;
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
	private  static final String testProject="eu.aniketos.dasca.dataflow.test.data";
	
	public static void init() throws IllegalArgumentException, CancelException, IOException, CoreException {
		if (null == superGraph){
		    IJavaProject javaProject = null;
		    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		    for (IProject project : projects) {
			    try {
			    	project.open(null /* IProgressMonitor */);
			        IJavaProject javaProjectintern = JavaCore.create(project);
			        if(javaProjectintern.getElementName().toString().equals(testProject)){
				        javaProject = javaProjectintern;
			        }
			    }catch (Exception e){
			    	e.printStackTrace();
			    }
		   }
		   assert javaProject != null : "Project >>"+testProject+"<< not found.";
		   System.err.println(""+javaProject.getElementName());

		   JDTJavaSourceAnalysisEngine engine = PlugInUtil.createJDTJavaEngine(javaProject);
		   CallGraph cg = engine.buildDefaultCallGraph();

		   AnalysisCache ac = new AnalysisCache();
		   superGraph = ICFGSupergraph.make(cg, ac);
		}
	} 
}

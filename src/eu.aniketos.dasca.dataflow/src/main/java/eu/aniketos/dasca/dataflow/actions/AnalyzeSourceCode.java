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

package eu.aniketos.dasca.dataflow.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import eu.aniketos.dasca.dataflow.util.AnalysisUtil;
import eu.aniketos.dasca.dataflow.util.PlugInUtil;
import eu.aniketos.dasca.dataflow.util.SuperGraphUtil;

import com.ibm.wala.cast.java.client.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.util.CancelException;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class AnalyzeSourceCode implements IWorkbenchWindowActionDelegate {
    Logger log = AnalysisUtil.getLogger(AnalyzeSourceCode.class);
    /**
     * The constructor.
     */
    public AnalyzeSourceCode() {
    }


    /**
     * The action has been activated. The argument of the
     * method represents the 'real' action sitting
     * in the workbench UI.
     * @see IWorkbenchWindowActionDelegate#run
     */
    public void run(IAction action) {

        List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();

        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for(IProject project: projects) {
            try {
                project.open(null /* IProgressMonitor */);
            } catch (CoreException e) {
                e.printStackTrace();
            }
            IJavaProject javaProject = JavaCore.create(project);
            javaProjects.add(javaProject);
        }

        IJavaProject javaProject = null;
        String testProject = AnalysisUtil.getPropertyString(AnalysisUtil.CONFIG_ENTRY_CLASS);

        for (IJavaProject iJavaProject : javaProjects) {
            boolean bool = iJavaProject.exists();
            if(bool && iJavaProject.getElementName().startsWith(testProject)) {
                javaProject = iJavaProject;
            }
        }

        if(javaProject == null) {
            javaProject = PlugInUtil.getSelectedIJavaProject();
        }

        if (javaProject != null) {

            try {
                JDTJavaSourceAnalysisEngine engine = PlugInUtil.createEngine(javaProject);

                CallGraph cg = engine.buildDefaultCallGraph();

                log.debug("callgraph generated (size:" + cg.getNumberOfNodes() + ")");

                AnalysisCache ac = new AnalysisCache();
                ICFGSupergraph sg = ICFGSupergraph.make(cg, ac);
                log.debug("supergraph generated (size:" + sg.getNumberOfNodes() + ")");

                String entryClass = AnalysisUtil.getPropertyString(AnalysisUtil.CONFIG_ENTRY_CLASS);
                String entryMethods = AnalysisUtil.getPropertyString(AnalysisUtil.CONFIG_ENTRY_METHOD);

                String[] methods = entryMethods.split(",");
                for (int i = 0; i < methods.length; i++) {
                    String entryMethod = methods[i].trim();
                    SuperGraphUtil.analyzeAndSaveSuperGraph(sg, entryClass, entryMethod);
                }

            } catch (IOException e) {
                log.error("IOException In AnalyzeSourceCode.run: ",e);
            } catch (CoreException e) {
                log.error("CoreException In AnalyzeSourceCode.run: ",e);
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException In AnalyzeSourceCode.run: ",e);
            } catch (CancelException e) {
                log.error("CancelException In AnalyzeSourceCode.run: ",e);
            }
        } else {
            log.error("Warning: You did not select a project or something failed while getting the project");
        }
    }

    /**
     * Selection in the workbench has been changed. We
     * can change the state of the 'real' action here
     * if we want, but this can only happen after
     * the delegate has been created.
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * We can use this method to dispose of any system
     * resources we previously allocated.
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
    }

    /**
     * We will cache window object in order to
     * be able to provide parent shell for the message dialog.
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window) {
    }
}
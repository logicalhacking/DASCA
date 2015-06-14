/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.util;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.wala.cast.java.client.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.client.JavaSourceAnalysisEngine;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;

public class PlugInUtil {

    public static String REGRESSION_EXCLUSIONS;

    /**
     * Private constructor to ensure no object will be created
     */
    private PlugInUtil() {
    }

    /**
     * Get the selected project. Can return null if no project is selected
     *
     * @return
     */
    public static IJavaProject getSelectedIJavaProject() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IJavaProject javaProject = null;

        assert window == null : "Eclipse failed to load the active workbench window in BuildCallgraph.run()";
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof TreeSelection) {
                TreeSelection tselection = (TreeSelection) selection;
                Object firstElement = tselection.getFirstElement();
                if (firstElement instanceof IAdaptable) {
                    IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject.class);
                    javaProject = JavaCore.create(project);
                }
            }
        }
        return javaProject;
    }

    /**
     * Created the {@link JavaSourceAnalysisEngine} for a given {@link IJavaProject}
     * @param project
     * @return
     * @throws IOException
     * @throws CoreException
     * @throws IllegalArgumentException
     * @throws CancelException
     */
    public static JDTJavaSourceAnalysisEngine /* JavaSourceAnalysisEngine */
    createEngine(IJavaProject project) throws IOException, CoreException, IllegalArgumentException, CancelException {
        assert project == null : "You must provide a valid IJavaProject";
        project.open(null);

        JDTJavaSourceAnalysisEngine engine;
        // engine = new JDTJavaSourceAnalysisEngine(project.getElementName());


        engine = new JDTJavaSourceAnalysisEngine(project.getElementName()) {
            @Override
            protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
                String [] classes= {
                    "Ltests/Test01"
                    ,"Ltests/Test02"
                    ,"Ltests/Test03"
                    ,"Ltests/Test04"
                    ,"Ltests/Test05"
                    ,"Ltests/Test06"
                    ,"Ltests/Test07"
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
                };


                return Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE,cha, classes);

            }
        };
        engine.setExclusionsFile(REGRESSION_EXCLUSIONS);

        return engine;
    }

}

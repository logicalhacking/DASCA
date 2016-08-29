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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.ConcreteJavaMethod;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;

/**
 * static class for graph independent helper methods and constants
 *
 */
public class AnalysisUtil {

    private static final Logger log = AnalysisUtil.getLogger(AnalysisUtil.class);

    /**
     * Relative path to the main configuration file
     */
    public static final String MAIN_CONFIG = "config/main.config";

    /**
     * Relative path to the logging configuration file
     */
    public static final String LOGGING_FILE = "logging_properties_file";

    /**
     * Boolean value of configuration file, if subgraphs should be included into the DOT file
     */
    public static final String CONFIG_BOOLEAN_PRINT_SUBGRAPHS = "bool_print_subgraphs";

    /**
     * DOT files will be generated into the specified sub directory of the working directory
     */
    public static final String CONFIG_DOT_PATH = "dot_path";

    /**
     * DOT files will be generated into the specified sub directory of the working directory
     */
    public static final String CONFIG_DOT_REMOVE_EMPTY_NODES = "dot_remove_empty_nodes";

    /**
     * Specifies which project contains the entry class
     */
    public static final String CONFIG_ANALYSIS_PROJECT = "analysis_project";

    /**
     * Specifies which class contains the entry method
     */
    public static final String CONFIG_ENTRY_CLASS = "analysis_entry_class";

    /**
     * Specifies the entry methods of the analysis
     */
    public static final String CONFIG_ENTRY_METHOD = "analysis_entry_method";

    /**
     * Specifies sanitizing methods of the analysis
     */
    public static final String CONFIG_SANITIZER = "analysis_sanitizer";

    /**
     * Specifies bad source methods of the analysis
     */
    public static final String CONFIG_BAD_SRC = "analysis_bad_src";

    /**
     * Specifies the depth (precision) of the analysis
     */
    public static final String CONFIG_ANALYSIS_DEPTH = "analysis_depth";
    public static final int ANALYSIS_DEPTH_DETECTION = 0;
    public static final int ANALYSIS_DEPTH_EXCLUSIVE = 1;
    public static final int ANALYSIS_DEPTH_SANITIZING = 2;

    /**
     * Specifies where to find the exclusion file
     */
    public static final String CONFIG_EXCLUSION_FILE = "analysis_exclusion_file";

    /**
     * Gets callgraph for given parameters (binary analysis only)
     * @param exclusionFilePath
     * @param classPath
     * @param entryClass
     * @param entryMethod
     * @return
     */
    public static CallGraph getCallGraph(String exclusionFilePath, String classPath, String entryClass, String entryMethod) {
        AnalysisScope scope = null;
        ClassHierarchy cha = null;
        HashSet<Entrypoint> entryPoints = null;
        try {
            File exclusionFile = new File(exclusionFilePath);
            scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classPath, exclusionFile); // works with class and jar files
            cha = ClassHierarchy.make(scope);

            ClassLoaderReference clr = scope.getApplicationLoader();
            entryPoints = HashSetFactory.make();
            for(IClass class1 : cha) {
                if(class1.getClassLoader().getReference().equals(clr)) {
                    Collection<IMethod> allMethods = class1.getDeclaredMethods();
                    for(IMethod m : allMethods) {
                        if(m.isPrivate()) {
                            continue;
                        }
                        TypeName tn = m.getDeclaringClass().getName();//MainApplication
                        if(tn.toString().contains("/" + entryClass) && m.getName().toString().contains(entryMethod)) { // TODO: too weak
                            entryPoints.add(new DefaultEntrypoint(m, cha));
                        }
                    }
                }
            }
            //	    Iterable<Entrypoint> result1 = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha); // uses the static main methods as entry methods
            if(entryPoints.size() == 0) {
                log.error("Could not find specified entry point for analysis.\n" +
                          " path: " + classPath + "\n" +
                          " class: " + entryClass + "\n" +
                          " method: " + entryMethod);
                System.exit(1);
            }
            AnalysisOptions options = new AnalysisOptions(scope, entryPoints);

            //		CallGraphBuilder builder = com.ibm.wala.ipa.callgraph.impl.Util.makeRTABuilder(options, new AnalysisCache(), cha, scope); // Rapid Type Analysis
            SSAPropagationCallGraphBuilder builder = com.ibm.wala.ipa.callgraph.impl.Util.makeZeroCFABuilder(options, new AnalysisCache(), cha, scope); // 0-CFA = context-insensitive, class-based heap
            //		CallGraphBuilder builder = com.ibm.wala.ipa.callgraph.impl.Util.makeZeroOneCFABuilder(options, new AnalysisCache(), cha, scope); // 0-1-CFA = context-insensitive, allocation-site-based heap
            //		CallGraphBuilder builder = com.ibm.wala.ipa.callgraph.impl.Util.makeZeroOneContainerCFABuilder(options, new AnalysisCache(), cha, scope); // 0-1-Container-CFA = object-sensitive container

            return builder.makeCallGraph(options);
        } catch (Exception e) {
            log.error("Error while building the call graph");
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    /**
     * Gets the corresponding integer value of the given property name, or 0 if no integer value was found
     * @param name
     * @return
     */
    public static int getPropertyInteger(String name) {
        String value = getPropertyString(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Gets the corresponding boolean value of the given property name, or false if no value was found
     * @param name
     * @return
     */
    public static boolean getPropertyBoolean(String name) {
        String value = getPropertyString(name);
        return value.equalsIgnoreCase("yes") | value.equalsIgnoreCase("true");
    }

    /**
     * Gets the corresponding value of the given property name, or an empty String if no value was found
     * @param name
     * @return
     */
    public static String getPropertyString(String name) {
        Properties properties = new Properties();
        properties.setProperty("MAIN_CONFIG", MAIN_CONFIG);
        BufferedInputStream stream;
        try {
            InputStream in;
            File f = new File(MAIN_CONFIG);
            in = new FileInputStream(f);
            System.err.println("Reading configuration file: "+f);
            stream = new BufferedInputStream(in);
            properties.load(stream);
            stream.close();
        } catch (FileNotFoundException e) {
            log.warn("Warning: no config file found:", e);
            properties.setProperty("MAIN_CONFIG", MAIN_CONFIG + " (not found)");
        } catch (IOException e) {
            log.error("Failure reading no config file:", e);
            properties.setProperty("MAIN_CONFIG", MAIN_CONFIG + " (reading error)");
        }
        String value = properties.getProperty(name);
        return value == null ? "" : value;
    }

    /**
     * Removes all chars from the string which confuse dotty <br />
     * Currently '<' and '>' <br />
     * Expand this if you run into trouble with other chars
     * @param input
     * @return
     */
    public static String sanitize(String input) {
        String output = "";

        for (char c : input.toCharArray()) {
            if (!(c == '<' || c == '>')) {
                output += c;
            } else {
                output += ' ';
            }
        }
        return output;
    }

    /**
     * Get root logger
     * @param class
     * @return
     */
    public static org.apache.log4j.Logger getLogger(Class<?> class1) {
        Logger log = Logger.getLogger(class1);
        PropertyConfigurator.configure(getPropertyString(LOGGING_FILE));
        return log;
    }

    /**
     * Get source code line number for each instruction
     * @param sgNodes
     * @param print
     * @return
     */
    public static HashMap<SSAInstruction, Integer> getLineNumbers(HashMap<Integer,BasicBlockInContext<IExplodedBasicBlock>> sgNodes) {
        log.debug("** get source code line number for each instruction");
        HashMap<SSAInstruction, Integer> map = new HashMap<SSAInstruction, Integer>();
        for(BasicBlockInContext<IExplodedBasicBlock> bbic : sgNodes.values()) {
            SSAInstruction inst = bbic.getLastInstruction();
            if(inst == null) {
                continue;
            }
//            ConcreteJavaMethod method = (ConcreteJavaMethod) bbic.getMethod();
            IMethod method =  bbic.getMethod();
            int lineNumber = method.getLineNumber(bbic.getLastInstructionIndex());
            map.put(inst, lineNumber);
            log.debug(lineNumber + ". " + inst);
        }
        return map;
    }

    /**
     * Get corresponding instruction for the definition of each SSA value
     * @param sgNodes
     * @return
     */
    public static HashMap<Integer, SSAInstruction> getDefs(HashMap<Integer,BasicBlockInContext<IExplodedBasicBlock>> sgNodes) {
        log.debug("** get definition instruction for each SSA value");
        HashMap<Integer, SSAInstruction> map = new HashMap<Integer, SSAInstruction>();
        for(BasicBlockInContext<IExplodedBasicBlock> bbic : sgNodes.values()) {
            SymbolTable symbolTable = bbic.getNode().getIR().getSymbolTable();
            DefUse du = bbic.getNode().getDU();

            for (int i = 0; i <= symbolTable.getMaxValueNumber(); i++) {
                log.debug(i + " [" + symbolTable.getValueString(i) + "] " + du.getDef(i));
                map.put(i, du.getDef(i));
            }
            break; // there are the same definitions in each basic block, iff there is only one method FIXME: read different scopes, if multiple methods are required
        }
        return map;
    }

    /**
     * Get the corresponding instruction for each SQL execution (java.sql)
     * @return
     */
    public static ArrayList<SSAInstruction> getSQLExecutes(HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes) {
        log.debug("** get SQL execution instructions");
        ArrayList<SSAInstruction> list = new ArrayList<SSAInstruction>();
        for(BasicBlockInContext<IExplodedBasicBlock> bbic : sgNodes.values()) {
            SSAInstruction inst = bbic.getLastInstruction();
            if(inst != null && inst instanceof AstJavaInvokeInstruction && inst.toString().contains("java/sql") && inst.toString().contains("execute")) {
                log.debug("SQL execution instruction: " + inst.toString());
                list.add(inst);
            }
        }
        return list;
    }

    /**
     * Get the corresponding instruction for each conditional branch
     * @return
     */
    public static ArrayList<SSAInstruction> getConditions(HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes) {
        log.debug("** get conditional branch instructions");
        ArrayList<SSAInstruction> list = new ArrayList<SSAInstruction>();
        for(BasicBlockInContext<IExplodedBasicBlock> bbic : sgNodes.values()) {
            SSAInstruction inst = bbic.getLastInstruction();
            if(inst != null && inst instanceof SSAConditionalBranchInstruction) {
                log.debug("conditional branch instruction: " + inst.toString());
                list.add(inst);
            }
        }
        return list;
    }


    /**
     * Prints adjacency list to log using debug level
     * @param adjList
     */
    public static void printAdjList(HashMap<Integer, ArrayList<Integer>> adjList, Logger log) {
        ArrayList<Integer> keySet1 = new ArrayList<Integer>(adjList.keySet());
        Collections.sort(keySet1);
        for (Integer src: keySet1) {
            log.debug(" " + src + ":");
            StringBuffer sb = new StringBuffer();
            for(Integer dest : adjList.get(src)) {
                sb.append(", " + dest);
            }
            sb.append("  ");
            log.debug("  " + sb.substring(1));
        }
    }

    public static ArrayList<SSAInstruction> analyzeStatementExecute(
        SSAInstruction ssaInstruction,
        HashMap<Integer, SSAInstruction> definitions,
        boolean prepared,
        HashSet<String> badMethods) {

        if(prepared) {
            ssaInstruction = definitions.get(ssaInstruction.getUse(0));
        }

        ArrayList<SSAInstruction> sources = new ArrayList<SSAInstruction>();
        int use = ssaInstruction.getUse(1); // use 0 is the createStatement, use 1 is the SQL String
        SSAInstruction inst = definitions.get(use);
        if(inst != null) {
            String instString = inst.toString();
            if(isBadSource(instString, badMethods)) {
                if(!sources.contains(inst)) {
                    log.debug("SINK [bad]: " + ssaInstruction);
                    sources.add(inst);
                }
            }
            if(inst instanceof SSABinaryOpInstruction) {
                if(isBadBinaryOpSource(use, definitions, sources, badMethods)) {
                    log.debug("SINK [bad]: " + ssaInstruction);
                    return sources;
                } else {
                    log.debug("SINK [good]: " + ssaInstruction);
                }
            } else if(inst instanceof SSAPhiInstruction) {
                if(isBadPhiInstruction(use, definitions, sources, badMethods)) {
                    log.debug("SINK [bad]: " + ssaInstruction);
                    return sources;
                } else {
                    log.debug("SINK [good]: " + ssaInstruction);
                }
            } else { //FIXME: rewrite to according procedure
                log.debug("unidentified instruction [" + instString + "], handled like phi function");
                if(isBadPhiInstruction(use, definitions, sources, badMethods)) {
                    log.debug("SINK [bad]: " + ssaInstruction);
                    return sources;
                } else {
                    log.debug("SINK [good]: " + ssaInstruction);
                }
            }

        } else { // is constant String
            log.debug("SINK [good]: " + ssaInstruction.toString());
        }
        return sources;
    }

    public static boolean isBadSource(String instString, HashSet<String> badMethods) {
        if(instString != null) {
            for (String src : badMethods) {
                if(instString.contains(src)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isBadBinaryOpSource(int use, HashMap<Integer,SSAInstruction> definitions, ArrayList<SSAInstruction> badSources, HashSet<String> badMethods) {
        SSAInstruction inst = definitions.get(use);
        int part1 = inst.getUse(0);
        SSAInstruction inst1 = definitions.get(part1);
        String inst1String = "";
        if(inst1 != null) {
            inst1String = inst1.toString();
        }

        int part2 = inst.getUse(1);
        SSAInstruction inst2 = definitions.get(part2);
        String inst2String = "";
        if(inst2 != null) {
            inst2String = inst2.toString();
        }

        boolean isBad = false;
        if(inst1 == null) {
            isBad = isBad | false;
        } else if(inst1 instanceof SSABinaryOpInstruction) {
            isBad = isBad | isBadBinaryOpSource(part1, definitions, badSources, badMethods);
        } else if(inst1 instanceof SSAPhiInstruction) {
            isBad = isBad | isBadPhiInstruction(part1, definitions, badSources, badMethods);
        } else if(isBadSource(inst1String, badMethods)) {
            if(!badSources.contains(inst1)) {
                log.debug("SOURCE [bad]: " + inst1String);
                badSources.add(inst1);
            }
            isBad = true;
        }
        if(inst2 == null) {
            isBad = isBad | false;
        } else if(inst2 instanceof SSABinaryOpInstruction) {
            isBad = isBad | isBadBinaryOpSource(part2, definitions, badSources, badMethods);
        } else if(inst2 instanceof SSAPhiInstruction) {
            isBad = isBad | isBadPhiInstruction(part2, definitions, badSources, badMethods);
        } else if(isBadSource(inst2String, badMethods)) {
            if(!badSources.contains(inst2)) {
                log.debug("SOURCE [bad]: " + inst2String);
                badSources.add(inst2);
            }
            isBad = true;
        }
        return isBad;
    }


    private static boolean isBadPhiInstruction(int use,
            HashMap<Integer, SSAInstruction> definitions,
            ArrayList<SSAInstruction> badSources, HashSet<String> badMethods) {
        SSAInstruction inst = definitions.get(use);
        boolean isBad = false;
        SSAPhiInstruction phiInst = (SSAPhiInstruction) inst;
        for(int i=0; i<phiInst.getNumberOfUses(); i++) {
            int part = phiInst.getUse(i);
            SSAInstruction useInst = definitions.get(part);
            String useInstString = "";
            if(useInst != null) {
                useInstString = useInst.toString();
            }
            if(useInst == null) {
                isBad = isBad | false;
            } else if(useInst instanceof SSABinaryOpInstruction) {
                isBad = isBad | isBadBinaryOpSource(part, definitions, badSources, badMethods);
            } else if(useInst instanceof SSAPhiInstruction) {
                isBad = isBad | isBadPhiInstruction(part, definitions, badSources, badMethods);
            } else if(isBadSource(useInstString, badMethods)) {
                if(!badSources.contains(useInst)) {
                    log.debug("SOURCE [bad]: " + useInstString);
                    badSources.add(useInst);
                }
                isBad = true;
            }
        }
        return isBad;
    }
}

/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.js;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.ibm.wala.cast.ir.ssa.AstGlobalRead;
import com.ibm.wala.cast.ir.ssa.AstGlobalWrite;
import com.ibm.wala.cast.ir.ssa.AstIsDefinedInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess.Access;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.js.ipa.callgraph.JavaScriptEntryPoints;
import com.ibm.wala.cast.js.ssa.JavaScriptCheckReference;
import com.ibm.wala.cast.js.ssa.JavaScriptInvoke;
import com.ibm.wala.cast.js.ssa.PrototypeLookup;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.Statement.Kind;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;


public class Main {
    public static String fileName;
    public static String exampeDir;
    public static String fileExtension;
    public static SDG sdg;
    public static ArrayList<String> transfer = new ArrayList<String>();

    /**
     * @param args
     * @throws NoSuchMethodException
     * @throws ScriptException
     */
    public static void main(String[] args) throws ScriptException,
        NoSuchMethodException {

        System.out.println("Proogramm Start:");
        System.out.println(" Pls enter file to read without ending like .js");
        Scanner in = new Scanner(System.in);
        fileName = in.nextLine();

        // The folder within this project
        exampeDir = "tim";
        fileExtension = "js";

        try {

            analyzeHTML5(exampeDir, fileName, fileExtension);

        } catch (IllegalArgumentException e) {
            System.out.println("Wrong Filename?");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        } catch (CancelException e) {
            System.out.println("abbruch");
            e.printStackTrace();
        }

        System.out.println("File ge�ffnet:" + fileName);
    }

    /**
     * Analyzes the JavaScript first build the gallgraph.
     * {@link #createCallgraph(String, String, String)} Than create the SDG for
     * slice {@link #buildSDG(CallGraph, PointerAnalysis)} Than you can slice
     * {@link #doSlice(SDG, Statement)}
     *
     * @param dir
     *            The directory of the file
     * @param file
     *            The filename is a input from the console
     * @param ext
     *            The file extension (js, html)
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws CancelException
     * @throws NoSuchMethodException
     * @throws ScriptException
     */
    public static void analyzeHTML5(String dir, String file, String ext)
    throws IllegalArgumentException, IOException, CancelException,
        ScriptException, NoSuchMethodException {

        // Load the script and cast with Rhino

        // Create all available graphs and structures for Wala with JavaScript
        // CallGraph cg2 = ImprovedJSCallGraphBuilderUtil.makeScriptCG(dir, file
        // + "." + ext);

        // Improved graph needed for pointer analysis

        // JSCallGraphUtil.setTranslatorFactory(new
        // CAstRhinoTranslatorFactory());
        // System.out.println("at" + dir + file + ext);
        // CallGraph cg3 = createCallgraph(dir, file, ext);
        // ImprovedJSCallGraphBuilderUtil.CGBuilderType cgbtype = getCGB();
        // PropagationCallGraphBuilder b = ImprovedJSCallGraphBuilderUtil
        // .makeScriptCGBuilder(dir, file + "." + ext, cgbtype);
        // CallGraph cg = b.makeCallGraph(b.getOptions());
        //
        // PointerAnalysis pa = b.getPointerAnalysis();;
        //
        // AnalysisCache ac = new AnalysisCache();
        // ICFGSupergraph sg = ICFGSupergraph.make(cg, ac);
        // SuperGraphUtil.printSupergraphToDot(sg);
        //
        // SDG sdg = buildSDG(cg3, pa);
        //
        //
        // Collection<CGNode> NodesToAnalyze = new HashSet<CGNode>();
        // NodesToAnalyze.addAll(getFunctionNode(cg3, file));
        // printFirst(NodesToAnalyze);
        //

        // this is the number of the seed node
        // Statement st = sdg.getNode(1438); // the node must be a normal
        // statement

        // writeInstructions(st);
        // findSeed("invoke", sdg);

        // doSlice(sdg, st);

        // Get the fake root node and all nodes that contain the scripts name.

    }
    public static Statement findSeed(String part, SDG sdg) {
        Statement s = null;

        System.out
        .println("\n Looking for possible Seedstatements containing  "
                 + part + ":\n");
        for (int i = 0; i < sdg.getMaxNumber(); i++) {
            if ((sdg.getNode(i).toString().contains(part) == true)
                    && (sdg.getNode(i).toString().contains("prolo") == false)
                    && (((Statement)sdg.getNode(i)).getKind() == Kind.NORMAL)) {
                writeInstructions((Statement)sdg.getNode(i));

                System.out.println("Number:" + i + "|" + sdg.getNode(i) + "");

            }
        }

        System.out.println("No more Statements found\n");
        return s;
    }
    /**
     * Print the collection from nodes and call {@link showMe} for printing with
     * more details. TODO make it beautiful its to ugly
     *
     * @param NodesToAnalyze
     */
    public static void printFirst(Collection<CGNode> NodesToAnalyze) {

        System.out.println("NodesToAnalyze:");
        printSSA(NodesToAnalyze);
        System.out.println("Ende");
        System.out.println("Nodes to analyze=" + NodesToAnalyze.size());
        showMe(NodesToAnalyze);
    }

    /**
     * This method builds the SDG( system dependence graph) for the slice
     * Further all nodes will be printed with node+number.
     *
     * @param cg3
     * @param pa
     * @return
     */
    public static SDG buildSDG(CallGraph cg3, PointerAnalysis pa) {
        SDG sdg = new SDG(cg3, pa, Slicer.DataDependenceOptions.NO_HEAP,
                          Slicer.ControlDependenceOptions.NONE);
        // printSDG(sdg);
        return sdg;
    }

    /**
     * Method to print sdg to the Console and return a list of all SDG
     * Statement. Opt= false means only node not containing "prolo". Opt= true
     * means all nodes.
     *
     * @param sdg
     * @param opt
     */
    public static List<String> printSDG(SDG sdg, boolean opt) {
        System.out.println("Printing SDG");
        List<String> out = new ArrayList<String>();

        for (int i = 0; i < sdg.getNumberOfNodes(); i++) {

            if (opt == false) {
                if (sdg.getNode(i).toString().contains("prolo") == false) {
                    out.add(sdg.getNode(i).toString() + "This is node|" + i);
                    System.out.println(sdg.getNode(i) + "|This is node= |" + i
                                       + "|");
                }
            }
            if (opt == true) {
                out.add(sdg.getNode(i).toString() + "This is node|" + i);
                System.out.println(sdg.getNode(i) + "|This is node= |" + i
                                   + "|");

            }
        }
        return out;

    }
    /**
     * Build the Callgraph very important without CG no further analysis :(.
     *
     * @param dir
     * @param file
     * @param ext
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws CancelException
     * @throws WalaException 
     */
    public static CallGraph createCallgraph(String dir, String file, String ext)
    throws IllegalArgumentException, IOException, CancelException, WalaException {
        ImprovedJSCallGraphBuilderUtil.CGBuilderType cgbtype = getCGB();
        System.out.println("cg" + dir + file + "." + ext + cgbtype);
        CallGraph cg3 = ImprovedJSCallGraphBuilderUtil.makeScriptCG(dir, file,
                        cgbtype);

        return cg3;
    }
    /**
     * Creates the CGBTYPE for {@link #createCallgraph(String, String, String)}
     *
     * @return
     */
    public static ImprovedJSCallGraphBuilderUtil.CGBuilderType getCGB() {
        ImprovedJSCallGraphBuilderUtil.CGBuilderType cgbtype = ImprovedJSCallGraphBuilderUtil.CGBuilderType.ONE_CFA;
        return cgbtype;
    }

    /**
     * for every node in NodesToAnalyze get the IR and show the Number of
     * Parameters Get the Symboltable of the IR and and print values !=null so
     * you can see which content in which variable
     *
     * @param NodesToAnalyze
     * @TODO change it for our use
     */
    public static void showMe(Collection<CGNode> NodesToAnalyze) {
        for (CGNode node : NodesToAnalyze) {

            // System.out.println("Node Name=" + node.getIR().toString());
            System.out.println("__________start IR printing_____________");
            System.out.println("Node getIR=" + node.getIR());
            System.out.println("Parameters="
                               + node.getIR().getNumberOfParameters() + " |Methoden Name="
                               + node.getMethod().getName());
            System.out.println("__________Ende IR printing_____________");

            // Print all of the Symboltable
            SymbolTable symtab = node.getIR().getSymbolTable();

            List<String> list = new ArrayList<String>();
            System.out.println("--------Printing all available values--------");
            for (int i = 1; i < symtab.getMaxValueNumber(); i++) {
                if (symtab.getValue(i) != null) {
                    list.add(symtab.getValue(i).toString());

                }
                System.out.println(" | Symbol Value="
                                   + symtab.getValueString(i));

                if (symtab.isConstant(i) == true) {
                    System.out.println(" |isConstant =" + symtab.isConstant(i));
                }

            }

            System.out.println(list);
        }
        System.out.println("----------End values-----------");

    }
    // beta not yet included
    public static CGNode findMethod(CallGraph cg, String name) {
        Atom a = Atom.findOrCreateUnicodeAtom(name);

        for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext();) {
            CGNode n = it.next();
            if (n.getMethod().getName().equals(a)) {
                return n;
            }
        }
        System.err.println("call graph " + cg);
        Assertions.UNREACHABLE("failed to find method " + name);
        return null;
    }
    /**
     * This method reads a file and transfers input into a String List.
     */
    public static ArrayList<String> getList() throws FileNotFoundException {
        ArrayList<String> list = new ArrayList<String>();
        File file = new File("");

        final Scanner scanner = new Scanner(new File(fileName));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            list.add(line);

        }
        return list;
    }
    /**
     * This method gets the lineNumber from a statement. Also it will display
     * the Javascript Code from the input file where lineNumber=
     * getSourcenumber. Also the line of code in the sourcefile will be written
     * into slice .txt {@link doWriteFile }
     *
     * @param s
     * @return String Code
     * @throws IOException
     * @throws ScriptException
     */
    public static String getsourceNumber(Statement s) throws IOException,
        ScriptException {
        String code = null;
        int lineNum = 0;
        if (s.getKind() == Statement.Kind.NORMAL) {
            int instructionIndex = ((NormalStatement) s).getInstructionIndex();

            lineNum = s.getNode().getMethod().getLineNumber(instructionIndex);
            System.out.println(" -----------------------------------------"
                               + s.getNode().getMethod().getSignature());
            SymbolTable symtab = s.getNode().getIR().getSymbolTable();

            ArrayList<String> list = getList();

            if (lineNum >= 0 && lineNum < list.size()) {

                System.out.println("Source line number = " + lineNum
                                   + "   atIndex:" + instructionIndex);
                System.out.println("Line:" + (list.get(lineNum - 1)));

                code = "			" + list.get(lineNum - 1);
                // evalState(list.get(lineNum - 1));
            }

            System.out.println("----------End values-----------");

        } else
            System.out.println("No normal Statement found");

        return code;
    }
    /**
     * Method to get the instruction index. Statement s needs to be a
     * normalstatement.
     *
     * @param s
     */
    public static int getSliceIndex(Statement s) {
        int instructionIndex = 0;
        int lineNum = 0;
        if (s.getKind() == Statement.Kind.NORMAL) {

            instructionIndex = ((NormalStatement) s).getInstructionIndex();

        }
        return instructionIndex;
    }
    /**
     * Method for slice output in red color for better overview :) Also calls
     * writeInstructions
     *
     * @param slice
     * @throws IOException
     * @throws ScriptException
     */
    public static List<String> dumpSlice(Collection<Statement> slice, int mode)
    throws IOException, ScriptException {
        ArrayList<String> sliceCode = new ArrayList<String>();
        ArrayList<String> codeListe = new ArrayList<String>();

        for (Statement s : slice) {

            System.out
            .println("Das einzelne statement : ||||" + s + "||||||||");
            String temp = getsourceNumber(s);
            sliceCode.add(s.toString());
            if (codeListe.contains(temp) == false) {

                codeListe.add(temp);
                if (getSliceIndex(s) >= 0)
                    codeListe
                    .add("At index" + String.valueOf(getSliceIndex(s)));
                sliceCode.add(temp);

                // writeInstructions(s);
            } else {

                // do nothing and enjoy the day
            }
        }

        // doWriteFile(codeListe);
        System.out.println("SliceCode" + sliceCode);
        if (mode == 1) {
            return sliceCode;
        } else
            return codeListe;
    }
    /**
     * Method for build function body of js. It is calles in the gui through
     * button "generate funtion body" in the ui.
     *
     * @param slice
     *            the generated slice
     * @throws function
     *             for opt
     * @return List of new Code
     */
    public static List<String> dumpSliceCode(Collection<Statement> slice,
            boolean function) throws IOException, ScriptException {
        ArrayList<String> sliceCode = new ArrayList<String>();
        ArrayList<String> codeListe = new ArrayList<String>();
        ArrayList<String> thatList = new ArrayList<String>();
        String temp2 = "";
        String cat = "";

        int count = 0;
        for (Statement s : slice) {
            String temp4 = "";
            List<String> para = new ArrayList<String>();
            GUI gui = new GUI();
            // if controlbox createFuncBody is enabled

            if (function) {
                temp2 = gui.getFunctionName(s.toString());
                System.out.println("Temp2" + temp2);
                thatList.add(count, temp2);
                if (count >= 1 && temp2 != "") {
                    if (thatList.get(count - 1).equals(temp2) == false) {
                        codeListe.add("}");
                    }
                }
                para = gui.extractFunctionParameter(s);

                if (para.isEmpty()) {

                } else {
                    temp2 = temp2.substring(0, temp2.length() - 1);
                    cat = temp2 + "();";

                }
                // for (int i = 0; i < para.size(); i++) {
                // temp4 += para.get(i) + ",";
                //
                // }
                // if (temp4.endsWith(",")) {
                //
                // temp4 = (String) temp4.subSequence(0,
                // temp4.lastIndexOf(","));
                // }

                System.out.println(s.toString() + "temp" + temp2);
                String temp3 = "function " + temp2 + "(" + ")";
                temp4 = "function " + temp2;

                if (gui.checCode(temp4, transfer) == "") {

                } else
                    temp3 = gui.checCode(temp4, transfer);

                if (codeListe.contains(temp3) == false && temp3 != null) {
                    System.out.println("Temp3" + temp3);
                    codeListe.add(temp3);
                }
            }
            String temp = getsourceNumber(s);
            sliceCode.add(s.toString());
            if (codeListe.contains(temp) == false && temp != null) {
                System.out.println("Dieser Wert wird geadded=" + temp);
                System.out.println(temp);
                codeListe.add(temp);
                sliceCode.add(temp);
                writeInstructions(s);
            } else {

                // do nothing and enjoy the day
            }
            count++;
        }
        if (function) {
            codeListe.add("}");
            codeListe.add(cat);

        }
        // doWriteFile(codeListe);
        return codeListe;
    }
    /**
     * Method for get all sourcenumbers from slice list.
     *
     * @param slice
     *            collection of all slice statements
     */
    public static ArrayList<String> getSlices(Collection<Statement> slice)
    throws IOException, ScriptException {

        ArrayList<String> codeListe = new ArrayList<String>();
        for (Statement s : slice) {

            String temp = getsourceNumber(s);

            if (codeListe.contains(temp) == false) {
                System.out.println(temp);
                codeListe.add(temp);

                writeInstructions(s);
            } else {

                // do nothing and enjoy the day
            }
        }

        // doWriteFile(codeListe);
        return codeListe;
    }

    /**
     * This Method executes a string with Rhino.
     *
     * TODO maybe change parameters , Scope??
     *
     * @param arg
     * @return evaluated String
     * @throws FileNotFoundException
     * @throws ScriptException
     */
    public static String evalState(String arg) throws FileNotFoundException,
        ScriptException {

        // ScriptEngine engine = new ScriptEngineManager()
        // .getEngineByName("rhino");
        ScriptEngine engine = new ScriptEngineManager()
        .getEngineByName("JavaScript");
        engine.eval(arg);

        // if (arg.contains("var")) {
        // System.out.println("declaration cut var!");
        // arg = arg.substring(3);
        // }

        // only needed for reading code from a file
        // String files =
        // "C:/Users/D058783/brotzeit/com.sap.research.wala.js.demo/example/tim/input.js";
        // FileReader reader = new FileReader(files);
        // System.out.println(engine.eval(reader));
        System.out.println(arg);
        System.out.println("Evaluat |" + arg + "|" + "Result is=+"
                           + engine.eval(arg));

        if (engine.eval(arg) == null) {
            return "Evaluation is Null see Stacktrace for further information or call 911";
        } else

            return engine.eval(arg).toString();

    }
    /**
     * Method for show RhinoVersion in the gui Rhino panel.
     *
     * @return version list of information
     */
    public static List<String> rhinoVersion() {
        List<String> version = new LinkedList<String>();
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            System.out.println("ScriptEngineFactory Info");
            version.add(factory.getEngineName());
            version.add(factory.getEngineVersion());
        }
        return version;
    }

    /**
     * This method is constructed to save statements from a slice in a .txt file
     * This .js can be used for e evaluation the whole .txt file.
     *
     * @param list
     *            a list from Statements
     */
    public static String doWriteFile(ArrayList<String> list) {
        String status = null;
        Writer fw = null;
        File file = new File(fileName + "_generated_slice.js");

        String datei = "slice.js";
        try {
            boolean isEmpty = true;
            fw = new FileWriter(datei);
            fw.write("		function apfelsaft(){ "
                     + System.getProperty("line.separator"));
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null) {
                    fw.write(list.get(i));
                    isEmpty = false;
                    fw.append(System.getProperty("line.separator"));

                }
            }
            if (isEmpty == false) {
                fw.write("		}" + System.getProperty("line.separator"));
                fw.write("		apfelsaft();"
                         + System.getProperty("line.separator"));
                // Runtime.getRuntime().exec(
                // "notepad.exe " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            status = " Cant create SliceFile";
            System.err.println("Fehler beim erstellen der datei");
        } finally {
            if (fw != null)
                try {

                    fw.close();
                    status = "slice.js" + " created";

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return status;
    }

    /**
     * Method for computing backward slice.
     *
     *
     * @param sdg
     * @param statement
     * @throws IllegalArgumentException
     * @throws CancelException
     * @throws IOException
     * @throws ScriptException
     *
     */
    public static Collection<Statement> doSlice(SDG sdg, Statement statement)
    throws IllegalArgumentException, CancelException, IOException,
        ScriptException {

        Collection<Statement> slice;
        slice = Slicer.computeBackwardSlice(sdg, statement);
        System.out.println("----------------------------------");
        System.out.println("Generate Slice from Statement:    "
                           + statement.toString());
        System.out.println("That slice size=" + slice.size());
        System.out.println("-------doSlice End---------------");

        // dumpSlice(slice);
        System.out.println(slice);
        return slice;
    }

    /**
     * Display different values from Nodes maybe interesting for further
     * Analyzing
     *
     * @param nodes
     *            A collection of nodes from a callgraph
     */
    public static ArrayList<String> printSSA(Collection<CGNode> nodes) {
        ArrayList<String> liste = new ArrayList<String>();
        for (CGNode cgNode : nodes) {
            // get intermediate representation
            IR ir = cgNode.getIR();

            liste.add("Instruction: " + (ir.getInstructions().toString()));
            System.out.println("Instruction: " + ir.getInstructions());
            liste.add("Methode: " + ir.getMethod());
            System.out.println("Methode: " + ir.getMethod());

            // System.out.println("Flow Graph of Node="+ir.getControlFlowGraph());
            SSACFG cfg = ir.getControlFlowGraph();

            // Iterate over the Basic Blocks of CFG
            Iterator<ISSABasicBlock> cfgIt = cfg.iterator();
            SSAInstruction ssaInstr = null;
            while (cfgIt.hasNext()) {
                ISSABasicBlock ssaBb = cfgIt.next();

                // Iterate over SSA Instructions for a Basic Block
                Iterator<SSAInstruction> ssaIt = ssaBb.iterator();

                while (ssaIt.hasNext()) {
                    ssaInstr = ssaIt.next();

                    printSSAInstruction(ssaInstr);
                    liste.add("  " + ssaInstr.toString() + " |Count Defs="
                              + ssaInstr.getNumberOfDefs() + " |count uses="
                              + ssaInstr.getNumberOfUses());
                    System.out.println("  " + ssaInstr.toString()
                                       + " |Count Defs=" + ssaInstr.getNumberOfDefs()
                                       + " |count uses=" + ssaInstr.getNumberOfUses());

                }
            }
        }
        return liste;
    }
    /**
     * Method for print SSAInstructions to Console
     *
     * @param ssaInstr
     */
    static void printSSAInstruction(SSAInstruction ssaInstr) {

        System.out
        .println("------------------------------------------------------------------------");
        System.out.println("### New Instruction. Type: " + ssaInstr.getClass()
                           + " ###");
        System.out.println(ssaInstr);

        if (ssaInstr instanceof com.ibm.wala.cast.ir.ssa.AstIsDefinedInstruction) {
            AstIsDefinedInstruction typedInstr = (AstIsDefinedInstruction) ssaInstr;
        } else if (ssaInstr instanceof com.ibm.wala.ssa.SSAUnaryOpInstruction) {
            SSAUnaryOpInstruction typedInstr = (SSAUnaryOpInstruction) ssaInstr;
            System.out.println("OpCode: " + typedInstr.getOpcode());
        } else if (ssaInstr instanceof com.ibm.wala.ssa.SSAConditionalBranchInstruction) {
            SSAConditionalBranchInstruction typedInstr = (SSAConditionalBranchInstruction) ssaInstr;
            System.out.println("Operator: " + typedInstr.getOperator());
            System.out.println("Type: " + typedInstr.getType());
        } else if (ssaInstr instanceof com.ibm.wala.ssa.SSAGotoInstruction) {
            SSAGotoInstruction typedInstr = (SSAGotoInstruction) ssaInstr;
        } else if (ssaInstr instanceof com.ibm.wala.cast.js.ssa.JavaScriptInvoke) {
            JavaScriptInvoke typedInstr = (JavaScriptInvoke) ssaInstr;
            System.out.println("Function: " + typedInstr.getFunction());
            System.out.println("Program Counter: "
                               + typedInstr.getProgramCounter());
            System.out.println("Return value:" + typedInstr.getReturnValue(0));
            System.out.println("Invocation Code: "
                               + typedInstr.getInvocationCode());
        } else if (ssaInstr instanceof com.ibm.wala.ssa.SSAReturnInstruction) {
            SSAReturnInstruction typedInstr = (SSAReturnInstruction) ssaInstr;
            System.out.println("Result: " + typedInstr.getResult());
        } else if (ssaInstr instanceof com.ibm.wala.ssa.SSAPhiInstruction) {
            SSAPhiInstruction typedInstr = (SSAPhiInstruction) ssaInstr;
        } else if (ssaInstr instanceof com.ibm.wala.cast.js.ssa.JavaScriptCheckReference) {
            JavaScriptCheckReference typedInstr = (JavaScriptCheckReference) ssaInstr;
            System.out.println("Def: " + typedInstr.getDef());
        } else if (ssaInstr instanceof com.ibm.wala.cast.ir.ssa.AstGlobalRead) {
            AstGlobalRead typedInstr = (AstGlobalRead) ssaInstr;
            System.out.println("Field: " + typedInstr.getDeclaredField());
            System.out.println("Global Name: " + typedInstr.getGlobalName());
            System.out.println("Ref: " + typedInstr.getRef());
            System.out.println("Declared field: "
                               + typedInstr.getDeclaredField());

            System.out.println("Declared field type: "
                               + typedInstr.getDeclaredFieldType());
        } else if (ssaInstr instanceof com.ibm.wala.cast.ir.ssa.AstGlobalWrite) {
            AstGlobalWrite typedInstr = (AstGlobalWrite) ssaInstr;
            System.out.println("Field: " + typedInstr.getDeclaredField());
            System.out.println("Global Name: " + typedInstr.getGlobalName());
            System.out.println("Ref: " + typedInstr.getRef());
            System.out.println("Declared field: "
                               + typedInstr.getDeclaredField());
            System.out.println("Declared field type: "
                               + typedInstr.getDeclaredFieldType());
        } else if (ssaInstr instanceof com.ibm.wala.cast.ir.ssa.AstLexicalRead) {
            AstLexicalRead typedInstr = (AstLexicalRead) ssaInstr;
            Access[] arr = typedInstr.getAccesses();
            for (int i = 0; i < arr.length; i++)
                System.out.println("Access[" + i + "]: "
                                   + typedInstr.getAccesses()[0]);
        } else if (ssaInstr instanceof com.ibm.wala.cast.js.ssa.PrototypeLookup) {
            PrototypeLookup typedInstr = (PrototypeLookup) ssaInstr;
        } else {
            if (!ssaInstr.getClass().toString().contains("JavaScriptLoader"))
                System.out
                .println("No special branch for this type of instruction");
        }

    }
    /**
     * Return all noded which are really needed
     *
     * @param cg
     * @param file
     * @return
     */
    static Collection<CGNode> getFunctionNode(CallGraph cg, String file) {
        Collection<CGNode> nodes = new LinkedList<CGNode>();

        for (CGNode node : cg) {
            if (node.getMethod().toString().contains(file))
                nodes.add(node);
        }

        assert nodes.size() != 0 : "Could not find  " + file;

        return nodes;
    }

    /**
     * Is calld from within the JavaScriptAnalysisEngine creation
     *
     * @param scope
     * @param cha
     * @return
     */
    protected static Iterable<Entrypoint> getEntrypoints(AnalysisScope scope,
            IClassHierarchy cha) {
        return new JavaScriptEntryPoints(cha,
                                         cha.getLoader(JavaScriptTypes.jsLoader));
    }

    /**
     * Methode to pretty print Wala's SSAInstruction's
     *
     * @param s
     */
    private static boolean writeInstructions(Statement s) {

        try {
            if (s.getNode().getIR().toString().contains("pro") == false) {
                SSAInstruction[] instructions = s.getNode().getIR()
                                                .getInstructions();
                System.out
                .println("-------Beginning PrintInstructions---------");
                for (int i = 0; i < instructions.length; i++) {
                    System.out.println("*** start: " + i);

                    // printing the instructions to the console
                    SymbolTable symbolTable = s.getNode().getIR()
                                              .getSymbolTable();
                    try {
                        String x = new String(i + "| Symbol= "
                                              + instructions[i].toString(symbolTable));
                        System.out.println(x);
                    } catch (Exception e) {
                        System.out.println("");
                    }

                    // printing the offset of an instruction to the console
                    try {
                        IMethod method = s.getNode().getIR().getMethod();

                        String x = (instructionPosition(i, method));
                        System.out.println(x);
                    } catch (Exception e) {
                        System.out.println("Fehler beim Finden von IMethod.");
                    }
                    System.out.println("***");
                    System.out.println("");
                }
            } else {
                System.out.println("");
            }

        } catch (Exception e) {
            System.out
            .println("Bitte �berpr�fen, welche Instructionnummer als erste in der main-Methode vergeben wurde.");
            System.out
            .println("(Standard: 2, muss bei der Erzeugung des NormalStatement mit angegeben werden)");
        }

        return true;
    }
    protected static String instructionPosition(int instructionIndex,
            IMethod method) {
        Position pos = ((AstMethod) method).getSourcePosition(instructionIndex);
        if (pos == null) {
            return "";
        } else {
            return pos.toString();
        }
    }

    @SuppressWarnings("unused")
    private static void printBlock(
        Iterator<BasicBlockInContext<IExplodedBasicBlock>> it) {

        while (it.hasNext()) {
            BasicBlockInContext<IExplodedBasicBlock> explBB = it.next();
            System.out.println(explBB);

            for (SSAInstruction ssa : explBB) {
                System.out.println(ssa);
            }
        }

    }

}

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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.Value;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public class SuperGraphUtil {

    public static void printSupergraphToDot(ICFGSupergraph sg) {

        try {
            // Create file
            FileWriter fstream = new FileWriter("results/SuperGraphNoPrologue.dot");
            BufferedWriter out = new BufferedWriter(fstream);

            Iterator<BasicBlockInContext<IExplodedBasicBlock>> completeIterator = sg.iterator();

            HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgWithoutPrologue = new HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>>();
            HashMap<BasicBlockInContext<IExplodedBasicBlock>, Integer> sgWithoutPrologueReversed = new HashMap<BasicBlockInContext<IExplodedBasicBlock>, Integer>();

            // Add all blocks into a Map with a unique identifier in both directions
            int i = 0;
            while (completeIterator.hasNext()) {
                BasicBlockInContext<IExplodedBasicBlock> current = completeIterator.next();
                if (!current.getMethod().toString().contains("Lprologue.js")) {

                    sgWithoutPrologue.put(i, current);
                    sgWithoutPrologueReversed.put(current, i);
                    i++;
                }
            }

            // Start the graph
            out.write("digraph SuperGraph {");
            out.newLine();
            out.write("node [shape=record];");
            out.newLine();

            // Print the labels for the nodes
            for (Integer key : sgWithoutPrologue.keySet()) {
                BasicBlockInContext<IExplodedBasicBlock> val = sgWithoutPrologue.get(key);
                Iterator<SSAInstruction> insIt = val.iterator();

                // print the beginning of the label
                out.write(key + " [");
                out.write("label = \"" + "<f0>" + sanitize(val.getMethod().getSignature()));

                SymbolTable symTab = val.getNode().getIR().getSymbolTable();

                // print the intruction field of the label
                int j = 1;
                while (insIt.hasNext()) {
                    SSAInstruction ins = insIt.next();
                    //out.write(" | <f" + j + "> " + sanitize(ins.toString()));
                    out.write(" | <f" + j + "> " + sanitize(ins.toString(symTab)));
                    j++;
                }
                out.write("\"];");
                out.newLine();
            }

            // Print the connection for all the nodes
            for (Integer key : sgWithoutPrologue.keySet()) {
                BasicBlockInContext<IExplodedBasicBlock> val = sgWithoutPrologue.get(key);
                Iterator<BasicBlockInContext<IExplodedBasicBlock>> sucIt = sg.getSuccNodes(val);
                while (sucIt.hasNext()) {
                    BasicBlockInContext<IExplodedBasicBlock> suc = sucIt.next();
                    out.write(key + "->" + sgWithoutPrologueReversed.get(suc) + ";");
                    out.newLine();
                }
            }

            // finish the graph
            out.write("}");

            out.close();
        }
        catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

    /**
    	 * This function will remove all chars from the string that confuse dotty
    	 * Currently '<' and '>'
    	 * Expand this if you run into trouble with other chars
    	 * @param input
    	 * @return
    	 */
    private static String sanitize(String input) {
        String output = "";

        for (char c : input.toCharArray()) {
            if (!(c == '<' || c == '>')) {
                output += c;
            }
            else {
                output += ' ';
            }
        }
        return output;
    }

}

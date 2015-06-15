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
import java.util.List;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;

public class CFGAnalyzer {

    /**
     * This will search for a SSAConditionalBranchInstruction in the node and
     * print information related to this conditional to the console
     *
     * @param node
     */
    public static void analyzeConditional(CGNode node) {

        // The symbol table maps a variable number in the ssa to the actual name and value

        SymbolTable symtab = node.getIR().getSymbolTable();
        SSACFG cfg = node.getIR().getControlFlowGraph();

        // Iterate over all basic blocks

        for (int i = 0; i < cfg.getNumberOfNodes(); i++) {
            BasicBlock bb = cfg.getBasicBlock(i);

            // Iterate over all instructions in the basic block
            List<SSAInstruction> instructions = bb.getAllInstructions();
            if (containsConditional(instructions)) {
                for (SSAInstruction ins : instructions) {
                    if (ins.toString().contains("binaryop")) {
                        System.out.print(ins);
                        for (int u = 0; u < ins.getNumberOfUses(); u++) {
                            System.out.print(" && ");

                            // In case of null we have a phi instruction. JavaScript apparently does not handle this very well.
                            if (symtab.getValue(ins.getUse(u)) == null)
                                System.out.print(ins.getUse(u) + " depends on previous if ");
                            else
                                System.out.print(ins.getUse(u) + " = " + symtab.getValue(ins.getUse(u)));
                        }
                        System.out.println("");
                    }
                    if (ins instanceof SSAConditionalBranchInstruction) {
                        System.out.println(ins.toString(symtab));
                        System.out.println("----------");
                    }
                }
            }

        }
    }

    private static boolean containsConditional(List<SSAInstruction> list) {

        for (SSAInstruction ins : list) {
            if (ins instanceof SSAConditionalBranchInstruction)
                return true;
        }
        return false;

    }

}

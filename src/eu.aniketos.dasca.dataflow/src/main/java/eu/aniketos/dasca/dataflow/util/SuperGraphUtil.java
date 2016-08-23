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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.MethodReference;

import cvc3.Expr;
import cvc3.SatResult;
import cvc3.ValidityChecker;


/**
 * static class for SG helper methods
 *
 */
public class SuperGraphUtil {

    private static final Logger log = AnalysisUtil.getLogger(SuperGraphUtil.class);

    /**
     * Adds every nodes ID to iDs, iff it is on a path between the entry and exit point. Uses recursive DFS
     * @param iDs
     * @param sg
     * @param sgNodes
     * @param sgNodesReverse
     * @param currentId
     * @param endId
     */
    private static void relevantPathSearch(
        HashSet<Integer> iDs,
        ICFGSupergraph sg,
        HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes,
        HashMap<BasicBlockInContext<IExplodedBasicBlock>, Integer> sgNodesReverse,
        ArrayList<MethodReference> acceptedMethods,
        int currentId,
        int endId) {
        if(iDs.contains(currentId)) {
            return;
        }
        iDs.add(currentId);
        if(currentId == endId) {
            return;
        }
        BasicBlockInContext<IExplodedBasicBlock> bbic = sgNodes.get(currentId);
        boolean isInvoke = bbic.getLastInstruction() instanceof SSAInvokeInstruction;
        boolean isStringConcat = isInvoke && bbic.getLastInstruction().toString().contains("StringBuilder") && (bbic.getLastInstruction().toString().contains("append") | bbic.getLastInstruction().toString().contains("toString"));

        Iterator<BasicBlockInContext<IExplodedBasicBlock>> sucIt = sg.getSuccNodes(bbic);
        while(sucIt.hasNext()) {
            BasicBlockInContext<IExplodedBasicBlock> nextChild = sucIt.next();

            if(isStringConcat) {
                if(!sucIt.hasNext()) { // last association of a String concatenation is java intern
                    continue;
                }
            }

            MethodReference method = nextChild.getMethod().getReference();
            if(isInvoke) {
                if(!acceptedMethods.contains(method)) {
                    acceptedMethods.add(method);
                }
            } else {
                if(!acceptedMethods.contains(method)) {
                    log.debug("supergraph cut at '" + bbic.getNumber() + " -> " + nextChild.getNumber() + " (" + nextChild.toString() + ")'");
                    continue;
                }
            }

            IExplodedBasicBlock del = nextChild.getDelegate();
            if(del.isEntryBlock() && del.toString().contains("init")) {
                log.debug("supergraph cut at '" + bbic.getNumber() + " -> " + nextChild.getNumber() + " (" + nextChild.toString() + ")'");
                continue;
            }
            relevantPathSearch(iDs, sg, sgNodes, sgNodesReverse, acceptedMethods, sgNodesReverse.get(nextChild), endId);
        }
    }

    /**
     * Analyzes the supergraph and saves the relevant part as a dot file to the location &lt;graphs folder&gt;/<entryClass/
     * @return
     */
    public static int analyzeAndSaveSuperGraph(ICFGSupergraph sg, String entryClass, String entryMethod) {
        int weaknessCount = 0;

        if (sg == null) {
            throw new IllegalArgumentException("sg was null for entry Class \""+entryClass+"\" and entry method \""+entryMethod+"\"");
        }

        log.info("--- start analyzing " + entryClass + "." + entryMethod + " ---");
        ValidityChecker vc = ValidityChecker.create();
        vc.push();

        Iterator<BasicBlockInContext<IExplodedBasicBlock>> completeIterator = sg.iterator();
        int analysisLevel = AnalysisUtil.getPropertyInteger(AnalysisUtil.CONFIG_ANALYSIS_DEPTH);

        HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes = new HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>>();
        HashMap<BasicBlockInContext<IExplodedBasicBlock>, Integer> sgNodesReversed = new HashMap<BasicBlockInContext<IExplodedBasicBlock>, Integer>();
        HashMap<SSAInstruction, Integer> sgNodesInstId = new HashMap<SSAInstruction, Integer>();
        ArrayList<MethodReference> acceptedMethods = new ArrayList<MethodReference>();

        // Add all blocks into a Map with a unique identifier in both directions and find entry/exit node
        int i = 0;
        int mainEntryId = 0;
        int mainExitId = 0;
        while (completeIterator.hasNext()) {
            BasicBlockInContext<IExplodedBasicBlock> current = completeIterator.next();
            sgNodes.put(i, current);
            sgNodesReversed.put(current, i);
            Iterator<SSAInstruction> instIt = current.iterator();
            while (instIt.hasNext()) {
                SSAInstruction inst = instIt.next();
                sgNodesInstId.put(inst, i);
                /* add to include other required methods into CFG
                				if(inst instanceof AstJavaInvokeInstruction){
                					AstJavaInvokeInstruction in = (AstJavaInvokeInstruction) inst;
                					MethodReference meth = in.getDeclaredTarget();
                					if(meth.toString().contains("calledMethod")){ // check for required method
                						acceptedMethods.add(meth);
                					}
                				}
                //*/

            }
            String signature = current.getMethod().getSignature();
            // find entry and exit nodes
            if(signature.contains(entryClass) && signature.contains(entryMethod) && current.isEntryBlock()) { // FIXME: entry/exit nodes definition via name is too weak
                mainEntryId = i;
            } else if(signature.contains(entryClass) && signature.contains(entryMethod) && current.isExitBlock()) {
                mainExitId = i;
            }
            i++;
        }
        if(mainEntryId == 0 && mainExitId == 0) {
            log.error("   "+entryClass + "." + entryMethod + 
            		  ": empty entry method, ensure invocation in main method");
            return 0;
        }
        HashSet<Integer> relevantIDs = new HashSet<Integer>();
        BasicBlockInContext<IExplodedBasicBlock> bbic = sgNodes.get(mainEntryId);
        acceptedMethods.add(bbic.getMethod().getReference());
        log.error("   "+entryClass + "." + entryMethod + 
                  "start recursive graph building");
        relevantPathSearch(relevantIDs, sg, sgNodes, sgNodesReversed, acceptedMethods, mainEntryId, mainExitId);

        // remove irrelevant nodes (not on at least one path between entry and exit node)
        log.debug("remove irrelevant nodes");
        for (int j = 0; j < i; j++) {
            BasicBlockInContext<IExplodedBasicBlock> tmp = sgNodes.get(j);
            if(!relevantIDs.contains(j)) {
                sgNodesReversed.remove(tmp);
                sgNodes.remove(j);
            }
        }

        // build separate adjacency list
        log.debug("   "+entryClass + "." + entryMethod + 
                  "build seperate adjacency list");
        HashMap<Integer, ArrayList<Integer>> adjList = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, ArrayList<Integer>> adjListReverse = new HashMap<Integer, ArrayList<Integer>>();

        boolean removeEmptyNodes = AnalysisUtil.getPropertyBoolean(AnalysisUtil.CONFIG_DOT_REMOVE_EMPTY_NODES);
        ArrayList<Integer> emptyNodes = new ArrayList<Integer>();

        buildAdjacencyLists(sg, sgNodes, sgNodesReversed, adjList, adjListReverse, emptyNodes, relevantIDs);

        // <<< print original adjacency list to log
        		log.debug("adjacency list before removing empty nodes:");
        		AnalysisUtil.printAdjList(adjList, log);
        //
        if(removeEmptyNodes) {
            removeEmptyNodes(emptyNodes, adjList, adjListReverse, sgNodes, sgNodesReversed);
        }
        
        // <<< print adjacency list to log
		log.debug("adjacency after before removing empty nodes:");
		AnalysisUtil.printAdjList(adjList, log);
        //
        
        log.debug("   "+entryClass + "." + entryMethod + 
                  "add conditions to graph nodes");
        ArrayList<Integer> visited = new ArrayList<Integer>();
        ArrayList<Integer> currentConditions = new ArrayList<Integer>();
        HashMap<Integer, Integer> currentConditionsEndId = new HashMap<Integer, Integer>();
        HashMap<Integer, ArrayList<Integer>> finalConditions = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, Integer> loops = new HashMap<Integer, Integer>();
        addConditionsToGraph(sgNodes, adjList, mainEntryId, visited, mainExitId, currentConditions, currentConditionsEndId, finalConditions, loops);

        // remove goto statements for later analysis steps
        for (int loopId : loops.keySet()) {
            int gotoId = loops.get(loopId);
            int gotoTargetId = adjList.get(gotoId).get(0); // goto has exact one child
            int afterLoopId = 0;
            for(int id : adjList.get(loopId)) {
                afterLoopId = Math.max(afterLoopId, id);
            }
            ArrayList<Integer> newList = new ArrayList<Integer>();
            newList.add(afterLoopId);
            adjList.put(gotoId, newList);
            adjListReverse.get(afterLoopId).add(gotoId);
            ArrayList<Integer> gotoTargetReverseList = adjListReverse.get(gotoTargetId);
            ArrayList<Integer> newGotoTargetReverseList = new ArrayList<Integer>();
            for (int child : gotoTargetReverseList) {
                if(child != gotoId) {
                    newGotoTargetReverseList.add(child);
                }
            }
            adjListReverse.put(gotoTargetId, newGotoTargetReverseList);
        }

        /* <<< print required conditions for each node
        		for (Integer key : finalConditions.keySet()) {
        			StringBuffer sb = new StringBuffer();
        			for(int condition : finalConditions.get(key)){
        				sb.append(", " + condition);
        			}sb.append("  ");
        			log.debug(key + ": " + sb.substring(2));
        		}
        //*/

        /* <<< print changed adjacency list to log
        		log.debug("adjacency list after removing empty nodes:");
        		AnalysisUtil.printAdjList(adjList, log);
        //*/

//*  <<< get definition instruction for each SSA value
        HashMap<Integer, SSAInstruction> definitions = AnalysisUtil.getDefs(sgNodes);
//*/

//*  <<< get source code line number for each instruction
        HashMap<SSAInstruction, Integer> lineNumbers = AnalysisUtil.getLineNumbers(sgNodes);
//*/

//*  <<< get the corresponding instruction for each condition inside the callgraph
        ArrayList<SSAInstruction> conditionsList = AnalysisUtil.getConditions(sgNodes);
        ArrayList<Integer> conditionsIdList = new ArrayList<Integer>();
        for (SSAInstruction cond : conditionsList) {
            conditionsIdList.add(sgNodesInstId.get(cond));
        }
//*/
//*  <<< get the corresponding source code line number for each condition (node id) inside the callgraph
        HashMap<Integer, Integer> conditionLineNumber = new HashMap<Integer, Integer>();
        for (SSAInstruction instCondition : conditionsList) {
            int nodeId = sgNodesInstId.get(instCondition);
            int lineNumber = lineNumbers.get(instCondition);
            conditionLineNumber.put(nodeId, lineNumber);
        }

//*/

//*  <<< build CVC3 expressions from conditional instruction

        log.debug("** get CVC3 expressions for each conditional branch instruction");
        HashMap<Integer, Expr> expressions = new HashMap<Integer, Expr>();
        IR entryIR = sgNodes.values().iterator().next().getNode().getIR();
        vc.pop();
        vc.push();
        for (SSAInstruction instCondition : conditionsList) {
            int condId = sgNodesInstId.get(instCondition);
            if(loops.keySet().contains(condId)) {
                Expr expr = SMTChecker.getExprForLoop(vc , instCondition, entryIR);
                expressions.put(sgNodesInstId.get(instCondition), expr);
            } else {
                Expr expr = SMTChecker.getExprForConditionalBranchInstruction(vc , instCondition, entryIR);
                expressions.put(sgNodesInstId.get(instCondition), expr);
            }
            vc.pop();
            vc.push();
        }
        vc.pop();
        vc.push();
//*/

        ArrayList<SSAInstruction> sqlExecutes = AnalysisUtil.getSQLExecutes(sgNodes);

        log.debug("** get source/sink pairs for each SQL instruction");

        String sanitizerMethods = AnalysisUtil.getPropertyString(AnalysisUtil.CONFIG_SANITIZER); // good sources are specified as sanitizer

        String[] methods = sanitizerMethods.split(",");
        HashSet<String> sanitizer = new HashSet<String>();
        for (int k = 0; k < methods.length; k++) {
            String sanitizerMethod = methods[k].trim();
            sanitizer.add(sanitizerMethod);
        }

        String badSourceMethods = AnalysisUtil.getPropertyString(AnalysisUtil.CONFIG_BAD_SRC);

        methods = badSourceMethods.split(",");
        HashSet<String> badMethods = new HashSet<String>();
        for (int k = 0; k < methods.length; k++) {
            String badMethod = methods[k].trim() + "()";
            badMethods.add(badMethod);
        }

        //*  <<< get possible vulnerabilities for each SQL execute

        HashMap<SSAInstruction, ArrayList<SSAInstruction>> sinkSources = new HashMap<SSAInstruction, ArrayList<SSAInstruction>>();
        for (SSAInstruction ssaInstruction : sqlExecutes) {
            boolean isPreparedStmt = ssaInstruction.toString().contains("Prepared");
            sinkSources.put(ssaInstruction, AnalysisUtil.analyzeStatementExecute(ssaInstruction, definitions, isPreparedStmt, badMethods));
        }
        boolean containsVulnerability = false;
        for (SSAInstruction sink : sqlExecutes) {
            if(!sinkSources.containsKey(sink)) { // no vulnerability possible
                continue;
            }
            ArrayList<SSAInstruction> badSources = sinkSources.get(sink);
            if(badSources != null) {
                for (SSAInstruction source : badSources) {
                    boolean isNotMutuallyExclusive = true;
                    if(analysisLevel >= AnalysisUtil.ANALYSIS_DEPTH_EXCLUSIVE) {
                        isNotMutuallyExclusive = isNotMutuallyExclusive(sink, source, sgNodesInstId, finalConditions, expressions, vc);
                    }
                    if(isNotMutuallyExclusive) {
                        boolean isNotSanitized = true;
                        if(analysisLevel >= AnalysisUtil.ANALYSIS_DEPTH_SANITIZING) {
                            isNotSanitized = isNotSanitized(sgNodesInstId.get(source), sgNodesInstId.get(sink), adjList, finalConditions, expressions, vc, conditionsIdList, sanitizer, sgNodes);
                        }
                        if(isNotSanitized) {
                            weaknessCount++;
                            containsVulnerability = true;
                            log.warn("SQL execute [" + lineNumbers.get(sink) + "] with bad source readLine [" + lineNumbers.get(source) + "] (" + entryClass + "." + entryMethod + ")");
                        }
                    }
                }
            }
        }
        if(!containsVulnerability) {
            log.info("(" + entryClass + "." + entryMethod + ") is save");
        }
//*/


        String filePath = String.format("%s_SG.dot", entryMethod);
        generateDotFile(sgNodes, adjList, entryClass, filePath, finalConditions);
        return weaknessCount;
    }

    /**
     * Computes all possible control flows between source and sink and checks, if every single path is sanitized.
     * @return true, iff there exists at least one direct unsanitized path between source and sink
     */
    private static boolean isNotSanitized(int sourceId,
                                          int sinkId, HashMap<Integer, ArrayList<Integer>> adjList,
                                          HashMap<Integer, ArrayList<Integer>> finalConditions,
                                          HashMap<Integer, Expr> expressions,
                                          ValidityChecker vc,
                                          ArrayList<Integer> conditionsIdList,
                                          HashSet<String> sanitizer,
                                          HashMap<Integer,BasicBlockInContext<IExplodedBasicBlock>> sgNodes) {

        log.debug("** get possible control flows from source to sink");
        HashMap<Integer, HashSet<ArrayList<Integer>>> paths = new HashMap<Integer, HashSet<ArrayList<Integer>>>();

        calculatePaths(sourceId, sinkId, adjList, paths);
        HashSet<ArrayList<Integer>> pathList = paths.get(sourceId);
        HashSet<ArrayList<Integer>> possibleFlowPaths = new HashSet<ArrayList<Integer>>();

        for (ArrayList<Integer> path : pathList) {
            boolean isPossibleFlow = isPossibleFlow(path, finalConditions, expressions, vc, conditionsIdList);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < path.size(); i++) {
                sb.append(" -> " + path.get(i));
            }
            sb.append("    ");
            if(isPossibleFlow) {
                log.debug("FLOW [possible]: " + sb.substring(3)); // cut off the last arrow
                possibleFlowPaths.add(path);
            } else {
                log.debug("FLOW [impossible]: " + sb.substring(3));
            }
        }
        loopPossiblePaths:
        for (ArrayList<Integer> possibleFlowPath : possibleFlowPaths) {
            boolean containsSanitizer = false;
            for (String sanitizerMethod : sanitizer) {
                for(int nodeId : possibleFlowPath) {
                    BasicBlockInContext<IExplodedBasicBlock> node = sgNodes.get(nodeId);
                    SSAInstruction inst = node.getLastInstruction();
                    if(inst != null) {
                        if(inst.toString().contains(sanitizerMethod)) {
                            containsSanitizer = true;
                            continue loopPossiblePaths;
                        }
                    }
                }
            }
            if(!containsSanitizer) {
                return true; // path contains no sanitizer => is direct path between bad source and bad sink
            }
        }
        return false; // every possible path contained at least one sanitizer
    }

    private static boolean isPossibleFlow(ArrayList<Integer> path,
                                          HashMap<Integer, ArrayList<Integer>> finalConditions,
                                          HashMap<Integer, Expr> expressions,
                                          ValidityChecker vc,
                                          ArrayList<Integer> conditionsIdList) {
        vc.pop();
        vc.push();
        List<Expr> allExpr = new ArrayList<Expr>();
        ArrayList<Integer> allConditions = new ArrayList<Integer>();
        for (int i=0; i<path.size(); i++) {
            int node = path.get(i);
            ArrayList<Integer> conditions = finalConditions.get(node);
            if(conditionsIdList.contains(node)) {
                ArrayList<Integer> childConditions = finalConditions.get(path.get(i+1).intValue());
                if(childConditions == null || childConditions.isEmpty() || !(childConditions.contains(node) | childConditions.contains(-node))) { // jump over if-block without else part => else condition needs to be set
                    allExpr.add(vc.notExpr(expressions.get(node)));
                }
            }
            if(conditions != null) {
                for (int condId : conditions) {
                    if(!allConditions.contains(condId)) {
                        allConditions.add(condId);
                        if(condId < 0) {
                            allExpr.add(vc.notExpr(expressions.get(Math.abs(condId))));
                        } else {
                            allExpr.add(expressions.get(condId));
                        }
                    }
                }
            }
        }
        if(allExpr.isEmpty()) {
            return true;
        }
        Expr completeExpr = vc.andExpr(allExpr);
        SatResult satResult = vc.checkUnsat(completeExpr);
        boolean satisfiable = satResult.equals(SatResult.SATISFIABLE);
        log.info("     checking expression [" + (satisfiable?"SAT":"UNSAT") + "]: " + completeExpr);
        return satisfiable;
    }

    private static void calculatePaths(int currentId, int target,
                                       HashMap<Integer, ArrayList<Integer>> adjList,
                                       HashMap<Integer, HashSet<ArrayList<Integer>>> paths) {

    	log.debug("     searching for "+currentId+" in adjList.");
        ArrayList<Integer> children = adjList.get(currentId);
        assert children != null : "No entry found in adjList for "+currentId;
        HashSet<ArrayList<Integer>> currentPaths = new HashSet<ArrayList<Integer>>();

        for(int child : children) {
            if(child == target) {
                ArrayList<Integer> last = new ArrayList<Integer>();
                last.add(child);
                currentPaths.add(last);
                continue;
            }
            calculatePaths(child, target, adjList, paths);
            HashSet<ArrayList<Integer>> childPaths = paths.get(child);
            if(childPaths!= null) {
                for (ArrayList<Integer> childPath : childPaths) {
                    ArrayList<Integer> newPath = new ArrayList<Integer>();
                    newPath.add(child);
                    newPath.addAll(childPath);
                    currentPaths.add(newPath);
                }
            }
        }
        paths.put(currentId, currentPaths);
    }

    /**
     * Checks, if sink and source node are mutually excluded because of the respective conditions
     * @param sink
     * @param source
     * @param sgNodesInstId
     * @param finalConditions
     * @param expressions
     * @param vc
     * @return
     */
    private static boolean isNotMutuallyExclusive(SSAInstruction sink,
            SSAInstruction source,
            HashMap<SSAInstruction, Integer> sgNodesInstId,
            HashMap<Integer,ArrayList<Integer>> finalConditions,
            HashMap<Integer, Expr> expressions, ValidityChecker vc) {
        vc.pop();
        vc.push();
        int sourceId = sgNodesInstId.get(source);
        int sinkId = sgNodesInstId.get(sink);
        ArrayList<Integer> combinedConditions = new ArrayList<Integer>();
        if(finalConditions.containsKey(sourceId)) {
            combinedConditions.addAll(finalConditions.get(sourceId));
        }
        if(finalConditions.containsKey(sinkId)) {
            combinedConditions.addAll(finalConditions.get(sinkId));
        }
        Expr expr = getConditionExpression(combinedConditions, expressions, vc);
        vc.pop();
        vc.push();
        if(expr.toString().equalsIgnoreCase("null")) {
            return true;
        }
        SatResult satResult = vc.checkUnsat(expr);
        boolean satisfiable = satResult.equals(SatResult.SATISFIABLE);
        return satisfiable;
    }

    private static Expr getConditionExpression(ArrayList<Integer> nodeIds, HashMap<Integer, Expr> expressions, ValidityChecker vc) {
        if(nodeIds.isEmpty()) {
            return vc.nullExpr();
        }
        List<Expr> exprList = new ArrayList<Expr>();
        for (int i=0; i<nodeIds.size(); i++) {
            Integer conditionId = nodeIds.get(i);
            Expr tmpExpr = expressions.get(Math.abs(conditionId));
            if(conditionId<0) {
                tmpExpr = vc.notExpr(tmpExpr);
            }
            exprList.add(tmpExpr);
        }
        return vc.andExpr(exprList);
    }

    private static void removeEmptyNodes(ArrayList<Integer> emptyNodes,
                                         HashMap<Integer, ArrayList<Integer>> adjList,
                                         HashMap<Integer, ArrayList<Integer>> adjListReverse, HashMap<Integer,BasicBlockInContext<IExplodedBasicBlock>> sgNodes, HashMap<BasicBlockInContext<IExplodedBasicBlock>,Integer> sgNodesReversed) {

        Collections.sort(emptyNodes);
        StringBuffer sb = new StringBuffer();
        for (int integer : emptyNodes) {
            sb.append(", " + integer);
        }
        sb.append("  ");
        log.debug("empty nodes in supergraph:" + sb.substring(1));

        // remove empty nodes from supergraph
        log.debug("remove empty nodes from supergraph");
        for (Integer kill: emptyNodes) {
            ArrayList<Integer> children = adjList.get(kill);
            if(children == null) {
                children = new ArrayList<Integer>();
            }
            // insert new edges around removed nodes
            for(Integer father : adjListReverse.get(kill)) {
                ArrayList<Integer> tmpList = adjList.get(father);
                if(tmpList==null) {
                    tmpList = new ArrayList<Integer>();
                }
                tmpList.remove(kill);
                tmpList.addAll(children);
                for (Integer childId : children) {
                    ArrayList<Integer> childFathers = adjListReverse.get(childId);
                    if(childFathers==null) {
                        childFathers = new ArrayList<Integer>();
                    }
                    childFathers.remove(kill);
                    childFathers.add(father);
                    adjListReverse.put(childId, childFathers);
                }
                adjList.put(father, tmpList);
            }
        }
        for (Integer kill: emptyNodes) { // requires two loops to build the new edges correct
            adjList.remove(kill);
            sgNodesReversed.remove(sgNodes.get(kill));
            sgNodes.remove(kill);
        }

    }

    private static void buildAdjacencyLists(
        ICFGSupergraph sg, HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes,
        HashMap<BasicBlockInContext<IExplodedBasicBlock>, Integer> sgNodesReversed,
        HashMap<Integer, ArrayList<Integer>> adjList,
        HashMap<Integer, ArrayList<Integer>> adjListReverse,
        ArrayList<Integer> emptyNodes, HashSet<Integer> relevantIDs) {

        for (int key1 : sgNodes.keySet()) {
            BasicBlockInContext<IExplodedBasicBlock> val1 = sgNodes.get(key1);
            Iterator<BasicBlockInContext<IExplodedBasicBlock>> sucIt = sg.getSuccNodes(val1);
            // add 'key1->sucId' to adjList
            ArrayList<Integer> list = adjList.get(key1);
            if(list == null) {
                list = new ArrayList<Integer>();
            }
            while (sucIt.hasNext()) {
                BasicBlockInContext<IExplodedBasicBlock> suc = sucIt.next();
                if(!sgNodesReversed.containsKey(suc)) {
                    continue;
                }
                int sucId = sgNodesReversed.get(suc);

                if(!list.contains(sucId)) {
                    list.add(sucId);
                }

                // add 'sucId->key1' to adjListReverse
                ArrayList<Integer> listReverse = adjListReverse.get(sucId);
                if(listReverse == null) {
                    listReverse = new ArrayList<Integer>();
                }
                if(!listReverse.contains(key1)) {
                    listReverse.add(key1);
                }
                adjListReverse.put(sucId, listReverse);

                // add empty nodes to list
                Iterator<SSAInstruction> it = suc.iterator();
                if(relevantIDs.contains(sucId)) {
                    if(!emptyNodes.contains(sucId)) {
                        if(!it.hasNext() && !suc.isEntryBlock() && !suc.isExitBlock() && !suc.isCatchBlock()) {
                            emptyNodes.add(sucId);
                        }
                    }
                }
            }
            adjList.put(key1, list);
        }
    }

    /**
     * Analyzes the given nodes of the supergraph  and fills the list finalConditions
     * @param sgNodes
     * @param adjList
     * @param currentId
     * @param visited
     * @param mainEndId
     * @param currentConditions
     * @param currentConditionsEndId
     * @param finalConditions
     * @param loops
     * @return
     */
    private static boolean addConditionsToGraph( // boolean return is used in recursion to signalize the existence of an else block
        HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes,
        HashMap<Integer, ArrayList<Integer>> adjList,
        Integer currentId,
        ArrayList<Integer> visited,
        Integer mainEndId,
        ArrayList<Integer> currentConditions,
        HashMap<Integer, Integer> currentConditionsEndId,
        HashMap<Integer, ArrayList<Integer>> finalConditions,
        HashMap<Integer, Integer> loops) {

        if(!visited.contains(currentId)) {
            visited.add(currentId);
        }

        if(currentId == mainEndId) {
            finalConditions.put(mainEndId, new ArrayList<Integer>());
            return false;
        }
        SSAInstruction currentInstruction = sgNodes.get(currentId).getLastInstruction();
        ArrayList<Integer> children = adjList.get(currentId);

        Integer ifStart = 0;
        Integer ifEnd = 0;

        if(!currentConditions.isEmpty()) { // inside an if-block

            ifStart = currentConditions.get(currentConditions.size()-1); // last condition is the most inner condition
            ifEnd = currentConditionsEndId.get(ifStart);

            if(currentId > Math.abs(ifEnd) && sgNodes.get(currentId).isCatchBlock()) { // most inner if block has else AND current node is join
                return false;
            }

            if(currentId > Math.abs(ifEnd) && Math.abs(ifEnd) != mainEndId) { // most inner if block has else AND current node is join
                currentConditionsEndId.put(ifStart,currentId);
                return true;
            }

            if(currentId.intValue() == ifEnd.intValue()) { // end of single if block
                return false;
            }

            if(currentId == (-ifEnd)) { // end of else block
                ArrayList<Integer> tmpCond = new ArrayList<Integer>(currentConditions);
                HashMap<Integer, Integer> tmpCondEnd = new HashMap<Integer, Integer>(currentConditionsEndId);
                int lastId = currentConditions.size();
                while(lastId > 0 && currentId == -tmpCondEnd.get(tmpCond.get(--lastId))) { // delete all conditions, which end at this node
                    Integer del = currentConditions.get(lastId);
                    currentConditions.remove(del);
                    currentConditionsEndId.remove(del);
                }
            }
        } // END IF inside an if-block
        if(currentInstruction instanceof SSAConditionalBranchInstruction) {

            Integer childTrue = Math.min(children.get(0), children.get(1)); // WALA always takes the true branch first
            Integer childFalse = Math.max(children.get(0), children.get(1));

            ArrayList<Integer> conditions = new ArrayList<Integer>();
            for(int i =0; i<currentConditions.size(); i++) {
                conditions.add(currentConditions.get(i));
            }
            finalConditions.put(currentId, conditions);

            currentConditions.add(currentId);
            boolean isLastIfBlock = childTrue.intValue() == mainEndId.intValue(); // => its an if block without else and without any instructions after it
            if(isLastIfBlock) {
                childTrue = Math.max(children.get(0), children.get(1));
                currentConditionsEndId.put(currentId, mainEndId);
            } else {
                currentConditionsEndId.put(currentId, childFalse);
            }

            boolean isRealIfElse = addConditionsToGraph(sgNodes, adjList, childTrue, visited, mainEndId, currentConditions, currentConditionsEndId, finalConditions, loops);

            currentConditions.remove(currentId);
            Integer newEnd = currentConditionsEndId.remove(currentId);

            if(isRealIfElse) {
                currentConditions.add(-currentId);
                currentConditionsEndId.put(-currentId, -newEnd); // last visited is the reached join from if-block
            }
            if(!isLastIfBlock) {
                return addConditionsToGraph(sgNodes, adjList, childFalse, visited, mainEndId, currentConditions, currentConditionsEndId, finalConditions, loops);
            }
            return isRealIfElse;
        } // END IF conditional branch instruction
        else if(currentInstruction instanceof SSAGotoInstruction) {
            int child = children.get(0); // goto always points to exactly one child node

            if(currentId == Math.abs(ifEnd)-1) { // last node of if block
                ArrayList<Integer> conditions = new ArrayList<Integer>();
                for(int i =0; i<currentConditions.size(); i++) {
                    conditions.add(currentConditions.get(i));
                }
                finalConditions.put(currentId, conditions);
                if(!(Math.abs(ifEnd) == child)) {
                    if(child <= Math.abs(ifStart)) { // is loop
                        loops.put(ifStart, currentId.intValue());
                        return false;
                    }
                    currentConditionsEndId.put(ifStart, child);
                    return true;
                } else {
                    return false;
                }
            }

        } // END IF goto instruction
        else {

            ArrayList<Integer> conditions = new ArrayList<Integer>();
            for(int i =0; i<currentConditions.size(); i++) {
                conditions.add(currentConditions.get(i));
            }
            finalConditions.put(currentId, conditions);
        }
        if (null == currentInstruction) return true;
        System.out.println(""+currentInstruction);
        boolean isRealIfElse = false;
        for(int i=0; i<children.size(); i++) {
            int child = children.get(i);
            isRealIfElse = isRealIfElse | addConditionsToGraph(sgNodes, adjList, child, visited, mainEndId, currentConditions, currentConditionsEndId, finalConditions, loops);
        }
        return isRealIfElse;
    }

    private static void generateDotFile(
        HashMap<Integer, BasicBlockInContext<IExplodedBasicBlock>> sgNodes,
        HashMap<Integer, ArrayList<Integer>> adjList, String entryClass,
        String filePath, HashMap<Integer,ArrayList<Integer>> finalConditions) {
        FileWriter fstream;
        try {
            log.debug("start generating dot file");
            String path = AnalysisUtil.getPropertyString(AnalysisUtil.CONFIG_DOT_PATH) + File.separator;
            path += "java" + File.separator;

            File sgDir = new File(path + entryClass);
            sgDir.mkdirs();
            File sgFile = new File(path + entryClass + File.separator + filePath);
            fstream = new FileWriter(sgFile);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("digraph SuperGraph {");
            out.newLine();
            out.write("node [shape=record];");
            out.newLine();

            // add relevant nodes to dot
            log.debug("add nodes to dot file");
            for (Integer key : sgNodes.keySet()) {
                BasicBlockInContext<IExplodedBasicBlock> val = sgNodes.get(key);
                Iterator<SSAInstruction> insIt = val.iterator();

                IExplodedBasicBlock del = val.getDelegate();


                int key2 = del.getGraphNodeId(); //cfg.getNumber(val);

                // print the labels and define the id (key)
                out.write(key + " [");
                out.write("label = \" <f0>" + key + "-" + key2 + "| <f1>" + AnalysisUtil.sanitize(val.getMethod().getSignature()));

                IR ir = val.getNode().getIR();
                SymbolTable symTab = ir.getSymbolTable();

                int j = 2;

                // print the instruction field of the label
                while (insIt.hasNext()) {
//					insCount++;
                    SSAInstruction ins = insIt.next();
                    if(ins instanceof SSAConditionalBranchInstruction) {
                        ins = (SSAConditionalBranchInstruction) ins;
                        SSAInstruction cmp = val.getNode().getDU().getDef(ins.getUse(0));
                        if(cmp instanceof SSABinaryOpInstruction) {
                            SSABinaryOpInstruction bCmp = (SSABinaryOpInstruction) cmp;
                            out.write(" | <f" + j++ + "> " + bCmp.toString(symTab));
                        }
                    } else if(ins instanceof SSAInvokeInstruction) {
                        //TODO
                    }
                    out.write(" | <f" + j++ + "> " + AnalysisUtil.sanitize(ins.toString(symTab)));
                }
                if(finalConditions.containsKey(key)) {
                    StringBuffer sb = new StringBuffer();
                    for(int condition : finalConditions.get(key)) {
                        sb.append(", " + condition);
                    }
                    sb.append("  ");
                    out.write(" | <f" + j++ + "> " + sb.substring(2));
                }
                if(val.isEntryBlock()) {
                    out.write(" | <f" + j++ + "> [ENTRY]");
                } else if(val.isExitBlock()) {
                    out.write(" | <f" + j++ + "> [EXIT]");
                } else if(val.isCatchBlock()) {
                    out.write(" | <f" + j++ + "> [CATCH]");
                }
                out.write("\"];");
                out.newLine();
            }

            // add relevant edges to dot
            log.debug("add edges to dot file");
            for (Integer src : adjList.keySet()) {
                for(int dest : adjList.get(src)) {
                    out.write(src + "->" + dest + ";");
                    out.newLine();
                }
            }

            // write dot file to file system
            out.write("}");
            out.close();
            log.info("dot file generated (" + sgFile.getAbsolutePath() + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

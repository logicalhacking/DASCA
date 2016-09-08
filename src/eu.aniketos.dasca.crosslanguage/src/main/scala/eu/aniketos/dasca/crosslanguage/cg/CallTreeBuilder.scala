/*
 * (C) Copyright 2016   The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.cg;

import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedList
import java.util.Queue
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.classLoader.Language
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.util.strings.Atom
import com.ibm.wala.cast.js.loader.JavaScriptLoader
import com.ibm.wala.cast.js.types.JavaScriptMethods
import eu.aniketos.dasca.crosslanguage.builder.MergedCallGraph
import scala.collection.Iterator
import java.io.File
import eu.aniketos.dasca.crosslanguage.builder.CordovaCGBuilder
import eu.aniketos.dasca.crosslanguage.builder.CrossBuilderOption
import collection.JavaConverters._

class CallTreeBuilder {
	var queue     = new LinkedList[CGNode]();
	var rootqueue = new LinkedList[CallTree]();
		
	def buildCallForest(cg:MergedCallGraph, sources:List[CGNode], sinks:List[CGNode]):List[CallTree] = {
	  return sources.map { src  => buildCallTree(cg,src,sinks) }.filter { x => null != x } 
	}
	  
	def buildCallTree(cg:MergedCallGraph, root: CGNode, sinks:List[CGNode]):CallTree = {
      var ct = null:CallTree
  	  var visited = List[CGNode]()
		  var NumofCount  = 0; 
			queue.add(root)
 			while(!queue.isEmpty()){
 			    var currentNode = queue.poll()
			    var RootOfCurrentNode = rootqueue.poll()
			
			    if(!visited.contains(currentNode)){
				      visited :+ currentNode
			        // search for the whether this node contains the method
		          var it = currentNode.iterateCallSites()
		          var method = null:CallSiteReference
		          
		          while (it.hasNext()) 
		              method = it.next()
		              // if this node contains the method we want
				          // print the path
			            if(sinks.contains(method.asInstanceOf[CallSiteReference].getDeclaredTarget())){ 
					          //  RootOfCurrentNode.printPath(method)
					            NumofCount = NumofCount+1
				          }else{
					          // if not find the method in current Node,
					          // add this node to the Node tree which used to create the path
					          if(currentNode.equals(root)){
						            var tree = new CallTree(currentNode)
						            createTree(cg,currentNode,method,tree)
					          }else{
						            createTree(cg,currentNode,method,RootOfCurrentNode)
					          } 
				         } 
      }
	  }
		return ct
	}			
			
	/**
	 *  this method is used to create the Node Tree.	
	 * @param cg   cg is the call graph
	 * @param currentNode  this parameter is the node that be search 
	 * @param method		method is the function that need to find in a particular node
	 * @param RootOfCurrentNode the previous node that has be search which is the parent of current node
	 */
	def createTree(cg:MergedCallGraph, currentNode: CGNode, method:CallSiteReference, RootOfCurrentNode:CallTree) = {
		var n = null:CGNode
		var child = null:CallTree
		var targetNode =  cg.getAllPossibleTargetNodes(currentNode)
		var it = targetNode.iterator
		for (n <- it){
			queue.add(n);
			if(RootOfCurrentNode!=null){
			  child = new CallTree(n,RootOfCurrentNode,RootOfCurrentNode.getLevel()+1);
			  rootqueue.add(child);
			}
		}
	}
	
}

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
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.classLoader.Language
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.cast.js.loader.JavaScriptLoader
import com.ibm.wala.cast.js.types.JavaScriptMethods

class CallTree( data:CGNode, parent:CallTree,level:Integer){
	private var children = List[CallTree]()

	def this(data:CGNode) = this(data, null, 0)
	
	def addChildren(data:CGNode) = children :+ new CallTree(data, this, level+1)
	
	/**
	 * this method return the path from current Node to root
	 * @return  
	 * return An ArrayList which contains path of currentNode 
	 */
	def getPathOfNode():List[CGNode] = {
		var path = List[CGNode]()
		var currentNode = this
		do{
	       path :+ this
     		 currentNode = currentNode.getParent()
		     if(currentNode.getParent()==null){
			       path :+ currentNode.getValue()
		     }
		}while(currentNode.getParent()!=null);
		return path;
	}
	
	def getValue() = data
	def getParent() = parent
	def getChildren() = children
	def getLevel() = level
	
	
	def printPath(method:CallSiteReference) = {
		var JSPart   = List[CGNode]()
	  var JavaPart = List[CGNode]()
	
		val it = getPathOfNode().reverse.iterator
		
		for(node <- it){
			if(node.getMethod().getDeclaringClass().getClassLoader().getLanguage()!=Language.JAVA){
				JSPart :+ node
			}else{
				JavaPart :+ node
			}
		}
		
		System.out.println("JavaScript Part: ");
			for(JSNode <- JSPart.iterator){
				val Entryposition = (JSNode.getMethod().asInstanceOf[JavaScriptLoader#JavaScriptMethodObject]).getEntity().getPosition().toString();
				val methodName =  (JSNode.getMethod().asInstanceOf[JavaScriptLoader#JavaScriptMethodObject]).getEntity().toString();
				val name = (JSNode.getMethod().asInstanceOf[JavaScriptLoader#JavaScriptMethodObject]).toString();
				System.out.println("FilePosition:	"+Entryposition);
				System.out.println("	Method name: "+ methodName+" ");
				System.out.println("		name: "+ name);
				System.out.print("  ->");
			}
			
		System.out.println("Java Part: ");
			for(JavaNode <- JavaPart.iterator){
				System.out.print(JavaNode.getMethod().getDeclaringClass().getName().getClassName()+": ");
				System.out.print("method: "+ JavaNode.getMethod().getName()+", ");
				System.out.println("path: "+JavaNode.getMethod().getDeclaringClass().getName().getPackage().toString());
				System.out.print("  ->");
			}
		
		System.out.print(method.getDeclaredTarget().getDeclaringClass().getName().getClassName()+": ");
		System.out.print("method: "+ method.getDeclaredTarget().getName()+", ");
		System.out.println("path: "+ method.getDeclaredTarget().getDeclaringClass().getName().getPackage().toString());
		System.out.println("------------");
	}
	
	
}

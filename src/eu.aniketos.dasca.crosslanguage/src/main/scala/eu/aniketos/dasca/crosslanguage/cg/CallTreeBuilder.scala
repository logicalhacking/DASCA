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

object CallTreeBuilder {
    def buildCallForest(cg:MergedCallGraph, sources:List[CGNode], sinks:List[CGNode]):List[CallTree] = {
        return sources.map { src  => buildCallTree(cg,src,sinks) }.filter { x => null != x } 
    }
	  
    def buildCallTree(cg:MergedCallGraph, root: CGNode, sinks:List[CGNode]):CallTree = {
    }
}

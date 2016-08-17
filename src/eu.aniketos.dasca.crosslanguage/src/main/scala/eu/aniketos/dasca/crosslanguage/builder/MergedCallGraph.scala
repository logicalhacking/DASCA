/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.crosslanguage.builder

import scala.collection.JavaConverters._
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.classLoader.Language
import scala.xml.XML
import scala.xml.Elem
import scala.collection.JavaConverters._
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.cast.js.loader.JavaScriptLoader
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction
import com.ibm.wala.ssa.SymbolTable
import scala.xml.Elem
import scala.xml.Node
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import com.ibm.wala.cast.js.loader.JavaScriptLoader
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.cast.js.ssa.JavaScriptInvoke
import com.ibm.wala.cast.js.types.JavaScriptMethods
import com.ibm.wala.ssa.SSAReturnInstruction
import com.ibm.wala.ssa.SSAPhiInstruction
import com.ibm.wala.cast.ir.ssa.AstLexicalRead
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite
import scala.{ Option => ? }
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess.Access
import scala.collection.immutable.HashMap
import com.ibm.wala.ssa.SSAGetInstruction
import eu.aniketos.dasca.crosslanguage.builder.algorithms.ExecuteActionBasedChecker
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import eu.aniketos.dasca.crosslanguage.builder.algorithms.ExecuteActionBasedChecker
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph
import eu.aniketos.dasca.crosslanguage.util.Util
import scala.collection.mutable.Queue
import scala.collection.mutable.ListBuffer
import com.ibm.wala.cast.ir.ssa.AstIRFactory
import scala.collection.mutable.LinkedHashSet
import com.ibm.wala.classLoader.IMethod

class MergedCallGraph(val javaCG: CallGraph, val jsCG: CallGraph, val configXml: Elem) extends Iterable[CGNode] {
  val logger = Logger(LoggerFactory.getLogger(getClass.toString))
  var targets = Map[(CGNode, CallSiteReference), LinkedHashSet[CGNode]]()
  var origins = Map[CGNode, LinkedHashSet[(CGNode, CallSiteReference)]]()
  var paramInfo = Map[(CGNode, CallSiteReference), String]()
  var paramMap = Map[(CGNode, CallSiteReference, CGNode), Map[Int, Set[Int]]]()

  var filterJavaCallSites = false
  var filterJSFrameworks = false

  lazy val classNames = {
    for (
      feature <- configXml \\ "feature";
      param <- feature \ "param" if param \@ "name" == "android-package"
    ) yield (feature \@ "name", param \@ "value")
  }.groupBy(_._1).mapValues(_.map("L" + _._2.replace('.', '/')))

  lazy val lexicalWrites = {
    for (
      node <- jsCG.iterator().asScala;
      write <- node.getIR().getInstructions.collect { case inst: AstLexicalWrite => inst };
      access <- write.getAccesses();
      use <- 0.to(write.getNumberOfUses()).find(use => write.getUse(use) == access.valueNumber)
    ) yield ((access.variableDefiner, access.variableName), (node, write, use))
  }.toSeq.groupBy(_._1).mapValues(_.map(_._2))

  lazy val executeNodesForClassName = {
    for (
      node <- javaCG.iterator.asScala;
      method = node.getMethod if method.getName.toString() == "execute";
      declaringClass = method.getDeclaringClass if Util.derivesFromCordovaPlugin(declaringClass.getSuperclass)
    ) yield (declaringClass.getName.toString(), node)
  }.toSeq.groupBy(_._1).mapValues(_.map(_._2))

  lazy val (successCalls, failCalls) = {
    val successLb = ListBuffer[(CGNode, SSAAbstractInvokeInstruction)]()
    val failLb = ListBuffer[(CGNode, SSAAbstractInvokeInstruction)]()

    for (
      node <- javaCG.iterator().asScala;
      ir <- ?(node.getIR());
      invoke <- ir.getInstructions().collect({ case invoke: SSAAbstractInvokeInstruction => invoke });
      csr = invoke.getCallSite()
    ) {
      if (isSuccessCall(node, csr)) successLb += ((node, invoke))
      if (isFailCall(node, csr)) failLb += ((node, invoke))
    }

    (successLb.toList, failLb.toList)
  }

  override def iterator(): Iterator[CGNode] = {
    javaCG.iterator.asScala ++ jsCG.iterator.asScala
  }

  def getPossibleTargets(node: CGNode, csr: CallSiteReference) = {
    if (Util.isJavaNode(node)) {
      javaCG.getPossibleTargets(node, csr).asScala
    } else {
      jsCG.getPossibleTargets(node, csr).asScala
    }
  }

  def connect(filterJavaCallSites: Boolean, filterJSFrameworks: Boolean): Unit = {
    this.filterJavaCallSites = filterJavaCallSites
    this.filterJSFrameworks = filterJSFrameworks
    connect()
  }

  def connect(): Unit = for (
    node <- jsCG.iterator.asScala;
    ir = node.getIR if ir != null;
    csr <- ir.iterateCallSites.asScala;
    jsExecuteNode <- findJsExecuteNode(node, csr);
    invoke <- ir.getCalls(csr);
    st = ir.getSymbolTable;
    featureName <- findFeatureString(st, node, invoke);
    classNames <- classNames.get(featureName);
    className <- classNames;
    executeNodes <- executeNodesForClassName.get(className);
    executeNode <- executeNodes;
    action <- determinePossibleActions(node, invoke)
  ) {
    val paramMap = Map(2 -> Set(3), 3 -> Set(3), 6 -> Set(2))
    addCrossCall(node, csr, executeNode, paramMap, action)

    val preds = jsCG.getPredNodes(node).asScala.toSeq
    logger.debug(s"There are ${preds.size} predecessors of node ${Util.prettyPrintInstruction(node, node.getIR.getInstructions.find { _ != null } get)}")
    for (
      pred <- preds;
      site <- jsCG.getPossibleSites(pred, node).asScala;
      invoke <- pred.getIR.getCalls(site)
    ) logger.debug("\t" + Util.prettyPrintInstruction(pred, invoke))

    def getPackagePrefix(pkg: String) = {
      val firstIndex = pkg.indexOf('/')
      val secondIndex = pkg.indexOf('/', firstIndex + 1)
      if (secondIndex == -1) {
        pkg
      } else {
        pkg.substring(0, secondIndex)
      }
    }

    val packageFilter = { n: CGNode =>
      getPackagePrefix(n.getMethod.getDeclaringClass.getName.getPackage.toString()) ==
        getPackagePrefix(executeNode.getMethod.getDeclaringClass.getName.getPackage.toString()) &&
        !("org/apache/cordova".equals(n.getMethod.getDeclaringClass.getName.getPackage.toString()))
    }

    val reachabilityChecker = new ExecuteActionBasedChecker(javaCG, packageFilter, action, executeNode)

    val successNodes = findFunctionNodes(2, node, invoke, jsExecuteNode)
    logger.info(s"Found ${successNodes.size} success nodes for action $action, ${Util.prettyPrintInstruction(node, invoke)}");
    val reachableSuccessCalls = if (filterJavaCallSites) {
      successCalls.filter({ case (successNode, successInvoke) => reachabilityChecker.isReachable(executeNode, successNode, successInvoke) })
    } else {
      successCalls.filter({ case (n, _) => packageFilter(n) })
    }
    for (
      successTarget <- successNodes;
      _ = logger.debug(s"\t-> ${Util.prettyPrintInstruction(successTarget, successTarget.getIR.getInstructions.find { _ != null } get)}");
      (successFrom, successInvoke) <- reachableSuccessCalls
    ) {
      addCrossCall(successFrom, successInvoke.getCallSite, successTarget, Map(1 -> Set(3)))
    }

    val failNodes = findFunctionNodes(3, node, invoke, jsExecuteNode)
    logger.info(s"Found ${failNodes.size} fail nodes for action $action, ${Util.prettyPrintInstruction(node, invoke)}")
    val reachableFailCalls = if (filterJavaCallSites) {
      failCalls.filter({ case (failNode, failInvoke) => reachabilityChecker.isReachable(executeNode, failNode, failInvoke) })
    } else {
      failCalls.filter({ case (n, _) => packageFilter(n) })
    }
    for (
      failTarget <- failNodes;
      _ = logger.debug(s"\t-> ${Util.prettyPrintInstruction(failTarget, failTarget.getIR.getInstructions.find { _ != null } get)}");
      (failFrom, failInvoke) <- reachableFailCalls
    ) {
      addCrossCall(failFrom, failInvoke.getCallSite, failTarget, Map(1 -> Set(3)))
    }
  }

  def findFeatureString(st: SymbolTable, node: CGNode, invoke: SSAAbstractInvokeInstruction) = {
    val featureUse = invoke.getUse(4)
    if (st.isStringConstant(featureUse)) {
      Option(st.getStringValue(featureUse))
    } else {
      logger.warn(s"Feature parameter was not found in symbol table for statement ${Util.prettyPrintInstruction(node, invoke)}")
      None
    }
  }

  def determinePossibleActions(node: CGNode, invoke: SSAAbstractInvokeInstruction) = {
    val lb = ListBuffer[String]()
    val st = node.getIR.getSymbolTable
    if (st.isStringConstant(invoke.getUse(5))) {
      lb += st.getStringValue(invoke.getUse(5));
    } else {
      node.getDU.getDef(invoke.getUse(5)) match {
        case phi: SSAPhiInstruction => {
          if (0.until(phi.getNumberOfUses).map({ i => st.isStringConstant(phi.getUse(i)) }).forall { _ == true }) {
            lb ++= 0.until(phi.getNumberOfUses).map { i => st.getStringValue(phi.getUse(i)) }
          }
        }
        case _ =>
      }
    }
    lb.toList
  }

  def isSuccessCall(node: CGNode, csr: CallSiteReference): Boolean = {
    val declaredTarget = csr.getDeclaredTarget
    if (declaredTarget.getDeclaringClass().getName().toString() != "Lorg/apache/cordova/CallbackContext")
      return false

    if (declaredTarget.getName().toString() == "success")
      return true

    if (declaredTarget.getName().toString() == "sendPluginResult" && isPossibleStatus(node, csr, "OK"))
      return true

    return false
  }

  def findJsExecuteNode(node: CGNode, csr: CallSiteReference) = getPossibleTargets(node, csr).collectFirst({
    n =>
      n.getMethod match {
        case m: JavaScriptLoader#JavaScriptMethodObject if (m.getEntity.getName.endsWith(CordovaCGBuilder.ExecuteSuffix)) => n
      }
  })

  private def addCrossCall(from: CGNode, csr: CallSiteReference, to: CGNode, params: Map[Int, Set[Int]]): Unit = {
    if (filterJSFrameworks && skipCrossCall(from, csr, to)) {
      logger.info(s"Skipped cross call from ${Util.prettyPrintInstruction(from, from.getIR.getCalls(csr)(0))} to ${Util.prettyPrintInstruction(to, to.getIR.getInstructions.find({ _ != null }).get)}")
      return
    }

    targets += (((from, csr), LinkedHashSet(to) ++ targets.getOrElse((from, csr), Set())))
    origins += ((to, LinkedHashSet((from, csr)) ++ origins.getOrElse(to, Set())))
    paramMap += (((from, csr, to), params))
  }

  private def skipCrossCall(from: CGNode, csr: CallSiteReference, to: CGNode): Boolean = {
    if (Util.isJavaNode(from)) {
      to.getMethod match {
        case m: JavaScriptLoader#JavaScriptMethodObject if (m.getEntity.getName.startsWith("make_")) => return true
        case _ =>
      }
      to.getIR match {
        case ir: AstIRFactory[IMethod]#AstIR => {
          val (_, _, _, _, relPath:String) = Util.getJavaScriptSourceInfo(ir, ir.getInstructions.find(_ != null).get)
          val lc = relPath.toLowerCase()
          val filename = lc.substring(lc.lastIndexOf('/'))
          if (filename.contains("jquery") || filename.contains("energize") || filename.contains("jqm") || filename.contains("prologue.js") ||
            filename.contains("preamble.js") || filename.contains("autonumeric") || filename.contains("modernizr") || filename.contains("cordova")) {
            true
          } else {
            false
          }
        }
        case _ => true
      }
    } else {
      from.getIR match {
        case ir: AstIRFactory[IMethod]#AstIR => {
          val (_, _, _, _, relPath:String) = Util.getJavaScriptSourceInfo(ir, ir.getInstructions.find(_ != null).get)
          val lc = relPath.toLowerCase()
          val filename = lc.substring(lc.lastIndexOf('/'))
          if (filename.contains("cordova")) {
            true
          } else {
            false
          }
        }
        case _ => true
      }
    }
  }

  
  /**
	 * Return all possible targets nodes (i.e., representing methods that are possibly invoked (either 
	 * within the same programming language or cross language) by the implementation of the method 
	 * that the node {@code node} represents). 
	 * @param cg                                                                                                                                                                   bvn 
	 * @param node
	 * @return 
	 */
	def getAllPossibleTargetNodes(node:CGNode) = 
	{
		 val ir = node.getIR();
		 val targetNodes= Set.empty[CGNode];
		 
		 if(Util.isJavaNode(node)){
			 // Java Node
		   val it = node.iterateCallSites();
		   while (it.hasNext()){
		         targetNodes.union(this.getPossibleTargets(node, it.next()));
			 }
		 }else{
			 // JavaScript Node
		   val it = ir.iterateAllInstructions();
		   while (it.hasNext()){
		         val ssa = it.next();
		         if (ssa.isInstanceOf[JavaScriptInvoke]) {
		           val invoke = ssa.asInstanceOf[JavaScriptInvoke];
		                 // see http://wala.sourceforge.net/files/WALAJavaScriptTutorial.pdf, page 11                 
		                 if (invoke.getCallSite().getDeclaredTarget().equals(
		                		                     JavaScriptMethods.dispatchReference)) {
		                     targetNodes.union(this.getPossibleTargets(node, invoke.getCallSite()));
		                 }
		         }
			 }			 
		 }
		 // get Cross Calls
		 val it = node.iterateCallSites();
		 while(it.hasNext()) {
	         targetNodes.union(this.getCrossTargets(node, it.next()));
		 }
		 targetNodes;
	}

  
  private def addCrossCall(from: CGNode, csr: CallSiteReference, to: CGNode, params: Map[Int, Set[Int]], param: String): Unit = {
    addCrossCall(from, csr, to, params)
    paramInfo += (((from, csr), param))
  }

  def getAllCrossTargets = targets

  def getCrossOrigins(node: CGNode) = origins.get(node) match {
    case Some(x) => x
    case None => LinkedHashSet[(CGNode, CallSiteReference)]()
  }

  def getCrossTargets(node: CGNode, csr: CallSiteReference): LinkedHashSet[CGNode] = targets.get((node, csr)) match {
    case Some(x) => x
    case None => LinkedHashSet[CGNode]()
  }


  
  def getParameterMapping(from: CGNode, csr: CallSiteReference, to: CGNode) = paramMap.get((from, csr, to))

  def findFunctionNodes(arg: Int, node: CGNode, inst: SSAInstruction, jsExecuteNode: CGNode) = {
    for (
      invoke <- jsExecuteNode.getDU.getUses(arg + 1).asScala.collectFirst({ case s: SSAAbstractInvokeInstruction => s });
      csr = invoke.getCallSite
    ) yield jsCG.getPossibleTargets(jsExecuteNode, csr).asScala
  }.getOrElse(Set[CGNode]())
  //    val queue = Queue((arg, node, inst))
  //    val visited = scala.collection.mutable.Set[(Int, CGNode, SSAInstruction)]()
  //    val result = ListBuffer[CGNode]()
  //    while (!queue.isEmpty) {
  //      val cur = queue.dequeue()
  //      if (!visited.contains(cur)) {
  //        visited += cur
  //        val (curArg, curNode, curInst) = cur
  //
  //        val use = curInst.getUse(curArg)
  //        val symbolTable = curNode.getIR().getSymbolTable()
  //
  //        if (use <= curNode.getMethod().getNumberOfParameters()) {
  //          for (
  //            pred <- jsCG.getPredNodes(curNode).asScala;
  //            csr <- jsCG.getPossibleSites(pred, curNode).asScala;
  //            invoke <- pred.getIR().getCalls(csr) if invoke.getNumberOfParameters() == curNode.getMethod().getNumberOfParameters()
  //          ) queue += ((use - 1, pred, invoke))
  //        } else {
  //          curNode.getDU().getDef(use) match {
  //            case invoke: JavaScriptInvoke if isMethodConstructor(invoke, symbolTable) => result ++= findCallbackNode(jsExecuteNode, invoke, symbolTable).toList
  //            case invoke: JavaScriptInvoke => for (
  //              target <- jsCG.getPossibleTargets(curNode, invoke.getCallSite()).asScala;
  //              ret <- target.getIR().getInstructions().collect({ case r: SSAReturnInstruction => r });
  //              if (ret.getNumberOfUses == 1)
  //            ) queue += ((0, target, ret))
  //            case phi: SSAPhiInstruction => for (i <- 0.until(phi.getNumberOfUses)) queue += ((i, curNode, phi))
  //            case read: AstLexicalRead => {
  //              for (
  //                node <- jsCG.iterator().asScala;
  //                method <- ?(node.getMethod()).collect({ case method: JavaScriptLoader#JavaScriptMethodObject => method }).toSeq;
  //                access <- read.getAccesses() if access.variableName == method.getEntity().getName();
  //                pkg <- ?(method.getReference().getDeclaringClass().getName().getPackage()) if pkg.toString() == access.variableDefiner.substring(1)
  //              ) yield result += node
  //              for (
  //                accessNo <- 0.until(read.getAccessCount);
  //                access = read.getAccesses().apply(accessNo);
  //                writes <- lexicalWrites.get((access.variableDefiner, access.variableName));
  //                write <- writes
  //              ) queue += ((accessNo, write._1, write._2))
  //            }
  //            case _ =>
  //          }
  //        }
  //      }
  //    }
  //    result.toList

  def findCallbackNode(jsExecNode: CGNode, inst: SSAInstruction, symbolTable: SymbolTable) = jsCG.getSuccNodes(jsExecNode).asScala.find {
    _.getMethod.toString().contains(symbolTable.getStringValue(inst.getUse(1)))
  }

  def isMethodConstructor(invoke: JavaScriptInvoke, symbolTable: SymbolTable) =
    invoke.getCallSite().getDeclaredTarget() == JavaScriptMethods.ctorReference &&
      invoke.getNumberOfUses > 1 && symbolTable.isStringConstant(invoke.getUse(1))

  def isPossibleStatus(node: CGNode, csr: CallSiteReference, args: String*): Boolean = {
    for (
      invoke <- node.getIR().getCalls(csr);
      inv <- node.getDU().getUses(invoke.getUse(1)).asScala.collect({ case i: SSAAbstractInvokeInstruction => i }) if inv != invoke;
      if inv.getCallSite().getDeclaredTarget().getName().toString() == "<init>";
      statusUse = inv.getUse(1)
    ) {
      if (statusUse <= node.getMethod().getNumberOfParameters())
        return true
      for (
        get <- ?(node.getDU().getDef(statusUse)).collect({ case i: SSAGetInstruction => i })
      ) {
        if (args.contains(get.getDeclaredField().getName().toString()))
          return true
      }
    }
    return false
  }

  def isFailCall(node: CGNode, csr: CallSiteReference): Boolean = {
    val declaredTarget = csr.getDeclaredTarget
    if (declaredTarget.getDeclaringClass().getName().toString() != "Lorg/apache/cordova/CallbackContext")
      return false
    if (declaredTarget.getName().toString() == "error")
      return true

    if (declaredTarget.getName().toString() == "sendPluginResult" && isPossibleStatus(node, csr, "INVALID_ACTION", "ERROR", "JSON_EXCEPTION", "IO_EXCEPTION"))
      return true

    return false
  }
}

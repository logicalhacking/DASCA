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

import com.ibm.wala.ipa.callgraph.CGNode;

class CallTree(value: CGNode, children: List[CallTree]) {
  def this(value: CGNode) = this(value, null)

  private val indent = 5

  def contains(v: CGNode): Boolean = {
    if (value == v) {
      true
    } else {
      (children.map { c => c.contains(v) }).fold(false) { (a, b) => a || b }
    }
  }

  override def toString(): String = {
    value.getMethod().getName().toString() + "\n" + ((children.map { c => "├── " + c.toString() + "\n" })
      .fold("") { (a, b) => a + b })
  }

}

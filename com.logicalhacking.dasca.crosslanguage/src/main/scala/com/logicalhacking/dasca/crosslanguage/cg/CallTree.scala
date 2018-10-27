/*
 * (C) Copyright 2016   The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.crosslanguage.cg;

import com.ibm.wala.ipa.callgraph.CGNode;

class CallTree(value: CGNode, children: List[CallTree]) {
  def this(value: CGNode) = this(value, null)

  def contains(v: CGNode): Boolean = {
    if (value == v) {
      true
    } else {
      if (null == children) {
        false
      } else {
        (children.map { c => c.contains(v) }).fold(false) { (a, b) => a || b }
      }
    }
  }

  def toString(prefix: String): String = {
    def childrenToString(c: List[CallTree]): String = c match {
      case null => ""
      case Nil => ""
      case c :: Nil => "\n" + prefix + "└─ " + c.toString(prefix + "   ")
      case c :: tail => "\n" + prefix + "├─ " + c.toString(prefix + "│  ") + childrenToString(tail)
    }

    if (null == value) {
      "null:CallTree"
    } else {
      value.toString() + childrenToString(children)
    }
  }

  override def toString(): String = {
    toString("")
  }
}

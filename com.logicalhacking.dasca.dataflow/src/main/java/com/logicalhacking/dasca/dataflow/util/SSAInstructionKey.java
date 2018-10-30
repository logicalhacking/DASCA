/*
 * Copyright (c) 2016-2018 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.logicalhacking.dasca.dataflow.util;

import com.ibm.wala.ssa.SSAInstruction;

public class SSAInstructionKey {

    private final SSAInstruction key;
    public SSAInstructionKey(SSAInstruction key) {
        this.key = key;
    }

    public int hashCode() {
        return key.hashCode();
    }

    public String toString() {
        if (null == key) {
            return "null (key)";
        } else {
            return key.toString();
        }
    }

    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        } else {
            if (obj instanceof SSAInstructionKey) {
                return key == ((SSAInstructionKey) obj).key;
            } else {
                return false;
            }
        }
    }
}

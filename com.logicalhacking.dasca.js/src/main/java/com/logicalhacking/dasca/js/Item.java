/*
 * Copyright (c) 2010-2015 SAP SE.
 *               2016-2018 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.logicalhacking.dasca.js;

public class Item {
    private String id;
    private String input;
    private String exp1;
    private String exp2;
    private String exp3;

    public String getid() {
        return id;
    }

    public void setid(String id) {
        this.id = id;

    }
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExp1() {
        return exp1;
    }
    public void setExp1(String exp1) {
        this.exp1 = exp1;
    }
    public String getCurrent() {
        return exp2;
    }
    public void setExp2(String exp2) {
        this.exp2 = exp2;
    }
    public String getExp2() {
        return exp2;
    }
    public String getExp3() {
        return exp3;
    }
    public void setExp3(String exp3) {
        this.exp3 = exp3;
    }

    @Override
    public String toString() {
        return "Item [" + "id=" + id + ",input" + input + ",exp1=" + exp1
               + ",exp2=" + exp2 + ", exp3=" + exp3 + "]";
    }
}

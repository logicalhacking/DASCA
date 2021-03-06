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

package com.logicalhacking.dasca.dataflow.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.util.CancelException;

import com.logicalhacking.dasca.dataflow.util.SuperGraphUtil;

public class Test06 {

    String entryClass = "Test06";

    @Before
    public void initTest() throws IllegalArgumentException, CancelException, IOException {
        TestSuite.initTestSG(entryClass);
    }

    @Test
    public void testBad() {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(TestSuite.superGraph, entryClass, "bad");
        assertEquals(1, result);
        return;
    }

    @Test
    public void testGood01() {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(TestSuite.superGraph, entryClass, "good01");
        assertEquals(0, result);
        return;
    }

    @Test
    public void testGood02() {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(TestSuite.superGraph, entryClass, "good02");
        assertEquals(0, result);
        return;
    }

}

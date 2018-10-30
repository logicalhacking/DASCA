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

public class Test19 {

    String entryClass = "Test19";

    @Before
    public void initTest() throws IllegalArgumentException, CancelException, IOException {
        TestSuite.initTestSG(entryClass);
    }

    @Test
    public void testBad() throws IllegalArgumentException, CancelException, IOException {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(TestSuite.superGraph, entryClass, "bad");
        assertEquals(2, result);
        return;
    }

}

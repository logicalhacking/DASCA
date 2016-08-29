/*
 * (C) Copyright 2010-2015 SAP SE.
 *               2016      The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ibm.wala.util.CancelException;

import eu.aniketos.dasca.dataflow.util.SuperGraphUtil;

public class Test17 {

    String entryClass = "Test17";

    @Before
    public void initTest() throws IllegalArgumentException, CancelException, IOException{
    	TestSuite.initTestSG(entryClass);
    }
    
    @Test
    public void testBad() throws IllegalArgumentException, CancelException, IOException {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(TestSuite.superGraph, entryClass, "bad");
        assertEquals(2, result);
        return;
    }

}

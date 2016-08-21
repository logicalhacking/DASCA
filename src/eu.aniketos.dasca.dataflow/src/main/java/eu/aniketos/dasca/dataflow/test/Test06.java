/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.aniketos.dasca.dataflow.util.SuperGraphUtil;

public class Test06 {

    String entryClass = "Test06";

    @Test
    public void testBad() {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(AllTests.superGraph, entryClass, "bad");
        assertEquals(1, result);
        return;
    }

    @Test
    public void testGood01() {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(AllTests.superGraph, entryClass, "good01");
        assertEquals(0, result);
        return;
    }

    @Test
    public void testGood02() {
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(AllTests.superGraph, entryClass, "good02");
        assertEquals(0, result);
        return;
    }

}

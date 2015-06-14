/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.aniketos.dasca.dataflow.util.SuperGraphUtil;

public class Test17 {

    String entryClass = "Test17";

    @Test
    public void testBad() {
        AllTests.init();
        int result = SuperGraphUtil.analyzeAndSaveSuperGraph(AllTests.superGraph, entryClass, "bad");
        assertEquals(2, result);
        return;
    }

}

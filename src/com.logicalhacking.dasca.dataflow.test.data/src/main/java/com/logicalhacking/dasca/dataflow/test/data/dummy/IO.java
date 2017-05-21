/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.dataflow.test.data.dummy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;

public class IO {

    public static Connection getDBConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    public static void writeString(String string) {
        // TODO Auto-generated method stub

    }

    public static String readLine() {
        /*/  // add/remove '*' to switch
        String s = "";
        try {
        	InputStreamReader converter = new InputStreamReader(System.in);
        	BufferedReader in = new BufferedReader(converter);
        	s = in.readLine();
        } catch (Exception e) {
        	System.out.println("Error! Exception: "+e);
        }
        return s;
        /*/
        return "";
        /**/
    }

    public static String readLine2() {
        /*/  // add/remove '*' to switch
        String s = "";
        try {
        	InputStreamReader converter = new InputStreamReader(System.in);
        	BufferedReader in = new BufferedReader(converter);
        	s = in.readLine();
        } catch (Exception e) {
        	System.out.println("Error! Exception: "+e);
        }
        return s;
        /*/
        return "";
        /**/
    }

    public static String readLineGood() {
        return "";
    }

    public static String sanitize(String s) {
        return "";
    }

    public static boolean testCondition(int i) throws IllegalArgumentException {
        if(i<0) {
            throw new IllegalArgumentException("number must be positive");
        }
        return true;
    }

    public static void a() {
    }

    public static InputStreamReader getInputStreamReader() {
        return null;
    }

    public static BufferedReader getBufferedReader() {
        return null;
    }

}

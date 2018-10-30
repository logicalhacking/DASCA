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

package com.logicalhacking.dasca.dataflow.test.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.logicalhacking.dasca.dataflow.test.data.dummy.IO;


// Test Case 12:
//reachability from bad sink to bad source via multiple if-statements and boolean expressions combined with arithmetics
public class Test12 {

    /*
     * bad for i==3 and !x
     */
    public void bad(boolean x, int i) {
        String userName = null;
        if(x | i > 3) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if( i < 3 ) {
            userName = IO.readLineGood();
        }
        Connection conn = IO.getDBConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void good01(boolean x, int i) {
        String userName = null;
        if(x | i > 3) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if(!x & i <= 3 ) {
            userName = IO.readLineGood();
        }
        Connection conn = IO.getDBConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Test12 test = new Test12();
        test.good01(true, 5);
        test.bad(true, 5);
    }
}

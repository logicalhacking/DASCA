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


// Test Case 13:
//reachability from bad sink to bad source via multiple if-statements with multiple arithmetics
public class Test13 {

    /*
     * bad for i==3 and j>5
     */
    public void bad(int i, int j) {
        String userName = null;
        if(j <= 5 | i > 3) {
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

    public void good01(int i, int j) {
        String userName = null;
        if(j <= 5 | i > 3) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if( j > 5 & i <= 3 ) {
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

    public void good02(int i, int j) {
        String userName = null;
        if(j > 0 & i > 0) {
            userName = IO.readLine();
            if(i + j > 0) {
                userName = IO.readLineGood();
            }
        } else {
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
        Test13 test = new Test13();
        test.good01(5, 10);
        test.good02(5, 10);
        test.bad(5, 10);
    }
}

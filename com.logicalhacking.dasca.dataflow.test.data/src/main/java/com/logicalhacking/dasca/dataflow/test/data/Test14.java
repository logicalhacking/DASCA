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


// Test Case 14:
//reachability from bad sink to bad source via if-statement and loops
public class Test14 {

    /*
     * bad for i==0
     */
    public void bad(int i) {
        String userName = IO.readLine();
        if(i < 0) {
            userName = IO.readLineGood();
        }

        for (int j = 0; j < i; j++) {
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

    public void good01() {
        String userName = IO.readLineGood();

        for (int j = 0; j < 0; j++) {
            userName = IO.readLine();

        }

        Connection conn = IO.getDBConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void good02(int i) {
        String userName = IO.readLine();
        if(i <= 0) {
            userName = IO.readLineGood();
        }

        for (int j = 0; j < i; j++) {
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
        Test14 test = new Test14();
        test.good01();
        test.good02(5);
        test.bad(5);
    }
}

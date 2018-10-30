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


// Test Case 06:
//reachability from bad sink to bad source via indirect data flow
public class Test06 {


    public void bad() {
        String userName;
        String fix   = "fix";
        String input = IO.readLine();
        if(false) {
            userName = fix;
        } else {
            userName = input;
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
        String userName;
        String fix   = "fix";
        String input = IO.readLine();
        if(true) {
            userName = fix;
        } else {
            userName = IO.readLine(); // TODO: = input;
        }
        Connection conn = IO.getDBConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void good02() {
        String userName;
        String fix   = "fix";
        if(true) {
            userName = fix + "";
        } else {
            userName = fix + IO.readLine();
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
        Test06 test = new Test06();
        test.good01();
        test.good02();
        test.bad();
    }
}

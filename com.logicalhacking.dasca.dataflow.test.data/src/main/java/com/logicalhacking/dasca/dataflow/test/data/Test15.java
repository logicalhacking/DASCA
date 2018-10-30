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


// Test Case 15:
//reachability from bad sink to bad source with guaranteed sanitizing
public class Test15 {

    public void bad(int i) {
        String userName = IO.readLine();

        if(i < 0) {
            userName = IO.sanitize(userName);
        }

        Connection conn = IO.getDBConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void good01(int i) {
        String userName = IO.readLine();
        if(i < 0) {
            userName = IO.sanitize(userName);
        } else {
            userName = IO.sanitize(userName);
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
        if(i < 0) {
            userName = IO.sanitize(userName);
        }

        if(i >= 0) {
            userName = IO.sanitize(userName);
        }

        Connection conn = IO.getDBConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void good03(int i) {
        String userName = IO.readLine();
        if(i < 0) {
            userName = IO.sanitize(userName);
        }

        if(i == 0) {
            userName = IO.readLineGood();
        }

        if(i > 0) {
            userName = IO.sanitize(userName);
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
        Test15 test = new Test15();
        test.good01(5);
        test.good02(5);
        test.good03(5);
        test.bad(5);
    }
}

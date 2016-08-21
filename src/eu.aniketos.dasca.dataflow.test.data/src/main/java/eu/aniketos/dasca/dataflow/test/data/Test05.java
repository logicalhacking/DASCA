/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.test.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import eu.aniketos.dasca.dataflow.test.data.dummy.IO;


// Test Case 05:
//reachability from bad sink to bad source via arithmetic expressions
public class Test05 {


    public void bad() {
        String userName;
        int i = 5;
        if(i > 10) {
            userName = "fix";
        } else {
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

    public void good01() {
        String userName = IO.readLine();
        int i = 5;
        if(i > 10) {
            userName = "fix";
        } else {
            userName = "fix";
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
        Test05 test = new Test05();
        test.good01();
        test.bad();
    }
}

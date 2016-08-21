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

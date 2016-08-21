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


// Test Case 08:
//reachability from bad sink to bad source via multiple if-statements (arithmetic)
public class Test08 {


    /*
     * bad for i==5
     */
    public void bad(int i) {
        String userName = null;
        if(i > 5) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if(i < 5) {
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

    public void good01(int i) {
        String userName = null;
        if(i >= 5) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if(i < 5) {
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

    public void good02(int i) {
        String userName = IO.readLine();
        if(i > 5) {
            userName = IO.readLineGood();
        }

        if(i == 5) {
            userName = IO.readLineGood();
        }

        if(i < 5) {
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

    public void good03(int i) {
        String userName = IO.readLine();
        if(i <= 5) {
            userName = IO.readLineGood();
        }

        if(i == 3) {
            Connection conn = IO.getDBConnection();
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Test08 test = new Test08();
        test.good01(10);
        test.good02(10);
        test.good03(10);
        test.bad(10);
    }
}

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


// Test Case 11:
//reachability from bad sink to bad source via multiple if-statements and multiple variables
public class Test11 {

    /*
     * bad for !x and !y
     */
    public void bad(boolean x, boolean y) {
        String userName = null;
        if(x | y) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if(x) {
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

    public void good01(boolean x, boolean y) {
        String userName = null;
        if(x | y) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if(!(x | y)) {
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

    public void good02(boolean x, boolean y) {
        String userName = null;
        if(x | y) {
            userName = IO.readLine();
        } else {
            userName = IO.readLineGood();
        }

        if(x) {
            userName = IO.readLineGood();
        }

        if(y) {
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

    public void good03(boolean x, boolean y) {
        String userName = null;
        if(x & y) {
            userName = IO.readLine();
        } else {
            userName = IO.readLineGood();
        }

        if(x) {
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

    public void good04(boolean x, boolean y) {
        String userName = null;
        if(x | y) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        if(!x & !y) {
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

    public void good05(boolean x) {
        String userName = null;
        if(x) {
            userName = IO.readLineGood();
        } else {
            userName = IO.readLine();
        }

        boolean y = !x;

        if(y) {
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
        Test11 test = new Test11();
        test.good01(true, false);
        test.good02(true, false);
        test.good03(true, false);
        test.good04(true, false);
        test.good05(true);
        test.bad(true, false);
    }
}

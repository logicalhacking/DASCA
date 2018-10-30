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


// Test Case 09:
//reachability from bad sink to bad source via mutually exclusive source and sink
public class Test09 {


    public void bad() {
        String userName = null;
        boolean local_true = true;
        if(local_true) {
            userName = IO.readLine();
        }

        if(local_true) {
            Connection conn = IO.getDBConnection();
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void good01() {
        String userName = null;
        boolean local_true = true;
        if(local_true) {
            userName = IO.readLine();
        }

        if(!local_true) {
            Connection conn = IO.getDBConnection();
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void good02() {
        String userName = null;
        int i = 1;
        if(i > 5) {
            userName = IO.readLine();
        }

        if(i < 3 ) {
            Connection conn = IO.getDBConnection();
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void good03(int i) {
        String userName = null;
        if(i > 3) {
            userName = IO.readLine();
        }

        if(i < 3 ) {
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
        Test09 test = new Test09();
        test.good01();
        test.good02();
        test.good03(5);
        test.bad();
    }
}

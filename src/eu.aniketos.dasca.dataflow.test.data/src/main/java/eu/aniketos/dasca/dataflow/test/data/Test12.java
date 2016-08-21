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

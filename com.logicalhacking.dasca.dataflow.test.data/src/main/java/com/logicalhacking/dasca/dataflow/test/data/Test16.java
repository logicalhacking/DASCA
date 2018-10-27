/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.dataflow.test.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.logicalhacking.dasca.dataflow.test.data.dummy.IO;


// Test Case 16:
//reachability from multiple identical bad sinks to one bad source with one vulnerability
public class Test16 {

    /*
     * 1 findings
     */
    public void bad(int i) {
        String userName = IO.readLine();

        if(i < 0) {
            userName = IO.readLine();
        }

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

    public static void main(String[] args) {
        Test16 test = new Test16();
        test.bad(5);
    }
}

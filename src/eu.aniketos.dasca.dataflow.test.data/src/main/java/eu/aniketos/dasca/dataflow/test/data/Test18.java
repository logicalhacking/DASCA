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


// Test Case 18:
//reachability from multiple differing bad sinks to one bad source with one vulnerability
public class Test18 {

    /*
     * 1 findings
     */
    public void bad() {
        String userName;

        if(true) {
            userName = IO.readLine();
        } else {
            userName = IO.readLine2();
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
        Test18 test = new Test18();
        test.bad();
    }
}

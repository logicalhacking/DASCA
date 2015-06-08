/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.tests;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import eu.aniketos.dasca.dataflow.tests.dummy.IO;


// Test Case 17:
//reachability from multiple identical bad sinks to one bad source with multiple vulnerabilities
public class Test17 {
	
	/*
	 * 2 findings
	 */
	public void bad(int i) {
        String userName = "";
        
        if(i < 0) {
        	userName = IO.readLine();
        } else{
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

    public static void main(String[] args) {
		Test17 test = new Test17();
		test.bad(5);
	}
}

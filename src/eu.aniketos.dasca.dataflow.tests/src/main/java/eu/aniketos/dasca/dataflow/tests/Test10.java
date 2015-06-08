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


// Test Case 10:
//reachability from bad sink to bad source via mutually exclusive source and sink (changing variables)
public class Test10 {
	
	/*
	 * bad for i==3
	 */
    public void bad(int i) {
        String userName = null;
        if(i > 2) {
            userName = IO.readLine();
        }
        
        i = i - 2;
        
        if(i < 2) {
	        Connection conn = IO.getDBConnection();
	        try {
	            Statement stmt = conn.createStatement();
	            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
	        } catch(SQLException e) {
	            e.printStackTrace();
	        }
        }
    }
    
    public void good01(int i) {
        String userName = null;
        if(i > 2) {
            userName = IO.readLine();
        }
        
        i = i - 2;
        
        if(i < 0) {
	        Connection conn = IO.getDBConnection();
	        try {
	            Statement stmt = conn.createStatement();
	            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
	        } catch(SQLException e) {
	            e.printStackTrace();
	        }
        }
    }
    
    public void good02(int i) {
        String userName = null;
        if(i > 2) {
            userName = IO.readLine();
        }
        
        i = i * -1;
        
        if(i > 2) {
	        Connection conn = IO.getDBConnection();
	        try {
	            Statement stmt = conn.createStatement();
	            stmt.execute("SELECT * FROM user WHERE name='" + userName + "';");
	        } catch(SQLException e) {
	            e.printStackTrace();
	        }
        }
    }
    
    public void good03(boolean bool) {
        String userName = null;
        if(bool) {
            userName = IO.readLine();
        }
        
        bool = !bool;
        
        if(bool) {
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
		Test10 test = new Test10();
		test.good01(10);
		test.good02(10);
		test.good03(true);
		test.bad(10);
	}
}

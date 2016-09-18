/*
 * (C) Copyright 2016      The University of Sheffield.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package eu.aniketos.dasca.dataflow.util;

import com.ibm.wala.ssa.SSAInstruction;

public class SSAInstructionKey {

	private final SSAInstruction key;
	public SSAInstructionKey(SSAInstruction key) {
		this.key = key;
	}
	
	public int hashCode(){
		return key.hashCode();
	}

	public String toString(){
		if (null == key){
			return "null (key)";
		}else{
			return key.toString();
		}		
	}
	
	public boolean equals(Object obj){
		if (null == obj){
			return false;
		}else{
			if (obj instanceof SSAInstructionKey){
 			    return key == ((SSAInstructionKey) obj).key;			
			}else{
				return false;
			}
		}
	}
}

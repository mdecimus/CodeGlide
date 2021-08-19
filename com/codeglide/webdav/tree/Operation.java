/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
*/
package com.codeglide.webdav.tree;

public class Operation {
	//"Constructor" functions  	
	public static Operation Set(String namespace,String name,String value) {
		return new Operation(SET,namespace,name,value);
	}
	
	public static Operation Remove(String namespace,String name) {
		return new Operation(REMOVE,namespace,name,null);
	}
	
	//Attributes
	public final int type;
	public final String namespace;
	public final String name;
	public final String value;
	
	//Types of operations
	public static final int SET = 0;
	public static final int REMOVE = 1;
	
	//Constructor; private, to ensure calling functions defined above
	public Operation(int type,String namespace,String name,String value) {
		this.type = type;		
		this.namespace = namespace;
		this.name = name;
		this.value = value;
	}
}

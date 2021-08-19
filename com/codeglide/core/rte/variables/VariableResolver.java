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
package com.codeglide.core.rte.variables;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

public class VariableResolver implements XPathVariableResolver {
	public HashMap<String, VariableHolder> variables = new HashMap<String, VariableHolder>();
	
	public synchronized void addVariables( String name, VariableHolder variableHolder ) {
		variables.put(name, variableHolder);
	}
	
	public synchronized void removeVariables( String name ) {
		variables.remove(name);
	}
	
	public synchronized VariableHolder getVariables( String name ) {
		return variables.get(name);
	}
	
	public Object resolveVariable(QName var) {
        if (var == null)
            throw new NullPointerException("The variable name cannot be null");
        return resolveVariable(var.getLocalPart());
	}

	public synchronized Object resolveVariable(String var) {
        Iterator<VariableHolder> it = variables.values().iterator();
        while( it.hasNext() ) {
        	VariableHolder vars = it.next();
        	if( vars.getVariableType(var) != -1 )
        		return vars.resolveVariable(var);
        }
        return null;
	}
	
	public synchronized Object getVariableValue(String var) {
        Iterator<VariableHolder> it = variables.values().iterator();
        while( it.hasNext() ) {
        	VariableHolder vars = it.next();
        	if( vars.getVariableType(var) != -1 )
        		return vars.getVariableValue(var);
        }
        return null;
	}

	public synchronized short getVariableType(String name) {
        Iterator<VariableHolder> it = variables.values().iterator();
        while( it.hasNext() ) {
        	VariableHolder vars = it.next();
        	short result = vars.getVariableType(name);
        	if( result != -1 )
        		return result;
        }
        return -1;
	}
	
	public void setVariable(String name, Object value) {
        Iterator<VariableHolder> it = variables.values().iterator();
        while( it.hasNext() ) {
        	VariableHolder vars = it.next();
        	if( vars.getVariableType(name) != -1 )
        		vars.setVariable(name, value);
        }
	}
	


}

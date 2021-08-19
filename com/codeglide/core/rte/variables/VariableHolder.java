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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.NullElement;

public class VariableHolder {
	public final static short STRING = 0;
	public final static short NUMBER = 1;
	public final static short BOOLEAN = 2;
	public final static short OBJECT = 3;
	public final static short OBJECTARRAY = 4;
	
	private HashMap<String, Variable> variableMap = new HashMap<String, Variable>();
	
	public void setVariable(String name, Object value) {
		Variable var = variableMap.get(name);
		if( var == null ) {
			var = new Variable();
			var.type = OBJECTARRAY;
			variableMap.put(name, var);
		}
		if( var.type == OBJECT && value != null ) { //TODO check this with new XPath engine
			if( value instanceof NodeList && ((NodeList)value).getLength() > 0 ) {
				List<Node> list = new LinkedList<Node>();
				list.add(((NodeList)value).item(0));
				value = new DummyNodeList(list);
			} else if( value instanceof Node ) {
				List<Node> list = new LinkedList<Node>();
				list.add((Node)value);
				value = new DummyNodeList(list);
			} else
				value = null;
		}
		var.value = value;
	}
	
	public int size() {
		return variableMap.size();
	}
	
	public Collection<String> getVariableNames() {
		return variableMap.keySet();
	}
	
	public void defineVariable(String name, short type) {
		Variable var = new Variable();
		var.value = null;
		var.type = type;
		variableMap.put(name, var);
	}
	
	public short getVariableType(String name) {
		Variable var = variableMap.get(name);
		if( var == null )
			return -1;
		else
			return var.type;
	}
	
	public Object getVariableValue(String name) {
		return variableMap.get(name).value;
	}
	
	public void undefineVariable( String name ) {
		variableMap.remove(name);
	}
	 
	public Object resolveVariable(String var) {
        Variable result = variableMap.get(var);
        if( result != null ) {
        	Object value = result.value;
        	if( value == null ) {
        		switch( result.type ) {
        			case STRING:
        				value = "";
        				break;
        			case NUMBER:
        				value = new Double(0);
        				break;
        			case BOOLEAN:
        				value = new Boolean(false);
        				break;
        			case OBJECT:
        				value = new NullElement(null);
        				break;
        			case OBJECTARRAY:
        				value = new DummyNodeList(new LinkedList<Node>());
        				break;
        		}
        	}
        	//Logger.debug("GetVariable ["+var+"] = ["+value+"]");
        	return value;
        } else
        	return null;
	}
	
	public void undefineVariables(Collection<String> vars) {
		Iterator<String> it = vars.iterator();
		while( it.hasNext() )
			variableMap.remove(it.next());
	}

	/*public synchronized void setVariables(HashMap<String, Object> vars) {
		Iterator<String> it = vars.keySet().iterator();
		while( it.hasNext() ) {
			String name = it.next();
			variableMap.put(name, vars.get(name));
		}
	}*/
	
	private class Variable {
		short type = -1;
		Object value = null;
	}

}

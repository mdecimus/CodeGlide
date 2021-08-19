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
package com.codeglide.xml.xpath;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathFunction;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.xml.dom.DummyNodeList;

public abstract class XpathFunction {

	private final HashMap<String, XPathFunction> fnc = new HashMap<String, XPathFunction>();
	
	public XPathFunction getFunction(String name) {
		return fnc.get(name);
	}
	
	protected void addFunction(String name, XPathFunction function) {
		fnc.put(name, function);
	}
	
	protected double getNumber(Object parameter) {
		if( parameter == null )
			return 0;
		else if( parameter instanceof Number )
			return ((Number)parameter).doubleValue();
		else if( parameter instanceof String ) {
			double result;
			try {
				result = Double.parseDouble((String)parameter);
			} catch (NumberFormatException e) {
				result = 0;
			}
			return result;
		} else
			return 0;
	}
	
	protected String getString(Object parameter) {
		if( parameter == null )
			return null;
		else if( parameter instanceof String )
			return (String)parameter;
		else if( parameter instanceof NodeList ) {
			if( ((NodeList)parameter).getLength() > 0 )
				return ((NodeList)parameter).item(0).getNodeValue();
		} else if( parameter instanceof Node )
			return ((Node)parameter).getNodeValue();
		else if( parameter instanceof Number )
			return ((Number)parameter).toString();
		return null;
	}
	
	protected NodeList getNodes(Object parameter) {
		if( parameter == null )
			return null;
		else if( parameter instanceof NodeList )
			return (NodeList)parameter;
		else if( parameter instanceof Node ) {
			List<Node> nodes = new LinkedList<Node>();
			nodes.add((Node)parameter);
			return new DummyNodeList(nodes);
		} else
			return null;
	}
	
	protected Node getNode(Object parameter) {
		if( parameter == null )
			return null;
		else if( parameter instanceof NodeList )
			return ((NodeList)parameter).item(0);
		else if( parameter instanceof Node )
			return (Node)parameter;
		else
			return null;
	}
	
	protected boolean isString(Object parameter) {
		return ( parameter == null || parameter instanceof String );
	}
	
}

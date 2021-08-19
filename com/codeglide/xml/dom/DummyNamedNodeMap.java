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
package com.codeglide.xml.dom;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DummyNamedNodeMap implements NamedNodeMap {
	private HashMap<String, Attr> attributesMap;
	
	public DummyNamedNodeMap(HashMap<String, Attr> attributesMap) {
		this.attributesMap = attributesMap;
	}

	public int getLength() {
		return attributesMap.size();
	}

	public Node getNamedItem(String name) {
		return attributesMap.get(name);
	}

	public Node getNamedItemNS(String namespaceURI, String localName)
			throws DOMException {
		return null;
	}

	public Node item(int index) {
		if( getLength() < 1 )
			return null;
		//TODO: Is this the fastest way to iterate a map? Should we use an ArrayList instead?
		Iterator<Attr> iterator = (attributesMap.values()).iterator();
		int c = 0;
		while( iterator.hasNext() ) {
			Attr attr = iterator.next();
			if( c == index )
				return attr;
			c++;
		}

		return null;
	}

	public Node removeNamedItem(String name) throws DOMException {
		return null;
	}

	public Node removeNamedItemNS(String namespaceURI, String localName)
			throws DOMException {
		return null;
	}

	public Node setNamedItem(Node node) throws DOMException {
		return null;//attributesMap.put(node.getNodeName(), (Attr) node);
	}

	public Node setNamedItemNS(Node arg) throws DOMException {
		return null;
	}


}

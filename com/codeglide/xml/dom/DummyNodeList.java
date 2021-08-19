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

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DummyNodeList implements NodeList {
	private List<Node> nodeList;
	
	public DummyNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}
	
	public int getLength() {
		return nodeList.size();
	}

	public Node item(int arg0) {
		return nodeList.get(arg0);
	}
	
	public void add(Node arg0) {
		nodeList.add(arg0);
	}

}

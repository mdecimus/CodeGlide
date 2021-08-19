/*
 * 	Copyright (C) 2008, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.util.mail.folder;

import com.codeglide.xml.dom.DynamicElement;

public class VirtualMailElement extends MailElement {
	//Constructor
	public VirtualMailElement(DynamicElement node) {
		super(node.getDocumentNode());
		this.node = node;
	}
	
	public void delete() {
		// TODO Auto-generated method stub		
	}
}

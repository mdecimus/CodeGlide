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
package com.codeglide.interfaces;

import java.util.HashMap;

import org.w3c.dom.Document;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.xml.dom.DynamicElement;

public abstract class Interface {
	protected HashMap<String, String> params = new HashMap<String, String>();
	
	public void setParameter( String name, String value ) {
		params.put(name.toLowerCase(), value);
	}
	
	public String getParameter( String name ) {
		return params.get(name);
	}
	
	public abstract void init() throws Exception;
	
	public abstract void initApplication(Application application) throws Exception;

	public abstract DynamicElement createRootElement(Document document) throws CodeGlideException;
	
	
}

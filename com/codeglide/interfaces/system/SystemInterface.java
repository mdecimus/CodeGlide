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
package com.codeglide.interfaces.system;

import org.w3c.dom.Document;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.interfaces.Interface;
import com.codeglide.xml.dom.DynamicElement;

public class SystemInterface extends Interface {

	public String getDefaultLanguage() {
		return "en";
	}
	
	public String getDefaultTimezone() {
		return "GMT-3";
	}
	
	public String getDefaultAutologout() {
		return "0";
	}

	public DynamicElement createRootElement(Document document) throws CodeGlideException {
		return new SystemNode(document);
	}

	public void init() throws Exception {
	}

	public void initApplication(Application application) throws Exception {
	}
	

}

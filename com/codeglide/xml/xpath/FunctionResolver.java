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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

import org.w3c.dom.DOMException;

public class FunctionResolver implements XPathFunctionResolver {

	private HashMap<String, XpathFunction> fncs = new HashMap<String, XpathFunction>();
	
	public FunctionResolver() {
		fncs.put(CGNamespaceContext.csvUri, new XpathFunctionCsv());
		fncs.put(CGNamespaceContext.vCardUri, new XpathFunctionVcard());
		fncs.put(CGNamespaceContext.mailUri, new XpathFunctionMail());
		fncs.put(CGNamespaceContext.utilUri, new XpathFunctionUtil());
		fncs.put(CGNamespaceContext.dbUri, new XpathFunctionDb());
		fncs.put(CGNamespaceContext.calUri, new XpathFunctionCal());
		fncs.put(CGNamespaceContext.fncUri, new XpathFunctionRun());
	}
	
	// Function Resolution
	public XPathFunction resolveFunction(QName fname, int arity){

		if (fname == null)
			throw new NullPointerException("The function name cannot be null.");
		
		XPathFunction result = null;
		XpathFunction functions = fncs.get(fname.getNamespaceURI());

		if( functions != null )
			result = functions.getFunction(fname.getLocalPart());
		
		if( result == null )
			throw new DOMException(DOMException.NOT_FOUND_ERR, "@function-not-found,"+fname.getLocalPart());
		
		return result;
	}
}

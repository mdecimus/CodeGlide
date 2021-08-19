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

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class CGNamespaceContext implements NamespaceContext {

	public final static String csvUri = "http://www.codeglide.com/csv";
	public final static String vCardUri = "http://www.codeglide.com/vcard";
	public final static String mailUri = "http://www.codeglide.com/mail";
	public final static String utilUri = "http://www.codeglide.com/util";	
	public final static String dbUri = "http://www.codeglide.com/db";	
	public final static String calUri = "http://www.codeglide.com/cal";	
	public final static String fncUri = "http://www.codeglide.com/fnc";	
	
	public String getNamespaceURI(String prefix){

		if (prefix == null)
			throw new IllegalArgumentException("The prefix cannot be null.");
	
		if(prefix.equals("cg:csv")){
			return csvUri;
		}else if(prefix.equals("cg:vcard")){
			return vCardUri;
		}else if(prefix.equals("cg:mail")){
			return mailUri;
		}else if(prefix.equals("cg:util")){
			return utilUri;
		}else if(prefix.equals("cg:db")){
			return dbUri;
		}else if(prefix.equals("cg:cal")){
			return calUri;
		}else if(prefix.equals("cg:fnc")){
			return fncUri;
		}else
			return null;
	}

	public String getPrefix(String namespace){

		if (namespace == null)
			throw new IllegalArgumentException("The namespace uri cannot be null.");
		
		if (namespace.equals(csvUri))
			return "cg:csv";
		else if(namespace.equals(vCardUri))
			return "cg:vcard";
		else if(namespace.equals(mailUri))
			return "cg:mail";
		else if(namespace.equals(utilUri))
			return "cg:util";		
		else if(namespace.equals(dbUri))
			return "cg:db";		
		else if(namespace.equals(calUri))
			return "cg:cal";		
		else if(namespace.equals(fncUri))
			return "cg:fnc";		
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public Iterator getPrefixes(String namespace){
		return null;
	}

}

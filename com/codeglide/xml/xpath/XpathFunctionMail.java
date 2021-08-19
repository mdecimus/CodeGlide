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

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import com.codeglide.util.mail.MailImport;

public class XpathFunctionMail extends XpathFunction {
	
	public XpathFunctionMail() {
		addFunction("get-conversation", new GetConversation());
	}

	public class GetConversation implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List parameters) throws XPathFunctionException {
			
			if(parameters.size() == 1){
				String subject = (String) parameters.get(0);
				return MailImport.getConverstion(subject);
			}else
				return null;		
		}
	}
}

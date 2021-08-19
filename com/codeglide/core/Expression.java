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
package com.codeglide.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.xpath.CGNamespaceContext;
import com.codeglide.xml.xpath.FunctionResolver;

/*
 * Compiles an XPath expression
 * 
 * An expression may contain:
 * 
 * this is a string
 * @ref-to-variable
 * !@attribute
 * !XPathExpression
 * 
 * 
 */

public class Expression {
	public static final short T_STRING = 1;
	public static final short T_STRINGREF = 2;
	public static final short T_ATTR = 3;
	public static final short T_XPATH = 4;
	
	public final static short STRING = 0;
	public final static short NODE = 1;
	public final static short NODELIST = 2;
	public final static short BOOLEAN = 3;
	public final static short NUMBER = 4;

	private final static XPathFactory xPathFactory;
	private final static XPath xpath;
	private static VariableResolver tempResolver = new VariableResolver();
	
	private Object var = null;
	private short type = 0;
	private String xpathQuery = null;

	//TODO implement xpath parser
	static {
		xPathFactory = XPathFactory.newInstance();
		xPathFactory.setXPathFunctionResolver(new FunctionResolver());
		xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(new CGNamespaceContext());
		xpath.setXPathVariableResolver(tempResolver);
	}
	
	public static XPathFactory getXPathFactory() {
		return xPathFactory;
	}
	
	public static NamespaceContext getNamespaceContext() {
		return xpath.getNamespaceContext();
	}
	
	public boolean isType( short type ) {
		return this.type == type;
	}
	
	public Expression( String exp ) {
		//TODO support storing numbers
		//TODO cache duplicated strings
		if( exp == null || exp.equals("") ) {
			type = T_STRING;
			var = "";
		} else if( exp.startsWith("!@") && !exp.contains(" ")) {
			var = exp.substring(2);
			type = T_ATTR;
		} else if( exp.startsWith("!")){
			// If the XPath references a variable, we need to compile the expression per user.
			exp = exp.substring(1);
			type = T_XPATH;
			xpathQuery = exp;
	        try {
				var = xpath.compile(exp);
			} catch (Exception e) {
				Logger.debug("Failed to compile [" + exp + "], reason :" + e.getCause());
				var = "";
				type = T_STRING;
			}
			
		} else {
			if( exp.startsWith("@") ) {
				var = exp.substring(1);
				type = T_STRINGREF;
			} else {
				var = exp;
				type = T_STRING;
			}
		}
	}
	
	public String getXpathQuery() {
		return xpathQuery; //TODO improve this, do not store variables
	}
	
	public boolean hasVariables() {
		return ( type == T_XPATH && getXpathVariables().size() > 0 );
	}
	
	public Collection<String> getXpathVariables() {
		char quoteChar = 0;
		boolean inVariable = false;
		StringBuffer varName = new StringBuffer();
		HashSet<String> result = new HashSet<String>();
		for( int i = 0; i < xpathQuery.length(); i++ ) {
			char c = xpathQuery.charAt(i);
			if( c == '\'' || c == '"') {
				if( quoteChar == 0 )
					quoteChar = c;
				else if( quoteChar == c )
					quoteChar = 0;
			} else if( c == '$' && quoteChar == 0 ) {
				inVariable = true;
				continue;
			}
			
			if( inVariable ) {
				boolean isAlphaNum = ( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') );
				
				if( isAlphaNum )
					varName.append(c);
				if( !isAlphaNum || i == xpathQuery.length() - 1 ) {
					inVariable = false;
					if( varName.length() > 0 ) {
						result.add(varName.toString());
						varName = new StringBuffer();
					}
				}
			}
		}
		
		return result;
	}
	
	public String evaluate(VariableResolver resolver, Object context) throws ExpressionException {
		String result = (String)evaluate(resolver, context, STRING);
		return (result!=null)?result:"";
	}
	
	public Object evaluate(VariableResolver resolver, Object context, short resultType) throws ExpressionException {
		if( resolver != null ) {
			// this is temporary
			tempResolver.variables = resolver.variables;
		}
		Object result = null;
		switch( type ) {
		case T_XPATH:
		{
			QName xpathType;
			switch( resultType ) {
				case NODE:
					xpathType = XPathConstants.NODE;
					break;
				case NODELIST:
					xpathType = XPathConstants.NODESET;
					break;
				case BOOLEAN:
					xpathType = XPathConstants.BOOLEAN;
					break;
				case NUMBER:
					xpathType = XPathConstants.NUMBER;
					break;
				case STRING:
				default:
					xpathType = XPathConstants.STRING;
					break;
			}
			try {
				result = ((XPathExpression)var).evaluate(context, xpathType);
			} catch (XPathExpressionException e) {
				ExpressionException e1 = new ExpressionException(e.getCause().getMessage(), this);
				e1.initCause(e);
				throw e1;
			}
		}
			break;
		case T_ATTR:
		{
			switch( resultType ) {
				case NODE:
					result = ((DynamicElement)context).getAttributeNode((String)var);
					break;
				case NODELIST:
					List<Node> nodes = new LinkedList<Node>();
					result = new DummyNodeList(nodes);
					Attr attr = ((DynamicElement)context).getAttributeNode((String)var);
					if( attr != null )
						nodes.add(attr);
					break;
				case STRING:
					result = ((DynamicElement)context).getAttribute((String)var);
					break;
			}
			
		}
			break;
		case T_STRING:
		{
			switch( resultType ) {
				case BOOLEAN:
					result = ( var != null && (((String)var).equalsIgnoreCase("true") || ((String)var).equalsIgnoreCase("yes") || ((String)var).equals("1") ));
					break;
				case NUMBER:
					result = new Double((String)var);
					break;
				case STRING:
					result = var;
					break;
			}
		}
			break;
		case T_STRINGREF:
		{
			switch( resultType ) {
				case STRING:
					Document doc = (context instanceof Document)?((Document)context):((DynamicElement)context).getDocumentNode();
					RootNode root = (RootNode)doc.getDocumentElement();
					result = root.getApplication().getLanguageEntry(root.getLanguage(), (String)var);
					break;
			}
			
		}
			break;
			
		}
		return result;
	}
	
	public String toString() {
		String result = null;
		try {
			result = evaluate(null, null);
		} catch (ExpressionException _) {
		}
		return result;
	}
}

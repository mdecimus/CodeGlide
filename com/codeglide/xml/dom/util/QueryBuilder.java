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
package com.codeglide.xml.dom.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.interfaces.xmldb.sql.And;
import com.codeglide.interfaces.xmldb.sql.Argument;
import com.codeglide.interfaces.xmldb.sql.BooleanExpression;
import com.codeglide.interfaces.xmldb.sql.Equals;
import com.codeglide.interfaces.xmldb.sql.Expression;
import com.codeglide.interfaces.xmldb.sql.GreaterOrLess;
import com.codeglide.interfaces.xmldb.sql.Like;
import com.codeglide.interfaces.xmldb.sql.Not;
import com.codeglide.interfaces.xmldb.sql.Or;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.xml.dom.DummyDocument;
import com.codeglide.xml.dom.DynamicElement;

public class QueryBuilder {

	/*
	 * Query format:
	 * 
				<_and>
					<To type="%" value="train"/> contains
					<From type="/" value="test"/> starts  with
					<Cc type="\\" value="domain.com"/> ends with
					<Bcc type="=" value="test"/> equals
					<_or>
						<Date type=">" value="123"/>
						<Date type=">=" value="444"/>
						<Date type="<" value="222"/>
						<Date type="<=" value="555"/>
					</_or>

				</_and>
	 * 
	 * 
	 */

	public static String getXpathQuery(DynamicElement query, HashMap<String, String>mappings ) {
		StringBuffer result = new StringBuffer();
		buildXPathQuery(result, query, mappings);
		return result.toString();
	}

	private static void buildXPathQuery(StringBuffer result, DynamicElement query, HashMap<String, String>mappings) {

		boolean isOr = query.getNodeName().equalsIgnoreCase("_or");
		NodeList subQueries = query.getChildNodes();

		for(int i = 0 ; i < subQueries.getLength() ; i++){

			if(i > 0){
				result.append(isOr ? "or" : "and");
			}

			Element subQueryElement = (Element) subQueries.item(i);
			String elementNodeName = subQueryElement.getNodeName();

			if(elementNodeName != "_and" && elementNodeName != "_or"){

				String hashValue = mappings.get(elementNodeName);
				String type = subQueryElement.getAttribute("type");
				String value = subQueryElement.getAttribute("value");

				// Reformulate Query
				if(value.contains(" ") && !value.startsWith("\"")){

					result.append(" (");
					buildXPathQuery(result, reformulateQuery(value, type, elementNodeName), mappings);
					result.append(") ");

					continue;
				}

				result.append(" ");

				if(type.equals("%"))
					result.append("contains(").append(hashValue).append(",'").append(value).append("')");
				else if(type.equals("/"))
					result.append("starts-with(").append(hashValue).append(",'").append(value).append("')");
				else if(type.equals("\\"))
					result.append("ends-with(").append(hashValue).append(",'").append(value).append("')");
				else if(type.equals("="))
					result.append("(").append(hashValue).append("='").append(value).append("')");
				else if(type.equals(">"))
					result.append("(").append(hashValue).append(">'").append(value).append("')");
				else if(type.equals(">="))
					result.append("(").append(hashValue).append(">='").append(value).append("')");
				else if(type.equals("<"))
					result.append("(").append(hashValue).append("<'").append(value).append("')");
				else if(type.equals("<="))
					result.append("(").append(hashValue).append("<='").append(value).append("')");
				else if(type.equals("~%"))
					result.append("not(").append("contains(").append(hashValue).append(",'").append(value).append("'))");
				else if(type.equals("~/"))
					result.append("not(").append("starts-with(").append(hashValue).append(",'").append(value).append("'))");
				else if(type.equals("~\\"))
					result.append("not(").append("ends-with(").append(hashValue).append(",'").append(value).append("'))");
				else if(type.equals("~="))
					result.append("(").append(hashValue).append("!='").append(value).append("')");

				result.append(" ");

			}else{
				result.append(" (");
				buildXPathQuery(result, (DynamicElement) subQueryElement, mappings);
				result.append(") ");
			}
		}

	}

	public static String getLuceneQuery(DynamicElement query, HashMap<String, String>mappings ) {
		StringBuffer result = new StringBuffer();
		buildLuceneQuery(result, query, mappings);
		return result.toString();
	}

	private static void buildLuceneQuery(StringBuffer result, DynamicElement query, HashMap<String, String> mappings) {

		boolean isOr = query.getNodeName().equalsIgnoreCase("_or");
		NodeList subQueries = query.getChildNodes();

		for(int i = 0 ; i < subQueries.getLength() ; i++){

			if(i > 0){
				result.append(isOr ? "OR" : "AND");
			}

			Element subQueryElement = (Element) subQueries.item(i);
			String elementNodeName = subQueryElement.getNodeName();

			if(elementNodeName != "_and" && elementNodeName != "_or"){

				String hashValue = mappings.get(elementNodeName);
				String type = subQueryElement.getAttribute("type");
				String value = subQueryElement.getAttribute("value");

				// Reformulate Query
				if(value.contains(" ") && !value.startsWith("\"")){

					result.append(" (");
					buildLuceneQuery(result, reformulateQuery(value, type, elementNodeName), mappings);
					result.append(") ");

					continue;
				}

				if(!value.startsWith("\"") && value.contains("\""))
					value = value.replace("\"", "\\\"");
				

				result.append(" ");

				if(type.equals("%") || type.equals("\\") || type.equals("="))
					result.append("+").append(hashValue).append(":\"").append(value).append("\"");
				else if(type.equals("/"))
					result.append(hashValue).append(":\"").append(value).append("*\"");
				else if(type.equals(">")) //{value TO *]
					result.append(hashValue).append(":{\"").append(value).append("\" TO *]");
				else if(type.equals(">="))//[value TO *]
					result.append(hashValue).append(":[\"").append(value).append("\" TO *]");
				else if(type.equals("<")) //[* TO value}
					result.append(hashValue).append(":[* TO \"").append(value).append("\"}");
				else if(type.equals("<=")) // [* TO value]
					result.append(hashValue).append(":[* TO \"").append(value).append("\"]");
				else if(type.equals("~%") || type.equals("~\\"))
					result.append("NOT( +").append(hashValue).append(":\"").append(value).append("\" )");
				else if(type.equals("~/"))
					result.append("NOT( ").append(hashValue).append(":\"").append(value).append("*\" )");
				else if(type.equals("~="))
					result.append("-").append(hashValue).append(":").append(value).append("*");

				result.append(" ");

			}else{
				result.append(" (");
				buildLuceneQuery(result, (DynamicElement) subQueryElement, mappings);
				result.append(") ");
			}
		}
	}

	public static BooleanExpression getSqlQuery(DynamicElement query, HashMap<String, String>mappings, Table table ) {

		Vector<Expression> terms = new Vector<Expression>();

		Iterator<Node>it = query.getChildren().iterator();

		while( it.hasNext() ) {

			Element subQueryElement = (Element) it.next();
			String elementNodeName = subQueryElement.getNodeName();

			if(elementNodeName != "_and" && elementNodeName != "_or"){

				String hashValue = mappings.get(elementNodeName);
				String type = subQueryElement.getAttribute("type");
				String value = subQueryElement.getAttribute("value");

				// Reformulate Query
				if(value.contains(" ") && !value.startsWith("\"")){
					terms.add(getSqlQuery(reformulateQuery(value, type, elementNodeName), mappings, table));
					continue;
				}

				if(type.equals("%"))
					terms.add(new Like(table.getColumn(hashValue), new Argument("%" + value + "%")));
				else if(type.equals("/"))
					terms.add(new Like(table.getColumn(hashValue), new Argument(value + "%")));
				else if(type.equals("\\"))
					terms.add(new Like(table.getColumn(hashValue), new Argument("%" + value)));
				else if(type.equals("="))
					terms.add(new Equals(table.getColumn(hashValue), new Argument(value)));
				else if(type.equals(">"))
					terms.add(new GreaterOrLess(table.getColumn(hashValue), new Argument(value), GreaterOrLess.GREATERTHAN ));
				else if(type.equals(">="))
					terms.add(new GreaterOrLess(table.getColumn(hashValue), new Argument(value), GreaterOrLess.GREATERTHANEQUAL ));
				else if(type.equals("<"))
					terms.add(new GreaterOrLess(table.getColumn(hashValue), new Argument(value), GreaterOrLess.LESSTHAN ));
				else if(type.equals("<="))
					terms.add(new GreaterOrLess(table.getColumn(hashValue), new Argument(value), GreaterOrLess.LESSTHANEQUAL ));
				else if(type.equals("~%"))
					terms.add(new Not(new Like(table.getColumn(hashValue), new Argument("%" + value + "%"))));
				else if(type.equals("~/"))
					terms.add(new Not(new Like(table.getColumn(hashValue), new Argument(value + "%"))));
				else if(type.equals("~\\"))
					terms.add(new Not(new Like(table.getColumn(hashValue), new Argument("%" + value))));
				else if(type.equals("~="))
					terms.add(new Not(new Equals(table.getColumn(hashValue), new Argument(value))));

			}else{		
				terms.add(getSqlQuery((DynamicElement) subQueryElement, mappings, table));
			}
		}

		// If the query is an Or, build an Or boolean expresion, otherwise And
		boolean isOr = query.getNodeName().equalsIgnoreCase("_or");

		BooleanExpression result = null;
		if(isOr)
			result = new Or((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));
		else		
			result = new And((BooleanExpression[]) terms.toArray(new BooleanExpression[terms.size()]));

		return result;

	}

	/*
	 * Converts -->	<To type="%" value=hola mundo/> 
	 * 
	 * To -------->	<_or>
	 *   				<To type="%" value="hola"/>
	 *   				<To type="%" value="mundo"/>
	 * 				</_or>
	 * */
	private static DynamicElement reformulateQuery(String value, String type, String nodeName){

		DummyDocument auxDocument = new DummyDocument();
		DynamicElement orElement = new DynamicElement(auxDocument, "_or");

		String[] values = value.split(" ");

		for(int j = 0 ; j < values.length ; j++){
			DynamicElement element = new DynamicElement(auxDocument, nodeName);
			element.setAttribute("type", type);
			element.setAttribute("value", values[j]);

			orElement.appendChild(element);
		}

		return orElement;
	}
}

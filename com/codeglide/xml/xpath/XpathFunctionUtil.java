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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.render.Action;
import com.codeglide.util.StringUtil;
import com.codeglide.util.spell.SpellChecker;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.TrackeableNode;

public class XpathFunctionUtil extends XpathFunction{
	
	public XpathFunctionUtil() {
		addFunction("sort", new Sort());
		addFunction("paginate", new Paginate());
		addFunction("find-text", new FindText());
		addFunction("if", new BoolMapper());
		addFunction("compare", new Compare());
		addFunction("has-changed", new HasChanged());
		addFunction("call", new Call());
	}

	public class Call implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,call");
			String fncName = getString(args.get(0));
			if( fncName != null && !fncName.isEmpty() ) {
				Context context = Context.getCurrent();
				Function function = context.getApplication().getFunction(getString(fncName).toLowerCase());
				if( function != null ) {
					// Add input parameters
					Vector<Object> inputArgs = new Vector<Object>();
					for( int i = 1; i < args.size(); i++ ) 
						inputArgs.add(args.get(i));
					
					try {
						return function.run(Context.getCurrent(), new LinkedList<Action>(), inputArgs);
					} catch (Exception e) {
						DOMException e1 = new DOMException(DOMException.INVALID_ACCESS_ERR,"@function-err,"+getString(args.get(1)));
						e1.initCause(e);
						throw e1;
					}
				}
			}
			throw new DOMException(DOMException.NOT_FOUND_ERR, "@function-not-found,"+fncName);
		}
	}	
	
	public class HasChanged implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,has-changed");
			Node node = getNode(args.get(0));
			return new Boolean(node != null && node instanceof TrackeableNode && ((TrackeableNode)node).hasChanged() );
		}
	}	
	

	public class Compare implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,compare");
			String arg1 = getString(args.get(0)), arg2 = getString(args.get(1));
			return new Double(arg1.compareTo(arg2));
		}
	}
	
	/*public class GetField implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,getField");
			Document documentNode = ((DynamicElement)getNode(args.get(0))).getDocumentNode();
			ObjectField field = ((RootNode)documentNode.getDocumentElement()).getApplication().getField(getString(args.get(1)));
			return (field != null) ? fieldToNode(documentNode, field) : null;
		}
	}
	
	public class GetFieldName implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,getField");
			try {
				Document documentNode = ((DynamicElement)getNode(args.get(0))).getDocumentNode();
				return ((RootNode)documentNode.getDocumentElement()).getApplication().getField(getString(args.get(1))).getName().evaluate(null, documentNode);
			} catch (Exception e) {
				Logger.debug(e);
				return "";
			}
		}
	}
	
	public class GetFormattedValue implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,formatValue");
			try {
				if( args.size() == 3 ) {
					// Get value
					String value = getString(args.get(2));
					if( value == null || value.isEmpty() )
						return value;

					// Get document and field
					DynamicElement rootNode = (DynamicElement)getNode(args.get(0));
					Document documentNode = rootNode.getDocumentNode();
					ObjectField field = ((RootNode)documentNode.getDocumentElement()).getApplication().getField(getString(args.get(1)));
					if( field.getFormat() == ObjectField.F_ENUM || field.getFormat() == ObjectField.F_LINK) {
						DynamicAttr resolver = null;
						boolean isLink = false;
						if( isLink = (field.getFormat() == ObjectField.F_LINK) ) 
							resolver = new DynamicAttrLink(rootNode, field, null);
						else
							resolver = new DynamicAttrEnum(rootNode, field, null);
						
						StringBuffer result = new StringBuffer();
						String[] valueParts = value.split(",");
						for( int i = 0; i < valueParts.length; i++ ) {
							if( result.length() > 0 )
								result.append(", ");
							resolver.setValue((isLink)?String.valueOf(Long.parseLong(valueParts[i], 36)):valueParts[i]);
							result.append(resolver.getExpandedValue());
						}
						value = result.toString();
					}
					return value;
				} else if( args.size() == 1 ){
					return ((DynamicAttr)getNode(args.get(0))).getExpandedValue();
				}
			} catch (Exception e) {
				Logger.debug(e);
			}
			return "";
		}
	}
	
	public class GetFields implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,getFields");
			Document documentNode = ((DynamicElement)getNode(args.get(0))).getDocumentNode();
			ObjectDefinition objDef = ((RootNode)documentNode.getDocumentElement()).getApplication().getObject(getString(args.get(1)));
			List<Node> result = new LinkedList<Node>();
			if( objDef != null ) {
				for( ObjectField field : objDef.getFields() ) {
					result.add(fieldToNode(documentNode, field));
				}
			}
			return new DummyNodeList(result);
		}
	}*/
	
	/**
	 * XPath function that creates a spell checker. Options:
	 * spellCheck(attr text)
	 * spellCheck(attr text,string lang)
	 * spellCheck(attr text,string lang,attr/string dict)
	 * spellCheck(attr text,string lang,attr/string dict,boolean isHtml)
	 */
	public class SpellCheck implements XPathFunction {

		public Object evaluate(List args) throws XPathFunctionException {
			boolean invalidParameter = false;
			DynamicAttr input = null;
			SpellChecker spellChecker = null;
			
			if (args.size() < 1)
				invalidParameter = true;
			
			// First parameter.
			if (!invalidParameter) {
				Object arg0 = args.get(0);
				if (arg0 instanceof DynamicAttr) {
					input = (DynamicAttr) arg0;
					spellChecker = new SpellChecker(input.getOwnerDocument()); // Create spell checker.
				} else
					invalidParameter = true;					
			}
			
			// Second parameter.
			if (!invalidParameter && args.size() > 1) { // Set language.
				Object arg1 = args.get(1);
				if (arg1 instanceof String)
					spellChecker.setLanguage((String) arg1);
				else
					invalidParameter = true;
			} else {
				spellChecker.setLanguage(null);
			}
			
			// Third parameter.
			if (!invalidParameter && args.size() > 2) { // Set user dictionary.
				Object arg2 = args.get(2);
				if (arg2 instanceof String)
					spellChecker.setUserDictionary((String) arg2);
				else if (arg2 instanceof DynamicAttr)
					spellChecker.setUserDictionary((DynamicAttr) arg2);
				else
					invalidParameter = true;
			}
			
			if (!invalidParameter) // Set input.
				if (args.size() > 3) {
					Object arg3 = args.get(3);
					if (arg3 instanceof Boolean) {
						boolean isHtml = (Boolean) arg3;
						spellChecker.setInput(input, isHtml);
					}
				} else {
					spellChecker.setInput(input);
				}

			if (invalidParameter)
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,spellCheck");
			
			return spellChecker;
		}
		
	}

	public class FindText implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,find-text");

			// Get list
			NodeList list = getNodes(args.get(0));
			if( list == null || list.getLength() < 1 )
				return list;
			
			// Obtain find text
			String findText = getString(args.get(1));
			if( findText == null || findText.isEmpty() )
				return list;
			
			String[] stringTokens = StringUtil.splitWithQuotes(findText.toLowerCase(), ' ', '"', true);
			List<Node> result = new LinkedList<Node>();
			
			for( int i = 0; i < list.getLength(); i++ ) {
				DynamicElement node = (DynamicElement)list.item(i);
				if( nodeMatches(node, stringTokens) )
					result.add(node);
			}
			return new DummyNodeList(result);
		}
		
		private boolean nodeMatches(DynamicElement node, String[] stringTokens ) {
			Collection<Attr> attrs = node.getAttributesCollection();
			if( attrs != null ) {
				for( Attr attr : attrs ) {
					String value = ((DynamicAttr)attr).getExpandedValue();
					if( value != null && value.length() > 0 ) {
						value = value.toLowerCase();
						boolean matches = true;
						for( int i = 0; i < stringTokens.length && matches; i++ ) 
							matches = value.contains(stringTokens[i]);
						if( matches )
							return true;
					}
				}
			}
			
			return ( node.getChildren() != null ) ? nodeMatches(node.getChildren(), stringTokens) : false;
		}

		private boolean nodeMatches(List<Node> nodes, String[] stringTokens ) {
			for( Node node : nodes ) {
				if( nodeMatches((DynamicElement)node, stringTokens) )
					return true;
			}
			return false;
		}

	}
	
	public class Paginate implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 3 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,paginate");

			// Get list
			NodeList list = getNodes(args.get(0));
			if( list == null || list.getLength() < 1 )
				return list;
			
			// Obtain page numbers
			int pageStart = (int)getNumber(args.get(1)), pageLimit = (int)getNumber(args.get(2));
			if( pageLimit < 1 || pageStart > list.getLength() )
				return list;
			List<Node> result = new LinkedList<Node>();
			pageStart--;
			while( (pageLimit-- > 0) && (pageStart < list.getLength()) )
				result.add(list.item(pageStart++));
			return new DummyNodeList(result);
		}
	}
	
	/*public class Tree2List implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 4 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,treeToList");
			String id = (String)args.get(2);
			String name = (String)args.get(3);
			String type = (String)args.get(1);
			if( id.equals("_id") )
				id = null;
			NodeList items = (NodeList)args.get(0);
			
			LinkedList<Node> result = new LinkedList<Node>();
			buildList(result, items, type, id, name, 0);
			return new DummyNodeList(result);
		}
		
		private void buildList( List<Node> result, NodeList items, String nodeType, String idAttr, String nameAttr, int level ) {
			for( int i = 0; i < items.getLength(); i++ ) {
				DynamicElement item = (DynamicElement)items.item(i);
				if( !item.getNodeName().equals(nodeType) )
					continue;
				DynamicElement node = new DynamicElement(item.getDocumentNode(), item.getNodeName());
				if( idAttr == null ) {
					if( item instanceof DbNode )
						node.setAttribute("value", Long.toString(((DbNode)item).getId(), 36) );
				} else
					node.setAttribute("value", item.getAttribute(idAttr));
				StringBuffer name = new StringBuffer();
				for( int s = 0; s < level; s++ )
					name.append(" ");
				name.append(item.getAttribute(nameAttr));
				node.setAttribute("name", name.toString());
				result.add(node);
				NodeList children = item.getChildNodes();
				if( children != null && children.getLength() > 0 )
					buildList(result, children, nodeType, idAttr, nameAttr, level+1);
			}
			
		}
		
	}
	
	public class NewObject implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			DynamicElement result = null;
			
			if( args.size() < 1 )
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,new-object");

			try {
				RootNode rootNode = Context.getCurrent().getRootNode();
				ObjectBuilder template = rootNode.getApplication().getObjectBuilder((String)args.get(1));
				if( template != null )
					result = template.buildObject(rootNode.getDocumentNode());
				else
					result = new DynamicElement(((RootNode)((NodeList)args.get(0)).item(0)).getDocumentNode(), (String)args.get(1));
			} catch (DOMException e) {
				Logger.debug(e);
				throw e;
			} catch (Exception e) {
				Logger.debug(e);
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@function-failed,new-object");
			}
			return result;
		}
		
	}
	
	public class AddChild implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			Node result = null;
			
			if( args.size() < 2 )
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,add-object");

			try {
				Node targetNode = (Node)((NodeList)args.get(0)).item(0);
				Node sourceNode = (Node)((NodeList)args.get(1)).item(0);
				result = targetNode.appendChild(sourceNode);
			} catch (DOMException e) {
				Logger.debug(e);
				throw e;
			} catch (Exception e) {
				Logger.debug(e);
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@function-failed,add-object");
			}
			return result;
		}
		
	}*/
	
	public class BoolMapper implements XPathFunction {

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			Object result = null;
			try {
				if( args.size() == 3 ) {
					result = ( (Boolean)args.get(0) ) ? args.get(1) : args.get(2);
				} else
					throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,if");
			} catch (DOMException e) {
				throw e;
			} catch (Exception e) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "@function-failed,if");
			}
			return result;
		}
		
	}
	
	public class Sort implements XPathFunction{
		
		/* ej. criteria
		 * 
		 * @name -> StringNodeComparator Sortkey = attribute to sort, Node to the node.
		 * number(@price) -> IntegerNodeComparator
		 * 
		 *  Use the comparator to sort a list containing the nodes to sort, return a dummyNodeList with the ordered nodes. 
		 * 
		 * - Sino uso Xpath.
		 * ./authors/author[position() = last()]/@name
		 * 
		 */

		//XXX take into account when the sortkey doesn't match with an attribute. Done?

		@SuppressWarnings("unchecked")
		public Object evaluate(List parameters) throws XPathFunctionException {

			if(parameters.size() == 2){

				NodeList in = (NodeList) parameters.get(0);
				String criteria = (String) parameters.get(1);
				boolean doReverse = false;

				if( criteria == null || criteria.isEmpty() || criteria.equals("@") )
					return in;

				ArrayList<Node> nodesList = new ArrayList<Node>();
				ArrayList<ComparableNodeWrapper> wrappersList = new ArrayList<ComparableNodeWrapper>();

				if( (doReverse = criteria.startsWith("@@")) )
					criteria = criteria.substring(1);
				
				if(criteria.startsWith("@")){

					String sortKey = criteria.substring(1);

					for(int i = 0; i < in.getLength() ; i ++){
						DynamicElement node = (DynamicElement) in.item(i);
						wrappersList.add(new StringComparableNodeWrapper(node.getAttribute(sortKey),node));
					}			

				}else if(criteria.startsWith("number(@")){

					String sortKey = criteria.substring(8 , criteria.length() - 1);

					for(int i = 0; i < in.getLength() ; i ++){
						DynamicElement node = (DynamicElement) in.item(i);
						wrappersList.add(new DoubleComparableNodeWrapper(Double.valueOf(node.getAttribute(sortKey)),node));
					}

				}else{

					XPathFactory factory = XPathFactory.newInstance();
					XPath xPath = factory.newXPath();

					try {
						XPathExpression expression = xPath.compile(criteria);


						if(criteria.startsWith("number(")){

							for(int i = 0; i < in.getLength() ; i++){
								Object result = expression.evaluate(in.item(i), XPathConstants.NUMBER);
								wrappersList.add(new DoubleComparableNodeWrapper((Double) result, in.item(i))); 
							}					
						}else{

							for(int i = 0; i < in.getLength() ; i++){
								Object result = expression.evaluate(in.item(i), XPathConstants.NODESET);
								Attr node = ((Attr) ((NodeList) result).item(0));
								wrappersList.add(new StringComparableNodeWrapper(node.getValue() , in.item(i)));
							}

						}

					} catch (XPathExpressionException _) {
					}


				}

				Collections.sort(wrappersList);

				if( !doReverse )
					Collections.reverse(wrappersList);
				
				for(int i = 0; i < wrappersList.size(); i++){
					nodesList.add(wrappersList.get(i).getNode());
				}

				return new DummyNodeList(nodesList);

			} else if( parameters.size() == 1)
				return parameters.get(0);
			else
				return null;
		}

		@SuppressWarnings("unchecked")
		public abstract class ComparableNodeWrapper implements Comparable {

			Object sortKey;
			Node node;

			public abstract int compareTo(Object cnw);

			public Node getNode() {
				return node;
			}

			public Object getSortKey() {
				return sortKey;
			}	
		}

		public class DoubleComparableNodeWrapper extends ComparableNodeWrapper {

			public DoubleComparableNodeWrapper(Double sortKey, Node node) {
				this.sortKey = sortKey;
				this.node = node;
			}

			public int compareTo(Object dcnw) {

				double result = (Double) this.sortKey - (Double)((DoubleComparableNodeWrapper)dcnw).getSortKey(); 

				if(result < 0)
					return -1;
				else if(result > 0)
					return 1;
				else
					return 0;
			}

		}

		public class StringComparableNodeWrapper extends ComparableNodeWrapper {

			public StringComparableNodeWrapper(String sortKey, Node node) {
				this.sortKey = (sortKey!=null)?sortKey:"";
				this.node = node;
			}

			public int compareTo(Object scnw) {
				return ((String)this.sortKey).compareTo((String)((StringComparableNodeWrapper) scnw).getSortKey());
			}
		}

	}

}
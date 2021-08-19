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

import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.acl.AclToken;
import com.codeglide.interfaces.LeavesNode;
import com.codeglide.interfaces.SearchableNode;
import com.codeglide.interfaces.xmldb.DbLeafNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.xml.dom.DummyDocument;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

/*
 * 
 * search( containerNode, searchString [,nodeType ,sortBy [,rangeMin] [,rangeMax]] )
 * search( containerNode, searchObject [,sortBy], [,rangeMin] [,rangeMax] )
 * 
 * select( containerNode [,rangeMin ,rangeMax] )
 * select( containerNode [,nodeType ,sortBy ,rangeMin ,rangeMax] )
 * 
 */

public class XpathFunctionDb extends XpathFunction {
	public XpathFunctionDb() {
		addFunction("check-acl", new AclCheck());
		addFunction("search", new Search());
		addFunction("search-text", new SearchText());
		addFunction("select", new GetChildren());
		addFunction("changelog", new GetChangelog());
	}
	
	public class GetChangelog implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,changelog");
			Node node = getNode(args.get(0));
			if( node != null && node instanceof DbNode )
				return new DummyNodeList(((DbNode)node).getChangeLog());
			else
				return null;
		}
	}	
	
	public class SearchText implements XPathFunction {
		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,search-text");
			SearchableNode searchNode = (SearchableNode) getNode(args.get(0));
			DynamicElement searchObject = new DynamicElement(new DummyDocument(), "SearchObject");
			String sortBy = null;
			int rangeMin = 0, rangeMax = 0;
			String searchString = getString(args.get(1));
			if( searchString != null && !searchString.isEmpty() ) {
				searchObject.setAttribute("Text", searchString);
				if( args.size() >= 3 )
					searchObject.setAttribute("Type", getString(args.get(2)));
				
				// Obtain sorting and ranges
				if( args.size() >= 4 ) {
					sortBy = getString(args.get(3));
					if( args.size() == 6 ) {
						rangeMin = (int)getNumber(args.get(4));
						rangeMax = (int)getNumber(args.get(5));
					}
				}
			}
			return new DummyNodeList((searchNode!=null)?searchNode.search(searchObject, sortBy, rangeMin, rangeMax):new LinkedList<Node>());
		}
	}
	
	public class Search implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,search");
			
			// If the second argument is a string, treat it as getChildren
			if( isString(args.get(1)) ) 
				return getFunction("select").evaluate(args);
			
			SearchableNode searchNode = (SearchableNode) getNode(args.get(0));
			DynamicElement searchObject = new DynamicElement(new DummyDocument(), "SearchObject");
			String sortBy = null;
			int rangeMin = 0, rangeMax = 0;
			StringBuffer searchFolders = new StringBuffer(), searchText = new StringBuffer();
			String searchType = null;
			List<Node> searchTerms = new LinkedList<Node>();
			NodeList searchObjects = getNodes(args.get(1));

			if( searchObjects != null && searchObjects.getLength() > 0 ) {
				for( int i = 0; i < searchObjects.getLength(); i++ ) {
					DynamicElement node = (DynamicElement)searchObjects.item(i);
					
					// Set type
					if( searchType == null || searchType.isEmpty() )
						searchType = node.getAttribute("Type");
					
					// Append search text
					String nodeText = node.getAttribute("Text");
					if( nodeText != null && !nodeText.isEmpty() ) {
						if( searchText.length() > 0 )
							searchText.append(" ");
						searchText.append(nodeText);
					}
					
					// Append folders
					String nodeFolder = node.getAttribute("Folder");
					if( nodeFolder != null && !nodeFolder.isEmpty() ) {
						if( searchFolders.length() > 0 )
							searchFolders.append(",");
						searchFolders.append(nodeFolder);
					}
					
					// Add search terms
					if( node.getChildren() != null )
						searchTerms.addAll(node.getChildren());
				}
			}
			
			// Add collected search terms
			if( searchTerms.size() > 0 )
				searchObject._setChildren(searchTerms);
			
			// Set type
			if( searchType != null && !searchType.isEmpty() )
				searchObject.setAttribute("Type", searchType);
			
			// Set folders
			if( searchFolders.length() > 0 )
				searchObject.setAttribute("Folder", searchFolders.toString());
			
			// Set search text
			if( searchText.length() > 0 )
				searchObject.setAttribute("Text", searchText.toString());

			// Obtain sorting and ranges
			if( args.size() >= 3 ) {
				sortBy = getString(args.get(2));
				if( args.size() == 5 ) {
					rangeMin = (int)getNumber(args.get(3));
					rangeMax = (int)getNumber(args.get(4));
				}
			}
			
			return new DummyNodeList(searchNode.search(searchObject, sortBy, rangeMin, rangeMax));
		}
	}	
	
	/*
	 * 
	 * search( containerNode, searchString [,nodeType ,sortBy [,rangeMin] [,rangeMax]] )
	 * search( containerNode, searchObject [,sortBy], [,rangeMin] [,rangeMax] )
	 * 
	 * select( containerNode [,rangeMin ,rangeMax] )
	 * select( containerNode [,nodeType ,sortBy ,rangeMin ,rangeMax] )
	 * 
	 */

	
	public class GetChildren implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,select");
			
			// Let's obtain the container node
			List<Node> result = null;
			
			Node node = getNode(args.get(0));
			
			// The user might be trying to select a node by ID
			if( node == null || node instanceof DynamicAttr ) {
				String nodeId = null;
				if( node == null )
					nodeId = getString(args.get(0));
				else
					nodeId = node.getNodeValue();
				try {
					return new DbLeafNode(getNode(args.get(0)).getOwnerDocument(), Long.parseLong(getString(nodeId), 36));
				} catch (NumberFormatException e) {
					return new DummyNodeList(new LinkedList<Node>());
				}
			}
			
			if( node == null || !(node instanceof LeavesNode ))
				return new DummyNodeList( new LinkedList<Node>() );
			if( args.size() == 2 )
				result = ((LeavesNode)node).getLeaves(getString(args.get(1)), null, 0, 0);
			else if( args.size() == 3 )
				result = ((LeavesNode)node).getLeaves(null, null, (int)getNumber(args.get(1)), (int)getNumber(args.get(2)));
			else if( args.size() == 5 )
				result = ((LeavesNode)node).getLeaves(getString(args.get(1)), getString(args.get(2)), (int)getNumber(args.get(3)), (int)getNumber(args.get(4)));
			else
				result = ((LeavesNode)node).getLeaves();
				
			return new DummyNodeList( result );
		}
	}	
	
	/*public class GetContainers implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,getContainers");
			DbRootNode rootNode = (DbRootNode) getNode(args.get(0));
			
			// Reset folder parents
			List<Node> folders = rootNode.getFolders(false);
			for( Node node : folders ) 
				((DynamicElement)node).setParent(null);

			return new DummyNodeList(folders);
		}

	}*/

	public class AclCheck implements XPathFunction{

		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() != 2 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,check-acl");
			String aclString = ((String)args.get(1)).toLowerCase();
			DbNode node = (DbNode) getNode(args.get(0));
			
			int perm = 0;
			for( int i = 0; i < aclString.length(); i++ ) {
				switch( aclString.charAt(i) ) {
				case 'i':
					perm |= AclToken.ACL_INSERT;
					break;
				case 'd':
					perm |= AclToken.ACL_DELETE;
					break;
				case 'u':
					perm |= AclToken.ACL_UPDATE;
					break;
				case 'r':
					perm |= AclToken.ACL_READ;
					break;
				case 'o':
					perm |= AclToken.ACL_OWNER;
					break;
				}
			}

			return new Boolean(node.hasPermission(perm));
		}

	}
}

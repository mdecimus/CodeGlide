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

package com.codeglide.webdav.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.session.Session;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.webdav.MultiStatusBody;
import com.codeglide.webdav.DavResponse.Status;
import com.codeglide.webdav.exceptions.WebdavTreeException;
import com.codeglide.webdav.exceptions.WebdavTreeUnsupportedOperationException;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class WebdavFileNode extends WebdavTreeNode {
	//Constructor. Protected to prevent using it directrly
	protected WebdavFileNode(Session session,DynamicElement node) {
		super(session,node);
	};
	
	protected WebdavFileNode(Session session,WebdavFolderNode parent,String name,InputStream stream) throws CodeGlideException, DOMException, IOException {
		//Create the folder node using the "File" template
		super(session,new DynamicElement(session.getRootNode().getDocumentNode(), "File"));
		//Set properties
		node.setAttribute("Name",name);
		node.setAttribute("Size",stream.available());
		node.setAttribute("Bin",stream);		
	}
	
	protected WebdavFileNode(Session session,WebdavFolderNode parent,String name,String string) throws CodeGlideException, DOMException, IOException {
		//Create the folder node using the "File" template
		super(session,new DynamicElement(session.getRootNode().getDocumentNode(), "File"));
		//Set properties
		node.setAttribute("Name",name);
		node.setAttribute("Size",string.length());
		node.setAttribute("Bin",string);
	}
	
	protected void defineProperties() {
		super.defineProperties();
		//TODO: properties added for IMAP services testing
		new PropertyAccessor("DAV:","subject") {
			public String get() throws WebdavTreeException {
				try {
					Expression subjectpath = new Expression("!@Subject");
					String subject = (String)subjectpath.evaluate(resolver,node,Expression.STRING);
					return (subject != null)?subject:"UNKNOWN-NAME";
				} catch (ExpressionException e) {
					throw new WebdavTreeException(e);
				}
			}
		};		
		new PropertyAccessor("DAV:","seen") {
			public String get() throws WebdavTreeException {
				try {
					Expression subjectpath = new Expression("!@FlagSeen");
					String subject = (String)subjectpath.evaluate(resolver,node,Expression.STRING);
					return (subject != null)?subject:"0";
				} catch (ExpressionException e) {
					throw new WebdavTreeException(e);
				}
			}
		};
		new PropertyAccessor("DAV:","flagged") {
			public String get() throws WebdavTreeException {
				try {
					Expression subjectpath = new Expression("!@FlagFlagged");
					String subject = (String)subjectpath.evaluate(resolver,node,Expression.STRING);
					return (subject != null)?subject:"0";
				} catch (ExpressionException e) {
					throw new WebdavTreeException(e);
				}
			}
		};
		new PropertyAccessor("DAV:","answered") {
			public String get() throws WebdavTreeException {
				try {
					Expression subjectpath = new Expression("!@FlagAnswered");
					String subject = (String)subjectpath.evaluate(resolver,node,Expression.STRING);
					return (subject != null)?subject:"0";
				} catch (ExpressionException e) {
					throw new WebdavTreeException(e);
				}
			}
		};
	}
	
	//Getters
	//For easy descriptive printing
	public String toString(String prefix) throws WebdavTreeException {
		String txt = prefix + ((prefix.isEmpty())?"":"|_") + "[I]" + node.getNodeName() + " [";
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0;i < attributes.getLength();i++)
			txt += ((i==0)?"":",") + attributes.item(i).getNodeName() + " = " + attributes.item(i).getNodeValue();
		txt += "]\n";
		return txt;
	};
	
	//The name of the node
	//TODO: getName() replaced for IMAP services testing 
/*
	public String getName() throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression namepath = session.getApplication().getService("webdav").getMap("item").getValue("name");
			String name = (String)namepath.evaluate(resolver,node,Expression.STRING);
			return (name != null)?name:"UNKNOWN-NAME";
		} catch (XPathExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
*/
	public String getName() throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression namepath = new Expression("!@Subject");
			String name = (String)namepath.evaluate(resolver,node,Expression.STRING);
			return (name != null)?name:"UNKNOWN-NAME";
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	
	//Size of node
	public String getSize() throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression sizepath = session.getApplication().getService("webdav").getMap("item").getValue("size");
			return (String)sizepath.evaluate(resolver,node,Expression.STRING);
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	//File's content. It could be either a string or an stream
	public Object getStream() throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression streampath = session.getApplication().getService("webdav").getMap("item").getValue("stream");
			DynamicAttr attribute = (DynamicAttr)streampath.evaluate(resolver,node,Expression.NODE);
			if (attribute == null)
				return "";
			else
				return attribute.getObjectValue();
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	//Resource type as webdav protocol define it
	public String getResourceType() {
		return "";
	};
	
	//Setters
	public void setName(String name) throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression namepath = session.getApplication().getService("webdav").getMap("item").getValue("name");
			((DynamicAttr)namepath.evaluate(resolver,node,Expression.NODE)).setValue(name);
		} catch (ClassCastException e) {
			throw new WebdavTreeException(e);
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	//For easy descriptive printing
	protected List<WebdavTreeNode> getChildren() throws WebdavTreeException {
		return new LinkedList<WebdavTreeNode>();
	};

	//Says if the node passes the filters
	public boolean isFiltered() throws WebdavTreeException {
		//Files have no filters, so always passes
		return true;
	}
	
	//To complete webdav methods
	//-MKCOL
	protected void makeFolder(String foldername,Set<String> tokens) throws WebdavTreeUnsupportedOperationException {
		//A file can never create a child collection
		throw new WebdavTreeUnsupportedOperationException();
	}
		
	//-GET
	public Object getContent() throws WebdavTreeException {
		return this.getStream();
	}
	
	//-PUT
	protected void makeFile(String filename, InputStream stream,Set<String> tokens) throws WebdavTreeException {
		//A file can never create a child file
		throw new WebdavTreeUnsupportedOperationException();
	}
	
	protected void setContent(InputStream stream) throws WebdavTreeException {
		try {
			//Set properties and save
			node.setAttribute("Size",stream.available());
			node.setAttribute("Bin",stream);
			((DbNode)node).update();
		} catch (IOException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	//-COPY
	protected boolean copyTree(WebdavTreeNode source, String name, boolean overwrite, int depth,Set<String> tokens,MultiStatusBody errors) throws WebdavTreeException {
		//A file can never create a child
		errors.addResponse(this.getFullpathName(),Status.METHOD_NOT_ALLOWED);
		throw new WebdavTreeUnsupportedOperationException();
	}
	
	protected void clone(WebdavFolderNode parent, String newname, int depth) throws WebdavTreeException {
		//Create a DynamicElement with the same attributes than "node", as child of "parent"
		Object content = this.getStream();
		try {
			if (content instanceof InputStream)
				parent.addNewChild(new WebdavFileNode(session,parent,(newname != null)?newname:this.getName(),(InputStream)content));
			if (content instanceof String)
				parent.addNewChild(new WebdavFileNode(session,parent,(newname != null)?newname:this.getName(),(String)content));
			else
				throw new WebdavTreeException("'" + content.toString() +"' as element content (String or InputStream expected)");
		} catch (CodeGlideException e) {
			throw new WebdavTreeException(e);
		} catch (DOMException e) {
			throw new WebdavTreeException(e);
		} catch (IOException e) {
			throw new WebdavTreeException(e);
		}
	}
}

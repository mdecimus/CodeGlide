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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.acl.AclToken;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.session.Session;
import com.codeglide.interfaces.xmldb.DbContainerNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.webdav.Depth;
import com.codeglide.webdav.MultiStatusBody;
import com.codeglide.webdav.DavResponse.Status;
import com.codeglide.webdav.exceptions.WebdavTreeException;
import com.codeglide.webdav.exceptions.WebdavTreeNoPermissionException;
import com.codeglide.webdav.exceptions.WebdavTreeNodeAlreadyExistsException;
import com.codeglide.webdav.exceptions.WebdavTreeNodeLockedException;
import com.codeglide.webdav.exceptions.WebdavTreeUnsupportedOperationException;
import com.codeglide.webdav.lock.LockManager;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class WebdavFolderNode extends WebdavTreeNode {	
	private List<WebdavTreeNode> children = null;
	//Constructor. Protected to prevent using it directly
	protected WebdavFolderNode(Session session,DynamicElement node) {
		super(session,node);
	}
	
	protected WebdavFolderNode(Session session,WebdavFolderNode parent,String name) throws CodeGlideException {
		//Create the folder node using the "Folder" template
		super(session,new DynamicElement(session.getRootNode().getDocumentNode(), "Folder"));
		//Set properties
		node.setAttribute("Name",name);
		node.setAttribute("_Container","");
		node.setAttribute("Type","File");	//TODO Esto no puede ser asi, porque que el filtro sea @Type = 'File' es circunstancial
	}
	
	//Getters
	//The name of the node, as the user defined it
	public String getName() throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression namepath = session.getApplication().getService("webdav").getMap("folder").getValue("name");
			String name = (String)namepath.evaluate(resolver,node,Expression.STRING);
			return (name != null)?name:"UNKNOWN-NAME";
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	//Size of node
	public String getSize() throws WebdavTreeException {
		return Integer.toString(this.getChildren().size());		
	}
	
	//Resource type as webdav protocol define it
	public String getResourceType() {
		return "<collection/>";
	}
	
	//Setters
	public void setName(String name) throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the node name
			Expression namepath = session.getApplication().getService("webdav").getMap("folder").getValue("name");
			((DynamicAttr)namepath.evaluate(resolver,node,Expression.NODE)).setValue(name);
		} catch (ClassCastException e) {
			throw new WebdavTreeException(e);
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}
	
	//For easy descriptive printing
	public String toString(String prefix) throws WebdavTreeException {
		String txt = prefix + ((prefix.isEmpty())?"":"|_") + "[F]" + node.getNodeName() + " [";
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0;i < attributes.getLength();i++) {
			txt += ((i==0)?"":",") + attributes.item(i).getNodeName() + " = ";
			txt += attributes.item(i).getNodeValue();
		}
		txt += "]:\n";
		try {
			for (WebdavTreeNode child : this.getChildren())
				txt += child.toString(prefix + "  ");
		} catch (ClassCastException e) {
			//NO-OP
		}
		return txt;
	}

	//Children management
	protected List<WebdavTreeNode> getChildren() throws WebdavTreeException {
		if (children == null) {
			children = new LinkedList<WebdavTreeNode>();
			//First, get the subfolders; they are the children of "node"
			NodeList folders = node.getChildNodes(); 
			for (int i = 0;i < folders.getLength();i++) {
				WebdavFolderNode folder = new WebdavFolderNode(session,(DynamicElement)folders.item(i));
				if (folder.isFiltered()) {	//If the folder does not pass the filter, is discarted
					children.add(folder);
					folder.setParent(this);
				}
			}
			//Now, add the files. Must be fetched from database
			List<Node> files = ((DbContainerNode)node).getLeaves();
			for(Node i : files) {
				WebdavTreeNode file = new WebdavFileNode(session,(DynamicElement)i);
				if (file.isFiltered()) {	//If the file does not pass the filter, is discarted
					children.add(file);
					file.setParent(this);
				}
			}
		}
		return children;
	};
	
	//Adds a recently created node and saves it in DB
	protected void addNewChild(WebdavTreeNode child) {
		this.node.appendChild(child.node);
	}
	
	//Says if the node passes the filters	
	public boolean isFiltered() throws WebdavTreeException {
		//TODO: para inspeccionar toda estructura con webdav. Retirar luego
/*
		try {
			Expression filter = session.getApplication().getService("webdav").getMap("folder").getValue("filter");
			return (Boolean)filter.evaluate(resolver,node,Expression.BOOLEAN);
		} catch (XPathExpressionException e) {
			throw new WebdavTreeException(e);
		}
*/
		return true;
	}
	
	//-MKCOL
	protected void makeFolder(String name,Set<String> tokens) throws WebdavTreeException {
		if (!LockManager.Instance.hasAccessTo((DbNode)node,tokens))
			throw new WebdavTreeNodeLockedException("Can't create folder");
		if (!((DbNode)node).hasPermission(AclToken.ACL_INSERT))
			throw new WebdavTreeNoPermissionException("Can't create folder");
		
		//Verify "foldername" does not exists
		for (WebdavTreeNode child : this.getChildren())
			if (child.getName().equals(name))
				throw new WebdavTreeNodeAlreadyExistsException();
		//Create folder
		try {
			WebdavFolderNode folder = new WebdavFolderNode(session,this,name);
			this.addNewChild(folder);
		} catch (CodeGlideException e) {
			throw new WebdavTreeException(e);
		}
/*
		//Update modification time
		((DbNode)node).update();
*/
	}

	//-GET
	public Object getContent() throws WebdavTreeException {
		//For a folder, build an html file containing each child and parent (if exists)
		String rows = "<TR><TD><B>File</B></TD><TD><B>Size</B></TD></TR>";
		//Each row has the node name (excepting the parent, having "..") and a <a>..</a> tag to its URL
		if (parent != null)
			rows += "<TR><TD><A HREF=\"" + parent.getFullpathName() + "\">..</A></TD><TD>"  + parent.getSize() + "</TD></TR>";
		for (WebdavTreeNode child : this.getChildren())
			rows += "<TR><TD><A HREF=\"" + child.getFullpathName() + "\">" + child.getName() + "</A></TD><TD>"  + child.getSize() + "</TD></TR>";
		return "<HTML><HEAD><TITLE>" + this.getName() + "</TITLE></HEAD><BODY><TABLE>" + rows + "</TABLE></BODY></HTML>";
	}
	
	//-PUT
	protected void makeFile(String name, InputStream stream,Set<String> tokens) throws WebdavTreeException {
		if (!LockManager.Instance.hasAccessTo((DbNode)node,tokens))
			throw new WebdavTreeNodeLockedException("Can't create/replace file");
		try {			
			//Try to find the file named "filename"
			for (WebdavTreeNode child : this.getChildren())
				if (child.getName().equals(name)) {
					//Node exists; change its content
					if (!((DbNode)child.node).hasPermission(AclToken.ACL_UPDATE))
						throw new WebdavTreeNoPermissionException("Can't replace file");
					child.setContent(stream);
					return;
				}
			//Does not exist, create it
			if (!((DbNode)node).hasPermission(AclToken.ACL_INSERT))
				throw new WebdavTreeNoPermissionException("Can't create file");
			WebdavFileNode file = new WebdavFileNode(session,this,name,stream);
			this.addNewChild(file);
		} catch (IOException e) {			
			throw new WebdavTreeException(e);
		} catch (CodeGlideException e) {
			throw new WebdavTreeException(e);
		}
/*
		//Update modification time
		((DbNode)node).update();
*/
	}
	
	protected void setContent(InputStream stream) throws WebdavTreeException {
		//Trying to change folder's content, and PUT fails for collections
		throw new WebdavTreeUnsupportedOperationException("PUT fails for collections");
	}
	
	//-COPY
	//-Returns "true" if destiny existed (and therefore it was overwrited) and "false" if it didn't (so it was created)
	protected boolean copyTree(WebdavTreeNode source, String name, boolean overwrite, int depth,Set<String> tokens,MultiStatusBody errors) throws WebdavTreeException {
		if (!LockManager.Instance.hasAccessTo((DbNode)node,tokens)) {
			errors.addResponse(this.getFullpathName(),Status.LOCKED);
			throw new WebdavTreeNodeLockedException("Can't create file/folder");
		}
		if (!((DbNode)node).hasPermission(AclToken.ACL_INSERT)) {
			errors.addResponse(this.getFullpathName(),Status.FORBIDDEN);
			throw new WebdavTreeNoPermissionException("Can't create file/folder");
		}
		
		//Look for "name" 
		boolean existed = false;
		for (WebdavTreeNode child : this.getChildren())
			if (child.getName().equals(name)) {
				//Node exists. Delete it (if able) and continue, or fail
				if (overwrite) {
					child.delete(tokens,errors);
					existed = true;
					break;
				} else {
					errors.addResponse(this.getFullpathName(),Status.PRECONDITION_FAILED);
					throw new WebdavTreeNodeAlreadyExistsException();
				}
			}
		//Clone source
		try {
			source.clone(this,name,depth);
		} catch (DOMException e) {
			throw new WebdavTreeException(e);
		}
		return existed;
	}
	
	protected void clone(WebdavFolderNode parent, String newname, int depth) throws WebdavTreeException {
		//Create a DynamicElement with the same attributes than "node", as child of "parent"
		try {
			WebdavFolderNode clone = new WebdavFolderNode(session,parent,(newname != null)?newname:this.getName());
			//Propagate
			if (depth > 0)
				for (WebdavTreeNode child : this.getChildren())
					child.clone(clone,null, Depth.Dec(depth));
			//Add it to tree
			parent.addNewChild(clone);
		} catch (CodeGlideException e) {
			throw new WebdavTreeException(e);
		}
	}
}

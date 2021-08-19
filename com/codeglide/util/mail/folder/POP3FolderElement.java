/*
 * 	Copyright (C) 2008, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.util.mail.folder;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

import com.codeglide.interfaces.xmldb.DbContainerNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.interfaces.xmldb.Transaction;
import com.codeglide.xml.dom.DynamicElement;

//Class modeling a POP3 folder, wich is a virtual folder indeed because POP3 allows only one folder: INBOX (see also POP3InboxFolderElement class)
public class POP3FolderElement extends FolderElement {
	protected POP3FolderElement(DynamicElement node) {
		super(node);
	}

	//Static funtions to access the wrapped DynamicElement
	static protected DynamicElement CreateDynamicElement(DynamicElement parent,String name) {
		DynamicElement child = new DynamicElement(parent.getDocumentNode(),"Folder");
		child.setAttribute("Name",name);
		child.setAttribute("_Container",1);
		return (DynamicElement)parent.appendChild(child);
	}

	//Add child
	protected FolderElement _appendDynamicElementFolderChild(DynamicElement child) {
		//Verify folder does not exist
		if (subfolders.get(GetName(child)) != null)
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Folder " + this.getName() + " already has a subfolder named " + GetName(child));
		//This is a virtual folder, so simply add node
		DynamicElement clone = (DynamicElement)node.appendChild(child.cloneNode(true));
		return this._appendChild(new POP3FolderElement(clone));
	}
	
	protected MailElement _appendDynamicElementMailChild(DynamicElement child) throws Exception {
		//This is a virtual folder, so simply add node 
		DynamicElement clone = (DynamicElement)node.appendChild((DynamicElement)child.cloneNode(true));
		return this._appendChild(new VirtualMailElement(clone));
	}
	
/*
	protected Node _appendDynamicElementChild(DynamicElement child) {
		if (node instanceof DbContainerNode && child instanceof DbContainerNode)
			//Since both nodes are already in DB, we can just move child instead of insert it
			((DbContainerNode)node).move(child);
		else
			child = (DynamicElement)node.appendChild(child);
		return this._appendChild(new POP3FolderElement(child));
	}

	protected Node _appendIMAPFolderElementChild(IMAPFolderElement child) throws MessagingException {
		if (node instanceof DbContainerNode && child.node instanceof DbContainerNode)
			//Since both nodes are already in DB, we can just move child instead of insert it
			((DbContainerNode)node).move((DbContainerNode)child.node);
		else
			child.node = (DynamicElement)node.appendChild(child.node);	//Node may change after insertion
		if (child.getParentNode() != null)
			child.getParentNode().removeChild(child);
		try {
			//Delete folders in imap server
			child.folder.delete(true);
		} catch (MessagingException e) {
			//TODO ROLLBACK EN DBNODE
			throw e;
		}
		return this._appendChild(new POP3FolderElement(child.node));
	}

	protected Node _appendPOP3FolderElementChild(POP3FolderElement child) {	
		if (node instanceof DbContainerNode && child.node instanceof DbContainerNode)
			//Since both nodes are already in DB, we can just move child instead of insert it
			((DbContainerNode)node).move((DbContainerNode)child.node);
		else
			child.node = (DynamicElement)node.appendChild(child.node);	//Node may change after insertion
		return this._appendChild(child);
	}
*/
	public void synchronizeMails() throws MessagingException, IOException {
		mails.clear();
		for(DynamicElement node : this.loadMails())
			this._appendChild(new VirtualMailElement(node));
	}

	public void synchronizeFolders() {
		NodeList children = node.getChildNodes();
		for (int i = 0;i < children.getLength();i++)
			this._appendChild(new POP3FolderElement((DynamicElement)children.item(i)));
	}

	public void delete() {
		container.getNode().removeChild(node);
	}

	public void expunge(Message message) throws MessagingException {
		//NO-OP. Not needed
	}

	protected FolderElement _move(FolderElement child) {
		return child.moveToPOP3FolderElement(this);
	}
	
	protected FolderElement moveToPOP3FolderElement(POP3FolderElement destiny) {
		//A (virtual) pop 3 folder moving to another pop 3 folder
		if (destiny.getNode() instanceof DbContainerNode)
			node = ((DbContainerNode)destiny.getNode()).move(node);
		else {
			DynamicElement _node = (DynamicElement)destiny.getNode().appendChild(node);
			container.getNode().removeChild(node);
			node = _node;
		}		
		container._removeChild(this);
		destiny._appendChild(this);
		return this;
	}
	
	protected FolderElement moveToIMAPFolderElement(IMAPFolderElement destiny) {
		return null;
	}

	protected MailElement _move(MailElement child) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}
}

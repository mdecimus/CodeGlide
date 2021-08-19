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
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Logger;
import com.codeglide.xml.dom.DynamicElement;
import com.sun.mail.pop3.POP3Folder;

public class RootFolderElement extends FolderElement {
	public enum Protocol {IMAP,POP3};	
	private String getProvider(Protocol protocol, Boolean ssl) throws NoSuchProviderException {
		switch (protocol) {
		case IMAP:
			return ssl ? "imaps" : "imap";
		case POP3:
			return ssl ? "pop3s" : "pop3";
		default:
			throw new NoSuchProviderException("Unsupported protocol: " + protocol.name());
		}
	}
	
	//A delegate for the RootFolderElement, when protocol is IMAP
	private class IMAPRootFolderElement extends IMAPFolderElement {
		//Constructor
		public IMAPRootFolderElement() throws MessagingException {
			super(RootFolderElement.this.node,RootFolderElement.this.store.getDefaultFolder());
		}
		//Getters
		protected boolean isRootFolder() {
			return true;
		}
	}
	
	//A delegate for the RootFolderElement, when protocol is POP3
	private class POP3RootFolderElement extends POP3FolderElement {
		//Constructor
		protected POP3RootFolderElement() {
			super(RootFolderElement.this.node);
		}
		//Getters
		protected boolean isRootFolder() {
			return true;
		}
		//Connect and load folders and ensure INBOX exists
		public void initChildren() {
			try {
				boolean inboxFound = false;
				//Iterate over node's children and wrap them. There is a special case for INBOX folder
				NodeList children = node.getChildNodes();
				for (int i = 0;i < children.getLength();i++) {
					if (GetName((DynamicElement)children.item(i)).equals("INBOX")) {
						this._appendChild(new POP3InboxFolderElement((DynamicElement)children.item(i),(POP3Folder)store.getDefaultFolder().getFolder("INBOX")));
						inboxFound = true;
					} else
						this._appendChild(new POP3FolderElement((DynamicElement)children.item(i)));
				}
				//In case INBOX do not exist
				if (!inboxFound)
					this._appendChild(new POP3InboxFolderElement(POP3FolderElement.CreateDynamicElement(node,"INBOX"),(POP3Folder)store.getDefaultFolder().getFolder("INBOX")));
			} catch (DOMException e) {
				Logger.debug(e);
			} catch (MessagingException e) {
				Logger.debug(e);
			}
		}
	}
	
	private FolderElement delegate;

	//Attributes
	private Store store;
	
	//Constructor
	public RootFolderElement(DynamicElement node) {
		super(node);
	}
	
	//Accessors
	public FolderElement getContainer() {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Root folder has no container");
	}
	
	protected void setContainer(FolderElement container) {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Root folder has no container");
	}
	
	protected boolean isRootFolder() {
		return true;
	}
	
	//Creates, if don't, the delegate, that is, the *real* root 
	protected FolderElement getDelegate() throws MessagingException {
		if (delegate == null) {
			//Get connection parameters
			String host = this.getAttribute("Host");
			Protocol protocol = Protocol.valueOf(this.getAttribute("Protocol").toUpperCase());
			Boolean ssl = this.getAttribute("SSL").equals("1");
			String user = this.getAttribute("User");
			String pass = this.getAttribute("Pass");
			//Use defaults for unknown parameters
			//TODO Load default parameters (from may God know where) for unspecified ones 
			//Connect to mail server
			Session session = Session.getDefaultInstance(new Properties());
			store = session.getStore(this.getProvider(protocol, ssl));
			store.connect(host,user,pass);
			//Init folder according protocol
			switch (protocol) {
			case IMAP:
				delegate = new IMAPRootFolderElement();
				break;
			case POP3:
				delegate = new POP3RootFolderElement();
				break;
			}		
		}
		return delegate;
	}
	
	public void delete() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Root folder can not be deleted");
	}
	
	protected FolderElement moveToPOP3FolderElement(POP3FolderElement destiny) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Root folder can not be moved");
	}
	
	protected FolderElement moveToIMAPFolderElement(IMAPFolderElement destiny) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Root folder can not be moved");
	}
	
	//Metheds below are just fowarded to the delegate 
	public void synchronizeMails() throws MessagingException, IOException {
		this.getDelegate().synchronizeMails();
	}
	
	public void synchronizeFolders() throws MessagingException {
		this.getDelegate().synchronizeFolders();
	}
	
	public Collection<FolderElement> getFolders() {
		try {
			return this.getDelegate().getFolders();
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}

	public Collection<MailElement> getMails() {
		try {
			return this.getDelegate().getMails();
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}

	public List<Node> getChildren() throws DOMException {
		try {
			return this.getDelegate().getChildren();
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
	
	public Node getChildNode(String name) throws DOMException {
		try {
			return this.getDelegate().getChildNode(name);
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
	
	protected void initChildren() {
		try {
			this.getDelegate().initChildren();
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
	
	public Node appendChild(Node node) {
		try {
			return this.getDelegate().appendChild(node);
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
	
	public Node _appendChild(Node node) {
		try {
			return this.getDelegate()._appendChild(node);
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}

	protected FolderElement _appendDynamicElementFolderChild(DynamicElement child) throws Exception {
		return this.getDelegate()._appendDynamicElementFolderChild(child);
	}
	
	protected MailElement _appendDynamicElementMailChild(DynamicElement child) throws Exception {
		return this.getDelegate()._appendDynamicElementMailChild(child);
	}

	public Node removeChild(Node node) {
		try {
			return this.getDelegate().removeChild(node);
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
	
	public Node _removeChild(Node node) {
		try {
			return this.getDelegate()._removeChild(node);
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}

	public void expunge(Message message) throws MessagingException {
		this.getDelegate().expunge(message);	
	}

	protected FolderElement _move(FolderElement child) throws DOMException {
		try {
			return this.getDelegate()._move(child);
		} catch (Exception e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}

	protected MailElement _move(MailElement child) throws DOMException {
		try {
			return this.getDelegate()._move(child);
		} catch (MessagingException e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
}
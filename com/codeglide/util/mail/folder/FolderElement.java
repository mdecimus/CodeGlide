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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.interfaces.xmldb.DbContainerNode;
import com.codeglide.xml.dom.DynamicElement;

//Class modeling a mail folder on memory. Contains a reference to its dbnode
public abstract class FolderElement extends DynamicElement implements Iterable<FolderElement> {
	protected FolderElement container;
	protected DynamicElement node;
	final protected Map<String,FolderElement> subfolders = new HashMap<String,FolderElement>();
	final protected List<MailElement> mails = new LinkedList<MailElement>();
	
	protected FolderElement(DynamicElement node) {
		super(node.getDocumentNode(),"FolderElement");
		this.node = node;
		// We need to initialize children before using
		nodeFlags |= NEEDS_CHILD_INIT;
	}
	
	//Static funtions to access the wrapped DynamicElement
	public static String GetName(DynamicElement node) {
		return node.getAttribute("Name");
	}
	
	private static boolean IsContainer(DynamicElement node) {
		return node.getAttribute("_Container") != null;
	}
	
	//Accessors
	protected boolean isRootFolder() {
		return false;
	}
	
	public String getName() {
		return GetName(node);
	}
	
	public DynamicElement getNode() {
		return node;
	}
	
	public void setNode(DynamicElement node) {
		this.node = node;
	}
	
	public FolderElement getContainer() {
		return container;
	}
	
	protected void setContainer(FolderElement container) {
		this.container = container;
	}
	
	//Iterable interface, just one method
	public Iterator<FolderElement> iterator() {
		//Use an annonimous class to represent the iterator
		return new Iterator<FolderElement>() {
			//Creates the queue...
			final private Queue<FolderElement> queue = new LinkedList<FolderElement>();
			//...and initializes it with the subtree's root
			{
				queue.add(FolderElement.this);
			}
			//Just return the first folder, adding its children to the end of the queue, so higher level folders are processed before lower ones
			public FolderElement next() {
				FolderElement folder = queue.poll();
				List<Node> children = folder.getChildren();
				if (children != null)
					for (Node child : children)
						if (child instanceof FolderElement)
								queue.add((FolderElement)child);
				return folder;
			}
			
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			public void remove() {
				throw new UnsupportedOperationException();			
			}		
		};
	}
	
	//Children management
	protected void initChildren() {		
		try {
			this.synchronizeFolders();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	//Synchronizing functions
	abstract public void synchronizeMails() throws MessagingException, IOException;
	abstract public void synchronizeFolders() throws MessagingException;

	protected List<DynamicElement> loadMails() {
		List<DynamicElement> nodes = new LinkedList<DynamicElement>();
		if (node instanceof DbContainerNode)
			for (Node leaf : ((DbContainerNode)node).getLeaves())
				nodes.add((DynamicElement)leaf);
		return nodes;
	}
	
	public Collection<FolderElement> getFolders() {
		return subfolders.values();
	}

	public Collection<MailElement> getMails() {
		return mails;
	}

	//Chooses the best way to append the child, acording its type
	protected MailElement _appendChild(MailElement child) {
		mails.add(child);
		child.setContainer(this);
		return child;
	}
	
	protected FolderElement _appendChild(FolderElement child) {
		subfolders.put(child.getName(),child);
		child.setContainer(this);
		super._appendChild(child);
		return child;
	}
	
	public Node appendChild(Node child) throws DOMException {
		if (this == child)
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,"A node may no be appended to its self");
		try {
			//Delayed initialization
			if((nodeFlags & NEEDS_CHILD_INIT) != 0) {
				nodeFlags &= ~NEEDS_CHILD_INIT;
				this.initChildren();
			}
			Node newchild;
			//Determining type
			if (child instanceof FolderElement)
				newchild = this._appendFolderElementChild((FolderElement)child);
			else if (child instanceof MailElement)
				newchild = this._appendMailElementChild((MailElement)child);
			else if (child instanceof DynamicElement) {
				if (IsContainer((DynamicElement)child))
					newchild = this._appendDynamicElementFolderChild((DynamicElement)child);
				else
					newchild = this._appendDynamicElementMailChild((DynamicElement)child);
			} else
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,"A " + child.getClass().getName() + " cat not be append to a FolderElement");
			//Tracking
			if((nodeFlags & NO_TRACKING) == 0)
				this.trackChange();
			return newchild;
		} catch (Exception e) {
			DOMException e2 = new DOMException(DOMException.INVALID_ACCESS_ERR,"Error appending child");
			e2.initCause(e);
			throw e2;
		}
	}
	
	protected abstract FolderElement _appendDynamicElementFolderChild(DynamicElement child) throws Exception;
	protected abstract MailElement _appendDynamicElementMailChild(DynamicElement child) throws Exception;

	protected FolderElement _appendFolderElementChild(FolderElement child) throws Exception {
		return this._appendDynamicElementFolderChild(child.getNode());
	}
	
	protected MailElement _appendMailElementChild(MailElement child) throws Exception {
		return this._appendDynamicElementMailChild(child.getNode());
	}
	
	//Child removing
	protected MailElement _removeChild(MailElement child) {
		mails.remove(child);
		return child;
	}
	
	protected FolderElement _removeChild(FolderElement child) {
		subfolders.remove(child.getName());
		super._removeChild(child);
		return child;
	}
	
	public Node removeChild(Node child) throws DOMException {
		try {
			Node removedchild;
			//Determining type
			if (child instanceof FolderElement && ((FolderElement)child).getContainer() == this)
				removedchild = this._removeFolderElementChild((FolderElement)child);
			else if (child instanceof MailElement && ((MailElement)child).getContainer() == this)
				removedchild = this._removeMailElementChild((MailElement)child);
			else if (child instanceof DynamicElement && child.getParentNode() == node) {
				if (IsContainer((DynamicElement)child))
					removedchild = this._removeDynamicElementFolderChild((DynamicElement)child);
				else
					removedchild = this._removeDynamicElementMailChild((DynamicElement)child);
			} else
				removedchild = null;
			//Tracking
			if((nodeFlags & NO_TRACKING) == 0)
				this.trackChange();
			return removedchild;
		} catch (Exception e) {
			DOMException e2 = new DOMException(DOMException.INVALID_ACCESS_ERR,"Error removing child");
			e2.initCause(e);
			throw e2;
		}
	}
	
	private FolderElement _removeDynamicElementFolderChild(DynamicElement child) throws Exception {
		FolderElement subfolder = subfolders.get(GetName(child));
		if (subfolder == null)
			return null;
		else
			return this._removeFolderElementChild(subfolder);
	}
	
	private MailElement _removeDynamicElementMailChild(DynamicElement child) {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"Implementar _removeDynamicElementMailChild!!!");
	}
	
	private FolderElement _removeFolderElementChild(FolderElement child) throws Exception {
		child.delete();
		this._removeChild(child);
		return child;
	}
	
	private MailElement _removeMailElementChild(MailElement child) throws Exception {
		child.delete();
		this._removeChild(child);
		return child;
	}
	
	//Child moving
/*
	public Node move(Node child) throws DOMException {
		if (this == child)
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,"A node may no be move to its self");
		try {
			//Delayed initialization
			if((nodeFlags & NEEDS_CHILD_INIT) != 0) {
				nodeFlags &= ~NEEDS_CHILD_INIT;
				this.initChildren();
			}
			Node movedchild;
			//Determining type
			if (child instanceof FolderElement)
				movedchild = this._moveFolderElement((FolderElement)child);
			else if (child instanceof MailElement)
				movedchild = this._moveMailElement((MailElement)child);
			else
				movedchild = this.appendChild(child);
			//Tracking
			if((nodeFlags & NO_TRACKING) == 0)
				this.trackChange();
			return movedchild;
		} catch (Exception e) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,e.getMessage());
		}
	}
*/
	public FolderElement move(FolderElement child) throws DOMException {
		if (this == child)
			//This may be extended to check child is not an ancestor of this
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,"A node may no be move to its self");
		try {
			//Delayed initialization
			if((nodeFlags & NEEDS_CHILD_INIT) != 0) {
				nodeFlags &= ~NEEDS_CHILD_INIT;
				this.initChildren();
			}
			FolderElement moved = this._move(child);
			//Tracking
			if((nodeFlags & NO_TRACKING) == 0)
				this.trackChange();
			return moved;
		} catch (Exception e) {
			DOMException e2 = new DOMException(DOMException.INVALID_ACCESS_ERR,"Error moving folder");
			e2.initCause(e);
			throw e2;
		}
	}
	
	protected abstract FolderElement _move(FolderElement child) throws Exception;
	protected abstract FolderElement moveToPOP3FolderElement(POP3FolderElement destiny);
	protected abstract FolderElement moveToIMAPFolderElement(IMAPFolderElement destiny);

	public MailElement move(MailElement child) throws DOMException {
		try {
			//Delayed initialization
			if((nodeFlags & NEEDS_CHILD_INIT) != 0) {
				nodeFlags &= ~NEEDS_CHILD_INIT;
				this.initChildren();
			}
			MailElement movedchild = this._move(child);
			//Tracking
			if((nodeFlags & NO_TRACKING) == 0)
				this.trackChange();
			return movedchild;
		} catch (Exception e) {
			DOMException e2 = new DOMException(DOMException.INVALID_ACCESS_ERR,"Error moving mail");
			e2.initCause(e);
			throw e2;
		}
	}
	
	protected abstract MailElement _move(MailElement child) throws DOMException;

	//Printing
	public String toString() {
		return this.toString("",true);
	}

	private String toString(String prefix,boolean last) {
		String txt = prefix + ((prefix.isEmpty())?"":"|_") + GetName(node) + " [" + node.getNodeName();
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0;i < attributes.getLength();i++) {
			txt += i==0 ? " -> " : ",";
			txt += attributes.item(i).getNodeName() + " = ";
			txt += attributes.item(i).getNodeName().equals("Bin") ? "..." : attributes.item(i).getNodeValue();
		}
		txt += "]\n";
		NodeList children = this.getChildNodes();
		for (int i = 0;i < children.getLength();i++)
			txt += ((FolderElement)children.item(i)).toString(prefix + (last ? "  " : "| "),i == children.getLength() - 1);
		return txt;
	}

	public String nodeToString() {
		return this.nodeToString(node,"",true);
	}

	private String nodeToString(Node node,String prefix,boolean last) {
		String txt = prefix + ((prefix.isEmpty())?"":"|_") + node.getNodeName() + " [";
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0;i < attributes.getLength();i++) {
			txt += i==0 ? "" : ",";
			txt += attributes.item(i).getNodeName() + " = ";
			txt += attributes.item(i).getNodeName().equals("Bin") ? "..." : attributes.item(i).getNodeValue();
		}
		txt += "]\n";
		NodeList children = node.getChildNodes();
		for (int i = 0;i < children.getLength();i++)
			txt += this.nodeToString(children.item(i),prefix + (last?"  ":"| "),i == children.getLength() - 1);
		return txt;
	}

	public abstract void delete() throws Exception;

	public abstract void expunge(Message message) throws MessagingException;
}

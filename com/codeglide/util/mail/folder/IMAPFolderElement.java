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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;

import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

import com.codeglide.interfaces.xmldb.DbContainerNode;
import com.codeglide.interfaces.xmldb.DbLeafNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.interfaces.xmldb.Transaction;
import com.codeglide.xml.dom.DynamicElement;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;

//Class modeling a IMAP mail folder that is mapped to a folder in the IMAP server
public class IMAPFolderElement extends FolderElement {
	final protected Folder folder;
	//Creates an imap hierarchy analogous to tree, starting beloww parent folder
	private static Folder CreateIMAPHierarchy(DynamicElement tree, Folder parent) throws MessagingException {
		//Creates a imap folder for tree's root node, then iterate and continue recursively 
		IMAPFolder folder = CreateIMAPFolder(parent,tree);			
		try {
			NodeList children = tree.getChildNodes();
			for (int i = 0;i < children.getLength();i++)
				CreateIMAPHierarchy((DynamicElement)children.item(i),folder);
			return folder;
		} catch (MessagingException e) {
			//Delete the folder (recursively) before propagate the exception 
			folder.delete(true);
			throw e;
		}
	}
	
	//Creates an imap subfolder of "folder"
	private static IMAPFolder CreateIMAPFolder(Folder parent, DynamicElement node) throws MessagingException {		
		if ((parent.getType() & Folder.HOLDS_FOLDERS) == 0)
			throw new MessagingException("Folder '" + parent.getFullName() + "' does not supports subfolders");
		//Look for the named folder. Fail if exist, proceed if  don't
		String path = (parent.getParent() == null ? "" : parent.getFullName() + "/") + GetName(node);
		IMAPFolder subfolder = (IMAPFolder)parent.getStore().getFolder(path);
		if (subfolder.exists())
			throw new MessagingException("Folder '" + subfolder.getFullName() + "' already exists");
		//Create folder acording its type. Type is 0 (zero) if was not specified
		int type = GetFolderType(node);
		if (type == 0)
			//By default, create the folder to contain both messages and folders. If server does not allow it, we create it just for folders
			try {
				if (!subfolder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS))
					throw new MessagingException("Folder '" + parent.getFullName() + "' prevented from creating subfolder '" + subfolder.getName() + "'");
			} catch (MessagingException e) {	//"create" will throw a MessagingException wrapping a ProtocolException if does not support mails + folders
				Exception ne = e.getNextException();
				if (ne != null && ne instanceof ProtocolException) {
					if (!subfolder.create(Folder.HOLDS_FOLDERS))	//Try with folders only
						throw new MessagingException("Folder '" + parent.getFullName() + "' prevented from creating subfolder '" + subfolder.getName() + "'");
				} else
					throw e;
			}
		else if (!subfolder.create(type))
			throw new MessagingException("Folder '" + parent.getFullName() + "' prevented from creating subfolder '" + subfolder.getName() + "'");
		return subfolder;
	}
	
	//Creates an message in imap server on "folder"
	private void appendMessage(Message message) throws MessagingException {		
		if ((folder.getType() & Folder.HOLDS_MESSAGES) == 0)
			throw new MessagingException("Folder '" + folder.getFullName() + "' does not supports mails");
		folder.appendMessages(new Message[] {message});
	}
/*
	private static void MoveIMAPFolder(Folder newparent,Folder target) throws MessagingException {		
		if (target.getStore() != newparent.getStore())
			throw new MessagingException("Folders '" + target.getFullName() + "' and '" + newparent.getFullName() + "' does not reside in the same server");
		if ((newparent.getType() & Folder.HOLDS_FOLDERS) == 0)
			throw new MessagingException("Folder '" + newparent.getFullName() + "' does not supports subfolders");
		//Look for the named folder. Fail if exist, proceed if  don't
		String path = (newparent.getParent() == null ? "" : newparent.getFullName() + "/") + target.getName();
		IMAPFolder newfolder = (IMAPFolder)newparent.getStore().getFolder(path);
		if (newfolder.exists())
			throw new MessagingException("Folder '" + newfolder.getFullName() + "' already exists");
		//Rename the origin folder
		if (!target.renameTo(newfolder))
			throw new MessagingException("Unable to rename '" + target.getFullName() + "' to '" + newfolder.getName() + "'");
	}
*/
	//Static funtions to access the wrapped DynamicElement
	protected static DynamicElement CreateDynamicElement(DynamicElement parent, IMAPFolder folder) throws MessagingException {
		DynamicElement child = new DynamicElement(parent.getDocumentNode(),"Folder");
		child.setAttribute("Name",folder.getName());
		child.setAttribute("_Container",1);
		//UID validity is defined only if folder holds messages
		if ((folder.getType() & Folder.HOLDS_MESSAGES) > 0)
			SetUIDValidity(child,folder.getUIDValidity());
		return (DynamicElement)parent.appendChild(child);
	}
	
	public static String GetUIDValidity(DynamicElement node) {
		return node.getAttribute("UIDValidity");
	}
	
	public static void SetUIDValidity(DynamicElement node,long uidValidity) {
		node.setAttribute("UIDValidity",uidValidity);
	}
	
	private static int GetFolderType(DynamicElement child) {
		int hmessages = child.getAttribute("_HOLDS_MESSAGES") != null ? Folder.HOLDS_MESSAGES : 0;
		int hfolders = child.getAttribute("_HOLDS_FOLDERS") != null ? Folder.HOLDS_FOLDERS : 0;
		return hmessages | hfolders;
	}
	
	//Constructor
	public IMAPFolderElement(DynamicElement node,Folder folder) {
		super(node);
		this.folder = folder;
	}

	//Add child
	protected FolderElement _appendDynamicElementFolderChild(DynamicElement child) throws Exception {
		//Clone and insert node, then create folders
		IMAPFolderElement subfolder;
		if (node instanceof DbContainerNode) {
			//Since node is in db, we may use a transaction to rollback if imap folder creation fails
			boolean localTransaction = false;
			if (Transaction.getActive() == null) {
				//Create a transaction for this operation only
				Transaction.create();
				localTransaction = true;
			}
			DynamicElement clone = (DynamicElement)((DbContainerNode)node).appendChild((DynamicElement)child.cloneNode(true));
			try {
				subfolder = new IMAPFolderElement(clone,CreateIMAPHierarchy(clone,folder));
				if (localTransaction)
					Transaction.getActive().commit();
			} catch (Exception e) {
				if (localTransaction)
					Transaction.getActive().rollback();
				throw e;
			}
		} else {
			//We must remove clone from node if imap folder creation fails
			DynamicElement clone = (DynamicElement)node.appendChild((DynamicElement)child.cloneNode(true));
			try {
				subfolder = new IMAPFolderElement(clone,CreateIMAPHierarchy(clone,folder));
			} catch (Exception e) {
				node.removeChild(clone);
				throw e;
			}
		}
		return this._appendChild(subfolder);
	}
	
	protected MailElement _appendDynamicElementMailChild(DynamicElement child) throws Exception {
		//Clone and insert node, then create mail
		MailElement mail;
		if (node instanceof DbContainerNode) {
			//Since node is in db, we may use a transaction to rollback if imap mail creation fails
			boolean localTransaction = false;
			if (Transaction.getActive() == null) {
				//Create a transaction for this operation only
				Transaction.create();
				localTransaction = true;
			}
			DynamicElement clone = (DynamicElement)((DbContainerNode)node).appendChild((DynamicElement)child.cloneNode(true));
			try {
				Message message = RealMailElement.CreateMessage(clone);
				this.appendMessage(message);
				mail = new RealMailElement(clone,message);
				if (localTransaction)
					Transaction.getActive().commit();
			} catch (Exception e) {
				if (localTransaction)
					Transaction.getActive().rollback();
				throw e;
			}
		} else {
			//We must remove clone from node if imap folder creation fails
			DynamicElement clone = (DynamicElement)node.appendChild((DynamicElement)child.cloneNode(true));
			try {
				Message message = RealMailElement.CreateMessage(clone);
				this.appendMessage(message);
				mail = new RealMailElement(clone,message);
			} catch (Exception e) {
				node.removeChild(clone);
				throw e;
			}
		}
		return this._appendChild(mail);
	}
	
	public void synchronizeFolders() throws MessagingException {
		//Set up a dictionary with dynamic element's children indexed by name
		Map<String,DynamicElement> fnodes = new HashMap<String,DynamicElement>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength();i++) {
			DynamicElement fnode = (DynamicElement)children.item(i);
			fnodes.put(GetName(fnode),fnode);
		}
		if ((folder.getType() & Folder.HOLDS_FOLDERS) > 0) {
			//Look for any folder in imap server and try to match it, or create a new one
			Folder[] folders = folder.list();
			for (int i = 0;i < folders.length;i++) {
				DynamicElement fnode = fnodes.remove(folders[i].getName());
				if (fnode == null)
					fnode = IMAPFolderElement.CreateDynamicElement(node,(IMAPFolder)folders[i]);
				this._appendChild(new IMAPFolderElement(fnode,folders[i]));
			}
		}
		//Every folder remaining in "fnodes" does not exists in the imap server, so must be removed
		for (DynamicElement fnode: fnodes.values())
			node.removeChild(fnode);
	}
	
	//Mail synchronization
	public void synchronizeMails() throws MessagingException, IOException {
		mails.clear();
		//Load children of dbFolder from DB
		List<DynamicElement> nodes = this.loadMails();
		//Iterate over every mail existing server (if folder allows mails) and try match it with a database mail in "dbMails"
		if ((folder.getType() & Folder.HOLDS_MESSAGES) > 0) {
			//Folder must be open to read its messages
			folder.open(Folder.READ_ONLY);
			Message[] messages = folder.getMessages();
			//TODO: analizar la utilidad/conveniencia de hacer un folder.fetch(messages)
			//Test if folder's uidValidity is the same as last time
			String uidValidity = IMAPFolderElement.GetUIDValidity(node);
			boolean uidValid = uidValidity != null && Long.parseLong(uidValidity) == ((IMAPFolder)folder).getUIDValidity();
			if (uidValid)
				this.synchronizeByUID(nodes,messages);
			else {
				//We cannot use UID. Use message-id header
				SetUIDValidity(node,((IMAPFolder)folder).getUIDValidity());
				if (node instanceof DbNode)
					((DbNode)node).update();	//Save changes
				this.synchronizeByMessageID(nodes,messages);
			}
			folder.close(false);
		}
		//Every mail remaining in "mails" does not exists in the inbox, so must be removed
		for (DynamicElement node: nodes)
			if (node instanceof DbLeafNode)
				((DbLeafNode)node).delete();
	}
	
	public void synchronizeByUID(List<DynamicElement> nodes,Message[] messages) throws MessagingException, IOException {
		for (int i = 0;i < messages.length;i++) {
			//Lets look for message[i]
			MailElement mail = null;
			long uid = ((IMAPFolder)folder).getUID(messages[i]);
			for (DynamicElement n: nodes) {
				String _uid = MailElement.GetUID(n);
				if (_uid != null && Long.parseLong(_uid) == uid) {
					mail = new RealMailElement(n,messages[i]);
					nodes.remove(n);
					break;
				}
			}
			//If mail not found, create it
			if (mail == null)
				mail = new RealMailElement(RealMailElement.CreateNode(node,Long.toString(uid),messages[i]),messages[i]);
			else
				mail.synchronize(messages[i]);
			this._appendChild(mail);
		}
	}
	
	public void synchronizeByMessageID(List<DynamicElement> nodes,Message[] messages) throws MessagingException, IOException {
		for (int i = 0;i < messages.length;i++) {
			//Lets look for message[i]
			MailElement mail = null;
			long uid = ((IMAPFolder)folder).getUID(messages[i]);
			String[] ids = messages[i].getHeader("message-id");
			String message_id = (ids != null && ids.length > 0)?ids[0]:null;
			for (DynamicElement n: nodes) {
				String Id = MailElement.GetMessageID(n);
				if (Id != null && message_id != null && Id.equals(message_id)) {
					mail = new RealMailElement(n,messages[i]);
					nodes.remove(n);
					break;
				}
			}
			//If mail not found, create it
			if (mail == null)
				mail = new RealMailElement(RealMailElement.CreateNode(node,Long.toString(uid),messages[i]),messages[i]);
			else {
				mail.synchronize(messages[i]);
				mail.setUID(uid);
			}
			this._appendChild(mail);
		}
	}
	
	public void delete() throws Exception {
		//TODO comentar!!
		if (node instanceof DbNode) {
			boolean localTransaction = false;
			if (Transaction.getActive() == null) {
				//Create a transaction for this operation only
				Transaction.create();
				localTransaction = true;
			}
			try {
				((DbNode)node).delete();
				if (folder.delete(true)) {
					((DynamicElement)node.getParentNode())._removeChild(node);
					if (localTransaction)
						Transaction.getActive().commit();
				} else
					throw new MessagingException("Unable to delete folder from IMAP server");
			} catch (MessagingException e) {
				if (localTransaction)
					Transaction.getActive().rollback();
				throw e;
			}
		} else {
			try {				
				node.getParentNode().removeChild(node);
				if (!folder.delete(true))
					throw new MessagingException("Unable to delete folder from IMAP server");
			} catch (MessagingException e) {
				node.getParentNode().appendChild(node);
				throw e;
			}
		}
	}

	public void expunge(Message message) throws MessagingException {
		folder.open(Folder.READ_WRITE);
		message.setFlag(Flag.DELETED,true);
		folder.close(true);
	}
/*	
	protected FolderElement _move(FolderElement child) {
		return child.moveToIMAPFolderElement(this);
	}
*/
	//Add child
	protected FolderElement _move(FolderElement child) throws Exception {
		//TODO comentar!!!!!
		IMAPFolderElement subfolder;
		if (node instanceof DbContainerNode) {
			boolean localTransaction = false;
			if (Transaction.getActive() == null) {
				//Create a transaction for this operation only
				Transaction.create();
				localTransaction = true;
			}
			DynamicElement moved = ((DbContainerNode)node).move(child.getNode());
			try {
				child.getContainer().removeChild(child);
				//TODO optimizar para que haga rename si esta en el mismo repositorio				
				subfolder = new IMAPFolderElement(moved,CreateIMAPHierarchy(moved,folder));
				if (localTransaction)
					Transaction.getActive().commit();
			} catch (Exception e) {
				if (localTransaction)
					Transaction.getActive().rollback();
				throw e;
			}
		} else {
			//We must remove clone from node if imap folder creation fails
			DynamicElement moved = (DynamicElement)node.appendChild(child.getNode());
			try {
				child.getContainer().removeChild(child);
				//TODO optimizar para que haga rename si esta en el mismo repositorio
				subfolder = new IMAPFolderElement(moved,CreateIMAPHierarchy(moved,folder));
			} catch (Exception e) {
				node.removeChild(moved);
				throw e;
			}
		}
		return this._appendChild(subfolder);
	}

	protected FolderElement moveToPOP3FolderElement(POP3FolderElement destiny) {
		return null;
	}
	
	protected FolderElement moveToIMAPFolderElement(IMAPFolderElement destiny) {
		return null;
	}

	protected MailElement _move(MailElement child) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}
}
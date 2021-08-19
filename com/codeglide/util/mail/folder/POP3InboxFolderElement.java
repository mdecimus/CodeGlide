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
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;

import org.w3c.dom.DOMException;

import com.codeglide.interfaces.xmldb.DbLeafNode;
import com.codeglide.xml.dom.DynamicElement;
import com.sun.mail.pop3.POP3Folder;

//Class modeling the POP3 Inbox folder that is mapped to the inbox in the POP3 server 
public class POP3InboxFolderElement extends POP3FolderElement {
	final private POP3Folder inbox;

	public POP3InboxFolderElement(DynamicElement node, POP3Folder inbox) throws MessagingException {
		super(node);
		this.inbox = inbox;
	}
	
	public void synchronizeMails() throws MessagingException, IOException {
		mails.clear();
		//Load children of dbFolder from DB
		List<DynamicElement> nodes = this.loadMails();
		//Iterate over every mail existing server and try match it with a database mail
		inbox.open(Folder.READ_ONLY);
		Message[] messages = inbox.getMessages();
		//TODO: analizar la utilidad/conveniencia de hacer un folder.fetch(messages)
		for (int i = 0;i < messages.length;i++) {
			MailElement mail = null;
			String uid = inbox.getUID(messages[i]);
			if (uid != null) {
				//We may match mails by UID
				for (DynamicElement n: nodes) {
					String _uid = MailElement.GetUID(n);
					if (_uid != null && _uid.equals(uid)) {
						mail = new RealMailElement(n,messages[i]);
						nodes.remove(n);
						break;	//Optimization, because uids are unique
					}
				}
			} else {
				//We cannot use UID. Use message-id header
				String message_id = MailElement.GetHeader(messages[i],"message-id");
				for (DynamicElement n: nodes) {
					String Id = MailElement.GetMessageID(n);
					if (Id != null && message_id != null && Id.equals(message_id)) {
						mail = new RealMailElement(n,messages[i]);
						nodes.remove(n);
						break;	//Optimization, because mail ids are unique
					}
				}
			}
			//Update the mail flags if it exists, or create a new one
			if (mail == null)
				mail = new RealMailElement(RealMailElement.CreateNode(node,uid,messages[i]),messages[i]);
			else
				mail.synchronize(messages[i]);
			this._appendChild(mail);
		}
		inbox.close(false);
		//Every mail remaining in "mails" does not exists in the inbox, so must be removed
		for (DynamicElement node: nodes)
			if (node instanceof DbLeafNode)
				((DbLeafNode)node).delete();
	}

	//Add child
	protected MailElement _appendDynamicElementMailChild(DynamicElement child) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"POP3 protocol does not allow mail appending");
	}
	
	public void delete() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"INBOX folder can not be deleted");
	}
	
	protected FolderElement moveToPOP3FolderElement(POP3FolderElement destiny) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"INBOX folder can not be moved");
	}
	
	protected FolderElement moveToIMAPFolderElement(IMAPFolderElement destiny) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"INBOX folder can not be moved");
	}
	
	public void expunge(Message message) throws MessagingException {
		inbox.open(Folder.READ_WRITE);
		message.setFlag(Flag.DELETED,true);
		inbox.close(true);
	}
	
	protected MailElement _move(MailElement child) throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR,"POP3 protocol does not allow mail appending");
	}
}

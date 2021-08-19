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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Logger;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.util.ISO8601;
import com.codeglide.util.StreamDataSource;
import com.codeglide.util.TemporaryInputStream;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public abstract class MailElement extends DynamicElement {
	protected DynamicElement node;
	protected FolderElement container;
	
	//Access to mail part headers
	public static String GetHeader(Part part,String header) throws MessagingException {
		String[] values = part.getHeader(header);
		return (values != null && values.length > 0)?values[0]:null;
	}

	//Constructor
	public MailElement(Document document) {
		super(document,"MailElement");
	}
/*
	public Message exportTo(Folder folder) throws MessagingException {
		//Create and fill message
		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		//-Set properties
		message.addHeader("message-id",this.getMessageID());
		message.setSubject(this.getSubject());
		message.setSentDate(this.getSentDate());
		message.addHeader("importance",this.getImportance());
		message.addHeader("sensitivity",this.getSensitivity());
		//-Set flags
		message.setFlag(Flag.FLAGGED,this.getFlag("FlagFlagged"));
		message.setFlag(Flag.SEEN,this.getFlag("FlagSeen"));
		message.setFlag(Flag.DRAFT,this.getFlag("FlagDraft"));
		message.setFlag(Flag.ANSWERED,this.getFlag("FlagAnswered"));
		message.setFlag(Flag.DELETED,this.getFlag("FlagDeleted"));
		//-Set addresses
		DynamicElement addresses = (DynamicElement) node.getChildNode("Addresses");
		this.setAddresses(message,addresses == null ? null : addresses.getChildren());
		//-Set headers
		DynamicElement headers = (DynamicElement) node.getChildNode("Headers");
		this.setHeaders(message,headers == null ? null : headers.getChildren());
		//-Add mail body and attachments 
		DynamicElement content = (DynamicElement) node.getChildNode("Content");
		DynamicElement attachments = (DynamicElement) node.getChildNode("Attachments");		
		this.setBody(message,content == null ? null : content.getChildren(),attachments == null ? null : attachments.getChildren());
		return message;
	}
	
	private void setAddresses(Message message,List<Node> addresses) {
		if (addresses != null) {
			List<Address> replyTo = new ArrayList<Address>();
			for (Node _address : addresses) {
				DynamicElement address = (DynamicElement)_address;
				try {
					InternetAddress iaddress = new InternetAddress();
					iaddress.setAddress(address.getAttribute("Addr"));
					String name = address.getAttribute("Name");
					if (name != null)
						iaddress.setPersonal(name);
					String type = address.getAttribute("Type");
					if(type.equals("from"))
						message.setFrom(iaddress);
					else if(type.equals("to"))
						message.addRecipient(Message.RecipientType.TO, iaddress);
					else if(type.equals("cc"))
						message.addRecipient(Message.RecipientType.CC, iaddress);
					else if(type.equals("bcc"))
						message.addRecipient(Message.RecipientType.BCC, iaddress);
					else if(type.equals("reply-to"))
						replyTo.add(iaddress);
				} catch (MessagingException e) {
					Logger.debug(e);
				} catch (UnsupportedEncodingException e) {
					Logger.debug(e);
				}
			}
			try {
				message.setReplyTo(replyTo.toArray(new Address[replyTo.size()]));
			} catch (MessagingException e) {
				Logger.debug(e);
			}
		}
	} 
	
	private void setHeaders(Message message,List<Node> headers) {
		if (headers != null)
			for (Node _header : headers)
				try {
					DynamicElement header = (DynamicElement)_header;
					message.addHeader(header.getAttribute("Name"),header.getAttribute("Value"));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
	}
	
	private void setBody(Message message,List<Node> contents,List<Node> attachments) {
		try {
			MimeMultipart multipart = new MimeMultipart();
			this.addBodies(multipart,contents);
			this.addBodies(multipart,attachments);
			message.setContent(multipart);
		} catch (MessagingException e) {
			Logger.debug(e);
		} catch (IOException e) {
			Logger.debug(e);
		}
	}

	private void addBodies(MimeMultipart multipart,List<Node> bodies) throws IOException, MessagingException {
		if (bodies != null)
			for(Node _body : bodies) {
				DynamicElement body = (DynamicElement) _body;
				MimeBodyPart part = new MimeBodyPart();
				InputStream stream = new TemporaryInputStream((InputStream)((DynamicAttr)body.getAttributeNode("Bin")).getObjectValue());
				part.setDataHandler(new DataHandler(new StreamDataSource(stream,body.getAttribute("Type"))));
				part.setHeader("content-disposition",body.getAttribute("Disposition"));
				multipart.addBodyPart(part);
			}
	}
*/
	//Accessors
	public FolderElement getContainer() {
		return container;
	}
	
	protected void setContainer(FolderElement container) {
		this.container = container;
	}
	
	public DynamicElement getNode() {
		return node;
	}
	
	public void setUID(long uid) {
		node.setAttribute("UID",uid);
	}
	
	static public String GetUID(DynamicElement node) {
		return node.getAttribute("UID");
	}
	
	public String getUID() {
		return GetUID(node);
	}
	
	static public String GetMessageID(DynamicElement node) {
		return node.getAttribute("ID");
	}
	
	public String getMessageID() {
		return GetMessageID(node);
	}
	
	public String getSubject() {
		return GetSubject(node);
	}
	
	static public String GetSubject(DynamicElement node) {
		return node.getAttribute("Subject");
	}
	
	public Date getSentDate() {
		return GetSentDate(node);
	}
	
	static public Date GetSentDate(DynamicElement node) {
		try {
			return ISO8601.parseDate(node.getAttribute("Sentdate"));
		} catch (java.text.ParseException e) {
			//Bad-formed date
			return null;
		}
	}
	
	public String getImportance() {
		return GetImportance(node);
	}

	static public String GetImportance(DynamicElement node) {
		return node.getAttribute("Importance");
	}
	
	public String getSensitivity() {
		return GetSensitivity(node);
	}
	
	static public String GetSensitivity(DynamicElement node) {
		return node.getAttribute("Sensitivity");
	}
	
	public boolean getFlag(String name) {
		return GetFlag(node,name);
	}
	
	static public boolean GetFlag(DynamicElement node,String name) {
		String value = node.getAttribute(name);
		return value != null && value.equals("1");	//Flag is true IIF Flag exists and its value is 1		
	}
	
	private boolean _setFlag(String name, boolean value) {
		if (value != this.getFlag(name)) {
			node.setAttribute(name,value);
			return true;	//Flag chanded
		} else
			return false;	//Flag remains the same
	};
	
	public abstract void delete() throws Exception;
	
	//Update mail flags
	public void synchronize(Message message) throws MessagingException {
		boolean changed = false;
		changed |= this._setFlag("FlagSeen",message.getFlags().contains(Flag.SEEN));
		changed |= this._setFlag("FlagFlagged",message.getFlags().contains(Flag.FLAGGED));
		changed |= this._setFlag("FlagAnswered",message.getFlags().contains(Flag.ANSWERED));
		if (changed && node instanceof DbNode)
			((DbNode)node).update();
	}

	//Printing
	public String toString() {
		String txt = this.getSubject() + " [" + node.getNodeName();
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0;i < attributes.getLength();i++) {
			txt += i==0 ? " -> " : ",";
			txt += attributes.item(i).getNodeName() + " = ";
			txt += attributes.item(i).getNodeName().equals("Bin") ? "..." : (attributes.item(i).getNodeValue() == null ? "[null]" : attributes.item(i).getNodeValue());
		}
		txt += "]\n";
		return txt;
	}
	
	public String nodeToString() {
		String txt = this.nodeToString(node,"",true);
		return txt;
	}
	
	public String nodeToString(Node node,String prefix,boolean last) {
		String txt = prefix + ((prefix.isEmpty())?"":"|_") + node.getNodeName() + " [";
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0;i < attributes.getLength();i++) {
			txt += (i==0 ? " -> " : ",") + this.printAttribute((DynamicAttr)attributes.item(i));
		}
		txt += "]\n";
		NodeList children = node.getChildNodes();
		for (int i = 0;i < children.getLength();i++)
			txt += this.nodeToString(children.item(i),prefix + (last?"  ":"| "),i == children.getLength() - 1);
		return txt;
	}

	private String printAttribute(DynamicAttr attr) {
		String txt = attr.getNodeName() + " = ";	
		if (attr.getObjectValue() == null)
			txt += "null";
		else if (attr.getObjectValue() instanceof InputStream) {
			txt += "[STREAM]";
/*
			InputStream stream = (InputStream) attr.getObjectValue();
			byte[] buffer = new byte[20000];
			int len = 0;
			try {
				while ((len = stream.read(buffer)) > -1)
					for (int i = 0; i < len; i++)
						txt += (char)buffer[i];
			} catch (IOException e) {
				txt += e.getMessage();
			}
*/
		} else
			txt += attr.getObjectValue();
		return txt;
	}
}

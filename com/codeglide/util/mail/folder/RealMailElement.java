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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Flags.Flag;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.interfaces.xmldb.Transaction;
import com.codeglide.util.StreamDataSource;
import com.codeglide.util.TemporaryInputStream;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class RealMailElement extends MailElement {
	private final Message message;
	
	//Convert: DynamicElement to Message
	public static Message CreateMessage(DynamicElement mail) throws MessagingException {
		//Create and fill message
		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		//-Set properties
		message.addHeader("message-id",GetMessageID(mail));
		message.setSubject(GetSubject(mail));
		message.setSentDate(GetSentDate(mail));
		message.addHeader("importance",GetImportance(mail));
		message.addHeader("sensitivity",GetSensitivity(mail));
		//-Set flags
		message.setFlag(Flag.FLAGGED,GetFlag(mail,"FlagFlagged"));
		message.setFlag(Flag.SEEN,GetFlag(mail,"FlagSeen"));
		message.setFlag(Flag.DRAFT,GetFlag(mail,"FlagDraft"));
		message.setFlag(Flag.ANSWERED,GetFlag(mail,"FlagAnswered"));
		message.setFlag(Flag.DELETED,GetFlag(mail,"FlagDeleted"));
		//-Set addresses
		DynamicElement addresses = (DynamicElement) mail.getChildNode("Addresses");
		SetAddresses(message,addresses == null ? null : addresses.getChildren());
		//-Set headers
		DynamicElement headers = (DynamicElement) mail.getChildNode("Headers");
		SetHeaders(message,headers == null ? null : headers.getChildren());
		//-Add mail body and attachments 
		DynamicElement content = (DynamicElement) mail.getChildNode("Content");
		DynamicElement attachments = (DynamicElement) mail.getChildNode("Attachments");		
		SetBody(message,content == null ? null : content.getChildren(),attachments == null ? null : attachments.getChildren());
		return message;
	}
	
	private static void SetAddresses(Message message,List<Node> addresses) {
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
	
	static private void SetHeaders(Message message,List<Node> headers) {
		if (headers != null)
			for (Node _header : headers)
				try {
					DynamicElement header = (DynamicElement)_header;
					message.addHeader(header.getAttribute("Name"),header.getAttribute("Value"));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
	}
	
	static private void SetBody(Message message,List<Node> contents,List<Node> attachments) {
		try {
			MimeMultipart multipart = new MimeMultipart();
			AddBodies(multipart,contents);
			AddBodies(multipart,attachments);
			message.setContent(multipart);
		} catch (MessagingException e) {
			Logger.debug(e);
		} catch (IOException e) {
			Logger.debug(e);
		}
	}

	static private void AddBodies(MimeMultipart multipart,List<Node> bodies) throws IOException, MessagingException {
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
	
	//Convert: Message to DynamicElement
	public static DynamicElement CreateNode(DynamicElement parent,String uid, Message message) throws MessagingException, IOException {
		//Create node and set attributes
		DynamicElement node = new DynamicElement(parent.getDocumentNode(),"Mail");
		//-UID
		if (uid != null)
			node.setAttribute("UID",uid);
		//-Message ID
		String id = GetHeader(message,"message-id");
		if (id != null)
			node.setAttribute("ID",id);
		//-Subject
		node.setAttribute("Subject",message.getSubject());
		//-Conversation
		Pattern pattern = Pattern.compile("^(?:\\w{2,3}\\:\\s*+)?(.*+)");	//Means: "Match but don't capture 2 or 3 word characters, a colon, and any number of blank spaces, if exist; then match and capture the remainder". *+ is the possesive version of the * operator
		Matcher matcher = pattern.matcher(message.getSubject());
		if (matcher.matches())
			node.setAttribute("Conversation",matcher.group(1));
		//-Sent and received dates
		Date sdate = message.getSentDate();
		if (sdate != null)
			node.setAttribute("Sentdate",sdate);
		Date rdate = message.getReceivedDate();
		if (rdate != null)
			node.setAttribute("Rcvddate",rdate);
		//-Importance
		String importance = GetHeader(message,"importance");
		if (importance != null) {
			importance = importance.toLowerCase();
			if (importance.equals("1") || importance.equals("high"))
				node.setAttribute("Importance","high");
			else if(importance.equals("2") || importance.equals("low"))
				node.setAttribute("Importance","low");
			else
				node.setAttribute("Importance","normal");
		}
		//-Sensitivity
		String sensitivity = GetHeader(message,"sensitivity");
		if (sensitivity != null) {
			sensitivity = sensitivity.toLowerCase();
			if (sensitivity.equals("personal") || sensitivity.equals("private") || sensitivity.equals("company-confidential"))
				node.setAttribute("Sensitivity",sensitivity);
		}
		//-Flags
		node.setAttribute("FlagFlagged",message.isSet(Flag.FLAGGED));
		node.setAttribute("FlagSeen",message.isSet(Flag.SEEN));
		node.setAttribute("FlagDraft",message.isSet(Flag.DRAFT));
		node.setAttribute("FlagAnswered",message.isSet(Flag.ANSWERED));
		node.setAttribute("FlagDeleted",message.isSet(Flag.DELETED));
		//-Add addresses
		DynamicElement addressesNode = (DynamicElement) node.appendChild("Addresses");
		AddAddresses(addressesNode,message.getFrom(), "from");
		AddAddresses(addressesNode,message.getRecipients(Message.RecipientType.TO), "to");
		AddAddresses(addressesNode,message.getRecipients(Message.RecipientType.CC), "cc");
		AddAddresses(addressesNode,message.getRecipients(Message.RecipientType.BCC), "bcc");
		AddAddresses(addressesNode,message.getReplyTo(), "reply-to");
		//-Add other headers
		DynamicElement headersNode = (DynamicElement) node.appendChild("Headers");
		//--Ask the message for headers other than those explicity or implicity already added
		Enumeration headers = message.getNonMatchingHeaders(new String[]{"message-id","subject","date","importance","sensitivity","from","to","cc","bcc","content-type"});
		while (headers.hasMoreElements()) {
			Header header = (Header) headers.nextElement();
			DynamicElement headerNode = (DynamicElement) headersNode.appendChild("Header");
			headerNode.setAttribute("Name", header.getName());
			headerNode.setAttribute("Value", header.getValue());
		}
		//-Add mail body and attachments 
		DynamicElement contentNode = (DynamicElement)node.appendChild("Content");
		ContentType type = new ContentType(message.getContentType());
		contentNode.setAttribute("Type",type.getBaseType());
		contentNode.setAttribute("Charset",type.getParameter("charset"));
		DynamicElement attachments = (DynamicElement)node.appendChild("Attachments");
		AddContentPart(contentNode,attachments,message);
		//-Finally, append
		return (DynamicElement)parent.appendChild(node);
	}
/*
	public RealMailElement(DynamicElement parent,String uid, Message message) throws MessagingException, IOException {
		super(parent.getDocumentNode());
		this.message = message;
		//Create node and set attributes
		this.node = new DynamicElement(parent.getDocumentNode(),"Mail");
		//-UID
		if (uid != null)
			this.node.setAttribute("UID",uid);
		//-Message ID
		String id = GetHeader(message,"message-id");
		if (id != null)
			this.node.setAttribute("ID",id);
		//-Subject
		this.node.setAttribute("Subject",message.getSubject());
		//-Conversation
		Pattern pattern = Pattern.compile("^(?:\\w{2,3}\\:\\s*+)?(.*+)");	//Means: "Match but don't capture 2 or 3 word characters, a colon, and any number of blank spaces, if exist; then match and capture the remainder". *+ is the possesive version of the * operator
		Matcher matcher = pattern.matcher(message.getSubject());
		if (matcher.matches())
			this.node.setAttribute("Conversation",matcher.group(1));
		//-Sent and received dates
		Date sdate = message.getSentDate();
		if (sdate != null)
			this.node.setAttribute("Sentdate",sdate);
		Date rdate = message.getReceivedDate();
		if (rdate != null)
			this.node.setAttribute("Rcvddate",rdate);
		//-Importance
		String importance = GetHeader(message,"importance");
		if (importance != null) {
			importance = importance.toLowerCase();
			if (importance.equals("1") || importance.equals("high"))
				this.node.setAttribute("Importance","high");
			else if(importance.equals("2") || importance.equals("low"))
				this.node.setAttribute("Importance","low");
			else
				this.node.setAttribute("Importance","normal");
		}
		//-Sensitivity
		String sensitivity = GetHeader(message,"sensitivity");
		if (sensitivity != null) {
			sensitivity = sensitivity.toLowerCase();
			if (sensitivity.equals("personal") || sensitivity.equals("private") || sensitivity.equals("company-confidential"))
				this.node.setAttribute("Sensitivity",sensitivity);
		}
		//-Flags
		this.node.setAttribute("FlagFlagged",message.isSet(Flag.FLAGGED));
		this.node.setAttribute("FlagSeen",message.isSet(Flag.SEEN));
		this.node.setAttribute("FlagDraft",message.isSet(Flag.DRAFT));
		this.node.setAttribute("FlagAnswered",message.isSet(Flag.ANSWERED));
		this.node.setAttribute("FlagDeleted",message.isSet(Flag.DELETED));
		//-Add addresses
		DynamicElement addressesNode = (DynamicElement) this.node.appendChild("Addresses");
		AddAddresses(addressesNode,message.getFrom(), "from");
		AddAddresses(addressesNode,message.getRecipients(Message.RecipientType.TO), "to");
		AddAddresses(addressesNode,message.getRecipients(Message.RecipientType.CC), "cc");
		AddAddresses(addressesNode,message.getRecipients(Message.RecipientType.BCC), "bcc");
		AddAddresses(addressesNode,message.getReplyTo(), "reply-to");
		//-Add other headers
		DynamicElement headersNode = (DynamicElement) this.node.appendChild("Headers");
		//--Ask the message for headers other than those explicity or implicity already added
		Enumeration headers = message.getNonMatchingHeaders(new String[]{"message-id","subject","date","importance","sensitivity","from","to","cc","bcc","content-type"});
		while (headers.hasMoreElements()) {
			Header header = (Header) headers.nextElement();
			DynamicElement headerNode = (DynamicElement) headersNode.appendChild("Header");
			headerNode.setAttribute("Name", header.getName());
			headerNode.setAttribute("Value", header.getValue());
		}
		//-Add mail body and attachments 
		DynamicElement contentNode = (DynamicElement)this.node.appendChild("Content");
		ContentType type = new ContentType(message.getContentType());
		contentNode.setAttribute("Type",type.getBaseType());
		contentNode.setAttribute("Charset",type.getParameter("charset"));
		DynamicElement attachments = (DynamicElement)this.node.appendChild("Attachments");
		AddContentPart(contentNode,attachments,message);
		//-Finally, append
		parent.appendChild(this.node);
	}
*/
	public RealMailElement(DynamicElement node,Message message) throws MessagingException, IOException {
		super(node.getDocumentNode());
		this.node = node;
		this.message = message;
	}
	
	//Node's getters	
	private static void AddContentPart(DynamicElement contentNode, DynamicElement attachmentsNode, Part part) throws ParseException, MessagingException, IOException {
		if (part.isMimeType("multipart/*")) {
			//Part is composed; iterate over subparts and call recursively
			Multipart multipart = (Multipart)part.getContent();
			int count = multipart.getCount();
			for (int i = 0; i < count; i++)
				AddContentPart(contentNode,attachmentsNode,multipart.getBodyPart(i));
		} else {
			//Part is a basic piece; store as mail body or attachment, acording its disposition (part is considered attachment if header absent)
			ContentType type = new ContentType(part.getContentType());
			String disposition = GetHeader(part,"content-disposition");
			if (disposition == null || disposition.startsWith("attachment")) {				
				DynamicElement file = (DynamicElement)attachmentsNode.appendChild("File");
				file.setAttribute("Type",type.getBaseType().toLowerCase());
				file.setAttribute("Name",part.getFileName());
				file.setAttribute("Bin",new TemporaryInputStream(part.getInputStream()));
				if (disposition != null)
					file.setAttribute("Disposition",disposition);
			} else {
				DynamicElement body = (DynamicElement)contentNode.appendChild("Body");
				body.setAttribute("Type",type.getBaseType().toLowerCase());
				body.setAttribute("Charset",type.getParameter("charset"));
				body.setAttribute("Bin",new TemporaryInputStream(part.getInputStream()));
				body.setAttribute("Disposition",disposition);
			}
		}
	}
	
	//Adds an address set to the adresses node of the mail node
	private static void AddAddresses(DynamicElement addressesNode,Address[] addresses,String field) throws AddressException {
		if (addresses == null)
			return;
		for (int i = 0; i < addresses.length; i++) {
			if (addresses[i] instanceof InternetAddress) {
				InternetAddress address = (InternetAddress)addresses[i];
				if (address.isGroup())
					//Add every address in the group
					AddAddresses(addressesNode,address.getGroup(false),null);
				else {
					DynamicElement addressNode = (DynamicElement)addressesNode.appendChild("Address");
					addressNode.setAttribute("Type",field);
					if (address.getPersonal() != null)
						addressNode.setAttribute("Name", address.getPersonal());
					addressNode.setAttribute("Addr", address.getAddress());
				}
			} else {
				DynamicElement addressNode = (DynamicElement)addressesNode.appendChild("Address");
				addressNode.setAttribute("Type", field);
				addressNode.setAttribute("Addr", addresses[i].toString());
			}
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
				container.expunge(message);
				if (localTransaction)
					Transaction.getActive().commit();
			} catch (MessagingException e) {
				if (localTransaction)
					Transaction.getActive().rollback();
				throw e;
			}
		} else {
			try {
				container.getNode().removeChild(node);
				container.expunge(message);
			} catch (MessagingException e) {
				container.getNode().appendChild(node);
				throw e;
			}
		}
	}
}

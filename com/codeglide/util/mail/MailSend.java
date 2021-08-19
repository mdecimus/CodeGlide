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
package com.codeglide.util.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.codeglide.core.Logger;
import com.codeglide.xml.dom.DynamicElement;


public class MailSend {
			
	public static void send(DynamicElement mail, String hostname, int port, boolean useTSL, String user, String password){
		
		// Get system Properties
		Properties properties = System.getProperties();

		// Setup properties
		properties.put("mail.smtp.host", hostname);
		properties.put("mail.smtp.auth" , "true");
	    properties.put("mail.debug", "true");
	    
	    if(port > 0)
	    	properties.put("mail.smtp.port", port);
	    
	    if(useTSL)
	    	properties.put("mail.smtp.starttls.enable", "true");
	    	
		Session session = null;
	    
		// Setup Authentication and get Session
	    if(user != null && password != null){
	    	Authenticator authenticator = (new MailSend()).new SMTPAuthenticator(user, password);
	    	session = Session.getDefaultInstance(properties, authenticator);
	    }else
	    	session = Session.getDefaultInstance(properties, null);
		
		session.setDebug(true);
		
		// Create a new Message and fill it using the exporter
		MimeMessage message = new MimeMessage(session);
		MailExport.exportMail(session, message, mail, 0, null);

		// Send the message
		try {
			Transport.send(message);
		} catch (MessagingException me) {
			me.printStackTrace();
			Logger.debug(me);
		}

		
	}
	
	public class SMTPAuthenticator extends Authenticator {
		
		private String user;
		  private String pass;
		  		  
		public SMTPAuthenticator(String user, String pass) {
			super();
			this.user = user;
			this.pass = pass;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pass);
		}	

	}
	
/*	public static void main(String[] args) {
		
		DynamicElement mail = buildXMLMail();
		//DynamicElement mail = getAMailFromMBox(); // Not Working
		
		XMLMailSender.send(mail, "smtp.gmail.com", 465, true, "pablo.fernandez.busch@gmail.com", "saradasa");
	}
	
	
	private static DynamicElement buildXMLMail() {

		DummyDocument document = new DummyDocument();
		DynamicElement mail = new DynamicElement(document, "Mail");
		document.setDocumentElement(mail);

		// Set Mail Attributes
		mail.setAttribute("Id", "1234");
		mail.setAttribute("Subject", "This is the Subject");
		mail.setAttribute("Importance", "high");
		mail.setAttribute("Sensitivity", "confidential");
		mail.setAttribute("Sentdate", new Date());
		mail.setAttribute("Rcvddate", new Date());
		mail.setAttribute("F_seen", true);
		mail.setAttribute("F_answered", true);

		// Set Addresses and their corresponding Attributes
		DynamicElement addresses = (DynamicElement) mail.appendChild("Addresses");

		DynamicElement firstAddress =  (DynamicElement) addresses.appendChild("Address");
		firstAddress.setAttribute("Name", "Pablo - Gmail");
		firstAddress.setAttribute("Addr", "pablo.fernandez.busch@gmail.com");
		firstAddress.setAttribute("Type", "from");
		
		DynamicElement secondAddress =  (DynamicElement) addresses.appendChild("Address");
		secondAddress.setAttribute("Addr", "pbusch@codeglide.com");
		secondAddress.setAttribute("Type", "to");

		DynamicElement thirdAddress =  (DynamicElement) addresses.appendChild("Address");
		thirdAddress.setAttribute("Name", "Louis Armstrong");
		thirdAddress.setAttribute("Addr", "louis@rmstrong.com");
		thirdAddress.setAttribute("Type", "reply-to");
		
		DynamicElement fourthAddress =  (DynamicElement) addresses.appendChild("Address");
		fourthAddress.setAttribute("Name", "Pablo - Gmail");
		fourthAddress.setAttribute("Addr", "pablo.fernandez.busch@gmail.com");
		fourthAddress.setAttribute("Type", "bcc");
		
		DynamicElement fifthAddress =  (DynamicElement) addresses.appendChild("Address");
		fifthAddress.setAttribute("Name", "Pablo - Gmail CC");
		fifthAddress.setAttribute("Addr", "pablo.fernandez.busch@gmail.com");
		fifthAddress.setAttribute("Type", "cc");

		// Set Other Headers
		DynamicElement headers = (DynamicElement) mail.appendChild("Headers");

		DynamicElement firstHeader = (DynamicElement) headers.appendChild("Header");
		firstHeader.setAttribute("Name", "Return-Path");
		firstHeader.setAttribute("Value", "&lt;pablo.fernandez.busch@gmail.com&gt;");

		DynamicElement secondHeader = (DynamicElement) headers.appendChild("Header");
		secondHeader.setAttribute("Name", "Received");
		secondHeader.setAttribute("Value", "from rly-yi06.mx.aol.com (rly-yi06.mail.aol.com [172.18.180.134]) by air-yi01.mail.aol.com (v118.4) with ESMTP id MAILINYI13-7d946a79cb21bc; Wed, 25 Jul 2007 14:55:56 -0400");

		DynamicElement thirdHeader = (DynamicElement) headers.appendChild("Header");
		thirdHeader.setAttribute("Name", "In-Reply-To");
		thirdHeader.setAttribute("Value", "&lt;012201c7ceb6$1bc91080$535b3180$@com&gt;");

		// Set Content
		DynamicElement content = (DynamicElement) mail.appendChild("Content");
		content.setAttribute("Charset", "iso-8859-1");
		content.setAttribute("Type", "multipart/MIXED; &#13;&#10;&#9;boundary=&quot;----=_Part_207067_16702812.1185389745935&quot;");


		DynamicElement firstBody = (DynamicElement) content.appendChild("Body");
		firstBody.setAttribute("Charset", "ISO-8859-1");
		firstBody.setAttribute("Bin", "");
		firstBody.setAttribute("Type", "text/plain");

		DynamicElement secondBody = (DynamicElement) content.appendChild("Body");
		secondBody.setAttribute("Charset", "ISO-8859-1");
		secondBody.setAttribute("Bin", "");
		secondBody.setAttribute("Type", "text/html");

		// Set Attachments
		DynamicElement attachments = (DynamicElement) mail.appendChild("Attachments");

		File image = new File("/home/user/workspace/CodeGlide/trunk/com/codeglide/interfaces/mail/res/image.jpg");

		try {
			FileInputStream fis = new FileInputStream(image);

			DynamicElement firstFile = (DynamicElement) attachments.appendChild("File");
			firstFile.setAttribute("Name", image.getName());
			firstFile.setAttribute("Type", "jpg");
			firstFile.setAttribute("Size", image.length());
			firstFile.setAttribute("Bin",  new ByteArrayInputStream ( new byte[] {'e','m','p','t','y'}));
			firstFile.setAttribute("Disposition", "attachment");
		} catch (FileNotFoundException fnfe) {
			logger.debug(fnfe);
		}

		return mail;
	}*/


}

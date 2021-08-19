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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import com.codeglide.core.Logger;
import com.codeglide.util.StringInputStream;
import com.codeglide.xml.dom.DynamicElement;

public class MailImport {

	/**
	 * @param p - Mail or part of the mail to import
	 * @param ks - KeyStore
	 * @param doc - Dom Tree's Parent Document
	 * @return The imported mail
	 * @throws KeyStoreException
	 */
	public static DynamicElement importMail( Part p, KeyStore ks, Document doc ) throws KeyStoreException {

		DynamicElement mail = new DynamicElement(doc, "Mail");

		importMail( p, ks, mail);
		return mail;
	}

	public static void importMail( Part part, KeyStore ks, DynamicElement mail ) throws KeyStoreException {

		if (part instanceof Message)
			addHeaders( (Message)part, mail );

		DynamicElement content = (DynamicElement) mail.appendChild("Content");

		try {
			content.setAttribute("Type", part.getContentType());
			ContentType ct = new ContentType( part.getContentType() );
			content.setAttribute("Charset", validateCharset(ct.getParameter("charset")));
		} catch (Exception e) {Logger.debug(e);}

		DynamicElement attach = (DynamicElement) mail.appendChild("Attachments");

		addParts( part, mail, ks );

		/*//Perform virus checking on attachments
		DynamicElement[] list = attach.getChildNodes();
		if( list != null && list.length > 0 ) {
			n.setAttribute("hasattach", "1");
			LinkedList streams = new LinkedList();
			for( int i = 0; i < list.length; i++ ) {
				if( list[i].getStream("bin") != null )
					streams.add(list[i].getStream("bin"));
			}
			if( streams.size() > 0 ) {
				boolean isInfected = false;
				Object result = Antivirus.isInfected(streams);
				String virusReport = null;
				if( result instanceof String ) {
					virusReport = (String)result;
					isInfected = true;
				} else
					isInfected = ((Boolean)result).booleanValue();
				try {
					if( isInfected ) {
						n.setAttribute("f_virii", null);
						attach.removeAttribute("file");
						if( virusReport != null ) {

							DynamicElement file = new DynamicElement()

							attach = attach.appendChild("file");
							attach.setAttribute("name", "Virus_Report.txt");
							attach.setAttribute("type", "text/plain");
							attach.setAttribute("size", String.valueOf(virusReport.length()));
							attach.setAttribute("bin", new StringInputStream(virusReport,"utf-8") );
						}
					} else {
						for (Iterator it = streams.iterator(); it.hasNext();)
							 ((InputStream) it.next()).reset();
					}
				} catch (Exception e) {
					//EventLog.debug(e);
				}
			}
		}*/

	}

	private static String validateCharset( String charset ) {
		if( charset == null || !Charset.isSupported(charset) )
			return "iso-8859-1";
		else
			return charset;
	}

	private static void addHeaders( Message m, DynamicElement n )  {

		DynamicElement n1;
		String hdr;

		// Read Message-ID
		n.setAttribute("Id", getMessageId( getHeader(m, "message-id") ) );

		// Read Subject
		try {
			String subject = m.getSubject();
			if( subject != null ) {
				n.setAttribute("Subject", subject);
				n.setAttribute("Conversation", getConverstion(subject));
			}
		} catch (Exception e) {Logger.debug(e);}

		// Read Dates
		try {
			n.setAttribute("Sentdate",m.getSentDate());
			n.setAttribute("Rcvddate",m.getReceivedDate());
		} catch (Exception e) {Logger.debug(e);}

		// Read Importance
		hdr = getHeader(m, "importance" );

		if( hdr != null ) {
			hdr = hdr.toLowerCase();

			if(hdr.equals("1")){
				n.setAttribute("Importance", "high");
			}else if(hdr.equals("2")){
				n.setAttribute("Importance", "low");
			}else if(hdr.equals("low") || hdr.equals("normal") || hdr.equals("high")){
				n.setAttribute("Importance", hdr);
			}else{
				n.setAttribute("Importance", "normal");
			}
		}

		// Read Sensitivity
		hdr = getHeader(m, "sensitivity");
		if( hdr != null ) {
			hdr = hdr.toLowerCase();
			if(hdr.equals("personal") || hdr.equals("private") || hdr.equals("confidential"))
				n.setAttribute("Sensitivity", hdr);
			else{
				n.setAttribute("Sensitivity", "normal");
			}
		}

		// Read Flag
//		hdr = getHeader(m, "x-message-flag"); // No Forward Flags...
//		if( hdr != null )
//		n.setAttribute("f_forwarded", true);

		// Read Flags' Status
		try {
			if(m.isSet(Flags.Flag.FLAGGED))
				n.setAttribute("FlagFlagged", true);
			if(m.isSet(Flags.Flag.SEEN))
				n.setAttribute("FlagSeen", true);
			if(m.isSet(Flags.Flag.DRAFT))
				n.setAttribute("FlagDraft", true);
			if(m.isSet(Flags.Flag.ANSWERED))
				n.setAttribute("FlagAnswered", true);
			if(m.isSet(Flags.Flag.DELETED))
				n.setAttribute("FlagDeleted", true);
		} catch (DOMException dome) {
			Logger.debug(dome);
		} catch (MessagingException me) {
			Logger.debug(me);
		}

		// Read Addresses
		n1 = (DynamicElement) n.appendChild("Addresses");

		try {
			addAddresses(n1, m.getFrom(), "from");
		} catch (Exception e) {Logger.debug(e);}
		try {
			addAddresses(n1, m.getRecipients(Message.RecipientType.TO), "to");
		} catch (Exception e) {Logger.debug(e);}
		try {
			addAddresses(n1, m.getRecipients(Message.RecipientType.CC), "cc");
		} catch (Exception e) {Logger.debug(e);}
		try {
			addAddresses(n1, m.getRecipients(Message.RecipientType.BCC), "bcc");
		} catch (Exception e) {Logger.debug(e);}
		try {
			addAddresses(n1, m.getReplyTo(), "reply-to");
		} catch (Exception e) {Logger.debug(e);}

		// Read additional headers
		try {
			DynamicElement otherHeadersElement = (DynamicElement) n.appendChild("Headers");

			for (Enumeration en = m.getNonMatchingHeaders( new String[] { "subject", "from", "to", "cc", "bcc", "importance", "sensitivity", "date", "message-id", "content-type" }); en.hasMoreElements(); ) {

				Header header = (Header) en.nextElement();

				DynamicElement headerElement = (DynamicElement) otherHeadersElement.appendChild("Header");
				headerElement.setAttribute("Name", header.getName());
				headerElement.setAttribute("Value", header.getValue());

				//otherHeadersElement.appendChild(headerElement);
			}

			//n.appendChild(otherHeadersElement);
		} catch (Exception e) {Logger.debug(e);}

	}

	public static String getHeader( Part m, String hdr ) {

		String[] list = null;

		try {
			list = m.getHeader(hdr);
		} catch (Exception e) {Logger.debug(e);}

		if( list == null )
			return null;
		else
			return list[0];
	}

	private static String getMessageId( String id ) {
		if( id != null && !id.equals(""))
			return id;
		return generateId("mail");
	}

	public static String generateId( String section ) {
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {Logger.debug(e);}
		return "<" + String.valueOf(System.currentTimeMillis()) + "." + section.hashCode() + "." + section + "@" + hostName + ">";
	}

	public static String getConverstion( String subject ) {
		if( subject.length() > 2 && Character.isLetter(subject.charAt(0)) && Character.isLetter(subject.charAt(1)) &&
				(subject.charAt(2) == ':' || ( subject.length() > 3 && Character.isLetter(subject.charAt(2)) && subject.charAt(3) == ':' )) ) {
			return subject.substring(subject.indexOf(':')+1).trim();
		} else
			return subject;
	}

	private static void addAddresses( DynamicElement n, Address[] a, String typeAttribute) {

		if( a == null )
			return;

		for( int i = 0; i < a.length; i++ ) {

			if( a[i] instanceof InternetAddress ) {
				InternetAddress ia = (InternetAddress) a[i];
				if( ia.isGroup() ) {
					//n1 = n.appendChild("list");
					//n1.setAttribute("type", tag );
					try {
						addAddresses( n, ia.getGroup(false), null );
					} catch (Exception e) {Logger.debug(e);}
				} else {

					DynamicElement address = (DynamicElement) n.appendChild("Address");
					address.setAttribute("Type", typeAttribute);

					if(ia.getPersonal() != null)
						address.setAttribute("Name", ia.getPersonal());

					address.setAttribute("Addr", ia.getAddress());

					//n.appendChild(address);

				}
			} else {

				DynamicElement address = (DynamicElement) n.appendChild("Address");
				address.setAttribute("Type", typeAttribute);
				address.setAttribute("Addr", a[i].toString());
			}
		}
	}

	// Add Content and Attachments
	private static void addParts( Part part, DynamicElement n, KeyStore ks ) throws KeyStoreException {

		try {

			String fileName = null;
			//HashMap<String, String> flags = new HashMap<String, String>();
			DynamicElement nb = null;
			DynamicElement content = (DynamicElement) n.getChildNode("Content");
			DynamicElement attachments = (DynamicElement) n.getChildNode("Attachments");


			fileName = part.getFileName();
			if (fileName == null && part.getContentType() != null) {
				try {
					ContentType ct = new ContentType(part.getContentType());
					fileName = ct.getParameter("name");
				} catch (Exception e) {Logger.debug(e);}
			}

			if (part.isMimeType("text/*") && fileName == null) {

				nb = (DynamicElement) content.appendChild("Body");

				try {
					ContentType ct = new ContentType(part.getContentType());
					nb.setAttribute("Type", ct.getBaseType().toLowerCase());
					nb.setAttribute("Charset", validateCharset(ct.getParameter("charset")));

				} catch (Exception e) {
					nb.setAttribute("Type", part.getContentType());
				}

				/*	} else if (p.isMimeType("application/pkcs7-mime") ||
					p.isMimeType("application/x-pkcs7-mime")) {

				String smimeClass = null;
				try {
					smimeClass = (new ContentType(p.getContentType().toLowerCase())).getParameter("smime-type");
				} catch (Exception e) {}
				if( smimeClass == null )
					smimeClass = "signed-data";

				if( smimeClass.equals("compressed-data") ) {
					try {
						if( p instanceof Message )
							flags.put("smime", "c");
						SMIMECompressed s = new SMIMECompressed((MimeMessage)p);
						addParts(SMIMEUtil.toMimeBodyPart(s.getContent()), n, ks);
					} catch( Exception e ) {
						flags.put("smimeCompressError", "several;"+e.getMessage());
					}
				} else if( smimeClass.equals("enveloped-data") ) {
					if( ks == null )
						throw new KeyStoreException();
					try {
						if( p instanceof Message )
							flags.put("smime", "e");
						SMIMEEnveloped s = new SMIMEEnveloped((MimeMessage)p);
						RecipientInformationStore recipients = s.getRecipientInfos();
						RecipientInformation recipient = null;
						Key recipientKey = null;

						for( Enumeration en = ks.aliases(); en.hasMoreElements() && recipient == null; ) {
							String alias = (String) en.nextElement();
							if( ks.isKeyEntry(alias) ) {
								X509Certificate cert = (X509Certificate)ks.getCertificate(alias);
								RecipientId recId = new RecipientId();
								recId.setSerialNumber(cert.getSerialNumber());
								recId.setIssuer(cert.getIssuerX500Principal().getEncoded());
								recipient = recipients.get(recId);
								if( recipient != null )
									recipientKey = ks.getKey(alias, null);
							}
						}
						if( recipient != null )
							addParts(SMIMEUtil.toMimeBodyPart(recipient.getContent(recipientKey, "BC")), n, ks);
						else
							flags.put("smimeEnvelopeError", "nokey");
					} catch( Exception e ) {
						flags.put("smimeEnvelopeError", "several;"+e.getMessage());
					}

				} else if( smimeClass.equals("signed-data") ) {
					try {
						if( p instanceof Message )
							flags.put("smime", "s");
						SMIMESigned s = new SMIMESigned(p);
						verifySignature(s,n,flags,ks);
						Object content = s.getContent();
						if (content instanceof MimeMultipart) {
							int count = ((MimeMultipart)content).getCount();
							for (int i = 0; i < count; i++)
								addParts(((MimeMultipart)content).getBodyPart(i), n, ks);
						} else if (content instanceof MimeBodyPart)
							addParts((MimeBodyPart) content, n, ks);
					} catch( Exception e ) {
						flags.put("smimeSignError", "several;"+e.getMessage());
					}
				}*/
			} else if (part.isMimeType("multipart/*")) {
				Multipart mp = null;
				/*if (p.isMimeType("multipart/signed")) {
					try {
						if( p instanceof Message )
							flags.put("smime", "s");
						SMIMESigned s = new SMIMESigned((MimeMultipart) p.getContent());
						verifySignature(s, n, flags,ks);
						Object content = s.getContent();
						if (content instanceof MimeMultipart)
							mp = (MimeMultipart) content;
						else if (content instanceof MimeBodyPart)
							addParts((MimeBodyPart) content, n, ks);
					} catch (Exception e) {
						flags.put("smimeSignError", "several;"+e.getMessage());
						mp = (Multipart) p.getContent();
					}
				} else*/
				mp = (Multipart) part.getContent();
				if( mp != null ) {
					try {
						int count = mp.getCount();
						for (int i = 0; i < count; i++)
							addParts(mp.getBodyPart(i), n, ks);
					} catch (MessagingException e) {

						nb = (DynamicElement) content.appendChild("Body");

						nb.setAttribute("Charset", "iso-8859-1");
						nb.setAttribute("Type", "text/plain");
						nb.setAttribute("Bin", part.getInputStream());
						nb = null;
					}
				}
			} else if (part.isMimeType("message/rfc822")) {
				attachments.appendChild(importMail((Part) part.getContent(), ks, attachments.getOwnerDocument()));
			} else {

				nb = (DynamicElement) attachments.appendChild("File");
				//attachments.appendChild(nb);

				if (fileName != null)
					nb.setAttribute("Name", MimeUtility.decodeText(fileName));
				fileName = getHeader(part, "content-id");
				if( fileName == null )
					fileName = getHeader(part, "content-location");
				if( fileName != null )
					nb.setAttribute("Id", fileName);	

				nb.setAttribute("Type", part.getContentType());
				nb.setAttribute("Size", part.getSize());
				nb.setAttribute("Disposition", part.getDisposition());

			}

//			if( flags.size() > 0 ) {
//				for( Iterator it = flags.keySet().iterator(); it.hasNext(); ) { //ASK: do we need this again over here (4 each part)?
//					String key = (String) it.next();
//					Object val = flags.get(key);
//					n.setAttribute("Flag"+key, val.toString());
//				}
//
//			}

			if (nb != null) {
				boolean setCharset = false;
				String charset = null;
				Object o = null;

				try {
					try {
						o = part.getContent();
					} catch (UnsupportedEncodingException e) {
						o = part.getInputStream();
						setCharset = true;
					}
				} catch (IOException e) {
					if( part instanceof MimeBodyPart )
						o = ((MimeBodyPart)part).getRawInputStream();
					else if( part instanceof MimeMessage )
						o = ((MimeMessage)part).getRawInputStream();
					setCharset = true;
				}

				try {
					ContentType ct = new ContentType(part.getContentType());
					charset = validateCharset(ct.getParameter("charset"));
				} catch (Exception e) {
					charset = "iso-8859-1";
				}

				InputStream i = null;
				if (o instanceof InputStream) {
					i = (InputStream) o;
				} else if (o instanceof String ){
					setCharset = true;
					try {
						i = new StringInputStream( (String)o, charset);
					} catch (Exception e) {
						charset = "utf-8";
						i = new StringInputStream( (String)o, charset);
					}
				} else if( o instanceof MimeMultipart || o instanceof MimeBodyPart )
					i = ((MimeMessage)part).getRawInputStream();

				if( setCharset )
					nb.setAttribute("Charset",charset);
				nb.setAttribute("Bin",i);
			}
		} catch (KeyStoreException e) {
			throw e;
		} catch (Exception e) {
			Logger.debug(e);
		}
	}


}

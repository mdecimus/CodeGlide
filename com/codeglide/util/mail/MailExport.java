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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.text.ParseException;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.codeglide.core.Logger;
import com.codeglide.util.ISO8601;
import com.codeglide.util.mimedir.VCard;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;

public class MailExport {

	public static final int FL_SIGN      = 0x0001;
	public static final int FL_SIGNCLEAR = 0x0002;
	public static final int FL_CRYPT     = 0x0004;
	public static final int FL_COMPRESS  = 0x0008;

	public static void exportMail( Session session, Message message, DynamicElement element, int smimeFlags, KeyStore store ) {

		//DynamicElement[] list = element.getObjects();
		String charset = null;
		MimeMultipart mimeMultipart = null;
		MimeMultipart mimeAlternative = null;
		MimeBodyPart smimePart = null;
		boolean hasBody = false, hasAltBody = false, hasAttachments = false;

		try {
			message.setHeader("Message-ID", getMessageId(element.getAttribute("Id")));
		} catch (Exception e) {
			Logger.debug(e);
		}

		// Fill Boolean values: hasBody, hasAltBody, hasAttachments
		DynamicElement content = (DynamicElement) element.getChildNode("Content");
		DummyNodeList bodiesList = (DummyNodeList) content.getChildNodes();

		charset = content.getAttribute("Charset");

		if(bodiesList != null && bodiesList.getLength() > 0){
			hasBody = true;
			if(bodiesList.getLength() > 1)
				hasAltBody = true;
			if(bodiesList.getLength() > 2) //ASK: >= otherwise the example fails and has 1 attachment!
				hasAttachments = true;
		}

		DynamicElement attachments = (DynamicElement) element.getChildNode("Attachments");
		DummyNodeList attachmentsList = (DummyNodeList) attachments.getChildNodes();

		if(attachmentsList != null && attachmentsList.getLength() > 0)
			hasAttachments = true;


		/*for( int i = 0; i < list.length; i++ ) {
			if( list[i].getName().equals("bodypart") ) {
				charset = (String)list[i].getProperty("charset");
				DynamicElement[] l = list[i].getObjects();
				if( l != null && l.length > 0 ) {
					hasBody = true;
					if( l.length > 1 )
						hasAltBody = true;
					if( l.length > 2 )
						hasAttachments = true;
				}
			} else if( list[i].getName().equals("attach") ) {
				if( list[i].getObjects() != null && list[i].getObjects().length > 0 )
					hasAttachments = true;
			}
		}*/

		if( charset == null )
			charset = new String("utf-8");
		
		session.getProperties().setProperty("mail.mime.charset", charset );

		if( hasAltBody )
			mimeAlternative = new MimeMultipart("alternative");

		if( hasAttachments )
			mimeMultipart = new MimeMultipart();

		// Set subject
		try {
			message.setSubject(element.getAttribute("Subject"));
		} catch( Exception e ) {Logger.debug(e);}

		// Set Importance
		try {
			message.setHeader("Importance", element.getAttribute("Importance"));
		} catch (MessagingException me) {
			Logger.debug(me);
		}


		// Set Sensitivity
		try {
			message.setHeader("Sensitivity", element.getAttribute("Sensitivity"));
		} catch (MessagingException me) {
			Logger.debug(me);
		}

		// Set Sent Date
		try {
			message.setSentDate(ISO8601.parseDate(element.getAttribute("Sentdate")));
		} catch (MessagingException me) {
			Logger.debug(me);
		} catch (ParseException pe) {
			Logger.debug(pe);
		}

		// Set Flags
		try {
			if(element.getAttribute("FlagFlagged") != null)
				message.setFlag(Flags.Flag.FLAGGED, true);	
			if(element.getAttribute("FlagSeen") != null)
				message.setFlag(Flags.Flag.SEEN, true);
			if(element.getAttribute("FlagDraft") != null)
				message.setFlag(Flags.Flag.DRAFT, true);
			if(element.getAttribute("FlagAnswered") != null)
				message.setFlag(Flags.Flag.ANSWERED, true);
			if(element.getAttribute("FlagDeleted") != null)
				message.setFlag(Flags.Flag.DELETED, true);
//			if(element.getAttribute("f_forward") != null){
//			// Add the forward flag where it should be.
//			}
		} catch (MessagingException me) {
			Logger.debug(me);
		}

		// Set Headers
		DynamicElement headers = (DynamicElement) element.getChildNode("Headers");

		DummyNodeList headersList = (DummyNodeList) headers.getChildNodes();

		for(int i = 0 ; i < headersList.getLength() ; i++){
			DynamicElement header = (DynamicElement) headersList.item(i);
			try {
				message.addHeader(header.getAttribute("Name"), header.getAttribute("Value"));
			} catch (MessagingException e) {
				Logger.debug(e);
			}
		}


		// Add Addresses
		DynamicElement addresses = (DynamicElement) element.getChildNode("Addresses");

		DummyNodeList addressesList = (DummyNodeList) addresses.getChildNodes();

		addAddresses(message, addressesList);

		// Set Body
		if(hasBody){

			if( bodiesList.getLength() == 1 ) {
				try {
					if (hasAttachments) {
						MimeBodyPart b = new MimeBodyPart();
						b.setDataHandler( new DataHandler( new MailDataSource((DynamicElement) bodiesList.item(0)) ) );
						mimeMultipart.addBodyPart(b);
					} else {
						if( smimeFlags != 0 ) {
							smimePart = new MimeBodyPart();
							smimePart.setDataHandler( new DataHandler( new MailDataSource((DynamicElement) bodiesList.item(0)) ) );
						} else
							message.setDataHandler( new DataHandler( new MailDataSource((DynamicElement) bodiesList.item(0)) ) );
					}
				} catch (Exception e) {
					Logger.debug(e);
				}
			} else {
				for( int j = 0; j < 2; j++ ) {

					try {
						MimeBodyPart b = new MimeBodyPart();
						b.setDataHandler( new DataHandler( new MailDataSource((DynamicElement) bodiesList.item(j)) ) );
						mimeAlternative.addBodyPart(b);
					} catch (Exception e) {
						Logger.debug(e);
					}
				}
				if( bodiesList.getLength() > 1 && hasAttachments ) {
					try {
						MimeBodyPart wrap = new MimeBodyPart();
						wrap.setContent(mimeAlternative);
						mimeMultipart.addBodyPart(wrap);
					} catch (Exception e) {
						Logger.debug(e);
					}
				}
				for( int j = 2; j < bodiesList.getLength(); j++ ) {
					try {
						MimeBodyPart b = new MimeBodyPart();
						b.setDataHandler( new DataHandler( new MailDataSource((DynamicElement) bodiesList.item(j)) ) );
						mimeMultipart.addBodyPart(b);
					} catch (Exception e) {
						Logger.debug(e);
					}
				}
			}

		}

		// Set Attachments
//		messageBodyPart = new MimeBodyPart();
//		DataSource source = new FileDataSource(filename);
//		messageBodyPart.setDataHandler(new DataHandler(source));
//		messageBodyPart.setFileName(filename);
//		multipart.addBodyPart(messageBodyPart);
		if(hasAttachments) {

			for( int j = 0; j < attachmentsList.getLength() ; j++ ) {

				try {

					String attachmentName = ((DynamicElement) attachmentsList.item(j)).getAttribute("Name");

					MimeBodyPart mimeBodyPart = new MimeBodyPart();

					if(attachmentName != null && attachmentName.equals("mail")){

						MimeMessage mimeMessage = (MimeMessage) exportMail((DynamicElement) attachmentsList.item(j));
						mimeBodyPart.setContent(mimeMessage, "message/rfc822");
						mimeMultipart.addBodyPart(mimeBodyPart);

					}else if(attachmentName != null && attachmentName.equals("contact")){
						
						VCard vCard = new VCard((DynamicElement) attachmentsList.item(j)); 
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						vCard.getVcard(new OutputStreamWriter(bout,"utf-8"));

						DynamicElement vHolder = new DynamicElement(null, "File");
						vHolder.setAttribute("Bin", new ByteArrayInputStream(bout.toByteArray()));
						vHolder.setAttribute("Name", ((attachmentName != null) ? attachmentName : "contact") + ".vcf");
						vHolder.setAttribute("Type", "text/x-vcard; charset=utf-8");

						mimeBodyPart.setDataHandler( new DataHandler( new MailDataSource(vHolder) ) );
						mimeBodyPart.setFileName(MimeUtility.encodeText( vHolder.getAttribute("Name")));
						mimeMultipart.addBodyPart(mimeBodyPart);

					} else {

						mimeBodyPart.setDataHandler( new DataHandler( new MailDataSource((DynamicElement) attachmentsList.item(j)) ) );

						String fileName = attachmentName;

						if( fileName != null ) {
							mimeBodyPart.setFileName(MimeUtility.encodeText(fileName));
						}

						mimeBodyPart.setContentID(((DynamicElement) attachmentsList.item(j)).getAttribute("Id"));
						String disposition = ((DynamicElement) attachmentsList.item(j)).getAttribute("Disposition");

						if( disposition != null )
							mimeBodyPart.setDisposition(disposition);
						mimeMultipart.addBodyPart(mimeBodyPart);
					}

				} catch (Exception e) {
					Logger.debug(e);
				}
			}

		}


		/*		for( int i = 0; i < list.length; i++ ) {
			if( list[i].getName().equals("bodypart") && hasBody ) {
				DynamicElement[] body = list[i].getObjects();

				if( body.length == 1 ) {
					try {
						if (hasAttachments) {
							MimeBodyPart b = new MimeBodyPart();
							b.setDataHandler( new DataHandler( new XmlNodeDataSource(body[0]) ) );
							mimeMultipart.addBodyPart(b);
						} else {
							if( smimeFlags != 0 ) {
								smimePart = new MimeBodyPart();
								smimePart.setDataHandler( new DataHandler( new XmlNodeDataSource(body[0]) ) );
							} else
								message.setDataHandler( new DataHandler( new XmlNodeDataSource(body[0]) ) );
						}
					} catch (Exception e) {
						//EventLog.debug(e);
					}
				} else {
					for( int j = 0; j < 2; j++ ) {

						try {
							MimeBodyPart b = new MimeBodyPart();
							b.setDataHandler( new DataHandler( new XmlNodeDataSource(body[j]) ) );
							mimeAlternative.addBodyPart(b);
						} catch (Exception e) {
							//EventLog.debug(e);
						}
					}
					if( body.length > 1 && hasAttachments ) {
						try {
							MimeBodyPart wrap = new MimeBodyPart();
							wrap.setContent(mimeAlternative);
							mimeMultipart.addBodyPart(wrap);
						} catch (Exception e) {
							//EventLog.debug(e);
						}
					}
					for( int j = 2; j < body.length; j++ ) {
						try {
							MimeBodyPart b = new MimeBodyPart();
							b.setDataHandler( new DataHandler( new XmlNodeDataSource(body[j]) ) );
							mimeMultipart.addBodyPart(b);
						} catch (Exception e) {
							//EventLog.debug(e);
						}
					}
				}

			} else if( list[i].getName().equals("attach") ) {
				DynamicElement[] attach = list[i].getObjects();

				for( int j = 0; j < attach.length; j++ ) {
					try {
						if (attach[j].getName().equals("mail")) {
							MimeMessage mc =
								(MimeMessage) exportMail(attach[j]);
							MimeBodyPart b = new MimeBodyPart();
							b.setContent(mc, "message/rfc822");
							mimeMultipart.addBodyPart(b);
						} else if (attach[j].getName().equals("contact")) {
							VCard vc = new VCard(attach[j]);
							ByteArrayOutputStream bout = new ByteArrayOutputStream();
							vc.getVcard(new OutputStreamWriter(bout,"utf-8"));
							DynamicElement vHolder = new DynamicElement("file");
							vHolder.setProperty("file",new ByteArrayInputStream(bout.toByteArray()));
							vHolder.setProperty("name", ((attach[j].getProperty("name")!=null)?attach[j].getProperty("name"):"contact") + ".vcf");
							vHolder.setProperty("type", "text/x-vcard; charset=utf-8");
							MimeBodyPart b = new MimeBodyPart();
							b.setDataHandler( new DataHandler( new XmlNodeDataSource(vHolder) ) );
							b.setFileName(MimeUtility.encodeText((String)vHolder.getProperty("name")));
							mimeMultipart.addBodyPart(b);
						} else {
							MimeBodyPart b = new MimeBodyPart();

							b.setDataHandler( new DataHandler( new XmlNodeDataSource(attach[j]) ) );
							String fn = (String)attach[j].getProperty("name");
							if( fn != null ) {
								b.setFileName(MimeUtility.encodeText(fn));
							}
							b.setContentID((String)attach[j].getProperty("id"));
							fn = (String)attach[j].getProperty("disposition");
							if( fn != null )
								b.setDisposition(fn);
							mimeMultipart.addBodyPart(b);
						}
					} catch (Exception e) {
						//EventLog.debug(e);
					}
				}
			}

		}*/
		try {
			if (mimeMultipart != null) {
				if( smimeFlags != 0 ) {
					smimePart = new MimeBodyPart();
					smimePart.setContent(mimeMultipart);
				} else
					message.setContent(mimeMultipart);
			} else if (mimeAlternative != null) {
				if( smimeFlags != 0 ) {
					smimePart = new MimeBodyPart();
					smimePart.setContent(mimeAlternative);
				} else
					message.setContent(mimeAlternative);
			}
			/*if( smimeFlags != 0 && (smimePart != null) ) {
				if( (smimeFlags & FL_COMPRESS) != 0 ) 
					smimePart = (new SMIMECompressedGenerator()).generate(smimePart, SMIMECompressedGenerator.ZLIB);

				if( (smimeFlags & FL_SIGN) != 0 ) {
					PrivateKey key = null;
					Certificate[] certs = null;
					for( Enumeration en = store.aliases(); en.hasMoreElements(); ) {
						String alias = (String)en.nextElement();
						if( store.isKeyEntry(alias) ) {
							key = (PrivateKey)store.getKey(alias,null);
							certs = store.getCertificateChain(alias);
						}
					}
					ArrayList certList  = new ArrayList(certs.length);
					for( int i = 0; i < certs.length; i++ )
						certList.add(certs[i]);
					CertStore certsAndcrls = CertStore.getInstance("Collection",new CollectionCertStoreParameters(certList), "BC");
					ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
					SMIMECapabilityVector caps = new SMIMECapabilityVector();
					caps.addCapability(SMIMECapability.dES_EDE3_CBC);
					caps.addCapability(SMIMECapability.rC2_CBC, 128);
					caps.addCapability(SMIMECapability.dES_CBC);
					signedAttrs.add(new SMIMECapabilitiesAttribute(caps));
					signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(new IssuerAndSerialNumber(new X509Name(((X509Certificate)certs[0]).getIssuerDN().getName()), ((X509Certificate)certs[0]).getSerialNumber())));

					SMIMESignedGenerator gen = new SMIMESignedGenerator();
					gen.addSigner(key, (X509Certificate)certs[0], "DSA".equals(key.getAlgorithm()) ? SMIMESignedGenerator.DIGEST_SHA1 : SMIMESignedGenerator.DIGEST_MD5, new AttributeTable(signedAttrs), null);
					gen.addCertificatesAndCRLs(certsAndcrls);

					if( (smimeFlags & FL_SIGNCLEAR) == 0 || (smimeFlags & FL_CRYPT) != 0 ) {
						MimeBodyPart signedPart = new MimeBodyPart();
						signedPart.setContent( gen.generateEncapsulated(smimePart, "BC").getContent(), "application/pkcs7-mime; name=\"smime.p7m\"; smime-type=signed-data");
						smimePart = signedPart;
					} else {
						message.setContent(gen.generate(smimePart,"BC"));
						smimePart = null;
					}
				}

				if( (smimeFlags & FL_CRYPT) != 0 ) {
					SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
					for( Enumeration en = store.aliases(); en.hasMoreElements(); ) {
						String alias = (String)en.nextElement();
						if( store.isCertificateEntry(alias) ) 
							gen.addKeyTransRecipient((X509Certificate)store.getCertificate(alias));
					}
					smimePart = gen.generate(smimePart, SMIMEEnvelopedGenerator.RC2_CBC, "BC");
				}

				if( smimePart != null ) {
					String contentType = smimePart.getContentType();
					if( (smimeFlags & FL_SIGN) != 0 && (smimeFlags & FL_CRYPT) == 0 && (smimeFlags & FL_SIGNCLEAR) == 0 )
						contentType = "application/pkcs7-mime; name=smime.p7m; smime-type=signed-data";
					message.setContent(smimePart.getContent(), contentType);
				}
			}*/

		} catch (Exception e) {Logger.debug(e);}
	}

	public static void exportMail( OutputStream os, DynamicElement n ) throws IOException, MessagingException {
		MimeMessage m = exportMail(n);
		m.saveChanges();
		m.writeTo( os );
	}

	public static MimeMessage exportMail( DynamicElement n ) {
		Session session = Session.getInstance( System.getProperties(), null );
		session.getProperties().setProperty("mail.mime.address.strict","false");
		session.getProperties().setProperty("mail.mime.decodetext.strict","false");
		MimeMessage m = new MimeMessage(session);
		exportMail( session, m, n, 0, null );
		return m;		
	}

	/*public static void setDefaultMailcap()
	{
		MailcapCommandMap _mailcap = (MailcapCommandMap)CommandMap.getDefaultCommandMap();

		_mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
		_mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
		_mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
		_mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
		_mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

		CommandMap.setDefaultCommandMap(_mailcap);
	} */


	/**
	 * This method adds the corresponding Addresses to the message to be exported.
	 * @param message - The message to be exported.
	 * @param addressesList - The list of addresses.
	 */
	private static void addAddresses(Message message, DummyNodeList addressesList) {

		if(addressesList == null)
			return;

		ArrayList<InternetAddress> replyToList = new ArrayList<InternetAddress>();

		for(int i = 0; i < addressesList.getLength() ; i++){
			DynamicElement address = (DynamicElement) addressesList.item(i);

			InternetAddress ia = new InternetAddress();
			ia.setAddress(address.getAttribute("Addr"));

			String name = address.getAttribute("Name");

			if(name != null){
				try {
					ia.setPersonal(name);
				} catch (UnsupportedEncodingException uee) {
					Logger.debug(uee);
				}
			}

			String type = address.getAttribute("Type");

			try{

				if(type.equals("from"))	
					message.setFrom(ia);
				else if(type.equals("to"))
					message.addRecipient(Message.RecipientType.TO, ia);
				else if(type.equals("cc"))
					message.addRecipient(Message.RecipientType.CC, ia);
				else if(type.equals("bcc"))
					message.addRecipient(Message.RecipientType.BCC, ia);
				else if(type.equals("reply-to"))
					replyToList.add(ia);
			}catch(MessagingException me){
				Logger.debug(me);
			}

		}

		try {
			message.setReplyTo((Address[]) replyToList.toArray(new Address[replyToList.size()]));
		} catch (MessagingException me) {
			Logger.debug(me);
		}
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



}

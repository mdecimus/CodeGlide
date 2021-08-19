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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.internet.ContentType;

import org.w3c.dom.Attr;

import com.codeglide.xml.dom.DynamicAttrStream;
import com.codeglide.xml.dom.DynamicElement;

public class MailDataSource implements DataSource {
	private DynamicElement node = null;

	public MailDataSource(DynamicElement node) {
			this.node = node;
	}
	public InputStream getInputStream() throws IOException {
		
		Attr attr = node.getAttributeNode("Bin");
		DynamicAttrStream bin = null;
		if(attr == null ){
			bin = (DynamicAttrStream) node.getAttributeNode("Bin");
		}
		if( bin == null || bin.getInputStream() == null) {
			return new ByteArrayInputStream ( new byte[] {'e','m','p','t','y'});
		}
		return new StreamWrapper( bin.getInputStream() );
	}
	public String getContentType() {
		String ct = node.getAttribute("Type");
		if( ct == null || ct.equals("") )
			return "application/octet-stream";
		else {
			try {
				ContentType cv = new ContentType(ct);
				try {
					if (node.getNodeName().equals("Body")) {
						String ch = node.getAttribute("Charset");
						if (ch != null) {
							cv.setParameter("charset", ch);
							ct = cv.toString();
						}
					}
				} catch (Exception e) {}
			} catch (Exception e) {
				//System.out.println("Invalid encoding " + ct);
				if( ct.toLowerCase().startsWith("text") )
					return "text/plain";
				else
					return "application/octet-stream";
			}
			return ct;
		}
	}
	public String getName() {
		String name = node.getAttribute("Name");
		if( name != null )
			return name;
		name = node.getAttribute("Type");
		if( name != null )
			if( name.toLowerCase().startsWith("text/") )
				return "file.txt";
		return "file.bin";
	}
	public OutputStream getOutputStream() {
		return null;
	}
	class StreamWrapper extends InputStream {
		InputStream i;
		
		StreamWrapper( InputStream in ) throws IOException {
			this.i = in;
			i.reset();
		}

		public synchronized int read() throws IOException {
			return i.read();
		}

		public synchronized int read(byte b[], int off, int len) throws IOException {
			return i.read(b,off,len);
		}

		public synchronized long skip(long n) throws IOException {
			return i.skip(n);
		}

		public synchronized int available() throws IOException {
			return i.available();
		}

		public synchronized void reset() throws IOException {
			i.reset();
		}

		public synchronized void close() throws IOException {
			i.reset();
		}
	
		public boolean markSupported() {
			return i.markSupported();
		}
	}
}

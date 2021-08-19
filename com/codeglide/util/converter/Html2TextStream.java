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

package com.codeglide.util.converter;

import java.io.IOException;
import java.io.Reader;

/**
 * @author admin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Html2TextStream extends Reader {
	private Reader in;
	private boolean inTag = false;
	private StringBuffer tag = null;
	private StringBuffer tags = null;
	private int hr = 0;

	public Html2TextStream( Reader in ) {
		this.in = in;
	}

	public Html2TextStream( Reader in, StringBuffer tags ) {
		this.in = in;
		this.tags = tags;
	}

	public int read() throws IOException {
		int c;
		if( hr >= 0 )
			return (hr-->0)?'-':'\n';
		
		while( (c = in.read()) > -1 ) {
			if( c == '<' ) {
				inTag = true;
				tag = new StringBuffer();
				continue;
			} else if( c == '>' && inTag ) {
				inTag = false;
				String hTag = tag.toString();
				if( tags != null )
					tags.append(extractAttributes(hTag)).append(" ");
				if( hTag.startsWith("br") || hTag.startsWith("/p") || hTag.startsWith("/div") )
					c = '\n';
				else if( hTag.startsWith("hr")) {
					hr = 59;
					c = '-';
				} else
					continue;
			} else if( inTag ) {
				tag.append(Character.toLowerCase((char)c));
				continue;
			} else if( c == '\r') {
				continue;
			} else if( c == '\n' ) {
				c = ' ';
			} else if( c == '&' )
				c = decodeEntity();
			return c;
		}
		return -1;
	}

	private int decodeEntity() throws IOException {
		int c;
		boolean doRead = true;
		StringBuffer code = new StringBuffer();
		while( doRead && (c = in.read()) > -1 ) {
			if( c == ';' )
				doRead = false;
			else if( !Character.isLetterOrDigit((char)c) && c != '#' )
				return 0;
			else
				code.append((char)c);
		}
		return HtmlCoder.decodeHtmlEntity(code.toString());
	}

	public void close() throws IOException {
	}

	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		throw new IOException("Not implemented");
	}

	private String extractAttributeValue( String element ) {
		int index = element.indexOf('=');
		if( index < 0 )
			return null;
		return element.substring(index+1).trim();
	}

	private String extractAttributes( String tags ) {
		boolean inQuote = false;
		StringBuffer element = null, result = new StringBuffer();
		for( int i = 0; i < tags.length(); i++ ) {
			char c = tags.charAt(i);
			if( c == '"' )
				inQuote = !inQuote;
			else if( !inQuote && (Character.isWhitespace(c) || i == tags.length() ) ) {
				if( element != null ) {
					String data = extractAttributeValue(element.toString());
					element = null;
					if( data != null && !data.equals(""))
						result.append(data).append(" ");
				}
			} else {
				if( element == null )
					element = new StringBuffer();
				element.append(c);
			}
		}
		return result.toString();
	}

}

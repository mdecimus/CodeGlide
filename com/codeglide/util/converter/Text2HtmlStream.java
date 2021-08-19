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
import java.io.StringReader;

/**
 * @author admin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Text2HtmlStream extends Reader {
	private Reader in;
	private StringReader buffer = null;

	public Text2HtmlStream( Reader in ) {
		this.in = in;
	}

	public int read() throws IOException {
		int c;

		if( buffer != null && (c = buffer.read()) > -1 )
			return c;
		else if( buffer != null )
			buffer = null;
		
		c = in.read();
		if( c == '<') {
			buffer = new StringReader("&lt;");
			return buffer.read();
		} else if( c == '>') {
			buffer = new StringReader("&gt;");
			return buffer.read();
		} else if( c == '"') {
			buffer = new StringReader("&quot;");
			return buffer.read();
		} else if( c == '&') {
			buffer = new StringReader("&amp;");
			return buffer.read();
		} else if( c == '\n') {
			buffer = new StringReader("<br>\n");
			return buffer.read();
		} else if( c == '\r') {
			return read();
		}
		
		return c;
	}

	public void close() throws IOException {
		
	}

	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		throw new IOException("Not implemented");
	}

}

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
package com.codeglide.util.mimedir;

import java.io.IOException;
import java.io.Reader;

public class UnfoldReader extends Reader {
	private Reader in;
	private int bufChar = -1;

	public UnfoldReader(Reader in) throws IOException {
		this.in = in;
	}

	public void close() throws IOException {}

	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		return -1;
	}
		
	public int read() throws IOException {
		boolean seenCrLf = false;
		int c;
		if( bufChar > -1 ) {
			c = bufChar;
			bufChar = -1;
			return c;
		}
		while( (c = in.read()) > -1 ) {
			if( c == '\r' )
				continue;
			else if( c == '\n' )
				seenCrLf = true;
			else if( Character.isWhitespace((char)c)) {
				if( seenCrLf )
					seenCrLf = false;
				else
					return c;
			} else {
				if( seenCrLf ) {
					bufChar = c;
					return '\n';
				} else
					return c;
			}
		}
		return -1;
	}
		
	public String readLine() throws IOException {
		int c;
		StringBuffer result = new StringBuffer();
		while( (c = read()) != -1 && c != '\n')
			result.append((char)c);
		if( c == -1 && result.length() < 1 )
			return null;
		else
			return result.toString();
	}

}

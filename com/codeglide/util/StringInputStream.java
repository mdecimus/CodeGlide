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
package com.codeglide.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

public class StringInputStream extends InputStream {
	private ByteArrayInputStream in;
	private String string;

	public StringInputStream(String s, String charset) throws CharacterCodingException {
		ByteBuffer buffer = Charset.forName(charset).newEncoder().encode(CharBuffer.wrap(s));
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes, 0, bytes.length);
		in = new ByteArrayInputStream( bytes );
		string = s;
	}

	public String getString() {
		return (string==null)?"":string;
	}

	public synchronized int read() throws IOException {
		return in.read();
	}

	public synchronized int read(byte b[], int off, int len) throws IOException {
		return in.read(b,off,len);
	}

	public synchronized long skip(long n) throws IOException {
		return in.skip(n);
	}

	public synchronized int available() throws IOException {
		return in.available();
	}

	public synchronized void reset() throws IOException {
		in.reset();
	}

	public synchronized void close() throws IOException {

	}
	
	public boolean markSupported() {
		return true;
	}
	
	
	
}

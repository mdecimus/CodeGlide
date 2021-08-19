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
import java.io.InputStream;
import java.io.Reader;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import com.codeglide.core.Logger;

public class Rtf2TextStream extends Reader {

	InputStream inputStream;
	String rtfText;
	int position;
	
	public Rtf2TextStream(InputStream inputStream) {
		this.inputStream = inputStream;
		
		DefaultStyledDocument styledDocument =  new DefaultStyledDocument();
		try {
			
			new RTFEditorKit().read(inputStream, styledDocument, 0);
			rtfText = styledDocument.getText(0, styledDocument.getLength());
			position = 0;
			
		} catch (BadLocationException e) {
			Logger.debug(e);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.debug(e);
		}
	}
	
	public int read(){

		if(position < rtfText.length())
			return rtfText.charAt(position++);			
		else
			return -1;
	}

	@Override
	public void close() throws IOException {}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		throw new IOException("Not implemented");
	}

}

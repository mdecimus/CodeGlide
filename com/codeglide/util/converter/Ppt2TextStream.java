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

import org.apache.poi.hslf.extractor.PowerPointExtractor;

import com.codeglide.core.Logger;

public class Ppt2TextStream extends Reader {
	
	InputStream inputStream;
	String pptText;
	int position;
	

	public Ppt2TextStream(InputStream in) {
		
		this.inputStream = in;
		
		try {
			
			PowerPointExtractor extractor = new PowerPointExtractor(inputStream);
			pptText = extractor.getText(true, true);
			position = 0;
			
		} catch (IOException e) {
			Logger.debug(e);
		}
		
	}


	public int read(){
		
		if(position < pptText.length())
			return pptText.charAt(position++);
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

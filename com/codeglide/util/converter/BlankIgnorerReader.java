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

import com.codeglide.core.Logger;

public class BlankIgnorerReader extends Reader {
	
	private Reader reader;
	private int currentChar;
	private boolean lastWasWhite;
	
	public BlankIgnorerReader(Reader reader) {
		this.reader = reader;
		currentChar = ' ';
		lastWasWhite = true;
	}
	
	public int read(){
		
		try {					
			while(currentChar != -1){
				currentChar = reader.read();
				
				if(lastWasWhite && Character.isWhitespace(currentChar)){
					
					while(currentChar != -1 && Character.isWhitespace(currentChar) ){
						currentChar = reader.read();
					}
					
					lastWasWhite = false;
					return currentChar;
					
				}else{
					
					if(Character.isWhitespace(currentChar))
						lastWasWhite = true;
					else
						lastWasWhite = false;

					return currentChar;
				}				
			}			
		} catch (IOException e) {
			Logger.debug(e);
		}
		
		return -1;			
	}

	@Override
	public void close() throws IOException {}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		throw new IOException("Not implemented");
	}

}

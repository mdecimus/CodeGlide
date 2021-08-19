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

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import com.codeglide.core.Logger;

public class Pdf2TextStream extends Reader {

	private InputStream inputStream;
	private PDDocument document;
	private PDFTextStripper stripper;
	private String pdfText;
	private int position;

	public Pdf2TextStream( InputStream in ) {

		this.inputStream = in;

		try {

			this.document = PDDocument.load(inputStream);

			if(!document.isEncrypted()){

				stripper = new PDFTextStripper();
				pdfText = stripper.getText(document);
				position = 0;

			}else
				throw new IOException( "You do not have permission to extract text" );

		} catch (IOException e) {
			Logger.debug(e);
		}		

	}

	public int read(){

		if(position < pdfText.length())
			return pdfText.charAt(position++);
		else
			return -1;
	}

	@Override
	public void close() throws IOException {}

	@Override
	public int read(char[] cbuf, int off, int len)  throws IOException {
		throw new IOException("Not implemented");
	}

}

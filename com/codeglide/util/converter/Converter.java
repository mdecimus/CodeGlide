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
import java.io.InputStreamReader;
import java.io.Reader;

public class Converter {

	/*
	 * Content-types:
	 * 
	 * text/html
	 * Xml	->	"text/xml" or "application/xml"
	 * Doc	->	"application/ms-word" or "application/msword"
	 * Xls	->	"application/ms-excel" or "application/msexcel"
	 * Ppt	->	"application/ms-powerpoint" of "application/mspowerpoint"
	 * Docx	->	PartName="/word/document.xml" ContentType="application/vnd.ms-word.main+xml"
	 * Xlsx	->	PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"
	 * Pptx	->	
	 * PDF	->	"application/pdf" or "application/x-pdf" ?
	 * rtf	->	"application/rtf"
	 * Postcript -> "application/postscript" - Not used
	 * Vsd	-> Not Used
	 * 
	 */
	
	public static Reader getReaderConverter( String contentType, InputStream in, String charset ) throws IOException {
		
		if (contentType.equals("text/html"))
			return new Html2TextStream(new InputStreamReader(in, charset));
		else if (contentType.equals("text/xml")  || contentType.equals("application/xml"))
			return new Xml2TextStream(in, true);
		else if (contentType.equals("application/ms-word")  || contentType.equals("application/msword"))
			return new Doc2TextStream(in);
		else if (contentType.equals("application/ms-excel") || contentType.equals("application/msexcel") || contentType.equals("application/x-msexcel") || contentType.equals("application/vnd.ms-excel"))
			return new Xls2TextStream(in);
		else if (contentType.equals("application/ms-powerpoint") || contentType.equals("application/mspowerpoint") || contentType.equals("application/x-mspowerpoint"))
			return new Ppt2TextStream(in);
		else if (contentType.equals("application/pdf") || contentType.equals("application/x-pdf"))
			return new Pdf2TextStream(in);
		else if (contentType.equals("application/rtf"))
			return new Rtf2TextStream(in);
		else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
			return new Docx2TextStream(in);
		else if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
			return new Xlsx2TextStream(in);
		else if (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
			return new Pptx2TextStream(in);
		else
			throw new IOException("File type not supported!");
	}
	
}

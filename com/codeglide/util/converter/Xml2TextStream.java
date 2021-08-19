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
import java.io.StringWriter;
import java.util.ArrayList;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.codeglide.core.Logger;

public class Xml2TextStream extends Reader {

	private InputStream inputStream;
	private String xmlText;
	private int position;

	public Xml2TextStream(InputStream in, boolean includeTags) {
		this.inputStream = in;

		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			StringWriter out = new StringWriter();
			ContentHandler handler = new XmlTextExtractor(out, includeTags);
			parser.setContentHandler(handler);

			InputSource inputSource = new InputSource(inputStream);
			parser.parse(inputSource);

			out.flush();
			xmlText = out.getBuffer().toString();
			position = 0;
		}
		catch (Exception e) {
			Logger.debug(e); 
		}


	}

	public Xml2TextStream(ArrayList<InputStream> list, boolean includeTags) {

		for(int i = 0; i < list.size() ; i++){
			this.inputStream = list.get(i);

			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				StringWriter out = new StringWriter();
				ContentHandler handler = new XmlTextExtractor(out, includeTags);
				parser.setContentHandler(handler);

				InputSource inputSource = new InputSource(inputStream);
				parser.parse(inputSource);

				out.flush();
				xmlText += out.getBuffer().toString();
			}
			catch (Exception e) {
				Logger.debug(e); 
			}	

		}

		position = 0;
	}


	public int read(){

		if(position < xmlText.length())
			return xmlText.charAt(position++);			
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

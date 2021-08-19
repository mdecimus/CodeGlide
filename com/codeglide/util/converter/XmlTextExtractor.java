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
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Steppenwolf
 */
public class XmlTextExtractor extends DefaultHandler{

	private Writer out;
	private boolean includeTags;

	public XmlTextExtractor(Writer out, boolean includeTags) {
		this.out = out;   
		this.includeTags = includeTags;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			if(includeTags)
				out.write(localName  + " "); 
		}
		catch (IOException e) {
			throw new SAXException(e);   
		}
	}



	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if(includeTags){
				out.write(localName + " "); 
				for(int i = 0 ; i < attributes.getLength() ; i++ ){
					out.write(attributes.getLocalName(i)  + " ");
					out.write(attributes.getValue(i)  + " ");
				}
			}
		}
		catch (IOException e) {
			throw new SAXException(e);   
		}
	}



	public void characters(char[] text, int start, int length)
	throws SAXException {
		try {
			out.write(text, start, length);
			out.write(" ");
		}
		catch (IOException e) {
			throw new SAXException(e);   
		}
	}

}

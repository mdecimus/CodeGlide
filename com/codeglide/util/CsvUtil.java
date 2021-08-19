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

import java.io.Reader;
import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Logger;
import com.codeglide.util.mimedir.UnfoldReader;
import com.codeglide.xml.dom.DynamicElement;

public class CsvUtil {

	static public Node importCsv(Document doc, Reader in, boolean hasHeaders, char separator) {

		DynamicElement result = new DynamicElement(doc, "CsvFile");
		
		try {
			UnfoldReader ur = new UnfoldReader(in);
			int c, numCol = 1, maxCol = 0;
			StringBuffer data = new StringBuffer();
			boolean escapeChar = false, read = true, isComiles = false;
			DynamicElement node = new DynamicElement(doc, "Row");
					
			//Read all file
			while (read) {
				c = ur.read();
				
				if (c == '"') {
					isComiles = !isComiles;
				}
				
				if (((char) c != separator) && (c != '\n') && (c != -1)) {
					data.append((char) c);
				} else {
					//The character is between comiles
					if (isComiles) {
							data.append((char) c);
					} else if (!escapeChar) {

						if (data.length() > 0) {
							if( numCol > maxCol )
								maxCol = numCol;

							if (hasHeaders) { // data contains the name of the
								// column
								result.setAttribute("c"+numCol, unescapeTextValue(data.toString()));
								numCol++;
								
								data = new StringBuffer();

							} else { // data contains information

								// Obtain the name of the column
								node.setAttribute("c" + numCol,
										unescapeTextValue(data.toString()));
								
								numCol++;
								data = new StringBuffer();
							}
						}
						if ((c == '\n' || c == -1) && !isComiles) {
							if (!hasHeaders) {
								result.appendChild(node);
								node = new DynamicElement(doc, "Row");
							}
							// End of the line of head
							hasHeaders = false;
							numCol = 1;
						}
					} else {
						//There is escape character
						data.append((char) c);
						
					}
				}

				// If c is a escape character
				if ((c == '\\') && (!escapeChar)) {
					escapeChar = true;
					continue;
				}
				if (c != '\n') {
					escapeChar = false;
				}

				if (c == -1) {
					read = false;
				}
			}
			
			result.setAttribute("ColumnCount", maxCol);
			
		} catch (Exception e) {
			Logger.debug(e);
		}
		return result;
	}

	static public void exportCsv(Writer out, NodeList list, char separator) {
		String nameCol, value;
		DynamicElement node;
		int cantAttr = 0;
		
		if (list != null) {
			try {
				for (int i = 0; i < list.getLength(); i++) {
					node = (DynamicElement) list.item(i);
					cantAttr = node.getAttributes().getLength();
					for (int p = 0; p < cantAttr; p++) {
						nameCol = "c" + (p + 1);
						value = escapeTextValue(node.getAttribute(nameCol), separator);
						if (value != null) {
							if ((p + 1) == cantAttr)
								out.write(value);
							else
								out.write(value + separator);
						}
					}
					out.write("\n");
				}
			} catch (Exception e) {
				Logger.debug(e);
			}
		}
	}

	static private String escapeTextValue(String text, char separator) {
		if (text == null)
			return null;
		StringBuffer sb = new StringBuffer(text);
		boolean insertComiles = false;
		
		for (int i = 0; i < text.length(); i++) {
			char c = sb.charAt(i);
			
			if( (c == ' ') || (c=='\n') || (c=='"') || (c == separator) )
				insertComiles = true;
			if (c == '\t'){
				sb.deleteCharAt(i);
				sb.insert(i,'\\');
				sb.insert(i + 1,'t');
			}else if (c == '\\' || c == '"'){
				sb.insert(i, '\\');
				i++;
			}
		}
		if (insertComiles){
			sb.insert(0, '"');
			sb.insert(sb.length(), '"');
		}
		return sb.toString();
	}

	static private String unescapeTextValue(String text) {
		if (text == null)
			return null;
		final StringBuffer sb = new StringBuffer(text);

		for (int n = 0; n < sb.length(); n++) {
			char c = sb.charAt(n);
			if (c != '\\')
				continue;
			if (n == sb.length() - 1)
				continue;
			char nc = sb.charAt(n + 1);
			if (nc == '\\') {
				sb.deleteCharAt(n);
			} else if ((nc == 'N') || (nc == 'n')) {
				sb.setCharAt(n + 1, '\n');
				sb.deleteCharAt(n);
			} else if ((nc == 'R') || (nc == 'r')) {
				sb.setCharAt(n + 1, '\r');
				sb.deleteCharAt(n);
			} else if ((nc == 'T') || (nc == 't')) {
				sb.setCharAt(n + 1, '\t');
				sb.deleteCharAt(n);
			} else if ((nc == ';') || (nc == ',') || (nc == ':') || (nc == '"')) {
				sb.deleteCharAt(n);
			}
		}
		
		if (sb.charAt(0) == '"')
			sb.deleteCharAt(0);
		if (sb.charAt(sb.length() - 1) == '"' )
			sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	static private String onlyAlpNum(String text) {
		if (text == null)
			return null;
		final StringBuffer sb = new StringBuffer(text);

		for (int n = 0; n < sb.length(); n++) {
			int c = (int) sb.charAt(n);
			if ((!Character.isDigit(c) && !Character.isLetter(c))
					|| (Character.isWhitespace(c))){
				sb.deleteCharAt(n);
				n--;
			}
				
		}
		return sb.toString();
	}
}

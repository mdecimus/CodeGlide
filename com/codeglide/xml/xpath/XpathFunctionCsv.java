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
package com.codeglide.xml.xpath;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

import com.codeglide.core.Logger;
import com.codeglide.util.CsvUtil;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrStream;

public class XpathFunctionCsv extends XpathFunction {
	
	public XpathFunctionCsv() {
		addFunction("export", new ExportCSV());
		addFunction("import", new ImportCSV());
	}
	
	public class ExportCSV implements XPathFunction{

		/**
		 * @return A boolean value if the export operation was successful or null if the parameters were wrong.
		 * */
		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,exportCsv");

			NodeList nodeList = getNodes(args.get(0));
			String separator = null;
			if( args.size() >= 2 )
				separator = getString(args.get(1));
			/*String charset = null;
			if( args.size() >= 3 )
				charset = getString(args.get(2));
			if( charset == null || charset.isEmpty() || !Charset.isSupported(charset) )
				charset = "UTF-8";*/
			if( separator == null || separator.isEmpty() )
				separator = ",";
			
			StringWriter writer = new StringWriter();
			
			CsvUtil.exportCsv(writer, nodeList, separator.charAt(0));

			return writer.toString();
		}
	}

	public class ImportCSV implements XPathFunction{

		/**
		 * @return The node list if the charset is supported || null if not or if the parameters where wrong
		 * */
		@SuppressWarnings("unchecked")
		public Object evaluate(List args) throws XPathFunctionException {
			if( args.size() < 1 ) 
				throw new DOMException(DOMException.SYNTAX_ERR, "@function-invalid-parameter,importCsv");

			try {
				String separator = null, hasHeaders = null, charset = null;
				if( args.size() >= 2 )
					separator = getString(args.get(1));
				if( args.size() >= 3 )
					hasHeaders = getString(args.get(2));
				if( args.size() >= 4 )
					charset = getString(args.get(3));
				
				if( separator == null || separator.isEmpty() )
					separator = ",";
				else if( separator.startsWith("\\") ) {
					if( separator.equals("\\t") )
						separator = "\t";
					else if( separator.equals("\\n") )
						separator = "\n";
					else if( separator.equals("\\r") )
						separator = "\r";
				}
				try {
					if( charset == null || charset.isEmpty() || !Charset.isSupported(charset))
						charset = "ISO-8859-1";
				} catch (Exception e) {
					charset = "ISO-8859-1";
				}

				Reader inputReader = null;
				DynamicAttr attr = (DynamicAttr) getNode(args.get(0));
				if( attr != null ) {
					if( attr instanceof DynamicAttrStream ) 
						if( ((DynamicAttrStream)attr).getInputStream() != null )
							inputReader = new InputStreamReader( ((DynamicAttrStream)attr).getInputStream(), charset);
					else {
						if( attr.getValue() != null )
							inputReader = new StringReader(attr.getValue());
					}
					if( inputReader != null )
						return CsvUtil.importCsv(attr.getOwnerDocument(), inputReader, hasHeaders!=null && (hasHeaders.equals("1") || hasHeaders.equalsIgnoreCase("true")), separator.charAt(0));
				} /*else {
					String value = getString(args.get(0));
					if( value != null )
						inputReader = new StringReader(value);
				}*/
				
			} catch (Exception e) {
				Logger.debug(e);
			}
			return null;
		}
	}

}
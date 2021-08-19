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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Logger;
import com.codeglide.util.mimedir.VCard;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicAttrStream;
import com.codeglide.xml.dom.DynamicElement;

public class XpathFunctionVcard extends XpathFunction {

	public XpathFunctionVcard() {
		addFunction("export", new ExportVCard());
		addFunction("import", new ImportVCard());
	}

	public class ExportVCard implements XPathFunction{

		/**
		 * @return A boolean value if the export operation was successful or null if the parameters were wrong. 
		 * */
		@SuppressWarnings("unchecked")
		public Object evaluate(List parameters) throws XPathFunctionException {

			if(parameters.size() == 3 || parameters.size() == 2){

				NodeList in = (NodeList) parameters.get(0);
				NodeList out = (NodeList) parameters.get(1);
				String charset;
				
				if(parameters.size() == 3){
					charset = (String) parameters.get(2);		
					if(!Charset.isSupported(charset) || charset == null)
						charset = "ISO-8859-1";	
				}else
					charset = "ISO-8859-1";
					
				
				
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				Writer wout;

				for(int i = 0; i < in.getLength() ; i ++ ){

					try {
						wout = new OutputStreamWriter(bout, charset);

						VCard vcard;

						vcard = new VCard((DynamicElement)in.item(i));
						vcard.getVcard(wout);
						wout.flush();
						wout.close();
						bout.flush();

					} catch (UnsupportedEncodingException e) {
						Logger.debug(e);
					} catch (IOException e) {
						Logger.debug(e);
					}
				}

				((DynamicElement)out.item(0)).setAttribute("Bin", new ByteArrayInputStream(bout.toByteArray()));
				
				return true;

			}else
				return null;
		}
	}

	public class ImportVCard implements XPathFunction{

		/**
		 * @return a NodeList with the imported VCards.
		 * */
		@SuppressWarnings("unchecked")
		public Object evaluate(List parameters) throws XPathFunctionException {

			if(parameters.size() == 2 || parameters.size() == 1){

				NodeList in = (NodeList) parameters.get(0);
				String charset;
				
				if(parameters.size() == 2){
					charset = (String) parameters.get(1);		
					if(!Charset.isSupported(charset) || charset == null)
						charset = "ISO-8859-1";	
				}else
					charset = "ISO-8859-1";

				List<Node> nodes = new ArrayList<Node>();

				for(int i = 0; i < in.getLength() ; i++ ){
					try {
						Reader isr = new InputStreamReader(((DynamicAttrStream) in.item(i)).getInputStream(), charset);
						VCard vCard = null;
						while ((vCard = new VCard(isr)) != null && vCard.isValid())
							nodes.add(vCard.getXml());

					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						Logger.debug(e);
					} catch (Exception e) {
						e.printStackTrace();
						Logger.debug(e);
					}

				}

				return new DummyNodeList(nodes);

			}else	
				return null;
		}
	}

}
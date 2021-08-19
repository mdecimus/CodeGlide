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
package com.codeglide.util.mimedir;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class MimeDir {
	protected String mimeType = null;
	protected LinkedList<Object> elements = new LinkedList<Object>();
	private UnfoldReader ur;
	private boolean isValid = false;
	
	public MimeDir() {
	}
	
	public MimeDir(Reader in) {
		this(in,null);
	}
	
	public MimeDir(Reader in, String mimeType) {
		try {
			if (in instanceof UnfoldReader)
				ur = (UnfoldReader) in;
			else
				ur = new UnfoldReader(in);
			String line = null;
			if( mimeType == null ) {
				//TODO Solucionar el problema de la lectura de varios vCard en el mismo Reader
				//while( (line = ur.readLine()) != null && !line.startsWith("BEGIN:") );
				while( (line = ur.readLine()) != null && !line.toUpperCase().startsWith("BEGIN:") 
						&& !line.toUpperCase().startsWith("EGIN:") );
				if (line == null)
					return;
				//TODO Quitar el if cuando se solucione el problema de lectura
				if (line.toUpperCase().startsWith("EGIN:"))
					line  = "B" + line;
				
				parseLine(line);
				if(!isValid)
					isValid = true;
			}
			
			while( (line = ur.readLine()) != null && !line.toUpperCase().startsWith("END:") ) {
				parseLine(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public Iterator<Object> getElementIterator() {
		return elements.iterator();
	}
	
	public String getName() {
		return mimeType;
	}

	private String[] split( String ln, char separator, boolean doEscape ) {
		StringBuffer data = new StringBuffer(ln.length());
		Vector<Object> list = new Vector<Object>();
		char lc = 0;
		for( int i = 0; i < ln.length(); i++ ) {
			char cc = ln.charAt(i);
			if( cc == separator && lc != '\\' ) {
				list.add((doEscape)?unescapeTextValue(data.toString()):data.toString());
				data.setLength(0);
			} else {
				data.append(cc);
			}
			lc = cc;
		}
		if( data.length() > 0 )
			list.add((doEscape)?unescapeTextValue(data.toString()):data.toString());
		return (String[]) list.toArray(new String[list.size()]);
	}

	private void parseLine(String ln) throws IOException {
		int idx = ln.indexOf(":");
		if( idx == -1 )
			return;
		String[] params = split(ln.substring(0,idx).toLowerCase(),';',false);
		String[] values = split(ln.substring(idx+1),';',true);
		
		if( params.length < 1 || values.length < 1)
			return;
		if( params[0].equals("begin") ) {
			if( mimeType == null )
				mimeType = values[0];
			else
				elements.add(new MimeDir(ur,values[0]));
		} else if( mimeType == null ) {
			return;
		} else if( params[0].equals("end") ) {
			throw new IOException();
		} else {
			elements.add(new VElement(params,values));
		}
	}

	protected void addElement( String elementName, String params, Object values ) {
		if( elementName != null && values != null )
			elements.add(new VElement(elementName, (params!=null)?(split(params,';',false)):null, values));
	}

	public class VElement {
		private String elementName = null;
		private Object values = null;
		private HashMap<String, Object> params = null;
		public VElement(String elementName, String[] params, Object values) {
			this.elementName = elementName;
			if( params != null && params.length > 0)
				parseParams(params,0);
			if( values instanceof byte[] ) {
				if( this.params == null )
					this.params = new HashMap<String, Object>();
				this.params.put("encoding",new String[] {"b"});
			}
			this.values = values;
		}

		public VElement(String[] params, String[] values) {
			this.elementName = params[0];
			if( values.length > 1 )
				this.values = values;
			else
				this.values = values[0];
			if( params.length > 1) {
				parseParams(params,1);
				//String enc = null;
				if( this.params.containsKey("base64") || (this.params.containsKey("encoding") && ((String[])this.params.get("encoding"))[0].equals("b") )) {
					try {
						BASE64Decoder dec = new BASE64Decoder();
						this.values = dec.decodeBuffer(values[0]);
					} catch (Exception e) {
						this.values = "!!!";
					}
				}
			}
		}
		private void parseParams( String[] params, int startIdx ) {
			this.params = new HashMap<String, Object>();
			String[] keyval;
			String key;
			String types="";
			for( int i = startIdx; i < params.length; i++ ) {
				int idx = params[i].indexOf('=');
				if( idx != -1 ){
					keyval = split(params[i].substring(idx+1), ',', true);
					key = params[i].substring(0,idx);
					//If the key exist, add the new value
					Object obj = this.params.get(key);
					if (obj != null){
						if (obj instanceof String[]){
							String valAux = ((String[])obj)[0] + "," + params[i].substring(idx+1);
							keyval = split(valAux, ',', true);
						}
					}
					this.params.put(key, keyval);
					
				}else
					types = types + params[i] + "," ;
			}
			keyval = split(types, ',', true);
			if (keyval.length > 0){
				if (startIdx == 0)
					this.params.put("Type", keyval);
				else
					this.params.put(params[0], keyval);
			}
		}
		
		public String getName() {
			return elementName;
		}
		public Object getValues() {
			return values;
		}
		
		public String[] getParam(String name) {
			
			if( this.params != null ){
				return (String[])this.params.get(name);
			}else
				return null;
		}
		
		public Iterator<String> getParamKeys() {
			if( this.params != null )
				return this.params.keySet().iterator();
			else
				return null;
		}
	}

	private String escapeTextValue(String text) {
		if (text == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < text.length(); i++ ) {
			char c = text.charAt(i);
			if( c == '\\' || c == ':' || c == ';' || c == ',' || c == '\n' )
				sb.append("\\");
			if( c == '\n' )
				sb.append("n");
			else if( c != '\r' )
				sb.append(c);
		}
		return sb.toString();
	}

	private String unescapeTextValue(String text) {
	  if (text == null) return null;
	  boolean modified = false;
	  final StringBuffer sb = new StringBuffer(text);
	  for (int n = 0; n < sb.length(); n++) {
		char c = sb.charAt(n);
		if (c != '\\') continue;
		if (n == sb.length() - 1) continue;
		char nc = sb.charAt(n + 1);
		if (nc == '\\') {
		  sb.deleteCharAt(n);
		  n += 1;
		  modified = true;
		} else if ((nc == 'N') || (nc == 'n')) {
		  sb.setCharAt(n + 1, '\n');
		  sb.deleteCharAt(n);
		  n += 1;
		  modified = true;
		} else if ((nc == 'R') || (nc == 'r')) {
		  sb.setCharAt(n + 1, '\r');
		  sb.deleteCharAt(n);
		  n += 1;
		  modified = true;
		} else if ((nc == ';') || (nc == ',') || (nc == ':')) {
		  sb.deleteCharAt(n);
		  n += 1;
		  modified = true;
		}
	  }
	  return (modified) ? sb.toString() : text;
	}

	private String fold(String string) {
	  if (string == null || string.length() <= 75)
	  	return string;
	  StringBuffer sb = new StringBuffer();
	  int c = 0;
	  for( int i = 0; i < string.length(); i++ ) {
	  	if( c == 75 && i < string.length() - 1) {
	  		sb.append("\r\n ");
	  		c = 1;
	  	}
	  	sb.append(string.charAt(i));
	  	c++;
	  }
	  return sb.toString();
	}

	protected void export(Writer out) throws IOException {
		for( Iterator<Object> it = elements.iterator(); it.hasNext(); ) {
			Object item = it.next();
			if( item instanceof MimeDir )
				continue;
			VElement ve = (VElement)item;
			StringBuffer line = new StringBuffer();
			line.append(ve.getName().toUpperCase());
			Iterator<String> itt = ve.getParamKeys();
			if( itt != null ) {
				while( itt.hasNext() ) {
					line.append(";");
					String key = (String)itt.next();
					String[] values = ve.getParam(key);
					line.append(key.toUpperCase()).append("=");
					for( int i = 0; i < values.length; i++ ) {
						if( i > 0 )
							line.append(",");
						line.append(escapeTextValue(values[i].toUpperCase()));
					}
				}
			}
			line.append(":");
			item = ve.getValues();
			boolean hasData = false;
			if( item instanceof String ) {
				line.append(escapeTextValue((String)item));
				hasData = true;
			} else if( item instanceof String[] ) {
				for( int i = 0; i < ((String[])item).length && !hasData; i++ )
					if( ((String[])item)[i] != null )
						hasData = true;
				if( hasData ) {
					for( int i = 0; i < ((String[])item).length; i++ ) {
						if( i > 0 )
							line.append(";");
						if( ((String[])item)[i] != null )
							line.append( escapeTextValue(((String[])item)[i]));
					}
				}
			} else if( item instanceof byte[] ) {
				line.append((new BASE64Encoder().encode((byte[])item)).replaceAll(" ", "").replaceAll("\r\n","") );
				hasData = true;
			}
			if( hasData ) {
				out.write( fold(line.toString()) );
				out.write( "\r\n" );
			}
		}
	}

	public boolean isValid() {
		return isValid;
	}
}

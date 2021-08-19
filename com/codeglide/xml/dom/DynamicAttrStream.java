package com.codeglide.xml.dom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;

import org.w3c.dom.DOMException;

import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.util.StringInputStream;

public class DynamicAttrStream extends DynamicAttr {

	public DynamicAttrStream(DynamicElement parentNode, ObjectField fieldDefinition, InputStream value) {
		super(parentNode, fieldDefinition, value);
	}

	public DynamicAttrStream(DynamicElement parentNode, String fieldName, InputStream value) {
		super(parentNode, fieldName, value);
	}

	public void setInputStream(InputStream value) {
		this.value = value;
		trackChange();
	}
	
	public void _setInputStream(InputStream value) {
		this.value = value;
	}

	public InputStream getInputStream() {
		return (value!=null)? (InputStream)value:null;
	}

	public InputStream getPreviousInputStream() {
		return (oldValue!=null)? (InputStream)oldValue:null;
	}
	
	public String peekValue(int maxLength) {
		if( value == null )
			return "";
		StringBuffer result = new StringBuffer();
		int c, i = 0;
		try {
			while( (c = ((InputStream)value).read()) != -1 && (i++ < maxLength) ) 
				result.append((char)c);
			((InputStream)value).reset();
		} catch (IOException e) {
			Logger.debug(e);
		}
		return result.toString();
	}

	public String getValue() {
		if( value == null )
			return "";
		StringBuffer result = new StringBuffer();
		int c;
		try {
			while( (c = ((InputStream)value).read()) != -1 )
				result.append((char)c);
			((InputStream)value).reset();
		} catch (IOException e) {
			Logger.debug(e);
		}
		return result.toString();
	}

	public void setValue(String arg0) throws DOMException {
		try {
			this.value = new StringInputStream(arg0, "utf-8");
			trackChange();
		} catch (CharacterCodingException _) {}
	}

	public String getExpandedValue() {
		return getValue();
	}
	
	


}

package com.codeglide.xml.dom.util;

import org.w3c.dom.DOMException;

import com.codeglide.core.objects.ObjectField;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class StringTokenizerAttr extends DynamicAttr {
	private StringTokenizer tokenizer;
	
	public StringTokenizerAttr(DynamicElement parentNode, ObjectField fieldDefinition, StringTokenizer tokenizer, String value) {
		super(parentNode, fieldDefinition, value);
		this.tokenizer = tokenizer;
		
		// Parse this string
		this.tokenizer.parse((String)value);
	}

	public String getExpandedValue() {
		return getValue();
	}

	public String getValue() {
		if( tokenizer.hasChanged() ) {
			value = tokenizer.toString();
			tokenizer.resetChanged();
		}
		return ( value != null ) ? (String)value : "";
	}

	public void setValue(String value) throws DOMException {
		if( value == null && this.value == null )
			return;
		else if( value == null || this.value == null || !this.value.equals(value) ) {
			// Set the new value
			this.value = value;
			trackChange();

			// Parse this string
			this.tokenizer.parse(value);
		}
	}

}

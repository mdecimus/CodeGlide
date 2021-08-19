package com.codeglide.xml.dom;

import org.w3c.dom.DOMException;

import com.codeglide.core.objects.ObjectField;

public class DynamicAttrDate extends DynamicAttr {
	public DynamicAttrDate(DynamicElement parentNode, ObjectField fieldDefinition, String value) {
		super(parentNode, fieldDefinition, value);
	}
	
	public DynamicAttrDate(DynamicElement parentNode, String name, String value ) {
		super(parentNode, name, value);
	}

	public String getValue() {
		return ( value != null ) ? (String)value : "";
	}

	public void setValue(String value) throws DOMException {
		//TODO check timezones and format
		setIfChanged(value);
	}

	public String getExpandedValue() {
		//TODO format nicely
		return getValue();
	}

}

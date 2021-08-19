package com.codeglide.xml.dom;

import org.w3c.dom.DOMException;

import com.codeglide.core.objects.ObjectField;

public class DynamicAttrString extends DynamicAttr {

	public DynamicAttrString(DynamicElement parentNode, ObjectField fieldDefinition, String value) {
		super(parentNode, fieldDefinition, value);
	}
	
	public DynamicAttrString(DynamicElement parentNode, String name, String value ) {
		super(parentNode, name, value);
	}

	public String getValue() {
		return ( value != null ) ? (String)value : "";
	}

	public void setValue(String arg0) throws DOMException {
		setIfChanged(arg0);
	}

	public String getExpandedValue() {
		return getValue();
	}

}

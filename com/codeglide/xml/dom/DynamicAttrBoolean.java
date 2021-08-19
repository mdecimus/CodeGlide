package com.codeglide.xml.dom;

import org.w3c.dom.DOMException;

import com.codeglide.core.objects.ObjectField;

public class DynamicAttrBoolean extends DynamicAttr {

	public DynamicAttrBoolean(DynamicElement parentNode, ObjectField fieldDefinition, String value) {
		super(parentNode, fieldDefinition, value);
	}
	
	public DynamicAttrBoolean(DynamicElement parentNode, String name, String value ) {
		super(parentNode, name, value);
	}

	public String getValue() {
		return (value != null && !((String)value).isEmpty()) ? (String)value : "0";
	}

	public void setValue(String value) throws DOMException {
		setIfChanged(( value != null && (value.equals("1") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"))) ? "1" : "0");
	}

	public String getExpandedValue() {
		return getValue();
	}

}

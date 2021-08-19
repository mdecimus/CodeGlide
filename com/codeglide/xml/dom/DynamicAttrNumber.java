package com.codeglide.xml.dom;

import org.w3c.dom.DOMException;

import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;

public class DynamicAttrNumber extends DynamicAttr {
	public DynamicAttrNumber(DynamicElement parentNode, ObjectField fieldDefinition, String value) {
		super(parentNode, fieldDefinition, value);
	}
	
	public DynamicAttrNumber(DynamicElement parentNode, String name, String value ) {
		super(parentNode, name, value);
	}

	public String getValue() {
		return (value != null && !((String)value).isEmpty()) ? (String)value : "0";
	}

	public void setValue(String value) throws DOMException {
		try {
			if( value == null || value.isEmpty() ) {
				value = "0";
			} else if( fieldDefinition.getFormat() == ObjectField.F_INTEGER ) {
				value = String.valueOf(Integer.parseInt(value));
			} else if( fieldDefinition.getFormat() == ObjectField.F_DOUBLE ) {
				value = String.valueOf(Double.parseDouble(value));
			}
			setIfChanged(value);
		} catch (NumberFormatException e) {
			Logger.debug("Invalid number format '" + value + "' for field '" + fieldDefinition.getId() + "'.");
		}
	}

	public String getExpandedValue() {
		return getValue();
	}

}

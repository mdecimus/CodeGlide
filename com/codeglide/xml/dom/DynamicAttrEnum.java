package com.codeglide.xml.dom;

import java.util.HashMap;

import org.w3c.dom.DOMException;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.exceptions.ExpressionException;

public class DynamicAttrEnum extends DynamicAttr {
	private HashMap<String, Expression> enumMap = null;
	
	public DynamicAttrEnum(DynamicElement parentNode, ObjectField fieldDefinition, String value) {
		super(parentNode, fieldDefinition, value);
		enumMap = (HashMap<String, Expression>) fieldDefinition.getSetting(ObjectField.E_LIST);
	}

	public String getValue() {
		return (value != null) ? (String)value : "";
	}

	public void setValue(String value) throws DOMException {
		if( value == null || value.isEmpty() || enumMap.containsKey(value) ) 
			setIfChanged(value);
		else
			Logger.debug("Invalid value '" + value + "' specified for field '" + fieldDefinition.getId() + "'.");
	}

	public String getExpandedValue() {
		String result = null;
		try {
			if( value != null && !((String)value).isEmpty() )
				result = enumMap.get((String)value).evaluate(null, parentNode);
		} catch (ExpressionException _) {
		}
		return (result != null) ? (String)result : "";
	}

}

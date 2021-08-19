package com.codeglide.xml.dom;

import org.w3c.dom.DOMException;

import com.codeglide.core.Expression;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.exceptions.ExpressionException;

public class DynamicAttrCalculated extends DynamicAttr {

	public DynamicAttrCalculated(DynamicElement parentNode, ObjectField fieldDefinition ) {
		super(parentNode, fieldDefinition);
	}

	public DynamicAttrCalculated(DynamicElement parentNode, String name, Expression value) {
		super(parentNode, name, value);
	}
	
	public String getValue() {
		Expression expression = (fieldDefinition != null) ? fieldDefinition.getBind() : (Expression)value;
		try {
			if( expression != null )
				return ((Expression)expression).evaluate(null, parentNode);
		} catch (ExpressionException _) {
		}
		return "";
	}

	public void setValue(String arg0) throws DOMException {
		// Ignored - we don't allow changing calculated expressions
	}

	public String getExpandedValue() {
		return getValue();
	}

}

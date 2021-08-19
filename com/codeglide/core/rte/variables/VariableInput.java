package com.codeglide.core.rte.variables;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;

public class VariableInput extends Variable {
	private Expression inputListExpression;
	private String inputObject;

	//TODO remove this class, improve inputList
	public VariableInput(Item parent, String name, short variableType, short variableContext, String inputObject ) {
		super(parent, name, variableType, variableContext);
		this.inputObject = inputObject;
	}

	public VariableInput(Item parent, Element element) {
		super(parent, element);
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		inputListExpression = getExpression(element, "inputList");
	}

	public Expression getInputListExpression() {
		return inputListExpression;
	}

	public String getInputObject() {
		return inputObject;
	}
	
}

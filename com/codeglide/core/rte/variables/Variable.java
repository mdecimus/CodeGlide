package com.codeglide.core.rte.variables;

import org.w3c.dom.Element;

import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;

public class Variable extends Item {
	
	public final static short LOCAL = 0;
	public final static short INPUT = 1;
	public final static short OUTPUT = 2;
	
	private short variableType;
	private short variableContext;

	private String name;
	
	public Variable(Item parent, String name, short variableType, short variableContext ) {
		super(parent);
		this.name = name;
		this.variableType = variableType;
		this.variableContext = variableContext;
	}
	
	public Variable(Item parent, Element element) {
		super(parent, element);
	}

	public String getVariableName() {
		return name;
	}
	
	public short getVariableType() {
		return this.variableType;
	}
	
	public short getVariableContext() {
		return this.variableContext;
	}

	protected void parseElement(Element element, Application application) {
		// Get name
		this.name = element.getAttribute("name");

		// Get type
		String type = element.getAttribute("type");
		if (type.equalsIgnoreCase("string"))
			variableType = VariableHolder.STRING;
		else if (type.equalsIgnoreCase("number"))
			variableType = VariableHolder.NUMBER;
		else if (type.equalsIgnoreCase("boolean"))
			variableType = VariableHolder.BOOLEAN;
		else if (type.equalsIgnoreCase("object"))
			variableType = VariableHolder.OBJECT;
		else if (type.equalsIgnoreCase("objectarray"))
			variableType = VariableHolder.OBJECTARRAY;
		else
			Logger.warn("Unknown variable type '" + type + "'.");
		
		// Get context
		String context = element.getAttribute("context");
		if( context != null && !context.isEmpty() ) {
			if (context.equalsIgnoreCase("input"))
				variableContext = Variable.INPUT;
			else if (context.equalsIgnoreCase("local"))
				variableContext = Variable.LOCAL;
			else if (context.equalsIgnoreCase("output"))
				variableContext = Variable.OUTPUT;
			else
				Logger.warn("Unknown variable context '" + context + "'.");
		}
	}

}

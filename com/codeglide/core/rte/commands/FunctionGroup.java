package com.codeglide.core.rte.commands;

import java.util.HashMap;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;

public class FunctionGroup extends Item {
	protected HashMap<String, Function> functionsMap;
	
	public FunctionGroup(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		functionsMap = new HashMap<String, Function>();
		for( Element child : getChildrenElements(element) ) {
			functionsMap.put(child.getAttribute("name").toLowerCase(), new Function(this, child));
		}
	}

}

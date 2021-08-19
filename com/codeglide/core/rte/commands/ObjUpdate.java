package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.ExtendedElement;

public class ObjUpdate extends Command {
	private Expression targetExpression;
	
	public ObjUpdate(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		targetExpression = getExpression(element, "target");
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		DynamicElement targetNode = (DynamicElement)targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
		if( targetNode instanceof ExtendedElement )
			((ExtendedElement)targetNode).update();
		else {
			DynamicElement parent = (DynamicElement)targetNode.getParentNode();
			while( parent != null ) {
				if( parent instanceof ExtendedElement ) {
					((ExtendedElement)parent).update();
					parent = null;
				} else
					parent = (DynamicElement)targetNode.getParentNode();
			}
		}
		
	}

}

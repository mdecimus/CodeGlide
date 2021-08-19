package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;

public class Separator extends Widget {
	private Expression typeExpression;
	
	public Separator(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.SEPARATOR);
		action.addParameter(ActionParameter.VALUE, typeExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		result.add(action);
	}

	protected void parseElement(Element element, Application application) {
		typeExpression = getExpression(element, "type");
	}

}

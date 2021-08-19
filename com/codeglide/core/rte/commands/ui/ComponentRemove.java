package com.codeglide.core.rte.commands.ui;

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
import com.codeglide.core.rte.widgets.Widget;

public class ComponentRemove extends Widget {
	private Expression idExpression;
	
	public ComponentRemove(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.REMOVE_COMPONENT);
		action.addParameter(ActionParameter.ID, generateWidgetId(context, idExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode())));
		result.add(action);
	}

	protected void parseElement(Element element, Application application) {
		idExpression = getExpression(element, "id");
	}

}

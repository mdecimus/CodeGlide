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

public class ShowAlert extends Widget {
	private Expression titleExpression, messageExpression, typeExpression;

	public ShowAlert(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.SHOW_ALERT);
		action.addParameter(ActionParameter.ID, generateWidgetId((ContextUi)context, this));
		action.addParameter(ActionParameter.TITLE, titleExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		action.addParameter(ActionParameter.MESSAGE, messageExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		if( typeExpression != null )
			action.addParameter(ActionParameter.TYPE, typeExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		result.add(action);
	}

	protected void parseElement(Element element, Application application) {
		titleExpression = getExpression(element, "title");
		messageExpression = getExpression(element, "message");
		typeExpression = getExpression(element, "type");
	}

}

/*
 * 
 * 	protected Action cmdShowAlert( String title, String message, String type ) {
		Action result = new Action(WidgetElement.SHOW_ALERT);
		result.addParameter("title", title);
		result.addParameter("msg", message);
		result.addParameter("icon", type);
		return result;
	}

 * 
 * 
 */




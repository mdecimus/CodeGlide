package com.codeglide.core.rte.commands.ui;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.CustomEventHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.widgets.Widget;

/*
 * 
 * BUTTONS
 * 
 */

public class ShowMessage extends Widget implements CustomEventHandler {
	private Expression titleExpression, messageExpression, typeExpression, buttonsExpression, iconExpression;
	protected HashMap<String, Function> functionsMap;

	public ShowMessage(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		titleExpression = getExpression(element, "title");
		messageExpression = getExpression(element, "message");
		typeExpression = getExpression(element, "type");
		iconExpression = getExpression(element, "icon");
		buttonsExpression = getExpression(element, "buttons");
		functionsMap = new HashMap<String, Function>();
		application.getWidgetBucket().registerObject(this);
		for( Element container : getChildrenElements(element) ) {
			if( container.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element child : getChildrenElements(container) )
					functionsMap.put(child.getAttribute("name").toLowerCase(), new Function(this, child));
			}
		}
	}

	public void handleEvent(ContextUi context, List<Action> result,
			String eventName, Object input) throws CodeGlideException {
		Function function = functionsMap.get(eventName.toLowerCase());
		if( function != null )
			function.run(context, result, input);
	}

	protected void render(ContextUi context, List<Action> result)
		throws CodeGlideException {
		Action action = new Action(ActionType.SHOW_MESSAGE);
		action.addParameter(ActionParameter.ID, generateWidgetId((ContextUi)context, this));
		action.addParameter(ActionParameter.TITLE, titleExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		action.addParameter(ActionParameter.MESSAGE, messageExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		action.addParameter(ActionParameter.TYPE, typeExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		if( buttonsExpression != null )
			action.addParameter(ActionParameter.BUTTONS, buttonsExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		if( iconExpression != null )
			action.addParameter(ActionParameter.ICON, iconExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		result.add(action);
	}

}

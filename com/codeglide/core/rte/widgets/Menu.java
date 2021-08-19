package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.CommandGroup;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;

public class Menu extends CommandGroup {
	private Expression nameExpression, iconExpression;
	
	public Menu(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		nameExpression = getExpression(element, "name");
		iconExpression = getExpression(element, "icon");
	}
	
	public void run(Context context, List<Action> result) throws CodeGlideException {
		Action action = new Action(ActionType.MENU);
		if( nameExpression != null )
			action.addParameter(ActionParameter.NAME, nameExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		if( iconExpression != null )
			action.addParameter(ActionParameter.ICON, iconExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		List<Action> children = action.addChildren();
		result.add(action);
		super.run(context, children);
	}
	
	
}

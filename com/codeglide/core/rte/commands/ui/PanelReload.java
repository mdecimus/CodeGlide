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

public class PanelReload extends Widget {
	private Expression idExpression;
	
	public PanelReload(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		
		Widget panel = context.getApplication().getWidgetById(idExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		if( panel != null ) {
			Action action = new Action(ActionType.REPLACE_COMPONENT);
			action.addParameter(ActionParameter.ID, generateWidgetId(context, panel));
			result.add(action);
			List<Action> children = action.addChildren();
			panel.run(context, children);
		}
		
		/*Widget panel = context.getApplication().getWidgetById(idExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		Action action = new Action(ActionType.REMOVE_COMPONENT);
		action.addParameter(ActionParameter.ID, generatePanelId(context, panel));
		result.add(action);

		Item runner = panel.getParent();
		while( runner != null && !(runner instanceof Panel) )
			runner = runner.getParent();
		
		if( runner != null ) {
			Widget parentPanel = (Widget)runner;
			action = new Action(ActionType.ADD_COMPONENT);
			action.addParameter(ActionParameter.ID, generatePanelId(context, panel));
			action.addParameter(ActionParameter.TARGET, generatePanelId(context, parentPanel));
			result.add(action);
			List<Action> children = action.addChildren();
			panel.run(context, children);
		}*/
	}

	protected void parseElement(Element element, Application application) {
		idExpression = getExpression(element, "id");
	}

}

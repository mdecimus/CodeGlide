package com.codeglide.core.rte.widgets;

import java.util.HashMap;
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
import com.codeglide.xml.dom.DynamicElement;

public class DataViewPanel extends ListPanel {
	private Expression templateExpression;
	
	public DataViewPanel(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.DATAVIEW);
		HashMap<String, ListField> fieldsMap = getFieldsMap(context);
		addParameters(context, action, fieldsMap, (DynamicElement)displaySettingsExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE));
		action.addParameter(ActionParameter.TEMPLATE, templateExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		setRenderParameters(context, action);
		result.add(action);
		List<Action> children = action.addChildren();
		super.render(context, children);
	}

}

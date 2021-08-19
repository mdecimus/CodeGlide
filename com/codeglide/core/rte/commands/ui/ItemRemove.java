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
import com.codeglide.xml.dom.DynamicElement;

public class ItemRemove extends Widget {
	private Expression targetExpression, idExpression;
	
	public ItemRemove(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.ITEM_REMOVE);
		Widget widget = context.getApplication().getWidgetById(targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		action.addParameter(ActionParameter.TARGET, generateWidgetId(context, widget));
		if( idExpression != null ) {
			DynamicElement node = (DynamicElement)idExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
			action.addParameter(ActionParameter.ID, generateElementId(context, widget, node));
			removeElement(context, widget, node);
		}
		result.add(action);
	}

	protected void parseElement(Element element, Application application) {
		idExpression = getExpression(element, "id");
		targetExpression = getExpression(element, "target");
	}

}

/*
 * 
 * 			Action action = new Action(widget.getType());
			VisibleWidget tree = request.getSession().getApplication().getWidgetById(widget.getValue(WidgetElement.TARGET).evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
			action.addParameter("treeId", generateWidgetId((GuiRequest)request, tree));
			if( widget.getValue(WidgetElement.ID) != null ) {
				DynamicElement node = (DynamicElement)widget.getValue(WidgetElement.ID).evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
				action.addParameter("nodeId", generateElementId((GuiRequest)request, tree, node));
				if( widget.getType() == WidgetElement.TREE_REMOVE )
					removeElement((GuiRequest)request, tree, node);
			} else
				action.addParameter("nodeId", "root");
			result.addAction(action);

 * 
 */



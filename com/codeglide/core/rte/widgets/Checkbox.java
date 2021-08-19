package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;

public class Checkbox extends Field {

	public Checkbox(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.CHECKBOX);
		action.addParameter(ActionParameter.VALUE, 
				resolveFieldValue(addFieldProperties(context, action), false));
		result.add(action);
	}

}

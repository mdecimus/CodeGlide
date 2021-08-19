package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;

/*
 * 
 * Flags:
 * 
 * PASSWORDFIELD
 * 
 * 
 */

public class Textfield extends Field {

	public Textfield(Item parent, Element element) {
		super(parent, element);
	}

	//TODO add formatting
	
	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.TEXTFIELD);
		action.addParameter(ActionParameter.VALUE, 
				resolveFieldValue(addFieldProperties(context, action), false));
		result.add(action);
	}

}

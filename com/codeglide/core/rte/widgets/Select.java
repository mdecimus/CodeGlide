package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.xml.dom.DynamicAttr;

/*
 * 
 * Flags:
 * 
 * EDITABLE
 * 
 * 
 */


public class Select extends Itemlist {

	public Select(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		String value = null;
		ObjectField extendField = null;

		Action action = new Action(ActionType.SELECT);
		{
			Object[] data = addFieldProperties(context, action);

			// Obtain value from DynamicAttr
			if (data[0] != null)
				value = ((DynamicAttr) data[0]).getValue();

			// If value is null, use the default
			if (value == null || value.isEmpty())
				value = (String) data[2];

			extendField = (ObjectField) data[1];
		}
		action.addParameter(ActionParameter.VALUE, value);
		action.addRecords(getRecords(context, extendField));

		result.add(action);
	}

}

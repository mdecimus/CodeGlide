package com.codeglide.core.rte.widgets;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.xml.dom.DynamicAttr;

public class Itemselector extends Itemlist {

	public Itemselector(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		String value = null;
		ObjectField extendField = null;

		Action action = new Action(ActionType.ITEMSELECTOR);
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

		// Get records
		List<Record> items = getRecords(context, extendField);
		action.addRecords(items);

		ArrayList<String> itemSelect = null;
		if (value != null && !value.isEmpty()) {
			String[] elements = value.split(",");
			itemSelect = new ArrayList<String>();
			for (int i = 0; i < elements.length; i++) {
				itemSelect.add(elements[i]);
			}
			
			for( Record item : items ) {
				if( itemSelect.contains(item.getStringField("id")) )
					item.addField("selected", true);
			}
		}
		
		result.add(action);
	}

}

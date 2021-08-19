package com.codeglide.core.rte.widgets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Logger;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.util.ISO8601;

public class DateTimeField extends Field {
	public DateTimeField(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.DATEFIELD);
		String value = resolveFieldValue(addFieldProperties(context, action), false);
		if( value != null && !value.isEmpty() ) {
			try {
				DateFormat ISO8601f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				ISO8601f.setTimeZone(context.getRootNode().getTimezone());
				value = ISO8601f.format(ISO8601.parseDate(value));
			} catch (Exception _) {
				Logger.debug("DateTimeField could not format date '"+value+"'.");
			}
		}
		action.addParameter(ActionParameter.VALUE, value);
		result.add(action);
	}

	public void handleSet(ContextUi context, List<Action> result,
			SequenceBucketizable target, String value) {
		if( value != null && !value.isEmpty() ) {
			try {
				DateFormat ISO8601f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				ISO8601f.setTimeZone(context.getRootNode().getTimezone());
				value = ISO8601.formatUtc(ISO8601f.parse(value));
			} catch (Exception _) {
				Logger.debug("DateTimeField got invalid date '"+value+"'.");
			}
		}
		super.handleSet(context, result, target, value);
	}
	
	

}

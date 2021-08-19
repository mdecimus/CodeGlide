package com.codeglide.core.rte.widgets;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.CommandGroup;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionType;

public class Contents extends CommandGroup {

	public Contents(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
	}
	
	public void run(Context context, List<Action> result) throws CodeGlideException {
		Action action = new Action(ActionType.CONTENTS);
		List<Action> children = action.addChildren();
		result.add(action);
		super.run(context, children);
	}
}

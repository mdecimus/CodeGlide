package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.CommandGroup;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionType;

public class Buttons extends CommandGroup {
	private ActionType buttonsType;
	
	public Buttons(Item parent, Element element) {
		super(parent, element);
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		String nodeName = element.getNodeName().toLowerCase();
		if( nodeName.equals("topbar") )
			buttonsType = ActionType.TOPBAR;
		else if( nodeName.equals("bottombar") )
			buttonsType = ActionType.BOTTOMBAR;
		else
			buttonsType = ActionType.BUTTONS;
	}
	
	public void run(Context context, List<Action> result) throws CodeGlideException {
		Action action = new Action(buttonsType);
		List<Action> children = action.addChildren();
		result.add(action);
		super.run(context, children);
	}


}

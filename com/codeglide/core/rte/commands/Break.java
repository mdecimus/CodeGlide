package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public class Break extends Command {
	public Break(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		throw new com.codeglide.core.rte.exceptions.Break();
	}

}

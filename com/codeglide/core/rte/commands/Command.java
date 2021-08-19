package com.codeglide.core.rte.commands;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.interfaces.Runnable;

public abstract class Command extends Item implements Runnable {

	public Command(Item parent, Element element) {
		super(parent, element);
	}

}

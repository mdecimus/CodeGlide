package com.codeglide.core.rte.commands;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.Runnable;
import com.codeglide.core.rte.render.Action;

public class CommandGroup extends Item implements Runnable {
	protected LinkedList<Item> children;

	public CommandGroup(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		for( Element child : getChildrenElements(element) ) {
			Item item = Item.createItem(this, child);
			if( item != null )
				addChild(item);
		}
	}

	public void run(Context context, List<Action> result) throws CodeGlideException {
		if( children != null ) {
			for( Item item : children ) {
				if( item instanceof Runnable ) {
					((Runnable)item).run(context, result);
				}
			}
		}
	}
	
	public Item addChild(Item child) {
		if( children == null )
			children = new LinkedList<Item>();
		children.add(child);
		return child;
	}

	public List<Item> getChildren() {
		return children;
	}

}

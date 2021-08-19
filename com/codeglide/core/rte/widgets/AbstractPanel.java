package com.codeglide.core.rte.widgets;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.Runnable;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;

/*
 * 
 * TITLE    = "My panel title"
 * PADDING  = 00px
 * LAYOUT   = fit, border, accordion
 * BACKGROUND_COLOR = color
 * 
 * HtmlPanel
 * 
 * VALUE    = HTML contents of this panel
 * 
 * BorderLayout
 * 
 * REGION   = north, south, center, east, west
 * MARGINS  = 0 0 0 0
 * 
 * 
 * Flags
 * 
 * RELOADABLE
 * BORDER
 * COLLAPSIBLE (BorderLayout)
 * SPLIT  (BorderLayout)
 * LABELSONTOP (Form Layout)
 * 
 */

public abstract class AbstractPanel extends Widget {
	protected LinkedList<Item> children;
	
	public AbstractPanel(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		if( children != null ) {
			for( Item item : children ) {
				if( item instanceof Runnable ) {
					((Runnable)item).run(context, result);
				}
			}
		}
	}

	protected void parseElement(Element element, Application application) {
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("buttons") || containers.getNodeName().equalsIgnoreCase("topbar") || containers.getNodeName().equalsIgnoreCase("bottombar")) {
				addChild(new Buttons(this, containers));
			} else if( containers.getNodeName().equalsIgnoreCase("contents") ) {
				addChild(new Contents(this, containers));
			}
		}
		addRenderParameter(element, ActionParameter.TITLE, "title");
		addRenderParameter(element, ActionParameter.PADDING, "padding");
		addRenderParameter(element, ActionParameter.LAYOUT, "layout");
		addRenderParameter(element, ActionParameter.MARGINS, "margins");
		addRenderParameter(element, ActionParameter.REGION, "region");
		addRenderParameter(element, ActionParameter.BACKGROUNDCOLOR, "backgroundColor");
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

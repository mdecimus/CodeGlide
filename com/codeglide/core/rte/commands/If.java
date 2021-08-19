package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.Break;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public class If extends Command {
	private Expression test;
	private IfCommandGroup thenBlock, elseBlock;
	
	public If(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		test = new Expression(element.getAttribute("test"));
		for( Element child : getChildrenElements(element) ) {
			if( child.getNodeName().equalsIgnoreCase("then") )
				thenBlock = new IfCommandGroup(this, child);
			else if( child.getNodeName().equalsIgnoreCase("else") )
				elseBlock = new IfCommandGroup(this, child);
		}
	}

	public void run(Context context, List<Action> result) throws CodeGlideException {
		IfCommandGroup nextItem = ( (Boolean)test.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.BOOLEAN) ) ? thenBlock : elseBlock;
		
		try {
			if( nextItem != null )
				nextItem.run(context, result);
		} catch (Break _) {
		}
		
	}

	private class IfCommandGroup extends CommandGroup {

		public IfCommandGroup(Item parent, Element element) {
			super(parent, element);
		}
		
	}
	
}

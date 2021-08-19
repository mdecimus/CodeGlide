package com.codeglide.core.rte.commands;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.Break;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public class Case extends Command {
	private CommandGroup elseBlock;
	private LinkedList<EvalItem> evalList;
	
	public Case(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		evalList = new LinkedList<EvalItem>();
		for( Element child : getChildrenElements(element) ) {
			if( child.getNodeName().equalsIgnoreCase("if") ) 
				evalList.add(new EvalItem(new Expression(child.getAttribute("test")), new CommandGroup(this, child)));
			else if( child.getNodeName().equalsIgnoreCase("else") )
				elseBlock = new CommandGroup(this, child);
		}
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		try {
			for( EvalItem item : evalList ) {
				if( (Boolean)item.testExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.BOOLEAN) ) {
					item.testBlock.run(context, result);
					return;
				}
			}
			if( elseBlock != null )
				elseBlock.run(context, result);
		} catch (Break _) {
		}
	}
	
	private class EvalItem {
		CommandGroup testBlock = null;
		Expression testExpression = null;
		EvalItem(Expression testExpression, CommandGroup testBlock) {
			this.testExpression = testExpression;
			this.testBlock = testBlock;
		}
	}

}

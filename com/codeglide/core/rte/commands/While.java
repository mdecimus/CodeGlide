package com.codeglide.core.rte.commands;

import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.Break;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;

public class While extends CommandGroup {
	private Expression test;
	
	public While(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		test = new Expression(element.getAttribute("test"));
	}

	public void run(Context context, List<Action> result) throws CodeGlideException {
		int loopCount = 0;
		boolean doContinue = true;
		while( doContinue && (Boolean)test.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.BOOLEAN) ) {
			loopCount++;
			if( loopCount > 10000 )
				throw new RuntimeError("@runtime-infinite-loop");
			//System.out.println("Pass " + loopCount);
			try {
				super.run(context, result);
			} catch (Break _) {
				doContinue = false;
			}
		}
	}

}

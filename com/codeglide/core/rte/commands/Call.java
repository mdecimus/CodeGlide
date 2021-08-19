package com.codeglide.core.rte.commands;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.variables.VariableResolver;

public class Call extends Command {
	private Expression cmdExpression, outputExpression;
	private Collection<Expression> inputExpressions;
	
	public Call(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		cmdExpression = getExpression(element, "cmd");
		outputExpression = getExpression(element, "output");
		inputExpressions = Function.parseInputParameters(element);
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		String fncName = cmdExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
		Function fnc = context.getApplication().getFunction(fncName.toLowerCase());
		if( fnc == null )
			throw new RuntimeError("@runtime-missing-fnc,"+fncName);
		VariableResolver variables = context.getVariables();
		
		// Run function
		Object output = fnc.run(context, result, inputExpressions);
		
		// Set output, if any
		if( outputExpression != null ) {
			String outputVariable = outputExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
			if( outputVariable != null && variables.getVariableType(outputVariable) == -1 )
				throw new RuntimeError("@runtime-undefined-output," + fncName);
			variables.setVariable( outputVariable, output );
		}
	}
}

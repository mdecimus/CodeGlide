package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableResolver;

public class Setvar extends Command {
	private String target;
	private Expression sourceExpression;
	
	public Setvar(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		target = element.getAttribute("target");
		sourceExpression = new Expression(element.getAttribute("source"));
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		Object setResult = null;
		VariableResolver variables = context.getVariables();
		
		// value="" resets the value to null
		if( !sourceExpression.isType(Expression.T_STRING) || !sourceExpression.toString().isEmpty() ) {
			switch( variables.getVariableType(target) ) {
				case -1:
					throw new RuntimeError("@runtime-undefined-variable," + target);
				case VariableHolder.STRING:
					setResult = sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
					break;
				case VariableHolder.NUMBER:
					setResult = sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NUMBER);
					break;
				case VariableHolder.BOOLEAN:
					setResult = sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.BOOLEAN);
					break;
				case VariableHolder.OBJECT:
					setResult = sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
					break;
				default:
					setResult = sourceExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
					break;
			}
		}
		//Logger.debug("SetVariable ["+target+"] = "+((setResult!=null)?("["+setResult+"]"):"NULL"));
		variables.setVariable(target, setResult);
	}

}

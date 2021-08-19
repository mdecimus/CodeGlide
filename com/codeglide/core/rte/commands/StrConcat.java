package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.xml.dom.DynamicAttr;

public class StrConcat extends Command {
	private Expression targetExpression, sourceExpression;
	private String textContent;
	
	public StrConcat(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		sourceExpression = getExpression(element, "source");
		targetExpression = getExpression(element, "target");
		textContent = element.getTextContent();
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		StringBuffer strBuf = new StringBuffer();
		String addStr = targetExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
		if( addStr != null && !addStr.isEmpty())
			strBuf.append(addStr);
		if( sourceExpression != null)
			strBuf.append(sourceExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		else if( textContent != null )
			strBuf.append(textContent);
		((DynamicAttr)targetExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE)).setValue(strBuf.toString());

	}

}

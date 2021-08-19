package com.codeglide.core.rte.commands;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextWriter;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.xml.dom.DynamicAttr;

public class StrCopy extends Command {
	private Expression targetExpression, sourceExpression;
	private String textContent;
	
	public StrCopy(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		sourceExpression = getExpression(element, "source");
		targetExpression = getExpression(element, "target");
		textContent = element.getTextContent();
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		String strResult = null;
		if( sourceExpression != null)
			strResult = sourceExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
		else
			strResult = textContent;
		if( context instanceof ContextWriter ) {
			try {
				((ContextWriter)context).getWriter().write(strResult);
			} catch (IOException _) {
			}
		} else
			((DynamicAttr)targetExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE)).setValue(strResult);

	}

}

package com.codeglide.core.rte.commands.ui;

import java.io.StringWriter;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.CommandGroup;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.contexts.ContextWriter;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.xml.dom.DynamicAttr;

public class StrWriter extends CommandGroup {
	private Expression targetExpression;
	private String targetVariable;
	
	public StrWriter(Item parent, Element element) {
		super(parent, element);
	}

	public void run(Context context, List<Action> result) throws CodeGlideException {
		ContextUi uiContext = (ContextUi)context;
		ContextWriter writerContext = new ContextWriter(uiContext.getCurrentSession());
		StringWriter stringWriter = new StringWriter();
		writerContext.setPanelId(uiContext.getPanelId());
		writerContext.setWindowId(uiContext.getWindowId());
		writerContext.setVariables(uiContext.getVariables());
		writerContext.setRootNode(uiContext.getRootNode());
		writerContext.setWriter(stringWriter);
		super.run(writerContext, result);
		if( targetVariable != null )
			uiContext.getVariables().setVariable(targetVariable, stringWriter.toString());
		else
			((DynamicAttr)targetExpression.evaluate(context.getVariables(), context.getDocumentNode(), Expression.NODE)).setValue(stringWriter.toString());
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		targetExpression = getExpression(element, "target");
		if( targetExpression.isType(Expression.T_STRING) ) {
			targetVariable = targetExpression.toString();
			targetExpression = null;
		}
	}
	
}

package com.codeglide.core.rte.widgets;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.contexts.ContextWriter;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;

public class Link extends Button {

	public Link(Item parent, Element element) {
		super(parent, element);
	}
	
	protected void render(ContextUi context, List<Action> result) throws CodeGlideException {
		if( context instanceof ContextWriter ) {
			try {
				Writer writer = ((ContextWriter)context).getWriter();
				writer.write("{");
				writer.write(generateWidgetId(context, this));
				if( nameExpression != null ) {
					writer.write(";");
					writer.write(nameExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
				}
				writer.write("}");
			} catch (IOException _) {
			}
		} else
			super.render(context, result);
	}
}

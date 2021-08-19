package com.codeglide.core.rte.commands;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class Setfield extends Command {
	private Expression targetExpression;
	
	private LinkedList<SetEntry> setEntries;
	
	public Setfield(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		targetExpression = getExpression(element, "target");
		setEntries = new LinkedList<SetEntry>();
		
		addEntry(getExpression(element, "id"), getExpression(element, "name"), getExpression(element, "value"));

		for( Element child : getChildrenElements(element) ) 
			addEntry(getExpression(child, "id"), getExpression(child, "name"), getExpression(child, "value"));
	}
	
	private void addEntry(Expression idExpression, Expression nameExpression, Expression valueExpression) {
		if( valueExpression == null || (nameExpression == null && idExpression == null))
			return;
		setEntries.add(new SetEntry(idExpression, nameExpression, valueExpression));
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		DynamicElement targetNode = (DynamicElement) targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODE);
		
		for( SetEntry entry : setEntries ) {
			String value = entry.valueExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
			if( entry.idExpression != null ) {
				String fieldId = entry.idExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
				ObjectField field = context.getApplication().getField(fieldId);
				if( field != null )
					((DynamicAttr)field.getBind().evaluate(context.getVariables(), targetNode, Expression.NODE)).setValue(value);
				else
					Logger.debug("Field '" + fieldId + "' does not exist, setField failed.");
			} else
				targetNode.setAttribute(entry.nameExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()), value);
		}

	}
	
	protected class SetEntry {
		Expression idExpression, nameExpression, valueExpression;

		public SetEntry(Expression idExpression, Expression nameExpression,
				Expression valueExpression) {
			this.idExpression = idExpression;
			this.nameExpression = nameExpression;
			this.valueExpression = valueExpression;
		}
		
	}

}

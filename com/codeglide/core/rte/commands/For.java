package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.Break;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.xml.dom.DynamicElement;

public class For extends CommandGroup implements SequenceBucketizable {
	private String varName;
	private Expression each;
	
	public For(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		each = new Expression(element.getAttribute("each"));
		varName = element.getAttribute("var");
		seqId = -1;
		application.getWidgetBucket().registerObject(this);
	}
	
	public String getVariableName() {
		return varName;
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		
		NodeList items = (NodeList)each.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODELIST);
		VariableResolver variables = context.getVariables();
		VariableHolder varHolder = new VariableHolder(), previousCallVariables = null;
		String varHolderName = "FOR"+String.valueOf(getSequenceId());

		// Retrieve variables of a previous call of this function (when used recursively)
		previousCallVariables = variables.getVariables(varHolderName);
		
		// Set variables
		variables.addVariables(varHolderName, varHolder);
		try {
			for( int i = 0; i < items.getLength(); i++ ) {
				varHolder.setVariable(varName, (DynamicElement)items.item(i));
				try {
					super.run(context, result);
				} catch (Break _) {
					i = items.getLength() + 1;
				}
				varHolder.undefineVariable(varName);
			}
		} finally {
			if( previousCallVariables != null )
				variables.addVariables(varHolderName, previousCallVariables);
			else
				variables.removeVariables(varHolderName);
		}
	}

	// Sequence ID
	private int seqId;
	
	public int getSequenceId() {
		return seqId;
	}

	public void setSequenceId(int id) {
		this.seqId = id;
	}

}

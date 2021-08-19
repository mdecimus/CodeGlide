package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.For;
import com.codeglide.core.rte.commands.While;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.SetStringHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrString;

/*
 * 
 * Flags:
 * 
 * REQUIRED
 * 
 * 
 */

public abstract class Field extends Widget implements SetStringHandler {
	protected Expression bindExpression, nameExpression, defaultExpression, extendsExpression;
	protected boolean persistAttribute;
	
	public Field(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		bindExpression = getExpression(element, "bind");
		nameExpression = getExpression(element, "name");
		defaultExpression = getExpression(element, "default");
		extendsExpression = getExpression(element, "extends");
		persistAttribute = bindExpression != null && (getAncestor(For.class) != null || getAncestor(While.class) != null ) && bindExpression.hasVariables();
	}

	public void handleSet(ContextUi context, List<Action> result, SequenceBucketizable target, String value ) {
		Node setNode = null;
		
		// Convert empty strings to null
		if( value != null && value.isEmpty() )
			value = null;
		
		if( bindExpression != null ) {
			try {
				if( persistAttribute && target != null )
					setNode = (Node)target;
				else if( bindExpression.isType(Expression.T_STRING) ) {
					String varName = bindExpression.toString();
					if( context.getVariables().getVariableType(varName) == VariableHolder.STRING ) 
						context.getVariables().setVariable(varName, value);
					else
						Logger.debug("Variable '" + varName + "' is not defined as STRING, couldn't set values for widget " + this.getClass().getName() + ".");
					return;
				} else
					setNode = (Node)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
			} catch (Exception _) {}
		}
		
		if( setNode != null ) {
			if( setNode instanceof DynamicAttr )
				((DynamicAttr)setNode).setValue(value);
			else
				Logger.debug("Field bound to node '" + setNode.getNodeName() + "' which is not an Attribute.");
		} else
			Logger.debug("Could not resolve to a Field while setting values for widget " + this.getClass().getName() + ".");

	}
	
	protected String resolveFieldValue( Object[] data, boolean useExpanded ) {
		DynamicAttr attr = (DynamicAttr)data[0];
		String result = (String)data[2];
		
		if( attr != null) {
			if( useExpanded && attr.getExpandedValue() != null ) 
				result = attr.getExpandedValue();
			else if( attr.getValue() != null )
				result = attr.getValue();
		}
		return result;
	}

	protected Object[] addFieldProperties( ContextUi context, Action action ) throws CodeGlideException {
		String fieldName = null;
		ObjectField extendField = null;
		DynamicAttr attr = null;
		
		// Obtain DynamicAttr
		try {
			if( bindExpression != null ) {
				if( bindExpression.isType(Expression.T_STRING) ) {
					// Bound to a Variable
					Object varValue = context.getVariables().resolveVariable(bindExpression.toString());
					if( varValue == null )
						varValue = "";
					attr = new DynamicAttrString(null, "", varValue.toString());
				} else {
					// Bound to an attribute
					attr = (DynamicAttr)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
					extendField = attr.getFieldDefinition();
				}
			}
		} catch (Exception _) {}
		
		// Obtain extendedField, if any
		try {
			if( extendField == null && extendsExpression != null )
				extendField = context.getApplication().getField(extendsExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		} catch (Exception _) {}

		// Set Widget ID
		if( persistAttribute && attr != null ) {
			//removeAllElements(context, this);
			action.addParameter(ActionParameter.ID, generateWidgetId(context, this) + "." + generateElementId(context, this, attr) );
		} else
			action.addParameter(ActionParameter.ID, generateWidgetId(context, this));
		//action.addParameter(ActionParameter.ID, (extendField != null && (attr == null || attr.getFieldDefinition() == null)) ? (generateWidgetId(context, this) + "." + Integer.toString(extendField.getSequenceId(), 36)) : generateWidgetId(context, this));
			

		// Obtain field name
		try {
			if( nameExpression != null )
				fieldName = nameExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
		} catch (Exception _) {}

		// Use field's defaults
		if( extendField != null && fieldName == null && extendField.getName() != null) {
			// Obtain field name
			try {
				fieldName = extendField.getName().evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
			} catch( Exception _) {}
		}
		
		// Set field name
		if( fieldName != null )
			action.addParameter(ActionParameter.NAME, fieldName );
		
		// Add extra parameters
		setRenderParameters(context, action);
		
		// Set default value
		String defaultValue = null;
		if( defaultExpression != null) {
			try {
				defaultValue = defaultExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
			} catch (Exception _) {}
		}

		return new Object[] {attr, extendField, defaultValue};
	}


}

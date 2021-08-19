package com.codeglide.core.rte.widgets;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.ClickHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.variables.Variable;
import com.codeglide.core.rte.variables.VariableInput;
import com.codeglide.xml.dom.DynamicElement;

/*
 * 
 * ICON =  URL of Icon
 * GROUP = Group for checkbox menuitems
 * TOOLTIP = Tooltip String
 * 
 * Flags
 * 
 * CHECKED
 * 
 */

public class Button extends Widget implements ClickHandler {
	private Function onClickFunction, onClientClickFunction;
	protected Expression nameExpression, checkedExpression;
	
	public Button(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.BUTTON);
		action.addParameter(ActionParameter.ID, generateWidgetId(context, this));
		if( nameExpression != null )
			action.addParameter(ActionParameter.NAME, nameExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		if( checkedExpression != null ) {
			try {
				String isChecked = checkedExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
				if( isChecked.equalsIgnoreCase("true") || isChecked.equals("1") || isChecked.equalsIgnoreCase("yes") )
					action.addFlag(ActionFlag.CHECKED);
			} catch( Exception _ ) {}
		}
		setRenderParameters(context, action);
		result.add(action);
		
		// Include UI Actions
		if( onClientClickFunction != null ) {
			List<Action> children = action.addChildren();
			onClientClickFunction.run(context, children);
		}
		
		// Include onClickActions
		if( onClickFunction != null ) {
			//action.addFlag(ActionFlag.CLICKABLE);
			Collection<Variable> inputVars = onClickFunction.getVariables();
			if( inputVars != null ) {
				StringBuffer inputObjects = new StringBuffer();
				for( Variable inputVar : inputVars ) {
					if( inputVar instanceof VariableInput ) {
						if( ((VariableInput)inputVar).getInputObject() != null ) {
							// Include object reference
							
							DynamicElement item = (DynamicElement)context.getVariables().resolveVariable(((VariableInput)inputVar).getInputObject());
							if( inputObjects.length() > 0 )
								inputObjects.append(",");
							inputObjects.append(generateElementId(context, this, item));
						} else if( ((VariableInput)inputVar).getInputListExpression() != null ) {
							// Include list reference
							
							action.addParameter(ActionParameter.INPUTLIST, generateWidgetId(context, ((VariableInput)inputVar).getInputListExpression().evaluate(context.getVariables(),context.getRootNode().getDocumentNode())));
						}
					}
				}
				if( inputObjects.length() > 0 )
					action.addParameter(ActionParameter.PAYLOAD, inputObjects.toString());
			}
			
		}
	}

	protected void parseElement(Element element, Application application) {
		nameExpression = getExpression(element, "name");
		checkedExpression = getExpression(element, "checked");
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("onclick") )
							onClickFunction = new Function(this, function);
						else if( function.getAttribute("name").equalsIgnoreCase("onclientclick") )
							onClientClickFunction = new Function(this, function);
					}
				}
			}
		}
		addRenderParameter(element, ActionParameter.ICON, "icon");
		addRenderParameter(element, ActionParameter.TOOLTIP, "tooltip");
		addRenderParameter(element, ActionParameter.GROUP, "group");
	}

	public void handleClick(ContextUi context, List<Action> result, Object input)
			throws CodeGlideException {
		if( onClickFunction != null )
			onClickFunction.run(context, result, input);
	}

	public void handleDoubleClick(ContextUi context, List<Action> result,
			Object input) throws CodeGlideException {
		handleClick(context, result, input);
	}

}


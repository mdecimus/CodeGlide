package com.codeglide.core.rte.widgets;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.BeforeCloseHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.variables.Variable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.core.rte.windowmanager.WindowInstance;

/*
 * ID   = Window ID
 * PANEL_ID = ID to be used on event handlers
 * 
 * 
 * Flags:
 * 
 * CLOSABLE
 * BEFORECLOSABLE
 * DRAGGABLE
 * COLLAPSIBLE
 * 
 */

public class Window extends RootPanel implements BeforeCloseHandler {
	private Function onBeforeCloseFunction;

	public Window(Item parent, Element element) {
		super(parent, element);
	}

	public WindowInstance newWindowInstance(ContextUi context, Vector<Expression> inputExpression) throws CodeGlideException {
		// Create a new window instance
		WindowInstance instance = new WindowInstance(context.getWindowManager());
		instance.addPanelInstance("_m", newInstance(context, inputExpression));
		return instance;
	}
	
	/*protected void render(ContextUi context, List<Action> result) throws CodeGlideException {
		Action action = new Action(ActionType.PANEL);
		setRenderParameters(context, action);
		result.add(action);
		
		List<Action> children = new LinkedList<Action>();
		action.addChildren(children);
		super.render(context, children);
	}*/
	
	protected void setRenderParameters(ContextUi context, Action action) throws CodeGlideException {
		super.setRenderParameters(context, action);
		action.addParameter(ActionParameter.ID, generateWidgetId(context));
		if( onBeforeCloseFunction != null )
			action.addFlag(ActionFlag.BEFORECLOSABE);
	}
	
	public void handleBeforeClose(ContextUi context, List<Action> result) throws CodeGlideException {
		if( onBeforeCloseFunction != null )
			onBeforeCloseFunction.run(context, result);
	}

	public void handleClose(ContextUi context, List<Action> result) throws CodeGlideException {
		WindowInstance instance = context.getWindowManager().getWindow(context.getWindowId());
		context.getWindowManager().removeWindow(instance.getSequenceId());

		if( onCloseFunction != null )
			onCloseFunction.run(context, result);
		
		if( instance.getCallbackFunction() != null ) {
			Object inputVariable = null;
			
			// Look for output variables
			VariableHolder variableHolder = instance.getPanelInstance("_m").getVariables();
			if( variableList != null ) {
				for( Variable variable : variableList ) {
					if( variable.getVariableContext() == Variable.OUTPUT ) 
						inputVariable = variableHolder.resolveVariable(variable.getVariableName());
				}
			}

			try {
				// Create CallBack context
				VariableResolver varResolver = new VariableResolver();
				ContextUi callbackContext = new ContextUi(context);
				callbackContext.setWindowId(instance.getCallbackWindowId());
				callbackContext.setPanelId(instance.getCallbackPanelId());
				callbackContext.setVariables(varResolver);
				Context.setCurrent(callbackContext);

				// Restore variables for this window
				VariableHolder panelVariables = context.getWindowManager().getWindow(callbackContext.getWindowId()).getPanelInstance(callbackContext.getPanelId()).getVariables();
				varResolver.addVariables("_g", context.getCurrentSession().getGlobalVariables());
				varResolver.addVariables(getVariableHolderName(callbackContext), panelVariables);
				instance.getCallbackFunction().run(callbackContext, result, inputVariable);
			} finally {
				Context.setCurrent(context);
			}
		}
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("onbeforeclose") )
							onBeforeCloseFunction = new Function(this, function);
					}
				}
			}
		}	
	}

}

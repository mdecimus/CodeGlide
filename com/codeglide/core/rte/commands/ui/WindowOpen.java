package com.codeglide.core.rte.commands.ui;

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
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.core.rte.widgets.Window;
import com.codeglide.core.rte.windowmanager.PanelInstance;
import com.codeglide.core.rte.windowmanager.WindowInstance;

public class WindowOpen extends Widget {
	private Expression nameExpression, windowIdExpression;
	private Function onCallbackFunction;
	private Vector<Expression> inputExpressions;
	
	public WindowOpen(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {

		// Obtain window object
		Window window = context.getApplication().getWindow(nameExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		if( window == null)
			throw new RuntimeError("@runtime-undefined-window," + nameExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		
		// If we are opening a window, make sure that it wasn't opened already
		if( !window.hasFlag(ActionFlag.ALLOWMULTIPLE) ) {
			for( WindowInstance windowInstance : context.getWindowManager().getWindows() ) {
				for( PanelInstance itp : windowInstance.getPanelInstances() ) {
					if( window.getSequenceId() == itp.getPanel().getSequenceId() )
						return;
				}
			}
		}
		
		// Create a new window instance
		WindowInstance instance = window.newWindowInstance(context, inputExpressions);
		PanelInstance panelInstance = instance.getPanelInstance("_m");

		try {
			// Create new context
			ContextUi openContext = new ContextUi(context);
			Context.setCurrent(openContext);
			openContext.setWindowId(context.getWindowManager().addWindow(instance));
			openContext.setPanelId(panelInstance.getSequenceId());

			// Add panel variables
			VariableResolver variables = new VariableResolver();
			variables.addVariables("_g", context.getCurrentSession().getGlobalVariables());
			variables.addVariables(getVariableHolderName(openContext), panelInstance.getVariables());
			openContext.setVariables(variables);
			
			// Run onLoad function
			window.handleLoad(openContext, result);
			
			// Check if an onCallback handler is specified
			if( onCallbackFunction != null ) {
				instance.setCallbackFunction(onCallbackFunction);
				instance.setCallbackWindowId(context.getWindowId());
				instance.setCallbackPanelId(context.getPanelId());
			}

			// Open window
			Action action = new Action(ActionType.OPEN_WINDOW);
			List<Action> children = action.addChildren();
			result.add(action);
			window.run(openContext, children);
			
			// Set Window ID variable
			if( windowIdExpression != null ) {
				String instanceVar = windowIdExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
				if( instanceVar != null && variables.getVariableType(instanceVar) == -1 )
					throw new RuntimeError("@runtime-undefined-variable," + instanceVar);
				context.getVariables().setVariable(instanceVar, String.valueOf(openContext.getWindowId()));
			}
		} finally {
			Context.setCurrent(context);
		}
	}

	protected void parseElement(Element element, Application application) {
		nameExpression = getExpression(element, "name");
		windowIdExpression = getExpression(element, "setWindowId");
		inputExpressions = Function.parseInputParameters(element);
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("oncallback") )
							onCallbackFunction = new Function(this, function);
					}
				}
			}
		}

	}

}

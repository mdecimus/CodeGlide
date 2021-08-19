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
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.core.rte.widgets.RootPanel;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.core.rte.windowmanager.PanelInstance;
import com.codeglide.core.rte.windowmanager.WindowInstance;

public class PanelAdd extends Widget {
	private Expression nameExpression, targetExpression;
	private Function onCallbackFunction;
	private Vector<Expression> inputExpressions;
	
	public PanelAdd(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {

		// Obtain panel name
		String panelName = nameExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());
		String targetPanel = targetExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode());

		// Obtain panel
		RootPanel panel = context.getApplication().getPanel(panelName);
		if( panel == null)
			throw new RuntimeError("@runtime-undefined-panel," + ((panelName!=null)?panelName:"''"));
		else if( targetPanel == null )
			throw new RuntimeError("@runtime-undefined-target-panel" );

		if( targetPanel.equals("_top"))
			context.setWindowId(0);
		
		// Obtain window instance
		WindowInstance instance = context.getWindowManager().getWindow(context.getWindowId());

		// Call onClose event of the panel we are about to replace
		if( targetPanel != null && instance.hasPanelInstance(targetPanel) ) {
			// Close old panel
			RootPanel oldPanel = instance.getPanelInstance(targetPanel).getPanel();
			oldPanel.handleClose(context, result);
			instance.removePanelInstance(targetPanel);
			
			// Remove old panel from UI
			Action action = new Action(ActionType.REMOVE_COMPONENT);
			action.addParameter(ActionParameter.ID, generateWidgetId(context, oldPanel));
			result.add(action);
		}

		// Create new panel instance
		PanelInstance panelInstance = panel.newInstance(context, inputExpressions);
		
		try {
			// Create new context
			ContextUi openContext = new ContextUi(context);
			openContext.setPanelId(instance.addPanelInstance(targetPanel, panelInstance));
			
			// Add panel variables
			VariableResolver variables = new VariableResolver();
			variables.addVariables("_g", context.getCurrentSession().getGlobalVariables());
			variables.addVariables(getVariableHolderName(openContext), panelInstance.getVariables());
			openContext.setVariables(variables);

			// Run onLoad function
			Context.setCurrent(openContext);
			panel.handleLoad(openContext, result);
			
			// Check if an onCallback handler is specified
			if( onCallbackFunction != null ) {
				instance.setCallbackFunction(onCallbackFunction);
				instance.setCallbackWindowId(context.getWindowId());
				instance.setCallbackPanelId(context.getPanelId());
			}

			// Run panel
			Action action = new Action(ActionType.ADD_COMPONENT);
			action.addParameter(ActionParameter.ID, generateWidgetId(openContext, panel));
			action.addParameter(ActionParameter.TARGET, generateWidgetId(openContext, targetPanel));
			List<Action> children = action.addChildren();
			result.add(action);
			panel.run(openContext, children);
			
		} finally {
			Context.setCurrent(context);
		}
	}

	protected void parseElement(Element element, Application application) {
		nameExpression = getExpression(element, "name");
		targetExpression = getExpression(element, "target");
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

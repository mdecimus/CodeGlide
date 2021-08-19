package com.codeglide.core.rte.commands.ui;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.core.rte.widgets.Window;
import com.codeglide.core.rte.windowmanager.PanelInstance;

public class WindowClose extends Widget {
	private Expression idExpression;
	
	public WindowClose(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.CLOSE_WINDOW);
		int windowId = -1;

		if( idExpression != null ) {
			try {
				windowId = Integer.parseInt(idExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
			} catch (NumberFormatException e) {
				throw new RuntimeError("@id-not-found");
			}
		} else {
			windowId = context.getWindowId();
		}
		action.addParameter(ActionParameter.ID, Long.toString(windowId, 36));
		result.add(action);
		
		try {
			PanelInstance panelInstance = context.getWindowManager().getWindow(windowId).getPanelInstance("_m");
			Window window = (Window)panelInstance.getPanel();
			ContextUi closeContext = new ContextUi(context);
			closeContext.setWindowId(windowId);
			closeContext.setPanelId(panelInstance.getSequenceId());
			Context.setCurrent(closeContext);
			window.handleClose(closeContext, result);
		} catch (Throwable e) {
			Logger.debug("Failed to close window.");
		} finally {
			Context.setCurrent(context);
		}
	}

	protected void parseElement(Element element, Application application) {
		idExpression = getExpression(element, "id");
	}

}

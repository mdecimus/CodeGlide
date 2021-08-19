package com.codeglide.core.rte.contexts;

import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.windowmanager.WindowManager;

public class ContextUi extends Context {
	private int windowId = 0;
	private int panelId = 0;
	
	public ContextUi(Session session) {
		super(session);
	}
	
	public ContextUi(ContextUi cloneContext) {
		super(cloneContext.getCurrentSession());
		setWindowId(cloneContext.getWindowId());
		setPanelId(cloneContext.getPanelId());
		setRootNode(cloneContext.getRootNode());
		setVariables(cloneContext.getVariables());
	}

	public int getWindowId() {
		return windowId;
	}
	public ContextUi setWindowId(int windowId) {
		this.windowId = windowId;
		return this;
	}
	public int getPanelId() {
		return panelId;
	}
	public ContextUi setPanelId(int panelId) {
		this.panelId = panelId;
		return this;
	}
	
	public WindowManager getWindowManager() {
		return currentSession.getWindowManager();
	}
}

package com.codeglide.core.rte.engines;

import java.util.LinkedList;
import java.util.List;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.core.rte.windowmanager.PanelInstance;
import com.codeglide.core.rte.windowmanager.WindowInstance;
import com.codeglide.core.rte.windowmanager.WindowManager;
import com.codeglide.xml.dom.DynamicElement;

public abstract class GuiEngine extends Engine {
	
	protected void handleInit(Session session) throws Exception {
		context = new ContextUi(session);
		super.handleInit(session);
	}
	
	protected Session handleSessionNotFound(Application application) throws Exception {
		Session session = SessionManager.createSession(application, ServerSettings.COOKIES_IF | ServerSettings.MAIL_IF | ServerSettings.XMLDB_IF | ServerSettings.SYSTEM_IF );
		session.getRootNode().setUid("1", null);
		WindowManager winManager = new WindowManager();
		winManager.addWindow(new WindowInstance(winManager));
		session.setWindowManager(winManager);
		return session;
	}
	
	protected void handleError(Throwable e ) {
		Logger.debug(e);

		String[] errorMessage = errorToString(e);
		Action action = new Action(ActionType.SHOW_ALERT);
		action.addParameter(ActionParameter.TITLE, errorMessage[0]);
		action.addParameter(ActionParameter.MESSAGE, errorMessage[1]);
		((GuiResponse)response).getActionsList().add(action);
	}

	protected void handleTerminalInit() throws CodeGlideException {
		Application application = context.getApplication();
		
		// Call onApplicationDeployment event
		if( application.hasFlag(Application.FIRST_DEPLOYMENT) ) {
			synchronized( application ) {
				if( application.hasFlag(Application.FIRST_DEPLOYMENT) ) {
					Function onDeployment = application.getEvent("onapplicationdeployment");
					if( onDeployment != null )
						onDeployment.run(context, ((GuiResponse)response).getActionsList() );
					application.unsetFlag(Application.FIRST_DEPLOYMENT);
				}
			}
		}
		
		// Call onTerminalInit event
		Function onInit = application.getEvent("onterminalinit");
		if( onInit == null )
			throw new RuntimeError("@runtime-missing-onterminalinit");
		onInit.run(context, ((GuiResponse)response).getActionsList());
	}
	
	protected void handleSessionExpired() throws CodeGlideException {
		super.handleSessionExpired();
		
		// Call onExpire
		Function onExpire = context.getApplication().getEvent("onsessionexpire");
		if( onExpire != null )
			onExpire.run(context, ((GuiResponse)response).getActionsList());
	}

	protected void handleTerminalRequest() throws CodeGlideException {
		
		// Call onTerminalRequest
		Function onRequest = context.getApplication().getEvent("onterminalrequest");
		if( onRequest != null )
			onRequest.run(context, ((GuiResponse)response).getActionsList());
	}

	protected void addPanelVariables(int windowId, int panelId) {
		// Obtain variables object
		try {
			VariableHolder panelVariables = context.getCurrentSession().getWindowManager().getWindow(windowId).getPanelInstance(panelId).getVariables();
			if( panelVariables.size() > 0 ) 
				context.getVariables().addVariables("P" + windowId + "." + panelId, panelVariables);
		} catch (Exception _) {
			Logger.debug("Couldn't locate panel variables.");
		}

	}

	protected DynamicElement getElementById( String id ) {
		return (DynamicElement)context.getCurrentSession().getWindowManager().getObject(Integer.parseInt(id, 36));
	}

	protected Object[] parseId(String componentId) {
		String[] elements = componentId.split("\\.");
		Widget widget = null;
		int windowId = Integer.parseInt(elements[0], 36);
		int panelId = -1;
		SequenceBucketizable item = null;
		if( elements.length > 1 ) {
			panelId = Integer.parseInt(elements[1], 36);
			if( elements.length > 2 ) {
				widget = (Widget)context.getApplication().getWidgetBucket().getObject(Integer.parseInt(elements[2], 36));
				if( elements.length > 3 )
					item = context.getCurrentSession().getWindowManager().getObject(Integer.parseInt(elements[3], 36));
			}
		} else if( elements.length == 1 ) {
			PanelInstance panelInstance = context.getCurrentSession().getWindowManager().getWindow(windowId).getPanelInstance("_m");
			panelId = panelInstance.getSequenceId();
			widget = panelInstance.getPanel();
		} else
			Logger.warn("Invalid componentId '"+componentId+"'.");
		return new Object[] {windowId, panelId, widget, item};
	}
	
	protected abstract class GuiResponse extends Response {
		protected LinkedList<Action> actions = new LinkedList<Action>();
		
		public List<Action> getActionsList() {
			return actions;
		}
		
	}
}

package com.codeglide.core.rte.commands.ui;

import java.util.List;
import java.util.UUID;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Command;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.interfaces.root.RootNode;

public class StartSession extends Command {
	private Expression uidExpression, memberOfExpression, timezoneExpression, autologoutExpression, siteNameExpression, localeExpression;
	
	public StartSession(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		uidExpression = getExpression(element, "uid");
		memberOfExpression = getExpression(element, "memberOf");
		timezoneExpression = getExpression(element, "timezone");
		autologoutExpression = getExpression(element, "autologout");
		siteNameExpression = getExpression(element, "sitename");
		localeExpression = getExpression(element, "locale");
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		
		Session session = SessionManager.createSession(context.getApplication(), (context instanceof ContextUi) ? ServerSettings.ALL_INTERFACES : (ServerSettings.ALL_INTERFACES & ~ServerSettings.MESSAGES_IF));
		session.setWindowManager(context.getCurrentSession().getWindowManager());
		session.setGlobalVariables(context.getCurrentSession().getGlobalVariables());
		SessionManager.addSession(UUID.randomUUID().toString(), session);
		context.setCurrentSession(session);
		context.setRootNode(session.getRootNode());
		
		// Add action
		Action action = new Action(ActionType.SET_SESSION);
		action.addParameter(ActionParameter.ID, session.getSessionId());
		result.add(action);

		RootNode rootNode = session.getRootNode();
		if( localeExpression != null )
			rootNode.setLanguage(localeExpression.evaluate(context.getVariables(), context.getDocumentNode()));
		if( timezoneExpression != null )
			rootNode.setTimezone(timezoneExpression.evaluate(context.getVariables(), context.getDocumentNode()));
		rootNode.setUid(uidExpression.evaluate(context.getVariables(), context.getDocumentNode()), (memberOfExpression != null ) ? memberOfExpression.evaluate(context.getVariables(), context.getDocumentNode()) : null );
		if( autologoutExpression != null )
			rootNode.setExpireTimeout(autologoutExpression.evaluate(context.getVariables(), context.getDocumentNode()));
		if( siteNameExpression != null )
			rootNode.setSiteId(siteNameExpression.evaluate(context.getVariables(), context.getDocumentNode()));

		// Trigger onSessionStart event
		session.onSessionStart();
	}

}

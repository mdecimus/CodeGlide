package com.codeglide.core.rte.commands;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.interfaces.root.RootNode;

public class Sudo extends CommandGroup {
	private Expression uidExpression, memberOfExpression;
	
	public Sudo(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		uidExpression = getExpression(element, "uid");
		memberOfExpression = getExpression(element, "memberOf");
	}

	public void run(Context context, List<Action> result)
			throws CodeGlideException {
		Session sudoSession = SessionManager.createSession(context.getApplication(), ServerSettings.ALL_INTERFACES & ~(ServerSettings.COOKIES_IF | ServerSettings.MESSAGES_IF) );
		//sudoSession.getRootNode().setVariables(rootNode.getVariables());
		RootNode originalRootNode = context.getRootNode();
		RootNode rootNode = sudoSession.getRootNode();
		rootNode.setUid(uidExpression.evaluate(context.getVariables(), context.getDocumentNode()), (memberOfExpression!=null)?memberOfExpression.evaluate(context.getVariables(), context.getDocumentNode()):null);
		
		try {
			context.setRootNode(sudoSession.getRootNode());
			super.run(context, result);
		} catch (com.codeglide.core.rte.exceptions.Break _) {
		} finally {
			context.setRootNode(originalRootNode);
		}
	}

}

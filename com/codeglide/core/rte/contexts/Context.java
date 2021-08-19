package com.codeglide.core.rte.contexts;

import java.util.HashMap;

import org.w3c.dom.Document;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.interfaces.root.RootNode;

public class Context {
	// Static thread to context maps
	private static HashMap<Thread, Context> contextMap = new HashMap<Thread, Context>();
	
	// Variable holder for this request
	private VariableResolver varArena = new VariableResolver();

	// Current Session
	protected Session currentSession = null;
	
	// Current RootNode
	private RootNode rootNode = null;
	
	public Context( Session session ) {
		this.currentSession = session;
		this.rootNode = session.getRootNode();
	}
	
	public Session getCurrentSession() {
		return currentSession;
	}

	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
	}

	public void setVariable(String name, Object value) {
		varArena.setVariable(name, value);
	}

	public void setVariables(VariableResolver varArena) {
		this.varArena = varArena;
	}
	
	public VariableResolver getVariables() {
		return varArena;
	}

	public RootNode getRootNode() {
		return rootNode;
	}
	
	public void setRootNode(RootNode rootNode) {
		this.rootNode = rootNode;
	}

	public Application getApplication() {
		return currentSession.getApplication();
	}
	
	public Document getDocumentNode() {
		return currentSession.getRootNode().getDocumentNode();
	}

	public static Context getCurrent() {
		return contextMap.get(Thread.currentThread());
	}
	
	public static void setCurrent(Context context) {
		synchronized( contextMap ) {
			contextMap.put(Thread.currentThread(), context);
		}
	}

	public static void removeCurrent() {
		synchronized( contextMap ) {
			contextMap.remove(Thread.currentThread());
		}
	}
}

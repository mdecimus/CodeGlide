/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.core.rte.session;

import org.w3c.dom.Node;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.windowmanager.WindowManager;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.DbInterface;

public class Session {
	// User root node
	private RootNode rootNode = null;
	
	// Session ID
	private String sessionId = null;

	// Semaphore used to handle multiple requests
	private WindowManager windowManager = null;
	
	// Global variables
	private VariableHolder globalVariables = null;
	
	public boolean isAnonymous() {
		return sessionId == null;
	}

	public VariableHolder getGlobalVariables() {
		return globalVariables;
	}

	public void setGlobalVariables(VariableHolder globalVariables) {
		this.globalVariables = globalVariables;
	}

	public RootNode getRootNode() {
		return this.rootNode;
	}
	
	public Application getApplication() {
		return this.rootNode.getApplication();
	}
	
	public void setRootNode(RootNode rootNode) {
		this.rootNode = rootNode;
	}

	public boolean hasExpired() {
		return rootNode.hasExpired();
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public WindowManager getWindowManager() {
		return windowManager;
	}

	public void setWindowManager(WindowManager windowManager) {
		this.windowManager = windowManager;
	}
	
	public void onSessionStart() {
		// Call onSessionEnd function
		if( rootNode.getChildren() != null ) {
			for( Node node : rootNode.getChildren() ) {
				if( node instanceof SessionHook )
					((SessionHook)node).onSessionStart();
			}
		}
	}
	
	public void onSessionEnd() {
		
		// Call onSessionEnd function
		if( rootNode.getChildren() != null ) {
			for( Node node : rootNode.getChildren() ) {
				if( node instanceof SessionHook )
					((SessionHook)node).onSessionEnd();
			}
		}
		
		// Remove cached quota
		((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF)).removeCachedQuota(rootNode.getUserId());
	}
	
}

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

import java.util.HashMap;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ServerError;
import com.codeglide.core.rte.variables.Variable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.interfaces.Interface;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.system.SystemInterface;
import com.codeglide.xml.dom.DummyDocument;


public class SessionManager {
	private static HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	public static Session getSession( String name ) {
		return sessions.get(name);
	}
	
	public static void addSession( String name, Session session ) {
		session.setSessionId(name);
		sessions.put(name, session);
	}
	
	public static void removeSession( String name ) {
		Session session = sessions.remove(name);
		if( session != null )
			session.onSessionEnd();
	}
	
	public static Session createSession( Application application, int interfaces ) throws CodeGlideException {
		// Create session
		Session session = new Session();
		
		// Create dummy DOM document
		DummyDocument dummyDoc = new DummyDocument();
		
		// Create a variable holder for global variables
		VariableHolder globalVariables = new VariableHolder();
		for( Variable var : application.getVariables() ) 
			globalVariables.defineVariable(var.getVariableName(), var.getVariableType());

		// Create root node
		RootNode rootNode = (RootNode)ServerSettings.getRootInterface().createRootElement(application, dummyDoc);
		session.setGlobalVariables(globalVariables);
		session.setRootNode(rootNode);
		rootNode.setGlobalVariables(globalVariables);
		
		// Set root node values
		rootNode.disableTracking();
		
		for( int ifId = ServerSettings.XMLDB_IF; ifId <= ServerSettings.SYSTEM_IF; ifId = ifId << 1) {
			if( (ifId & interfaces) != 0 ) {
				Interface itf = ServerSettings.getInterface(ifId);
				if( itf != null ) {
					try {
						itf.initApplication(application);
					} catch (Exception e) {
						Logger.debug(e);
						throw new ServerError("Initialization error","Failed to intialize application. Please contact the server administrator.");
					}
					if( itf instanceof SystemInterface ) {
						SystemInterface sysIf = (SystemInterface)itf;
						if( rootNode.getLanguage() == null )
							rootNode.setLanguage(sysIf.getDefaultLanguage());
						if( rootNode.getTimezone() == null )
							rootNode.setTimezone(sysIf.getDefaultTimezone());
						//if( userNode.getAttribute("AutoLogout") == null )
						//	rootNode.setExpireTimeout(sysIf.getDefaultAutologout());
					}
					
					// Append root element
					rootNode._appendChild(itf.createRootElement(dummyDoc));
				}
			}
		}
		rootNode.enableTracking();
		
		return session;
	}


}

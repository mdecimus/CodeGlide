/*
 * 	Copyright (C) 2008, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */

package com.codeglide.webdav;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.engines.Engine;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.util.Base64Util;
import com.codeglide.util.Pair;
import com.codeglide.webdav.DavResponse.Status;
import com.codeglide.webdav.exceptions.WebdavServerUnauthorizedException;
import com.codeglide.webdav.lock.ExclusiveLock;
import com.codeglide.webdav.lock.Lock;
import com.codeglide.webdav.lock.SharedLock;
import com.codeglide.webdav.tree.Operation;
import com.codeglide.webdav.tree.WebdavRootNode;
import com.codeglide.webdav.tree.WebdavTreeNode;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class WebdavEngine extends Engine {
	private static final String treepath = "/webdav/";
	
	private Session getSession() throws Exception {
		String authorization = servletRequest.getHeader("Authorization");
		if (authorization == null)
			throw new WebdavServerUnauthorizedException("Client must identify himself");
		else {
			if (!authorization.startsWith("Basic"))
				//Unknown / unsupported authentication scheme
				throw new Exception("Unsupported authentication scheme");
			//Obtain application name from hostname
			Application application = ServerSettings.getApplicationByHostname(servletRequest.getServerName());
			if (application == null)
				throw new Exception("Application does not exist in this server.");
			//Get the encoded pair username:password
			String encoded = authorization.substring("Basic ".length());
			Session session = SessionManager.getSession(encoded);
			synchronized(application) {
				if (session != null)
					//Existing session
					return session;
				else {
					//Get usename and password 
					String decoded = Base64Util.decode(encoded);
					int separator = decoded.indexOf(':');
					String username = decoded.substring(0,separator);
					String password = decoded.substring(separator + 1);
					//Get the user, check the password, create session
					//FIXME fix this
					/*long uid = ((DbInterface)ServerSettings.getInterface(DbInterface.class)).getUserNodeId(application,username);
					if( uid != -1 ) {
						DbNode userNode = new DbLeafNode(new DummyDocument(), uid);
						if( userNode != null && userNode.getAttribute("Pass") != null && userNode.getAttribute("Pass").equals(password)) {
							session = SessionManager.createSession(application, userNode);
							SessionManager.addSession(encoded,session);
						}
					}*/
					return session;
				}
			}
		}
	}
	
	protected String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	//If request processing aborted
	protected void handleError(Throwable e) {
		//TODO generate DAV response with error
		response = new DavResponse(Status.UNAUTHORIZED);
		/*if (e instanceof WebdavServerUnauthorizedException) {
			response.setStatus(Status.UNAUTHORIZED.getCode(),Status.UNAUTHORIZED.getDescription());
			response.setHeader("WWW-Authenticate","Basic realm='" + treepath + "'");			
		} else {
			response.setStatus(Status.INTERNAL_SERVER_ERROR.getCode(),Status.INTERNAL_SERVER_ERROR.getDescription());
			try {
				response.getWriter().println(e.getMessage());
			} catch (IOException e2) {
				Logger.debug(e2);
			}
		}*/
	}

	//After request processing
	protected void handleFinalize() {
		//NO-OP
	}

	//Before request processing
	protected void handleInit() throws Exception {
		//NO-OP
	}

	//Request processing
	protected void handleRequest() throws Exception {
		//Processes the request according to its method
		String method = servletRequest.getMethod();
		if (method.equals("PROPFIND"))
			response = this.doPropfind();
		else if (method.equals("PROPPATCH"))
			response = this.doProppatch();
		else if (method.equals("MKCOL"))
			response = this.doMkcol();
		else if (method.equals("GET"))
			response = this.doGet();
		else if (method.equals("HEAD"))
			response = this.doGet();
		else if (method.equals("POST"))
			response = this.doPost();
		else if (method.equals("DELETE"))
			response = this.doDelete();
		else if (method.equals("PUT"))
			response = this.doPut();
		else if (method.equals("COPY"))
			response = this.doCopy();
		else if (method.equals("MOVE"))
			response = this.doMove();
		else if (method.equals("LOCK"))
			response = this.doLock();
		else if (method.equals("UNLOCK"))
			response = this.doUnlock();
		else if (method.equals("OPTIONS"))
			response = this.doOptions();
		else
			throw new Exception("Unsupported method: " + method);
	}

	protected Session handleSessionNotFound(Application application) throws Exception {
		return null;
	}

	protected void setSessionId(String sessionId) {
	}
	
	//Process handlers
	protected DavResponse doPropfind() throws Exception {
		DavResponse davResponse;
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		System.out.println(tree);
		//Extract parameters from header
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		int depth = Depth.ToInt(servletRequest.getHeader("Depth"));
		/*	Propfind may include
				-empty body
				-xml body containing the allprop tag
				-xml body containing the propname tag
				-xml body containing the prop tag (with a list of properties inside) 
		*/
		if (servletRequest.getContentLength() <= 0)
			//Empty body. RFC says it must be treated as allprop
			davResponse = tree.propfindAllProp(path,depth);
		else {
			//Parse request's content
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(servletRequest.getInputStream()));
			Element root = parser.getDocument().getDocumentElement();
			if (root.getElementsByTagNameNS("DAV:","allprop").getLength() > 0)
				//Return all properties
				davResponse = tree.propfindAllProp(path,depth);
			else if (root.getElementsByTagNameNS("DAV:","propname").getLength() > 0)
				//Return all property names
				davResponse = tree.propfindPropName(path,depth);
			else {
				List<Pair<String,String>> properties = new LinkedList<Pair<String,String>>();
				//Return named properties
				NodeList propTags = root.getElementsByTagNameNS("DAV:","prop");
				for (int i = 0;i < propTags.getLength();i++) {
					NodeList propertyNodes = propTags.item(i).getChildNodes();	//Get the tags between <prop> and </prop>
					for (int j = 0;j < propertyNodes.getLength();j++) {
						Node property = propertyNodes.item(j);
						if (property.getNodeType() == Node.ELEMENT_NODE)
							properties.add(new Pair<String,String>(property.getNamespaceURI(),property.getLocalName()));
					}
				}
				davResponse = tree.propfindProp(path,depth,properties);
			}
		}
		return davResponse;
	}

	protected DavResponse doProppatch() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		Set<String> tokens = this.getAccessTokens();
		//Parse request's content
		List<Operation> operations = new LinkedList<Operation>();  
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(servletRequest.getInputStream()));
		Element root = parser.getDocument().getDocumentElement();
		//-Read "set" operations
		NodeList setTags = root.getElementsByTagNameNS("DAV:","set");
		for (int i = 0;i < setTags.getLength();i++) {
			NodeList propTags = setTags.item(i).getChildNodes();		//Get the <prop> tags 
			for (int j = 0;j < propTags.getLength();j++) {
				NodeList properties = propTags.item(j).getChildNodes();	//Get the tags between <prop> and </prop>; every one is a request for setting a property's value 
				for (int k = 0;k < properties.getLength();k++) {
					Node property = properties.item(k);
					if (property.getNodeType() == Node.ELEMENT_NODE)
						operations.add(Operation.Set(property.getNamespaceURI(),property.getLocalName(),property.getTextContent()));
				}
			}
		}
		//-Read "remove" operations
		NodeList removeTags = root.getElementsByTagNameNS("DAV:","remove");
		for (int i = 0;i < removeTags.getLength();i++) {
			NodeList propTags = removeTags.item(i).getChildNodes();		//Get the <prop> tags 
			for (int j = 0;j < propTags.getLength();j++) {
				NodeList properties = propTags.item(j).getChildNodes();	//Get the tags between <prop> and </prop>; every one is a request for removing a property 
				for (int k = 0;k < properties.getLength();k++) {
					Node property = properties.item(k);
					if (property.getNodeType() == Node.ELEMENT_NODE)
						operations.add(Operation.Remove(property.getNamespaceURI(),property.getLocalName()));
				}
			}
		}
		return tree.proppatch(path,operations,tokens);
	}

	protected DavResponse doMkcol() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		Set<String> tokens = this.getAccessTokens();
		return tree.mkcol(path,tokens);
	}
	
	protected DavResponse doGet() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		return tree.get(path);
	}	
	
	protected DavResponse doHead() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		return tree.head(path);
	}
	
	protected DavResponse doPost() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		Set<String> tokens = this.getAccessTokens();
		return tree.post(path,servletRequest.getInputStream(),tokens);
	}
	
	protected DavResponse doDelete() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		Set<String> tokens = this.getAccessTokens();
		return tree.delete(path,tokens);
	}
	
	protected DavResponse doPut() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		Set<String> tokens = this.getAccessTokens();
		return tree.put(path,servletRequest.getInputStream(),tokens);
	}
	
	protected DavResponse doCopy() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		boolean overwrite = this.getOverwriteHeader();
		String destination = servletRequest.getHeader("Destination");
		int depth = Depth.ToInt(servletRequest.getHeader("Depth"));
		Set<String> tokens = this.getAccessTokens();
		//Destination header includes the hostname as prefix
		String prefix = "http://" + servletRequest.getHeader("Host");
		return tree.copy(path,destination.substring(prefix.length()),overwrite,depth,tokens);
	}
	
	protected DavResponse doMove() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		boolean overwrite = this.getOverwriteHeader();
		String destination = servletRequest.getHeader("Destination");
		Set<String> tokens = this.getAccessTokens();
		String prefix = "http://" + servletRequest.getHeader("Host");
		return tree.move(path,destination.substring(prefix.length()),overwrite,tokens);
	}
	
	private DavResponse doLock() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		int depth = Depth.ToInt(servletRequest.getHeader("Depth"));
		Set<String> tokens = this.getAccessTokens();
		//Parse servletRequest's content
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(servletRequest.getInputStream()));
		Element root = parser.getDocument().getDocumentElement();
		//Get the lock
		Lock lock;
		if (root.getElementsByTagNameNS("DAV:","exclusive").getLength() > 0)
			lock = new ExclusiveLock(depth);
		else
			lock = new SharedLock(depth);
		//Apply lock
		return tree.lock(path,lock,depth,tokens);
	}
	
	private DavResponse doUnlock() throws Exception {
		Session session = this.getSession();
		WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
		//Extract parameters from header and execute method
		String path = servletRequest.getContextPath() + servletRequest.getServletPath();
		String token = this.getLockTokenHeader();
		//Apply lock
		return tree.unlock(path,token);
	}

	public DavResponse doOptions() throws Exception {
		DavResponse davResponse = new DavResponse(Status.OK);
		davResponse.addHeader("Allow","GET, HEAD, POST, PUT, DELETE, OPTIONS, PROPFIND, PROPPATCH, MKCOL, COPY, MOVE");
		return davResponse;
	}
	
	//Misc
	private Set<String> getAccessTokens() {
		Set<String> tokens = new HashSet<String>();
		//Get the header and parse its value
		String value = servletRequest.getHeader("If");
		if (value != null) {
			Pattern pattern = Pattern.compile("[^\\(]*\\(<(opaquelocktoken:)?([0-9a-zA-Z-]+)>\\)");
			Matcher matcher = pattern.matcher(value);
			while (matcher.find())
				tokens.add(matcher.group(2));	//The group "2" is ([0-9a-zA-Z-]+)
		}
		return tokens;
	}
	
	private String getLockTokenHeader() {
		//Get the header and parse its value
		String value = servletRequest.getHeader("Lock-Token");
		if (value != null) {
			Pattern pattern = Pattern.compile("<(opaquelocktoken:)?([0-9a-zA-Z-]+)>");
			Matcher matcher = pattern.matcher(value);
			if (matcher.matches())
				return matcher.group(2);
		}
		return "";
	}

	private boolean getOverwriteHeader() {
		String header = servletRequest.getHeader("Overwrite");
		if (header == null)
			return false;
		else
			return header.equals("T");
	}
}

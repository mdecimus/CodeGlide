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

package com.codeglide.webdav;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.interfaces.xmldb.DbInterface;
import com.codeglide.interfaces.xmldb.DbLeafNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.util.Base64Util;
import com.codeglide.util.Pair;
import com.codeglide.webdav.DavResponse.Status;
import com.codeglide.webdav.exceptions.WebdavServerException;
import com.codeglide.webdav.exceptions.WebdavServerUnauthorizedException;
import com.codeglide.webdav.exceptions.WebdavTreeException;
import com.codeglide.webdav.lock.ExclusiveLock;
import com.codeglide.webdav.lock.Lock;
import com.codeglide.webdav.lock.SharedLock;
import com.codeglide.webdav.tree.Operation;
import com.codeglide.webdav.tree.WebdavRootNode;
import com.codeglide.webdav.tree.WebdavTreeNode;
import com.codeglide.xml.dom.DummyDocument;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class WebdavServer extends HttpServlet {
	private static final long serialVersionUID = 0L;
	private static final String treepath = "/webdav/";
	//private static final ServerRuntime runtime = new ServerRuntime();

	//Gets or creates the session from the user, or fails if the user didn't identified his self
	private Session getSession(HttpServletRequest request) throws WebdavServerException {
		try {
			String authorization = request.getHeader("Authorization");
			if (authorization == null)
				throw new WebdavServerUnauthorizedException("Client must identify him self");
			else {
				if (!authorization.startsWith("Basic"))
					//Unknown / unsupported authentication scheme
					throw new WebdavServerException("Unsupported authentication scheme");
				//Obtain application name from hostname
				Application application = ServerSettings.getApplicationByHostname(request.getServerName());
				if (application == null)
					throw new WebdavServerException("Application does not exist in this server.");
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
		} catch (CodeGlideException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}


/*
	private Session getSession(HttpServletRequest request) throws WebdavServerException {
		try {
			String authorization = request.getHeader("Authorization");
			if (authorization == null)
				throw new WebdavServerUnauthorizedException("Client must identify him self");
			else {
				if (!authorization.startsWith("Basic"))
					//Unknown / unsupported authentication scheme
					throw new WebdavServerException("Unsupported authentication scheme");
				//Obtain application name from hostname
				Application application = ServerSettings.getApplicationByHostname(request.getServerName());
				if (application == null) 
					throw new WebdavServerException("Application does not exist in this server.");
				String instanceName = ServerSettings.getInstanceName(request.getServerName());
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
						//Start an anonymous session (UID = 1)
						String nobody = "nbdy-" + application.getName() + "-" + instanceName;
						session = SessionManager.getSession(nobody);
						if (session == null) {
							//Create a virtual user node
							DynamicElement anonymousUser = new DynamicElement(new DummyDocument(), "User");
							anonymousUser.setAttribute("Uid", "1");
							anonymousUser.setAttribute("SiteName", instanceName);
							anonymousUser.setAttribute("AutoLogout", "0");
							//TODO do content negotiation with browser
							anonymousUser.setAttribute("Language", "en");
							session = runtime.createSession(application,anonymousUser,null);
							SessionManager.addSession(nobody, session);
						}					
						//Set anonymous flag and enable locking
						session.enableLocking();
						session.setAnonymous();
						//Validate username and password, and retrieve data 
						DynamicElement userNode = runtime.validateUser(application,session.getRootNode().getDocumentNode(),username,password);
						if (userNode == null)
							throw new WebdavServerUnauthorizedException("Bad user or password: " + username + "@" + password);
						session = runtime.createSession(application,userNode,null);
						SessionManager.addSession(encoded,session);
						return session;
					}
				}
			}
		} catch (CodeglideRuntimeException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
*/
	//Invoqued whenever a comes a request
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request);
/*
		System.out.println("getServerName(): " + request.getServerName());
		System.out.println("getHeader(\"Host\"): " + request.getHeader("Host"));
		System.out.println("getContextPath(): " + request.getContextPath());
		System.out.println("getHeader(\"Depth\"): " + request.getHeader("Depth"));
		System.out.println("getLocalAddr(): " + request.getLocalAddr());
		System.out.println("getPathInfo(): " + request.getPathInfo());
		System.out.println("getPathTranslated(): " + request.getPathTranslated());
		System.out.println("getQueryString(): " + request.getQueryString());
		System.out.println("getServletPath(): " + request.getServletPath());
*/
/*
		int b;
		System.out.println("Body:");
		int tab = -1;
		char a = ' ';
		boolean abre = false;
		while ((b = request.getInputStream().read()) >= 0) {			
			if ((char)b == '>') {
				if (!abre && tab > 0)
					tab--;
				if (abre && a != '/')
					tab++;
				abre = false;
				System.out.println(">");
			} else {
				if ((char)b == '<') {
					for (int i = 1;i <= tab;i++)
						System.out.print("\t");
				} else if (a == '<' && (char)b != '/')
					abre = true;
				System.out.print((char)b);
			}
			a = (char)b;
		}
*/
		try {
			//Processes the request acording to its method
			String method = request.getMethod();
			if (method.equals("PROPFIND")) {
				this.doPropfind(request,response);
				return;
			}
			if (method.equals("PROPPATCH")) {
				this.doProppatch(request,response);
				return;
			}
			if (method.equals("MKCOL")) {
				this.doMkcol(request,response);
				return;
			}
			if (method.equals("GET")) {
				this.doGet(request,response);
				return;
			}
			if (method.equals("HEAD")) {
				this.doGet(request,response);
				return;
			}
			if (method.equals("POST")) {
				this.doPost(request,response);
				return;
			}
			if (method.equals("DELETE")) {
				this.doDelete(request,response);
				return;
			}
			if (method.equals("PUT")) {
				this.doPut(request,response);
				return;
			}
			if (method.equals("COPY")) {
				this.doCopy(request,response);
				return;
			}
			if (method.equals("MOVE")) {
				this.doMove(request,response);
				return;
			}
			if (method.equals("LOCK")) {
				this.doLock(request,response);
				return;
			}
			if (method.equals("UNLOCK")) {
				this.doUnlock(request,response);
				return;
			}
			if (method.equals("OPTIONS")) {
				this.doOptions(request,response);
				return;
			}
		} catch (WebdavServerUnauthorizedException e) {
			response.setStatus(Status.UNAUTHORIZED.getCode(),Status.UNAUTHORIZED.getDescription());
			response.setHeader("WWW-Authenticate","Basic realm='" + treepath + "'");			
		} catch (WebdavServerException e) {
			response.setStatus(Status.INTERNAL_SERVER_ERROR.getCode(),Status.INTERNAL_SERVER_ERROR.getDescription());
			response.getWriter().println(e.getMessage());
			System.out.print(e.getMessage());
		}
	}

	protected void doPropfind(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			DavResponse davResponse;
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			System.out.println(tree);
			//Extract parameters from header
			String path = request.getContextPath() + request.getServletPath();
			int depth = Depth.ToInt(request.getHeader("Depth"));
			/*	Propfind may include
					-empty body
					-xml body containing the allprop tag
					-xml body containing the propname tag
					-xml body containing the prop tag (with a list of properties inside) 
			*/
			if (request.getContentLength() <= 0)
				//Empty body. RFC says it must be treated as allprop
				davResponse = tree.propfindAllProp(path,depth);
			else {
				//Parse request's content
				DOMParser parser = new DOMParser();
				parser.parse(new InputSource(request.getInputStream()));
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
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (SAXException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}

	protected void doProppatch(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			DavResponse davResponse;
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header
			String path = request.getContextPath() + request.getServletPath();
			Set<String> tokens = this.getAccessTokens(request);
			//Parse request's content
			List<Operation> operations = new LinkedList<Operation>();  
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(request.getInputStream()));
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
			davResponse = tree.proppatch(path,operations,tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (SAXException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}

	protected void doMkcol(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			Set<String> tokens = this.getAccessTokens(request);
			DavResponse davResponse = tree.mkcol(path,tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			DavResponse davResponse = tree.get(path);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}	
	
	protected void doHead(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			DavResponse davResponse = tree.head(path);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			Set<String> tokens = this.getAccessTokens(request);
			DavResponse davResponse = tree.post(path,request.getInputStream(),tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	protected void doDelete(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			Set<String> tokens = this.getAccessTokens(request);
			DavResponse davResponse = tree.delete(path,tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	protected void doPut(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			Set<String> tokens = this.getAccessTokens(request);
			DavResponse davResponse = tree.put(path,request.getInputStream(),tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	protected void doCopy(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			boolean overwrite = this.getOverwriteHeader(request);
			String destination = request.getHeader("Destination");
			int depth = Depth.ToInt(request.getHeader("Depth"));
			Set<String> tokens = this.getAccessTokens(request);
			//Destination header includes the hostname as prefix
			String prefix = "http://" + request.getHeader("Host");
			DavResponse davResponse = tree.copy(path,destination.substring(prefix.length()),overwrite,depth,tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	protected void doMove(HttpServletRequest request,HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			boolean overwrite = this.getOverwriteHeader(request);
			String destination = request.getHeader("Destination");
			Set<String> tokens = this.getAccessTokens(request);
			String prefix = "http://" + request.getHeader("Host");
			DavResponse davResponse = tree.move(path,destination.substring(prefix.length()),overwrite,tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	private void doLock(HttpServletRequest request, HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			int depth = Depth.ToInt(request.getHeader("Depth"));
			Set<String> tokens = this.getAccessTokens(request);
			//Parse request's content
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(request.getInputStream()));
			Element root = parser.getDocument().getDocumentElement();
			toString(root,"");
			//Get the lock
			Lock lock;
			if (root.getElementsByTagNameNS("DAV:","exclusive").getLength() > 0)
				lock = new ExclusiveLock(depth);
			else
				lock = new SharedLock(depth);
			//Apply lock
			DavResponse davResponse = tree.lock(path,lock,depth,tokens);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (SAXException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	private void doUnlock(HttpServletRequest request, HttpServletResponse response) throws WebdavServerException {
		try {
			Session session = this.getSession(request);
			WebdavRootNode tree = WebdavTreeNode.BuildTree(session,treepath);
			//Extract parameters from header and execute method
			String path = request.getContextPath() + request.getServletPath();
			String token = this.getLockTokenHeader(request);
			//Apply lock
			DavResponse davResponse = tree.unlock(path,token);
			davResponse.send(response);
		} catch (WebdavTreeException e) {
			throw new WebdavServerException(e.getMessage());
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}

	public void doOptions(HttpServletRequest request, HttpServletResponse response) throws WebdavServerException {
		try {
			DavResponse davResponse = new DavResponse(Status.OK);
			davResponse.addHeader("Allow","GET, HEAD, POST, PUT, DELETE, OPTIONS, PROPFIND, PROPPATCH, MKCOL, COPY, MOVE");
			davResponse.send(response);
		} catch (IOException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}
	
	private Set<String> getAccessTokens(HttpServletRequest request) {
		Set<String> tokens = new HashSet<String>();
		//Get the header and parse its value
		String value = request.getHeader("If");
		if (value != null) {
			Pattern pattern = Pattern.compile("[^\\(]*\\(<(opaquelocktoken:)?([0-9a-zA-Z-]+)>\\)");
			Matcher matcher = pattern.matcher(value);
			while (matcher.find())
				tokens.add(matcher.group(2));	//The group "2" is ([0-9a-zA-Z-]+)
		}
		return tokens;
	}
	
	private String getLockTokenHeader(HttpServletRequest request) {
		//Get the header and parse its value
		String value = request.getHeader("Lock-Token");
		if (value != null) {
			Pattern pattern = Pattern.compile("<(opaquelocktoken:)?([0-9a-zA-Z-]+)>");
			Matcher matcher = pattern.matcher(value);
			if (matcher.matches())
				return matcher.group(2);
		}
		return "";
	}

	private boolean getOverwriteHeader(HttpServletRequest request) {
		String header = request.getHeader("Overwrite");
		if (header == null)
			return false;
		else
			return header.equals("T");
	}
	
	public void toString(Element element,String prefix) {
		System.out.println(prefix + ((prefix.isEmpty())?"":"|_") + element.getNamespaceURI() + "[:]" + element.getLocalName());
		NodeList children = element.getChildNodes(); 
		for (int i = 0;i < children.getLength();i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
				toString((Element)child,prefix + "  ");
		}
	}
}

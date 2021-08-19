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

package com.codeglide.webdav.tree;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeglide.core.Expression;
import com.codeglide.core.acl.AclToken;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.interfaces.xmldb.DbContainerNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.util.Pair;
import com.codeglide.webdav.Depth;
import com.codeglide.webdav.MultiStatusBody;
import com.codeglide.webdav.DavResponse.Status;
import com.codeglide.webdav.MultiStatusBody.Response;
import com.codeglide.webdav.exceptions.WebdavTreeException;
import com.codeglide.webdav.exceptions.WebdavTreeNoPermissionException;
import com.codeglide.webdav.exceptions.WebdavTreeNodeLockedException;
import com.codeglide.webdav.exceptions.WebdavTreeNodeNotFoundException;
import com.codeglide.webdav.lock.Lock;
import com.codeglide.webdav.lock.LockManager;
import com.codeglide.xml.dom.DynamicElement;

public abstract class WebdavTreeNode {
	static protected final VariableResolver resolver = new VariableResolver();
	
	//For easily adding of properties. This may be replaced for a "case" if eficiency is needed
	protected final HashMap<String,PropertyAccessor> accessors = new HashMap<String,PropertyAccessor>();
	
	protected abstract class PropertyAccessor {
		protected final String namespace;
		protected final String name;
		protected PropertyAccessor(String namespace,String name) {
			this.namespace = namespace;
			this.name = name;
			WebdavTreeNode.this.accessors.put(namespace + ":" + name,this);
		}
		public String get() throws WebdavTreeException {
			throw new WebdavTreeException("Impossible to get value from property '" + namespace + ":" + name + "'");
		}
		public void set(String value) throws WebdavTreeException {
			throw new WebdavTreeException("Impossible to set value of property '" + namespace + ":" + name + "' to '" + value.toString() + "'");
		}
		public void remove() throws WebdavTreeException {
			throw new WebdavTreeException("Impossible to remove property '" + namespace + ":" + name + "'");
		}
	}
	protected void defineProperties() {
		//For every property name, add a annonymous helper class that retrieves the requested value
		new PropertyAccessor("DAV:","creationdate") {
			public String get() throws WebdavTreeException {
				return WebdavTreeNode.this.getCreationDate();
			}
		};
		new PropertyAccessor("DAV:","displayname") {
			public String get() throws WebdavTreeException {
				return WebdavTreeNode.this.getName();
			}
			public void set(String value) throws WebdavTreeException {
				WebdavTreeNode.this.setName(value);
			}
		};
		new PropertyAccessor("DAV:","getcontentlength") {
			public String get() throws WebdavTreeException {
				return WebdavTreeNode.this.getSize();
			}
		};
		new PropertyAccessor("DAV:","getlastmodified") {
			public String get() throws WebdavTreeException {
				return ToWebdavDate(WebdavTreeNode.this.getMofificationDate());				
			}
			public void set(String value) throws WebdavTreeException {
				WebdavTreeNode.this.setMofificationDate(FromWebdavDate(value));
			}
		};
		new PropertyAccessor("DAV:","resourcetype") {
			public String get() {
				return WebdavTreeNode.this.getResourceType();
			}
		};
		new PropertyAccessor("DAV:","lockdiscovery") {
			public String get() {
				return WebdavTreeNode.this.getLockDiscovery();
			}
		};
		new PropertyAccessor("DAV:","getcontenttype") {
			public String get() {
				return "text/xml";
			}
		};
/*
		//----------------For windows clients----------------//
		new PropertyAccessor("urn:schemas-microsoft-com:","win32creationtime") {
			public String get() throws WebdavTreeException {
				return WebdavTreeNode.this.getCreationDate();
			}
		};
		new PropertyAccessor("urn:schemas-microsoft-com:","win32lastmodifiedtime") {
			public String get() throws WebdavTreeException {
				return ToWebdavDate(WebdavTreeNode.this.getMofificationDate());
			}
		};
		new PropertyAccessor("DAV:","name") {
			public String get() throws WebdavTreeException {
				return WebdavTreeNode.this.getName();
			}
		};
		new PropertyAccessor("DAV:","parentname") {
			public String get() throws WebdavTreeException {
				return null;
			}
		};
		new PropertyAccessor("DAV:","href") {
			public String get() throws WebdavTreeException {
				return WebdavTreeNode.this.getFullpathName();
			}
		};
		new PropertyAccessor("DAV:","ishidden") {
			public String get() throws WebdavTreeException {
				return "false";
			}
		};
		new PropertyAccessor("DAV:","isreadonly") {
			public String get() throws WebdavTreeException {
				return "false";
			}
		};
		new PropertyAccessor("DAV:","contentclass") {
			public String get() throws WebdavTreeException {
				return null;
			}
		};
		new PropertyAccessor("DAV:","getcontentlanguage") {
			public String get() throws WebdavTreeException {
				return null;
			}
		};
		new PropertyAccessor("DAV:","lastaccessed") {
			public String get() throws WebdavTreeException {
				return ToWebdavDate(WebdavTreeNode.this.getMofificationDate());
			}
		};
		new PropertyAccessor("DAV:","iscollection") {
			public String get() throws WebdavTreeException {
				return (WebdavTreeNode.this.getResourceType().isEmpty())?"false":"true";
			}
		};
		new PropertyAccessor("DAV:","isstructureddocument") {
			public String get() throws WebdavTreeException {
				return "false";
			}
		};
		new PropertyAccessor("DAV:","isroot") {
			public String get() throws WebdavTreeException {
				return (WebdavTreeNode.this instanceof WebdavRootNode)?"true":"false";
			}
		};
*/
	}

	//Formats node's name to webdav format
	private static String ToWebdavName(String name) {
		if (name == null)
			return null;
		else {
			name = name.replace(" ","%20");
			name = name.replace("[","%5b");
			name = name.replace("]","%5d");
			name = name.replace("/","%2F");
			return name;
		}
	}
	
	protected static String FromWebdavName(String name) {
		if (name == null)
			return null;
		else {
			name = name.replace("%20"," ");
			name = name.replace("%5b","[");
			name = name.replace("%5d","]");
			name = name.replace("%2F","/");
			return name;
		}
	}
	
	
	private enum Day {Sun,Mon,Tue,Wed,Thu,Fri,Sat};
	private enum Month {Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec};
	//Transforms node's date to webdav format
	private static String ToWebdavDate(String datetime) {
		if (datetime == null)
			return "";
		else {
			Pattern pattern = Pattern.compile("(\\d+)-(\\d+)-(\\d+)T(\\d+:\\d+:\\d+)Z");
			Matcher matcher = pattern.matcher(datetime);
			if (matcher.matches()) {
				String year = matcher.group(1);
				String month = matcher.group(2);
				String day = matcher.group(3);
				String time = matcher.group(4);
			    int wday = (new GregorianCalendar(Integer.parseInt(year),Integer.parseInt(month) - 1,Integer.parseInt(day))).get(Calendar.DAY_OF_WEEK);
				return Day.values()[wday - 1].name() + ", " + day  + " " + Month.values()[Integer.parseInt(month) - 1].name() + " " + year + " " + time + " GMT";
			} else
				return "";
		}
	}
	
	private static String FromWebdavDate(String datetime) {
		if (datetime == null)
			return "";
		else {
			Pattern pattern = Pattern.compile("(\\w+, )?(\\d+) (\\w+) (\\d+) (\\d+:\\d+:\\d+) GMT");
			Matcher matcher = pattern.matcher(datetime);
			if (matcher.matches()) {
				String day = matcher.group(2);
				String month = matcher.group(3);
				String year = matcher.group(4);
				String time = matcher.group(5);
				return year + "-" + Integer.toString(Month.valueOf(month).ordinal() + 1) + "-" + day + "T" + time + "Z";
			} else
				return "";
		}
	}

	//Builds the tree view for the user
	public static WebdavRootNode BuildTree(Session session,String rootname) throws WebdavTreeException {
		try {
			//Ask application for the xpath expression to find the root folder and apply it on the entire document
			Expression rootpath = session.getApplication().getService("webdav").getParameter("root-folder");
			DynamicElement node = (DynamicElement)rootpath.evaluate(resolver,session.getRootNode().getDocumentNode(),Expression.NODE);
			//Use a builder to create the tree
			return new WebdavRootNode(session,node,rootname);
		} catch (ExpressionException e) {
			throw new WebdavTreeException(e);
		}
	}

	//Attributes
	protected Session session;
	protected DynamicElement node = null;
	protected WebdavFolderNode parent = null;
	private String fullpathName = null;
	//Constructor. Protected to prevent using it directrly
	protected WebdavTreeNode(Session session,DynamicElement node) {
		this.node = node;
		this.session = session;
		this.defineProperties();
	};
	
	public WebdavTreeNode(Session session) {
		this.session = session;
		this.defineProperties();
	}

	//Getters
	public String toString() {
		try {
			return this.toString("");
		} catch (WebdavTreeException e) {
			return super.toString();
		}
	}	
	
	//For easy descriptive printing
	protected abstract String toString(String prefix) throws WebdavTreeException;
	
	//The name of the node
	public abstract String getName() throws WebdavTreeException;
	
	public String getFullpathName() throws WebdavTreeException {
		return fullpathName;		
	}
	
	//Size of node
	public abstract String getSize() throws WebdavTreeException;
	
	//Dates
	public String getCreationDate() {
		return node.getAttribute("_CreationDate");
	}

	public String getMofificationDate() throws WebdavTreeException {
		String date = node.getAttribute("_ModifiedDate");
		if (date == null)
			date = this.getCreationDate();
		return date;
	}
	
	public void setMofificationDate(String date) throws WebdavTreeException {
		node.setAttribute("_ModifiedDate",date);
	}
	
	//Retrieves information about locks
	public String getLockDiscovery() {
		Map<String,Lock> locks = LockManager.Instance.getLocksFor((DbNode)node);
		if (locks != null) {
			String txt = "";
			for (Lock lock : locks.values())
				txt += "<activelock>" + lock.getProperty() + "</activelock>";
			return txt;
		} else
			return "";
	}
	
	//Says if the node passes the filters
	public abstract boolean isFiltered() throws WebdavTreeException;
	
	//Resource (file or collection)
	public abstract String getResourceType();
	
	//For easy descriptive printing
	protected abstract List<WebdavTreeNode> getChildren() throws WebdavTreeException;
	
	//Finds a node by its path, or fails
	protected WebdavTreeNode getNode(Queue<String>  nodes) throws WebdavTreeException {
		if (nodes.isEmpty())
			return this;
		else {
			String childname = nodes.poll();
			for (WebdavTreeNode child : this.getChildren())
				//Look for the named child
				if (child.getName().equals(childname))
					return child.getNode(nodes);
			//Child does not exist, fail
			throw new WebdavTreeNodeNotFoundException(this.getFullpathName() + " does not contain an element named " + childname);
		}
	}
	
	//Finds a node by its name or returns null
	protected WebdavTreeNode getNode(String name) throws WebdavTreeException {
		for (WebdavTreeNode child : this.getChildren())
			//Look for the named child
			if (child.getName().equals(name))
				return child;
		return null;
	}
	
	public Long getID() {
		return new Long(((DbNode)node).getId());
	}
	
	//Setters
	public abstract void setName(String name) throws WebdavTreeException;
	
	protected void setParent(WebdavFolderNode parent) throws WebdavTreeException {
		this.parent = parent;
		String parentFullpathName = parent.getFullpathName();
		if (parentFullpathName.endsWith("/"))
			fullpathName = parentFullpathName + ToWebdavName(this.getName());
		else
			fullpathName = parentFullpathName + "/" + ToWebdavName(this.getName());
	}
	
	//Webdav methods
	//-PROPFIND
	protected void propfindAllProp(int depth,MultiStatusBody davResponse) throws WebdavTreeException {
		MultiStatusBody.Response response = davResponse.addResponse(this.getFullpathName());
		//Add names and values for all properties defined for the node
		for (PropertyAccessor accessor : accessors.values())
			response.getPropertyStatus(Status.OK).addProperty(accessor.namespace,accessor.name,accessor.get());
		//Propagation
		if (depth > 0)
			for (WebdavTreeNode child : this.getChildren())
				child.propfindAllProp(Depth.Dec(depth),davResponse);
	}
	
	protected void propfindPropName(int depth,MultiStatusBody davResponse) throws WebdavTreeException {
		Response response = davResponse.addResponse(this.getFullpathName());
		//Add names for all properties defined for the node
		for (PropertyAccessor accessor : accessors.values())
			response.getPropertyStatus(Status.OK).addProperty(accessor.namespace,accessor.name);
		//Propagation
		if (depth > 0)
			for (WebdavTreeNode child : this.getChildren())
				child.propfindPropName(Depth.Dec(depth),davResponse);
	}

	protected void propfindProp(int depth, List<Pair<String,String>> properties, MultiStatusBody davResponse) throws WebdavTreeException {
		Response response = davResponse.addResponse(this.getFullpathName());
		//Look for every property and store its value if exists
		for (Pair<String,String> property : properties) {
			PropertyAccessor accessor = accessors.get(property.first + ":" + property.second);
			if (accessor != null)
				response.getPropertyStatus(Status.OK).addProperty(property.first,property.second,accessor.get());
			else
				response.getPropertyStatus(Status.NOT_FOUND).addProperty(property.first,property.second);
		}
		//Propagation
		if (depth > 0)
			for (WebdavTreeNode child : this.getChildren())
				child.propfindProp(Depth.Dec(depth),properties,davResponse);
	}
	
	//-PROPPATCH
	//--Remember: either all operations succeed or no one does
	public void proppatch(List<Operation> operations, MultiStatusBody davResponse, Set<String> tokens) throws WebdavTreeException {
		if (!LockManager.Instance.hasAccessTo((DbNode)node,tokens))
			throw new WebdavTreeNodeLockedException("Can't overwrite properties");
		if (!((DbNode)node).hasPermission(AclToken.ACL_UPDATE))
			throw new WebdavTreeNoPermissionException("Can't overwrite properties");
		
		Response response = davResponse.addResponse(this.getFullpathName());
		boolean success = true;
		//For every operation, get the property accessor (if exists) and ask it to do that
		for (Operation operation : operations) {
			PropertyAccessor accessor = accessors.get(operation.namespace + ":" + operation.name);
			if (accessor == null) {
				response.getPropertyStatus(Status.NOT_FOUND).addProperty(operation.namespace,operation.name);
				success = false;
			} else {
				try {
					switch (operation.type) {
					case Operation.SET:
						accessor.set(operation.value);
						break;
					case Operation.REMOVE:
						accessor.remove();
						break;
					}
					response.getPropertyStatus(Status.OK).addProperty(operation.namespace,operation.name);
				} catch (WebdavTreeException e) {
					response.getPropertyStatus(Status.CONFLICT).addProperty(operation.namespace,operation.name);
					success = false;					
				}
			}
		}
		//If every operation successfully changed the node, it must be updated
		if (success)
			((DbNode)node).update();
	}
	
	//-MKCOL
	protected abstract void makeFolder(String foldername, Set<String> tokens) throws WebdavTreeException;
	
	//-GET
	protected abstract Object getContent() throws WebdavTreeException;
	
	//-DELETE
	protected void delete(Set<String> tokens, MultiStatusBody errors) throws WebdavTreeException {
		if (!LockManager.Instance.hasAccessTo((DbNode)node,tokens)) {
			errors.addResponse(this.getFullpathName(),Status.LOCKED);
			throw new WebdavTreeNodeLockedException("Can't delete file/folder");
		}
		if (!((DbNode)node).hasPermission(AclToken.ACL_DELETE) ||
			(node instanceof DbContainerNode && !((DbContainerNode)node).hasResursivePermission(AclToken.ACL_DELETE))) {
			errors.addResponse(this.getFullpathName(),Status.FORBIDDEN);
			throw new WebdavTreeNoPermissionException("Can't delete file/folder");
		}		
		//Delete children
		for (WebdavTreeNode child : this.getChildren())
			child.delete(tokens,errors);
		//Delete the dynamic element
		((DbNode)node).delete();
	}
	
	//-PUT
	protected abstract void makeFile(String filename, InputStream stream, Set<String> tokens) throws WebdavTreeException;

	protected abstract void setContent(InputStream stream) throws WebdavTreeException;

	//-COPY
	protected abstract boolean copyTree(WebdavTreeNode source, String name, boolean overwrite, int depth,Set<String> tokens, MultiStatusBody errors) throws WebdavTreeException;

	protected abstract void clone(WebdavFolderNode parent, String newname, int depth) throws WebdavTreeException;

	//-LOCK
	protected void lockTree(int depth, Lock lock, Set<String> tokens, MultiStatusBody errors) throws WebdavTreeException, WebdavTreeNodeLockedException {
		try {
			LockManager.Instance.addLockTo((DbNode)node,lock);
		} catch (WebdavTreeNodeLockedException e) {
			errors.addResponse(this.getFullpathName(),Status.LOCKED);
			throw e;
		}
		//Propagation
		if (depth > 0)
			for (WebdavTreeNode child : this.getChildren())
				child.lockTree(Depth.Dec(depth),lock,tokens,errors);
	}
}

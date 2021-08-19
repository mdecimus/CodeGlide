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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.codeglide.core.rte.session.Session;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.util.Pair;
import com.codeglide.webdav.DavResponse;
import com.codeglide.webdav.Depth;
import com.codeglide.webdav.MultiStatusBody;
import com.codeglide.webdav.DavResponse.Status;
import com.codeglide.webdav.exceptions.WebdavTreeNodeLockedException;
import com.codeglide.webdav.exceptions.WebdavTreeException;
import com.codeglide.webdav.exceptions.WebdavTreeNodeAlreadyExistsException;
import com.codeglide.webdav.exceptions.WebdavTreeNodeNotFoundException;
import com.codeglide.webdav.exceptions.WebdavTreeUnsupportedOperationException;
import com.codeglide.webdav.lock.Lock;
import com.codeglide.webdav.lock.LockManager;
import com.codeglide.xml.dom.DynamicElement;

public class WebdavRootNode extends WebdavFolderNode {
	final private String name;
	
	//Constructor. Protected to prevent using it directrly
	protected WebdavRootNode(Session session, DynamicElement node,String rootname) {
		super(session, node);
		name = rootname;
	}
	
	//Getters
	//The root node must be renamed because its name should be the path the webdav client refers
	public String getName() throws WebdavTreeException {
		return name;
	};
	
	public String getFullpathName() throws WebdavTreeException {
		return name;
	}

	//Setters
	public void setName(String name) throws WebdavTreeException {
		throw new WebdavTreeException("Impossible to change name of " + name + " node");		
	}
	
	//Validates and parses a path
	private Deque<String> parsePath(String path) throws WebdavTreeNodeNotFoundException {
		path = FromWebdavName(path);
		//Path's prefix must be the root's name
		if (!path.startsWith(name))
			throw new WebdavTreeNodeNotFoundException("Filename " + path + " does not exists");
		//Discarding prefix, split to get the name sequence following root
		String[] names = path.substring(name.length()).split("/");
		Deque<String> folders = new LinkedList<String>();
		for (int i = 0;i < names.length;i++)
			if (!names[i].isEmpty())
				folders.offer(names[i]);
		return folders;
	};
	
	//Methods
	//-PROPFIND (3 variantions)
	public DavResponse propfindAllProp(String path,int depth) throws WebdavTreeException {
		try {
			MultiStatusBody body = new MultiStatusBody();	//To store the result
			Queue<String> folders = this.parsePath(path);
			this.getNode(folders).propfindAllProp(depth,body);
			return new DavResponse(Status.MULTI_STATUS,body.getContent());
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	public DavResponse propfindPropName(String path,int depth) throws WebdavTreeException {
		try {
			MultiStatusBody body = new MultiStatusBody();	//To store the result
			Queue<String> folders = this.parsePath(path);
			this.getNode(folders).propfindPropName(depth,body);
			return new DavResponse(Status.MULTI_STATUS,body.getContent());
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	public DavResponse propfindProp(String path,int depth,List<Pair<String,String>> properties) throws WebdavTreeException {
		try {
			MultiStatusBody body = new MultiStatusBody();	//To store the result
			Queue<String> folders = this.parsePath(path);
			this.getNode(folders).propfindProp(depth,properties,body);
			return new DavResponse(Status.MULTI_STATUS,body.getContent());
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	//-PROPPATCH
	public DavResponse proppatch(String path, List<Operation> operations, Set<String> tokens) throws WebdavTreeException {
		try {
			MultiStatusBody body = new MultiStatusBody();	//To store the result
			Queue<String> folders = this.parsePath(path);
			this.getNode(folders).proppatch(operations,body,tokens);
			return new DavResponse(Status.MULTI_STATUS,body.getContent());
		} catch (WebdavTreeNodeLockedException e) {
			return new DavResponse(Status.LOCKED);
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	//-MKCOL
	public DavResponse mkcol(String path, Set<String> tokens) throws WebdavTreeException {
		try {
			Deque<String> folders = this.parsePath(path);
			//The last name is the collection that must be created
			String name = folders.pollLast();
			this.getNode(folders).makeFolder(name,tokens);
			return new DavResponse(Status.CREATED);
		} catch (WebdavTreeNodeLockedException e) {
			return new DavResponse(Status.LOCKED);
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.CONFLICT);
		} catch (WebdavTreeNodeAlreadyExistsException e) {
			return new DavResponse(Status.METHOD_NOT_ALLOWED);
		} catch (WebdavTreeUnsupportedOperationException e) {
			return new DavResponse(Status.FORBIDDEN);
		}
	}
	
	//-GET
	public DavResponse get(String path) throws WebdavTreeException {
		try {
			Queue<String> folders = this.parsePath(path);
			Object content = this.getNode(folders).getContent();
			if (content instanceof InputStream)
				return new DavResponse(Status.OK,(InputStream)content);
			if (content instanceof String)
				return new DavResponse(Status.OK,(String)content);
			else
				throw new WebdavTreeException("'" + content.toString() +"' as element content (String or InputStream expected)");
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	//-HEAD
	public DavResponse head(String path) throws WebdavTreeException {
		try {
			Queue<String> folders = this.parsePath(path);
			this.getNode(folders);
			//Just validation was needed
			return new DavResponse(Status.OK);
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	//-POST
	public DavResponse post(String path,InputStream stream, Set<String> tokens) throws WebdavTreeException {
		//Resolve as PUT
		return this.put(path,stream,tokens);
	}
	
	//-DELETE
	public DavResponse delete(String path, Set<String> tokens) throws WebdavTreeException {
		try {
			Queue<String> folders = this.parsePath(path);
			WebdavTreeNode node = this.getNode(folders);
			MultiStatusBody errors = new MultiStatusBody();	//To store nested errors, if any
			try {
				node.delete(tokens,errors);
				return new DavResponse(Status.NO_CONTENT);
			} catch (WebdavTreeException e) {
				return new DavResponse(Status.MULTI_STATUS,errors.getContent());					
			}
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	//-PUT
	public DavResponse put(String path,InputStream stream, Set<String> tokens) throws WebdavTreeException {
		try {
			Deque<String> folders = this.parsePath(path);
			//The last name is the file that must be created/replaced
			String name = folders.pollLast();
			this.getNode(folders).makeFile(name,stream,tokens);
			return new DavResponse(Status.OK);
		} catch (WebdavTreeNodeLockedException e) {
			return new DavResponse(Status.LOCKED);
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		} catch (WebdavTreeUnsupportedOperationException e) {
			return new DavResponse(Status.FORBIDDEN);
		}
	}

	//-COPY
	public DavResponse copy(String sourcePath, String destinyPath, boolean overwrite, int depth,Set<String> tokens) throws WebdavTreeException {
		try {
			if (sourcePath.equals(destinyPath))
				//The source and destination are the same
				return new DavResponse(Status.FORBIDDEN);
			//Get the node 
			WebdavTreeNode source = this.getNode(this.parsePath(sourcePath));
			Deque<String> folders = this.parsePath(destinyPath);
			//The last name of destinyPath is the name for "source" node in the new location 
			String name = folders.pollLast();
			WebdavTreeNode destiny = this.getNode(folders);
			MultiStatusBody errors = new MultiStatusBody();	//To store nested errors, if any
			try {
				destiny.copyTree(source,name,overwrite,depth,tokens,errors);
				return new DavResponse(Status.CREATED);
			} catch (WebdavTreeException e) {
				return new DavResponse(Status.MULTI_STATUS,errors.getContent());					
			}
		} catch (WebdavTreeNodeLockedException e) {
			return new DavResponse(Status.LOCKED);
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.CONFLICT);
		} catch (WebdavTreeNodeAlreadyExistsException e) {
			return new DavResponse(Status.PRECONDITION_FAILED);
		}
	}

	//-MOVE
	public DavResponse move(String sourcePath, String destinyPath, boolean overwrite, Set<String> tokens) throws WebdavTreeException {
		try {
			if (sourcePath.equals(destinyPath))
				//The source and destination are the same
				return new DavResponse(Status.FORBIDDEN);
			//Get the node
			WebdavTreeNode source = this.getNode(this.parsePath(sourcePath));
			Deque<String> folders = this.parsePath(destinyPath);
			//The last name of destinyPath is the name for "source" node in the new location
			String name = folders.pollLast();
			WebdavTreeNode destiny = this.getNode(folders);
			MultiStatusBody errors = new MultiStatusBody();	//To store nested errors, if any
			try {
				//Copy and delete the original
				destiny.copyTree(source,name,overwrite,Depth.infinity,tokens,errors);
				source.delete(tokens,errors);
				return new DavResponse(Status.CREATED);
			} catch (WebdavTreeException e) {
				return new DavResponse(Status.MULTI_STATUS,errors.getContent());					
			}
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.CONFLICT);
		} catch (WebdavTreeNodeAlreadyExistsException e) {
			return new DavResponse(Status.PRECONDITION_FAILED);
		}
	}

	//-LOCK
	public DavResponse lock(String path, Lock lock, int depth, Set<String> tokens) throws WebdavTreeException {
		try {
			Deque<String> folders = this.parsePath(path);
			WebdavTreeNode node = this.getNode(folders);
			MultiStatusBody errors = new MultiStatusBody();	//To store nested errors, if any
			try {
				node.lockTree(depth,lock,tokens,errors);
				DavResponse response = new DavResponse(Status.OK,"<prop xmlns='DAV:'><lockdiscovery>" + node.getLockDiscovery() + "</lockdiscovery></prop>");
				response.addHeader("Lock-Token",lock.getLockTag());
				return response;
			} catch (WebdavTreeNodeLockedException e) {
				//Avoid partially-locked structure
				LockManager.Instance.removeLockFrom((DbNode)node.node,lock.getToken());
				return new DavResponse(Status.MULTI_STATUS,errors.getContent());
			}
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
	
	//-UNLOCK
	public DavResponse unlock(String path, String token) throws WebdavTreeException {
		try {
			WebdavTreeNode node = this.getNode(this.parsePath(path));
			LockManager.Instance.removeLockFrom((DbNode)node.node,token);
			return new DavResponse(Status.NO_CONTENT);
		} catch (WebdavTreeNodeNotFoundException e) {
			return new DavResponse(Status.NOT_FOUND);
		}
	}
}

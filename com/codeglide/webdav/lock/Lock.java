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

package com.codeglide.webdav.lock;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.codeglide.webdav.exceptions.WebdavTreeNodeLockedException;

//Class hierarchy used for locking elements 
public abstract class Lock {
	final static private long LIFETIME = 60;	//The time for the lock to expire, in seconds
	final private String token;
	final private int depth;
	final private List<Long> ids = new LinkedList<Long>();
	final private Date date = new Date(new Date().getTime() + LIFETIME * 1000);
	
	//Constructors
	public Lock(int depth) {
		this(depth,UUID.randomUUID().toString());
	}
	
	public Lock(int depth,String token) {
		this.depth = depth;
		this.token = token;
	}

	//Token handling
	public void testCompatibility(Lock lock) throws WebdavTreeNodeLockedException {
		//Emulating multiple dispatching
		if (lock instanceof SharedLock)
			this.testCompatibility((SharedLock)lock);
		else if (lock instanceof ExclusiveLock)
			this.testCompatibility((ExclusiveLock)lock);
		else
			throw new WebdavTreeNodeLockedException("Unimplemented lock compatibility! " + this.getClass().getName() + " vs " + lock.getClass().getName());		
	}
	
	abstract public void testCompatibility(SharedLock lock) throws WebdavTreeNodeLockedException;
	abstract public void testCompatibility(ExclusiveLock lock) throws WebdavTreeNodeLockedException;
	
	//Getters
	abstract protected String getScopeTag();

	public String getProperty() {
		String text = "<locktype><write/></locktype>";
		text += "<lockscope>" + this.getScopeTag() + "</lockscope>";
		text += "<depth>" + Integer.toString(depth) + "</depth>";
		text += "<timeout>Second-" + Long.toString(LIFETIME) + "</timeout>";
		text += "<locktoken><href>opaquelocktoken:" + token + "</href></locktoken>";
		return text;
	}

	public String getLockTag() {
		return "<opaquelocktoken:" + token + ">";
	}

	public String getToken() {
		return token;
	}

	public void addNodeID(Long id) {
		ids.add(id);		
	}
	
	public List<Long> getNodeIDs() {
		return ids;
	}

	public boolean expired() {
		return date.before(new Date());
	}
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.webdav.exceptions.WebdavTreeNodeLockedException;

public class LockManager {
	static public LockManager Instance = new LockManager();
	private final Map<Long,Map<String,Lock>> alllocks = new HashMap<Long,Map<String,Lock>>();
	
	synchronized public Map<String,Lock> getLocksFor(DbNode node) {
		Map<String,Lock> locks = alllocks.get(node.getId());
		if (locks == null)
			return null;
		//Remove expired locks
		for (Entry<String,Lock> entry : locks.entrySet())
			if (entry.getValue().expired())
				locks.remove(entry.getKey());

		//Drop the collection if empty
		if (locks.isEmpty()) {
			alllocks.remove(node.getId());
			return null;
		}
		return locks;
	}

	synchronized public boolean hasAccessTo(DbNode node, Set<String> tokens) {
		Map<String,Lock> locks = this.getLocksFor(node);
		if (locks == null)
			//The node has no locks
			return true;
		else {
			//Check if we have any of the locks
			for (String token : tokens)
				if (locks.get(token) != null)
					return true;
			return false;
		}
	}

	synchronized public void addLockTo(DbNode node, Lock lock) throws WebdavTreeNodeLockedException {
		Map<String,Lock> nodelocks = this.getLocksFor(node);
		if (nodelocks == null) {
			nodelocks = new HashMap<String,Lock>();
			alllocks.put(node.getId(),nodelocks);
		} else {
			for (Lock nl : nodelocks.values()) {
				nl.testCompatibility(lock);
				break;	//Optimization: if then new lock is compatible with a single existing one, it is compatible with all them 
			}
		}
		nodelocks.put(lock.getToken(),lock);
		lock.addNodeID(node.getId());
	}

	synchronized public void removeLockFrom(DbNode node, String token) {
		Map<String,Lock> nodelocks = this.getLocksFor(node);
		if (nodelocks == null)
			return;
		Lock lock = nodelocks.get(token);
		if (lock != null)
			for (Long id : lock.getNodeIDs())
				alllocks.get(id).remove(token);
	}
}

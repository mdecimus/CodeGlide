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

import com.codeglide.webdav.exceptions.WebdavTreeNodeLockedException;

public class ExclusiveLock extends Lock {

	//Constructors
	public ExclusiveLock(int depth) {
		super(depth);
	}
	
	public ExclusiveLock(int depth,String token) {
		super(depth,token);
	}

	//Token handling
	public void testCompatibility(SharedLock lock) throws WebdavTreeNodeLockedException {
		//Exclusive lock isn't compatible with any other lock
		throw new WebdavTreeNodeLockedException("Element already locked as Exclusive");
	}
	
	public void testCompatibility(ExclusiveLock lock) throws WebdavTreeNodeLockedException {
		//Exclusive lock isn't compatible with any other lock
		throw new WebdavTreeNodeLockedException("Element already locked as Exclusive");
	}

	//Getters
	protected String getScopeTag() {
		return "<exclusive/>";
	}

}

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
package com.codeglide.core.rte.sequencers;

public class SequenceGenerator {
	private int id = 0;

	public synchronized int registerObject(SequenceBucketizable object) {
		int result = id++;
		object.setSequenceId(result);
		return result;
	}
	
	public synchronized int getLastId() {
		return id;
	}
	
	public void setLastId(int id) {
		this.id = id;
	}
	
}

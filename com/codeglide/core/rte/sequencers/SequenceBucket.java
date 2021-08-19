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

import java.util.Collection;
import java.util.HashMap;

public class SequenceBucket extends SequenceGenerator{
	private HashMap<Integer, SequenceBucketizable> bucket = new HashMap<Integer, SequenceBucketizable>();
	
	public synchronized int registerObject(SequenceBucketizable object) {
		if( bucket.containsValue(object) )
			return object.getSequenceId();
		int result = (object.getSequenceId() != -1) ? object.getSequenceId() : super.registerObject(object);
		bucket.put(result, object);
		return result;
	}
	
	public synchronized SequenceBucketizable getObject( int id ) {
		return bucket.get(id);
	}
	
	public synchronized void unregisterObject( int id ) {
		bucket.remove(id);
	}
	
	public synchronized int size() {
		return bucket.size();
	}
	
	public Collection<SequenceBucketizable> getItems() {
		return bucket.values();
	}
	
}

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SequenceBucketGroup extends SequenceBucket {
	private HashMap<Integer, List<SequenceBucketizable>> group = new HashMap<Integer, List<SequenceBucketizable>>();

	public synchronized int registerObjectByGroup( int groupId, SequenceBucketizable object ) {
		int result = registerObject(object);
		List<SequenceBucketizable> buckets = group.get(groupId);
		if( buckets == null ) {
			buckets = new LinkedList<SequenceBucketizable>();
			group.put(groupId, buckets);
		}
		buckets.add(object);
		return result;
	}
	
	public synchronized void unregisterGroup( int groupId ) {
		List<SequenceBucketizable> buckets = group.get(groupId);
		if( buckets != null ) {
			for( SequenceBucketizable item : buckets ) 
				super.unregisterObject(item.getSequenceId());
			group.remove(groupId);
		}
	}
	
	public synchronized void unregisterObject( int id ) {
		SequenceBucketizable item = getObject(id);
		if( item != null ) {
			for( List<SequenceBucketizable> list : group.values() )
				list.remove(item);
			super.unregisterObject(id);
		}
	}
}

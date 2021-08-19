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
package com.codeglide.core.acl;

import java.util.ArrayList;
import java.util.List;

public class UidToken implements Comparable<UidToken> {
	public static final short OWNER = 0x0001;
	public static final short MEMBER_OF = 0x0002;
	private long uid;
	private short type;
	
	public UidToken( short type, long uid ) {
		this.type = type;
		this.uid = uid;
	}
	
	public short getType() {
		return type;
	}
	
	public long getUid() {
		return uid;
	}
	
	public String getUidString() {
		return Long.toString(uid, 36);
	}
	
	public static List<UidToken> getUidTokens(String uidlist) {
		if( uidlist == null || uidlist.length() < 1)
			return new ArrayList<UidToken>();
		String[] members = uidlist.split(":");
		ArrayList<UidToken> result = new ArrayList<UidToken>();
		for( int i = 0; i < members.length; i++ ) {
			UidToken token = new UidToken((i==0)?OWNER:MEMBER_OF, Integer.parseInt(members[i], 36));
			result.add(token);
		}
		return result;
	}

	public int compareTo(UidToken arg0) {
		return (int)(uid - arg0.getUid());
	}
	
}

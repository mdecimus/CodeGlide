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
import java.util.Iterator;
import java.util.List;

public class AclToken {
	public static final short ACL_READ = 0x001;
	public static final short ACL_INSERT = 0x002;
	public static final short ACL_UPDATE = 0x004;
	public static final short ACL_DELETE = 0x008;
	public static final short ACL_OWNER = 0x010;

	private short level = 0;
	private long uid = 0;
	
	public AclToken( short level, long uid ) {
		this.level = level;
		this.uid = uid;
	}
	
	public short getLevel() {
		return level;
	}
	
	public long getUid() {
		return uid;
	}
	
	public String getUidString() {
		return Long.toString(uid, 36);
	}

	public String toString() {
		return Integer.toString(level, 36) + Long.toString(uid, 36);
	}
	
	public static List<AclToken> getTokens(String arg) {
		if( arg == null || arg.length() < 1)
			return new ArrayList<AclToken>();
		String[] list = arg.split(" ");
		ArrayList<AclToken> result = new ArrayList<AclToken>();
		for(int i = 0; i < list.length; i++ ) {
			result.add(new AclToken(Short.parseShort(list[i].substring(0, 1), 36), Long.parseLong(list[i].substring(1), 36)));
		}
		return result;
	}
	
	public static boolean checkAcl( int perm, List<UidToken> uidList, List<AclToken> aclList ) {

		Iterator<AclToken> ita = aclList.iterator();
		while( ita.hasNext() ) {
			AclToken acl = ita.next();

			Iterator<UidToken> itu = uidList.iterator();
			while( itu.hasNext() ) {
				UidToken uid = itu.next();
				// If user belongs to group admin, allow all operations.
				if( uid.getUid() == 0 ||
					(acl.getUid() == uid.getUid() && (acl.getLevel() == AclToken.ACL_OWNER || (uid.getType() & perm) != 0)) )
					return true;
			}
		}
		return false;
	}
	
	public static String getAclString( List<AclToken> aclList ) {
		StringBuffer result = new StringBuffer();
		Iterator<AclToken> ita = aclList.iterator();
		while( ita.hasNext() )
			result.append(ita.next().toString()).append(" ");
		return result.toString();
	}

}

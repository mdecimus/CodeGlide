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

import java.util.List;

public interface AclNode {
	public void setAcl(List<AclToken> aclList);
	public void setAcl(String acl);
	public String getAcl();
}

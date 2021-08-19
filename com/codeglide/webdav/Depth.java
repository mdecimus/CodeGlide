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

package com.codeglide.webdav;

public final class Depth {
	public static final int infinity = 2;
	//Retrieves the integer value of the string
	public static int ToInt(String header) {
		if (header == null)
			return infinity;
		if (header.equals("0"))
			return 0;
		if (header.equals("1"))
			return 1;
		return infinity;
	}
	//An "infinity-safe" dec operation
	public static int Dec(int value) {
		if (value == infinity)
			return infinity;
		else
			return value - 1;
	}
}

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

package com.codeglide.util;

//Generic pairs
public class Pair<X,Y>  { 
	public X first;
	public Y second;
    //Constructor
    public Pair(X first, Y second) {
      this.first  = first;
      this.second = second;
    }
}

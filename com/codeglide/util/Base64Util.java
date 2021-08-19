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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;

public class Base64Util {
	public static String encode( String s ) {
		StringBuffer result = new StringBuffer( s.length() * 3 );
		byte b64result[] = BASE64EncoderStream.encode( s.getBytes() );
		for( int i = 0; i < b64result.length; i++ )
			result.append( (char)b64result[i] );
		return result.toString();
	}

	public static String decode( String s ) {
		StringBuffer result = new StringBuffer( s.length() );
		byte b64result[] = BASE64DecoderStream.decode( s.getBytes() );
		for( int i = 0; i < b64result.length; i++ ) 
			result.append( (char)b64result[i] );
		return result.toString();
	}

	public static String[] decode( String s, int token ) {
		ArrayList<Object> al = new ArrayList<Object>();
		StringBuffer result = new StringBuffer( s.length() );
		byte b64result[] = BASE64DecoderStream.decode( s.getBytes() );
		for( int i = 0; i < b64result.length; i++ ) {
			if( b64result[i] == token ) {
				al.add( result.toString() );
				result.setLength(0);
			} else
				result.append( (char)b64result[i] );
		}
		if( result.length() > 0 )
			al.add( result.toString() );
		return (String[]) al.toArray(new String[al.size()]);
	}

	public static byte[] getBytesFromStream(InputStream in) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int c;
		while( (c = in.read()) != -1 )
			bout.write(c);
		return bout.toByteArray();
	}

}

package com.codeglide.util;

import java.util.ArrayList;

public class StringUtil {

	public static String[] splitWithQuotes( String str, char delim, char quote, boolean removeQuotes ) {
		ArrayList<String> list = new ArrayList<String>();
		boolean inQuote = false;
		StringBuffer buffer = new StringBuffer();
		for( int i = 0; i < str.length(); i++ ) {
			char c = str.charAt(i);
			if( c == quote ) {
				inQuote = !inQuote;
				if( removeQuotes ) {
					if( str.charAt(i-1) != quote )
						continue;
				}
			} else if( c == delim && !inQuote ) {
				if( buffer.length() > 0 ) {
					String b = buffer.toString();
					list.add( (removeQuotes && b.equals("\""))?"":b );
					buffer.setLength(0);
				}
				continue;
			}
			buffer.append(c);
		}
		if( buffer.length() > 0 ) {
			String b = buffer.toString();
			list.add( (removeQuotes && b.equals("\""))?"":b );
		}
		
		return (String[]) list.toArray(new String[list.size()]);
	}

}

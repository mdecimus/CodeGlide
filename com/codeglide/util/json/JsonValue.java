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
package com.codeglide.util.json;

public class JsonValue {
	public final static short T_INVALID = 0;
	public final static short T_STRING = 1;
	public final static short T_INTEGER = 2;
	public final static short T_BOOLEAN = 3;
	public final static short T_ARRAY = 4;
	public final static short T_FLOAT = 5;
	public final static short T_OBJECT = 6;

	private Object value;
	
	public JsonValue(){
		this.value = null;
	}

	public JsonValue(String value) {
		this.value = value;
	}

	public JsonValue(Integer value) {
		this.value = value;
	}

	public JsonValue(Float value) {
		this.value = value;
	}

	public JsonValue(Boolean value) {
		this.value = value;
	}

	public JsonValue(JsonValue[] value) {
		this.value = value;
	}

	public JsonValue(JsonNode value) {
		this.value = value;
	}

	public short getType() {
		if( value == null )
			return T_INVALID;
		else if( value instanceof String )
			return T_STRING;
		else if( value instanceof Integer )
			return T_INTEGER;
		else if( value instanceof Float )
			return T_FLOAT;
		else if( value instanceof Boolean )
			return T_BOOLEAN;
		else if( value instanceof JsonValue[] )
			return T_ARRAY;
		else if( value instanceof JsonNode )
			return T_OBJECT;
		else
			return T_INVALID;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void setValue(Boolean value) {
		this.value = value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
	
	public void setValue(JsonValue[] value) {
		this.value = value;
	}

	public void setValue(JsonNode value) {
		this.value = value;
	}

}

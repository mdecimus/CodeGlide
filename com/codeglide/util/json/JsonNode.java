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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Vector;

public class JsonNode {

	private String name = null;
	private JsonValue value = null;
	private JsonNode next = null;

	public static JsonNode parse(String jsonString) {
		return parse( new StringCharacterIterator(jsonString) );
	}
	
	private static JsonNode parse(CharacterIterator it) {
		char c;
		int inQuote = -1;
		String nodeName = null;
		StringBuffer buf = new StringBuffer();
		JsonNode result = null, node = null;
		Vector<JsonValue> array = null;

		while( (c = it.next()) != CharacterIterator.DONE ) {
			// Skip white chars
			if( Character.isWhitespace(c) && inQuote == -1)
				continue;
			boolean valueReady = false;
			switch( c ) {
				case '\'':
				case '"':
					buf.append(c);
					if( inQuote == c ) {
						inQuote = -1;
						valueReady = true;
					} else if( inQuote == -1 )
						inQuote = c;
					break;
				case ':':
					if( inQuote == -1 ) {
						// We have the name
						nodeName = buf.toString();
						buf = new StringBuffer();
						
						if( nodeName.startsWith("\"") || nodeName.startsWith("'") )
							nodeName = nodeName.substring(1, nodeName.length() - 1);
					} else
						buf.append(c);
					break;
				case '{':
					if( inQuote == -1 ) {
						if( array != null )
							array.add(new JsonValue(parse(it)));
						else
							node = new JsonNode(nodeName, parse(it));
					} else
						buf.append(c);
					break;
				case '}':
					if( inQuote == -1 ) {
						if( nodeName != null && buf.length() > 0 ) {
							node = new JsonNode(nodeName, getJsonValue(buf.toString()));
							if( result == null )
								result = node;
							else
								result.appendNext(node);
						}
						return result;
					} else
						buf.append(c);
					break;
				case '[':
					if( inQuote == -1 )
						array = new Vector<JsonValue>();
					else
						buf.append(c);
					break;
				case ']':
					if( inQuote == -1 ) {
						if( buf.length() > 0 ) {
							array.add(new JsonValue(buf.toString()));
							buf = new StringBuffer();
						}
						node = new JsonNode(nodeName, (JsonValue[]) array
								.toArray(new JsonValue[array.size()]));
						array = null;
					} else
						buf.append(c);
					break;
				case ',':
					if( inQuote == -1 ) {
						if( (array != null || nodeName != null) && buf.length() > 0 )
							valueReady = true;
					} else
						buf.append(c);
					break;
				case '\\':
					if(inQuote != -1) {
						buf.append(it.next());
						//it.next();
					}
					break;
				default:
					buf.append(c);
					break;
			}
			if( valueReady && nodeName != null ) {
				String value = buf.toString();
				buf = new StringBuffer();
				if( array != null )
					array.add(getJsonValue(value));
				else 
					node = new JsonNode(nodeName, getJsonValue(value));
			}
			if( node != null ) {
				if( result != null )
					result.appendNext(node);
				else
					result = node;
				node = null;
				nodeName = null;
			}
		}
		return result;
	}
	
	private static JsonValue getJsonValue(String val) {
		JsonValue result = null;
		if( val.startsWith("\"") || val.startsWith("'"))
			result = new JsonValue(val.substring(1, val.length() - 1));
		else if( val.equalsIgnoreCase("null") )
			result = new JsonValue((String)null);
		else if( val.equalsIgnoreCase("true") )
			result = new JsonValue(true);
		else if( val.equalsIgnoreCase("false") )
			result = new JsonValue(false);
		else if( val.contains(".") )
			result = new JsonValue(Float.parseFloat(val));
		else
			result = new JsonValue(Integer.parseInt(val));
		return result;
	}
	
	public JsonNode(String name, String value) {
		this.name = name;
		this.value = new JsonValue(value);
	}

	public JsonNode(String name, Integer value) {
		this.name = name;
		this.value = new JsonValue(value);
	}

	public JsonNode(String name, Boolean value) {
		this.name = name;
		this.value = new JsonValue(value);
	}

	public JsonNode(String name, JsonValue[] value) {
		this.name = name;
		this.value = new JsonValue(value);
	}

	public JsonNode(String name, JsonNode value) {
		this.name = name;
		this.value = new JsonValue(value);
	}

	public JsonNode(String name, JsonValue value) {
		this.name = name;
		this.value = (value!=null)?value:new JsonValue();
	}

	public String getName() {
		return name;
	}

	public JsonValue getValue() {
		return value;
	}

	public JsonNode getNextNode() {
		return next;
	}
	public void setNextNode(JsonNode next) {
		this.next = next;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(JsonValue value) {
		this.value = value;
	}

	public JsonNode appendNext(String name, String value) {
		return appendNext(name, new JsonValue(value));
	}

	public JsonNode appendNext(String name, Integer value) {
		return appendNext(name, new JsonValue(value));
	}

	public JsonNode appendNext(String name, Boolean value) {
		return appendNext(name, new JsonValue(value));
	}

	public JsonNode appendNext(String name, JsonValue[] value) {
		return appendNext(name, new JsonValue(value));
	}

	public JsonNode appendNext(String name, JsonNode value) {
		return appendNext(name, new JsonValue(value));
	}

	public JsonNode appendNext(String name, JsonValue value) {
		return appendNext(new JsonNode(name, (value!=null)?value:new JsonValue()));
	}

	public JsonNode appendNext(JsonNode result) {
		JsonNode nextNode = this;
		while( nextNode.getNextNode() != null )
			nextNode = nextNode.getNextNode();
		nextNode.setNextNode(result);
		return result;
	}

	public String toString() {
		
		StringBuffer buffer = new StringBuffer();
		
		JsonNode node = this;

		while(node != null){
			
			buffer.append(node.innerToString());
			node = node.getNextNode();
			if( node != null )
				buffer.append("," );
		}	
		
		return "{" + buffer.toString() + "}";
	}
	
	private String innerToString(){
		
		StringBuffer stringRepresentation = new StringBuffer();
		short valueType = value.getType();
		Object nodeValue = value.getValue();
		
		stringRepresentation.append("\"" + name + "\":");
		stringRepresentation.append(valueToString(valueType, nodeValue));
				
		return stringRepresentation.toString();
		
	}
	
	private String valueToString(short valueType, Object value){

		StringBuffer valueBuffer = new StringBuffer();
		
		switch (valueType) {
		
			case JsonValue.T_INVALID:
				valueBuffer.append("null");
				break;
		
			case JsonValue.T_STRING:
				valueBuffer.append("\"");
				for( int i = 0; i < ((String)value).length(); i++ ) {
					char c = ((String)value).charAt(i);
					if( c == '"' || c == '\\' )
						valueBuffer.append("\\");
					valueBuffer.append(c);
				}
				valueBuffer.append("\"");
				break;
				
			case JsonValue.T_INTEGER:
				valueBuffer.append( (Integer) value );
				break;
				
			case JsonValue.T_FLOAT:
				valueBuffer.append( (Float) value );
				break;
				
			case JsonValue.T_BOOLEAN:
				valueBuffer.append( (Boolean) value);
				break;
				
			case JsonValue.T_ARRAY:
				JsonValue[] values = (JsonValue[]) value;
				valueBuffer.append("[");
				
				for(int i = 0 ; i < values.length ; i++){
					if (i != 0)
						valueBuffer.append(",");
					
					valueBuffer.append(valueToString(values[i].getType(), values[i].getValue()));					
				}
				valueBuffer.append("]");
				break;
				
			case JsonValue.T_OBJECT:
				valueBuffer.append(((JsonNode)value).toString());
				break;
				
			default:
				break;
		}
		
		return valueBuffer.toString();
	}

}

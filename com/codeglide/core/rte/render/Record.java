package com.codeglide.core.rte.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Record {
	private HashMap<String, Object> fields = new HashMap<String, Object>();

	public void addField( String name, String value ) {
		fields.put(name, value);
	}
	
	public void addField( String name, boolean value ) {
		fields.put(name, value);
	}
	
	public void addField( String name, double value ) {
		fields.put(name, value);
	}
	
	public void addField( String name, int value ) {
		fields.put(name, value);
	}
	
	public void addField( String name, List<Record> value) {
		fields.put(name, value);
	}
	
	public boolean getBooleanField( String name ) {
		Object value = fields.get(name);
		return value != null && value instanceof Boolean && ((Boolean)value);
	}
	
	public String getStringField(String name) {
		Object value = fields.get(name);
		return (value != null ) ? value.toString() : null;
	}
	
	public int getIntegerField(String name) {
		Object value = fields.get(name);
		return (value != null && value instanceof Integer) ? ((Integer)value).intValue() : -1;
	}
	
	public List<Record> getRecordListField(String name) {
		Object value = fields.get(name);
		return (value != null && (value instanceof List)) ? ((List<Record>)value) : null;
	}
	
	public Object getField(String name) {
		return fields.get(name);
	}
	
	public Collection<String> getFieldNames() {
		return fields.keySet();
	}
	 
}

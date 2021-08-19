package com.codeglide.core.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.xml.dom.util.GroupRegex;

public class ObjectField extends Item implements SequenceBucketizable {
	public static final short F_STRING = 0;
	public static final short F_INTEGER = 1;
	public static final short F_DOUBLE = 2;
	public static final short F_DATE = 3;
	public static final short F_BOOLEAN = 4;
	public static final short F_LINK = 5;
	public static final short F_LINK_N = 6;
	public static final short F_STREAM = 7;
	public static final short F_CALCULATED = 8;
	public static final short F_ENUM = 9;
	public static final short F_STRING_TOKENIZER = 10;

	public static final int T_CHANGELOG = 0x001;
	public static final int T_INDEX = 0x002;

	public static final short E_LINK_OBJECT = 0;
	public static final short E_LINK_FIELD = 1;
	public static final short E_CONTENT_TYPE = 2;
	public static final short E_LIST = 4;

	private HashMap<Short, Object> settings;
	private String id, localId;
	private Expression name, bind, defaultValue;
	private short format;
	private int type;

	public ObjectField(Item parent, Element element) {
		super(parent, element);
	}
	
	public ObjectField(Item parent) {
		super(parent);
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public void addSetting(short id, Object value) {
		if( settings == null )
			settings = new HashMap<Short, Object>();
		settings.put(id, value);
	}
	
	public Object getSetting(short id) {
		if( settings == null )
			return null;
		return settings.get(id);
	}

	public Expression getName() {
		return name;
	}

	public void setName(Expression name) {
		this.name = name;
	}
	
	public Expression getBind() {
		return bind;
	}

	public void setBind(Expression bind) {
		this.bind = bind;
	}

	public short getFormat() {
		return format;
	}

	public void setFormat(short format) {
		this.format = format;
	}

	public Expression getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Expression defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isType(int type) {
		return (type & this.type) != 0;
	}

	public void setType(int type) {
		this.type |= type;
	}
	
	public int getType() {
		return type;
	}

	private int seqId = -1;
	
	public int getSequenceId() {
		return seqId;
	}

	public void setSequenceId(int id) {
		this.seqId = id;
	}

	public HashMap<Short, Object> getSettings() {
		return settings;
	}

	public void setSettings(HashMap<Short, Object> settings) {
		this.settings = settings;
	}

	protected void parseElement(Element element, Application application) {
		int types = 0;

		// Set Id
		if( getId() == null )
			setId(element.getAttribute("id"));

		// Obtain type
		String type = element.getAttribute("format");
		if(type.equalsIgnoreCase("string"))
			setFormat(ObjectField.F_STRING);						
		else if(type.equalsIgnoreCase("integer"))
			setFormat(ObjectField.F_INTEGER);						
		else if(type.equalsIgnoreCase("double"))
			setFormat(ObjectField.F_DOUBLE);						
		else if(type.equalsIgnoreCase("date"))
			setFormat(ObjectField.F_DATE);
		else if(type.equalsIgnoreCase("boolean"))
			setFormat(ObjectField.F_BOOLEAN);						
		else if(type.equalsIgnoreCase("calculated") )
			setFormat(ObjectField.F_CALCULATED);						
		else if(type.equalsIgnoreCase("link") )
			setFormat(ObjectField.F_LINK);						
		else if(type.equalsIgnoreCase("link-n") )
			setFormat(ObjectField.F_LINK_N);						
		else if(type.equalsIgnoreCase("stream") )
			setFormat(ObjectField.F_STREAM);						
		else if(type.equalsIgnoreCase("enum") )
			setFormat(ObjectField.F_ENUM);						
		else if(type.equalsIgnoreCase("stringtokenizer") )
			setFormat(ObjectField.F_STRING_TOKENIZER);						
		else if( !type.isEmpty() ){
			Logger.warn("Unknown format '" + type + "' for field '" + getId() + "'.");
			setFormat(ObjectField.F_STRING);
		}
		
		// Set types
		String typeString = element.getAttribute("type");
		if( typeString != null && !typeString.isEmpty() ) {
			String[] typeArray = element.getAttribute("type").split(",");
			for( int i = 0; i < typeArray.length; i++ ) {
				if( typeArray[i].equals("changelog") ) 
					types |= ObjectField.T_CHANGELOG;
				else if( typeArray[i].equals("index") && getFormat() != ObjectField.F_LINK_N && getFormat() != ObjectField.F_STREAM) 
					types |= ObjectField.T_INDEX;
			}
		}
		setType(types);
		
		if( !element.getAttribute("bind").isEmpty() )
			setBind(new Expression(element.getAttribute("bind")));
		if( !element.getAttribute("default").isEmpty() )
			setDefaultValue(new Expression(element.getAttribute("default")));

		// Add enum
		if( getFormat() == ObjectField.F_ENUM ) {
			HashMap<String, Expression> enumList = new HashMap<String, Expression>();
			for( Element enumElement : getChildrenElements(element) )
				enumList.put(enumElement.getAttribute("id"), new Expression(enumElement.getAttribute("value")));
			if( enumList.size() > 0 )
				addSetting(ObjectField.E_LIST, enumList);
		} else if( getFormat() == ObjectField.F_STRING_TOKENIZER ) {
			LinkedList<Object> tokenConfig = new LinkedList<Object>();
			HashMap<String, String> tokenSettings = new HashMap<String, String>();
			for( Element tokenElement : getChildrenElements(element) ) {
				String tokenName = tokenElement.getAttribute("name");
				if( tokenName.equalsIgnoreCase("inputregex") ) {
					Map<String, Integer> map = new HashMap<String, Integer>();
					String[] groups = tokenElement.getAttribute("groups").split(",");
					for( int i = 0; i < groups.length; i++ ) {
						if( !groups[i].isEmpty() ) 
							map.put(groups[i], i+1);
					}
					tokenConfig.add(new GroupRegex(tokenElement.getAttribute("value"), map));
				} else if( tokenName.equalsIgnoreCase("outputexpression") ) {
					tokenConfig.add(new Object[] {tokenElement.getAttribute("value"), getExpression(tokenElement, "condition")});
				} else
					tokenSettings.put(tokenName, tokenElement.getAttribute("value"));
			}
			if( tokenSettings.size() > 0 )
				tokenConfig.add(tokenSettings);
			if( tokenConfig.size() > 0 )
				addSetting(ObjectField.E_LIST, tokenConfig);
		}
		
		// Add name
		if( !element.getAttribute("name").isEmpty() )
			setName(new Expression(element.getAttribute("name")));

		// Check for LinkField tag
		if( getFormat() == ObjectField.F_LINK || getFormat() == ObjectField.F_LINK_N ) {
			if( !element.getAttribute("link-field").isEmpty() )
				addSetting(ObjectField.E_LINK_FIELD, element.getAttribute("link-field"));
			else
				Logger.warn("Undefined link-field for field '" + getId() + "'.");
		// Check for Stream tags
		} 
		
		if( !element.getAttribute("content-type").isEmpty()) 
			addSetting(ObjectField.E_CONTENT_TYPE, new Expression(element.getAttribute("content-type")));
		
	}
	
}

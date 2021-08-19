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
package com.codeglide.core.objects;

import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.Exit;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.session.Session;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.DbLinknNode;
import com.codeglide.util.StringInputStream;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrBoolean;
import com.codeglide.xml.dom.DynamicAttrCalculated;
import com.codeglide.xml.dom.DynamicAttrDate;
import com.codeglide.xml.dom.DynamicAttrEnum;
import com.codeglide.xml.dom.DynamicAttrLink;
import com.codeglide.xml.dom.DynamicAttrNumber;
import com.codeglide.xml.dom.DynamicAttrStream;
import com.codeglide.xml.dom.DynamicAttrString;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.util.GroupRegex;
import com.codeglide.xml.dom.util.StringTokenizer;
import com.codeglide.xml.dom.util.StringTokenizerAttr;

public class ObjectDefinition extends Item {
	private String id;
	private HashMap<String, ObjectField> fields;
	private HashMap<String, ObjectDefinition> objects;
	private HashMap<Integer, Function> functionsMap;
	private Expression description;
	private boolean repeatable;
	
	public ObjectDefinition(Item parent, Element element, String parentId, int depth ) {
		super(parent);
		fields = new HashMap<String, ObjectField>();
		parseElement(element, (Application)getAncestor(Application.class), parentId, depth);
	}

	/*public ObjectDefinition getParent() {
		return (ObjectDefinition)parent;
	}*/

	/*public void setParent(ObjectDefinition parent) {
		this.parent = parent;
	}*/

	public void getFields(List<ObjectField> result, int type) {
		//LinkedList<Field> result = new LinkedList<Field>();
		for( ObjectField field : fields.values() ) {
			if( field.isType(type) )
				result.add(field);
		}
		//return (result.size()>0)?result:null;
	}
	
	public Function getFunction(String name) {
		return (functionsMap!=null) ? functionsMap.get(name) : null;
	}
	
	public ObjectField getField( String name ) {
		return fields.get(name);
	}
	
	public Collection<ObjectField> getFields() {
		return fields.values();
	}
	
	public void addField(ObjectField field){
		fields.put(field.getLocalId(), field);
	}
	
	public void addObject(ObjectDefinition object) {
		if( objects == null )
			objects = new HashMap<String, ObjectDefinition>();
		objects.put(object.getId(), object);
		object.setParent(this);
	}
	
	public ObjectDefinition getObject(String name) {
		return (objects != null) ? objects.get(name) : null;
	}
	
	public Collection<ObjectDefinition> getObjects() {
		return (objects != null) ? objects.values() : null;
	}

	public Expression getDescription() {
		return description;
	}

	public void setDescription(Expression description) {
		this.description = description;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}
	
	// Object building functions
	
	public DynamicElement buildObject(Document doc) {
		DynamicElement result = new DynamicElement(doc, id);
		addFields(result, false);
		if( objects != null ) {
			for( ObjectDefinition child : objects.values() ) 
				result._appendChild(child.buildObject(doc));
		}
		handleEvent(ON_CREATE, result);
		return result;
	}
	
	public final static int ON_CREATE = 1;
	public final static int ON_SAVE   = 2;
	public final static int ON_INSERT = 3;
	public final static int ON_UPDATE = 4;
	public final static int ON_DELETE = 5;
	
	public boolean handleEvent(int type, DynamicElement input ) {
		if( functionsMap != null ) {
			
			Function fnc = functionsMap.get(type);
			if( fnc != null ) {
				try {
					RootNode rootNode = (RootNode)input.getDocumentNode().getDocumentElement();
					Context context = Context.getCurrent();
					if( context == null ) {
						// Create virtual context and virtual session
						Session session = new Session();
						session.setRootNode(rootNode);
						session.setGlobalVariables(rootNode.getGlobalVariables());
						context = new Context(session);
						context.setRootNode(rootNode);
						context.getVariables().addVariables("_g", rootNode.getGlobalVariables());
					}
					Vector<Object> elements = new Vector<Object>();
					elements.add(input);
					fnc.run(context, new LinkedList<Action>(), elements, true);
				} catch (Exit _) {
					return false;
				} catch (CodeGlideException e) {
					Logger.debug(e);
					return false;
				}
			}
		}
		return true;
	}
	
	public DynamicAttr buildField(DynamicElement node, String fieldName, Object value) {
		ObjectField field = getField(fieldName);
		if(field == null) {
			if (value == null || value instanceof String)
				return new DynamicAttrString(node, fieldName, (String)value);
			else if (value instanceof InputStream)
				return new DynamicAttrStream(node, fieldName, (InputStream)value);
			else
				return new DynamicAttrString(node, fieldName, null);
		} else
			return buildField(node, field, value);
	}
	
	public DynamicAttr buildField(DynamicElement node, ObjectField field, Object value) {
		DynamicAttr attr = null;
		switch( field.getFormat() ) {
			case ObjectField.F_CALCULATED:
				attr = new DynamicAttrCalculated(node, field);
				break;
			case ObjectField.F_BOOLEAN:
				attr = new DynamicAttrBoolean(node, field, (value!=null)?value.toString():null);
				break;
			case ObjectField.F_DATE:
				attr = new DynamicAttrDate(node, field, (value!=null)?value.toString():null);
				break;
			case ObjectField.F_DOUBLE:
			case ObjectField.F_INTEGER:
				attr = new DynamicAttrNumber(node, field, (value!=null)?value.toString():null);
				break;
			case ObjectField.F_STREAM:
				try {
					if( value != null && !(value instanceof InputStream) )
						value = new StringInputStream(value.toString(), "utf-8");
				} catch (CharacterCodingException e) {
					value = null;
				}
				attr = new DynamicAttrStream(node, field, (InputStream)value);
				break;
			case ObjectField.F_ENUM:
				attr = new DynamicAttrEnum(node, field, (value!=null)?value.toString():null);
				break;
			case ObjectField.F_LINK:
				attr = new DynamicAttrLink(node, field, (value!=null)?value.toString():null);
				break;
			case ObjectField.F_LINK_N:
				{
					DbLinknNode link = new DbLinknNode(node.getDocumentNode(), field.getLocalId(), field);
					node._appendChild(link);
					//link.enableTracking();
				}
				break;
			case ObjectField.F_STRING_TOKENIZER:
			{
				List<Object> settings = (List<Object>) field.getSetting(ObjectField.E_LIST);
				if( settings != null ) {
					StringTokenizer stringTokenizer = new StringTokenizer(node.getDocumentNode(), field.getLocalId());
					for( Object setting : settings ) {
						if( setting instanceof GroupRegex )
							stringTokenizer.addInputRegex((GroupRegex)setting);
						else if( setting instanceof Object[] )
							stringTokenizer.addOutputExpression( (String)((Object[])setting)[0] , (Expression)((Object[])setting)[1]);
						else if( setting instanceof HashMap ) {
							HashMap<String, String>map = (HashMap<String, String>) setting;
							for( String key : map.keySet() ) {
								String keyValue = map.get(key);
								if( key.equalsIgnoreCase("quotechars") ) {
									for( int i = 0; i < keyValue.length(); i++ )
										stringTokenizer.addQuoteChar(keyValue.charAt(i));
								} else if( key.equalsIgnoreCase("separatorchars") ) {
									for( int i = 0; i < keyValue.length(); i++ )
										stringTokenizer.addSeparatorChar(keyValue.charAt(i));
								} else if( key.equalsIgnoreCase("escapechars") ) {
									for( int i = 0; i < keyValue.length(); i++ )
										stringTokenizer.addEscapeChar(keyValue.charAt(i));
								} else if( key.equalsIgnoreCase("childname") ) {
									stringTokenizer.setChildName(keyValue);
								}
							}
						}
					}
					attr = new StringTokenizerAttr(node, field, stringTokenizer, (value!=null)?value.toString():null);
					node._appendChild(stringTokenizer);
					stringTokenizer.enableTracking();
				}
			}
				break;
			case ObjectField.F_STRING:
			default:
				attr = new DynamicAttrString(node, field, (value!=null)?value.toString():null);
				break;
				
		}
		return attr;
	}
	
	public void addFields(DynamicElement node, boolean onlyMissing ) {
		for( ObjectField field : fields.values() ) {
			if( onlyMissing && node.hasAttribute(field.getLocalId()) )
				continue;

			String value = null;
			try {
				if( field.getDefaultValue() != null )
					value = field.getDefaultValue().evaluate(null, node.getDocumentNode());
			} catch (ExpressionException _) {}
			DynamicAttr attr = buildField(node, field, value);
			if( attr != null )
				node.setAttributeNode(attr);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	protected void parseElement(Element element, Application application, String parentId, int depth) {
		this.id = element.getAttribute("id");
		if( id.isEmpty() )
			Logger.warn("Empty object ID.");
		if( !element.getAttribute("name").isEmpty() )
			setDescription(new Expression(element.getAttribute("name")));
		
		// Is this object repeatable
		setRepeatable(element.getAttribute("repeatable").equalsIgnoreCase("true"));
		if( isRepeatable() ) {
			parentId = null;
			depth = 0;
		}

		if( depth > 0 ) {
			if( parentId == null )
				parentId = id + "/";
			else
				parentId += id + "/";
		}
		functionsMap = null;
		for( Element child : getChildrenElements(element) ) {
			//if (child.getNodeName().equals("functions"))
			//	setFunctionList(parseFunctions(new FunctionList(), child, application));
			//else 
			if( child.getNodeName().equals("functions") ) {
				functionsMap = new HashMap<Integer, Function>();
				for( Element function : getChildrenElements(child) ) {
					String fncName = function.getAttribute("name").toLowerCase();
					int fncType = -1;
					if( fncName.equals("oninsert") )
						fncType = ON_INSERT;
					else if( fncName.equals("onupdate") )
						fncType = ON_UPDATE;
					else if( fncName.equals("ondelete") )
						fncType = ON_DELETE;
					else if( fncName.equals("onsave") )
						fncType = ON_SAVE;
					else if( fncName.equals("oncreate") )
						fncType = ON_CREATE;
					if( fncType != -1 )
						functionsMap.put(fncType, new Function(this, function));
				}
			} else if (child.getNodeName().equals("field")) {
				ObjectField field = new ObjectField(this, child);
				field.setLocalId(field.getId());
				
				// Register field
				application.getFieldBucket().registerObject(field);
				
				// Build XPath expression
				String attributeName = null;
				if( field.getFormat() == ObjectField.F_LINK_N ) 
					attributeName = field.getLocalId();
				else
					attributeName = "@" + field.getLocalId();
				if( parentId != null ) {
					field.setId(parentId + field.getLocalId());
					field.setBind(new Expression( "!" + parentId + attributeName ));
				} else {
					field.setBind(new Expression( "!" + attributeName ));
				}
				addField(field);
			} else if (child.getNodeName().equals("object")) {
				addObject(new ObjectDefinition(this, child, parentId, depth + 1));
			}
		}
		
	}

	protected void parseElement(Element element, Application application) {
		// Not used
	}

}

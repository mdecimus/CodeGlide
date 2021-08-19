package com.codeglide.interfaces.system;

import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Document;

import com.codeglide.core.Expression;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.xml.dom.DynamicElement;

public class ObjectSchema extends DynamicElement {

	public ObjectSchema(Document parentDoc, String nodeName) {
		super(parentDoc, nodeName);
		
		this.nodeFlags |= NEEDS_CHILD_INIT;
	}

	protected void initChildren() {
		buildObjectTree( this, ((RootNode) getDocumentNode().getDocumentElement()).getApplication().getObjects() );
	}
	
	private void buildObjectTree(DynamicElement parentObj, Collection<ObjectDefinition> list ) {
		for( ObjectDefinition obj : list ) {
			DynamicElement objNode = (DynamicElement) parentObj.appendChild("Object");
			objNode.setAttribute("Id", obj.getId());
			try {
				objNode.setAttribute("Name", obj.getDescription().evaluate(null, getDocumentNode()));
			} catch (Exception _) {
			}
			for( ObjectField field : obj.getFields() ) 
				buildFieldNode(parentObj, field);
			
			if( obj.getObjects() != null )
				buildObjectTree(objNode, obj.getObjects());
		}
	}
	
	private void buildFieldNode(DynamicElement parentNode, ObjectField field) {
		DynamicElement node = (DynamicElement) parentNode.appendChild("Field");
		node.setAttribute("Id", field.getId());
		node.setAttribute("LocalId", field.getLocalId());
		try {
			node.setAttribute("Name", field.getName().evaluate(null, parentNode.getDocumentNode()));
		} catch (Exception _) {
		}
		
		// Set format
		String format = null;
		switch( field.getFormat() ) {
			case ObjectField.F_BOOLEAN:
				format = "boolean";
				break;
			case ObjectField.F_CALCULATED:
				format = "calculated";
				break;
			case ObjectField.F_DATE:
				format = "date";
				break;
			case ObjectField.F_DOUBLE:
				format = "double";
				break;
			case ObjectField.F_ENUM:
			{
				format = "enum";
				HashMap<String, Expression> values = (HashMap<String, Expression>) field.getSetting(ObjectField.E_LIST);
				if( values != null ) {
					for( String optionValue : values.keySet() ) {
						DynamicElement enumNode = (DynamicElement)node.appendChild("Enum");
						enumNode.setAttribute("Id", optionValue);
						try {
							enumNode.setAttribute("Value", values.get(optionValue).evaluate(null, parentNode.getDocumentNode()));
						} catch (Exception _) {
						}
					}
				}
			}
				break;
			case ObjectField.F_INTEGER:
				format = "integer";
				break;
			case ObjectField.F_LINK:
				format = "link";
				try {
					node.setAttribute("LinkObject", ((ObjectDefinition)field.getSetting(ObjectField.E_LINK_OBJECT)).getId() );
					node.setAttribute("LinkField", ((ObjectField)field.getSetting(ObjectField.E_LINK_FIELD)).getId() );
				} catch (Exception _) {
				}
				break;
			case ObjectField.F_LINK_N:
				format = "link-n";
				try {
					node.setAttribute("LinkObject", ((ObjectDefinition)field.getSetting(ObjectField.E_LINK_OBJECT)).getId() );
					node.setAttribute("LinkField", ((ObjectField)field.getSetting(ObjectField.E_LINK_FIELD)).getId() );
				} catch (Exception _) {
				}
				break;
			case ObjectField.F_STREAM:
				format = "stream";
				break;
			case ObjectField.F_STRING_TOKENIZER:
				format = "stringTokenizer";
				break;
			case ObjectField.F_STRING:
			default:
				format = "string";
				break;
		}
		node.setAttribute("Format", format);
		
		// Set type
		StringBuffer type = new StringBuffer();
		if( field.isType(ObjectField.T_INDEX) )
			type.append("index");
		if( field.isType(ObjectField.T_CHANGELOG) ) {
			if( type.length() > 0 )
				type.append(",");
			type.append("changelog");
		}
		node.setAttribute("Type", type.toString());
	}
	

}

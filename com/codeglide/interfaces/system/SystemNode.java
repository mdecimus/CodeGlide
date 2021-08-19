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
package com.codeglide.interfaces.system;

import java.util.Date;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.interfaces.xmldb.DbInterface;
import com.codeglide.util.ISO8601;
import com.codeglide.util.spell.SpellChecker;
import com.codeglide.xml.dom.DynamicAttrVirtual;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.NullElement;
import com.codeglide.xml.dom.VirtualElement;
import com.codeglide.xml.dom.util.CalendarElement;

public class SystemNode extends DynamicElement {

	public SystemNode(Document parentDoc) {
		super(parentDoc, "System");
		
		setAttributeNode(new IdFactoryAttr(this, "IdFactory"));
		setAttributeNode(new CurrentDateAttr(this, "CurrentDate"));
		setAttributeNode(new CurrentUidAttr(this, "CurrentUid"));

		// Add objects
		appendChild(new ObjectSchema(parentDoc, "ObjectSchema"));
		appendChild(new ToolsNode(parentDoc, "Tools"));
	}
	
	/*
	 * 
	 * ServerTimezone
	 * Timezone
	 * 
	 * LocalTime
	 * ServerLocalTime
	 * 
	 * EffectiveUid
	 * Language
	 * 
	 * SiteName
	 * 
	 */
	
	/*
	 *  
	 *  /CG/System/Tools/Calendar
	 *  /CG/System/Tools/SpellChecker
	 *  /CG/System/Tools/Report
	 *  
	 *  /CG/System/ObjectSchema/Name/FieldName
	 *  
	 *  Replace
	 *  cg:db:generateUniqueId() =>  /CG/System/@IdFactory
	 *  cg:db:isDbNode($Result)  =>  $Result/@_Id != ''
	 *  cg:dg:id($Result)        =>  $Result/@_Id
	 *  cg:db:getChangelog($Result)    =>  $Result/_Changelog
	 *  cg:db:hasChanged         =>  cg:util:has-changed(x)
	 *  runFunction              =>  cg:fnc:xx
	 *  isNullObject($Result)    =>  name($Result) = '__NULL'
	 *  cg:util:newObject(/CG,'xxx') => createObject
	 * 
	 *  sed 's/cg:util:isNewObject($)/name($) = \'__NULL\'/g'
	 * 
	 *  searchText search-text
	 *  aclCheck   check-acl
	 *  getChildren  select
	 *  findText     find-text
	 *  addChild     changes to <addObject
	 *  
	 *  addObject => addVar
	 * 
	 */
	
	public class ToolsNode extends VirtualElement {

		public ToolsNode(Document parentDoc, String nodeName) {
			super(parentDoc, nodeName);
			appendChild(new ToolItem(parentDoc, "SpellChecker"));
			appendChild(new ToolItem(parentDoc, "Calendar"));
		}
		
		public DynamicElement createObject(String name) {
			if( name.equals("SpellChecker") )
				return new SpellChecker(parentDoc);
			else if( name.equals("Calendar") )
				return new CalendarElement(parentDoc);
			else
				return new NullElement(parentDoc);
		}
	}
	
	public class ToolItem extends VirtualElement {

		public ToolItem(Document parentDoc, String nodeName) {
			super(parentDoc, nodeName);
		}

		public Node cloneNode(boolean deep) {
			return ((ToolsNode)parentNode).createObject(nodeName);
		}
	}
	
	public class IdFactoryAttr extends DynamicAttrVirtual {

		public IdFactoryAttr(DynamicElement parentNode, String name) {
			super(parentNode, name);
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return Long.toString(((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF)).getUniqueId(), 36);
		}

		public void setValue(String value) throws DOMException {
			
		}
		
	}
	
	public class CurrentDateAttr extends DynamicAttrVirtual {

		public CurrentDateAttr(DynamicElement parentNode, String name) {
			super(parentNode, name);
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return ISO8601.formatUtc(new Date());
		}

		public void setValue(String value) throws DOMException {
			
		}
		
	}
	
	public class CurrentUidAttr extends DynamicAttrVirtual {

		public CurrentUidAttr(DynamicElement parentNode, String name) {
			super(parentNode, name);
		}

		public String getExpandedValue() {
			return getValue();
		}

		public String getValue() {
			return Long.toString(Context.getCurrent().getRootNode().getUserId(), 36);
		}

		public void setValue(String value) throws DOMException {
			
		}
		
	}
	

}

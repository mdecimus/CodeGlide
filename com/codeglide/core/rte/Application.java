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
package com.codeglide.core.rte;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.sequencers.SequenceBucket;
import com.codeglide.core.rte.variables.Variable;
import com.codeglide.core.rte.widgets.RootPanel;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.core.rte.widgets.Window;
import com.codeglide.interfaces.xmldb.DbInterface;
import com.codeglide.interfaces.xmldb.SqlConnectionPool.SqlConnection;
import com.codeglide.interfaces.xmldb.sql.InsertQuery;
import com.codeglide.interfaces.xmldb.sql.SelectQuery;
import com.codeglide.interfaces.xmldb.sql.SqlDialect;
import com.codeglide.interfaces.xmldb.sql.Table;
import com.codeglide.interfaces.xmldb.sql.schema.ColumnDefinition;
import com.codeglide.interfaces.xmldb.sql.schema.TableDefinition;

public class Application extends Item {
	public final static int FIRST_DEPLOYMENT = 0x001;
	
	private String appName;
	private HashMap<String, ObjectDefinition> objDefinition;
	private List<Variable> variables;
	private HashMap<String, Function> functions;
	private HashMap<String, Function> events;
	private HashMap<String, RootPanel> panels;
	private HashMap<String, Service> services;
	private int flags = 0;
	
	// Widget bucket
	private SequenceBucket widgetBucket;
	
	// Field bucket
	private SequenceBucket fieldBucket;
	
	// ID to Widget map
	private HashMap<String, Widget> idWidgetMap;
	
	// Language entries
	private Language langEntries;

	// ID mapping lists
	private HashMap<String, Integer> mapStringId;
	private HashMap<Integer, String> mapIdString;

	//TODO delete this
	private String mainPath;
	
	public Application() {
		super(null);
		objDefinition = new HashMap<String, ObjectDefinition>();
		variables = new LinkedList<Variable>();
		functions = new HashMap<String, Function>();
		events = new HashMap<String, Function>();
		panels = new HashMap<String, RootPanel>();
		services = new HashMap<String, Service>();
		flags = 0;
		widgetBucket = new SequenceBucket();
		fieldBucket = new SequenceBucket();
		idWidgetMap = new HashMap<String, Widget>();
	}
	
	public Application(File file) throws SAXException, IOException {
		this();
		DOMParser docParser = new DOMParser();
		docParser.setFeature("http://xml.org/sax/features/namespaces", false);
		docParser.parse("file:" + file.getAbsolutePath());
		this.mainPath = file.getParent();
		parseElement(docParser.getDocument().getDocumentElement(), this);
		resolveLinks(objDefinition.values());
	}
	
	public Application(InputStream stream) throws SAXException, IOException {
		this();
		InputSource inputSource = new InputSource(stream);
		DOMParser docParser = new DOMParser();
		docParser.parse(inputSource);
		parseElement(docParser.getDocument().getDocumentElement(), this);
		resolveLinks(objDefinition.values());
	}

	public boolean hasFlag(int flag) {
		return (flags & flag) != 0;
	}
	
	public void setFlag(int flag) {
		flags |= flag;
	}
	
	public void unsetFlag(int flag) {
		flags &= ~flag;
	}
	
	public Collection<RootPanel> getPanels() {
		return panels.values();
	}
	
	public RootPanel getPanel(String name) {
		return panels.get(name);
	}
	
	public Window getWindow(String name) {
		RootPanel panel = panels.get(name);
		return (panel != null && panel instanceof Window) ? (Window)panel : null;
	}
	
	public Function getEvent(String name) {
		return events.get(name);
	}

	public Collection<ObjectDefinition> getObjects() {
		return objDefinition.values();
	}
	
	public Service getService(String name) {
		return services.get(name);
	}

	public List<ObjectField> getAllObjectFields(String name, int type) {
		ObjectDefinition obj = objDefinition.get(name);
		if( obj == null )
			return null;
		LinkedList<ObjectField> result = new LinkedList<ObjectField>();
		addObjectFields(result, obj, type);
		return (result.size()>0)?result:null;
	}
	
	private void addObjectFields( List<ObjectField> list, ObjectDefinition obj, int type ) {
		obj.getFields(list, type);
		Collection<ObjectDefinition> children = obj.getObjects();
		if( children != null ) {
			for( ObjectDefinition child : children ) {
				if( !child.isRepeatable() )
					addObjectFields(list, child, type);
			}
		}
	}

	public ObjectField getField(String name) {
		try {
			String[] p = name.split("\\/");
			ObjectDefinition obj = getObject(p[0]);
			for( int i = 1; i < p.length - 1; i++ ) {
				ObjectDefinition prevObj = obj;
				obj = obj.getObject(p[i]);
				
				// List might be a link or link-n field
				if( obj == null ) {
					ObjectField field = prevObj.getField(p[i]);
					if( field != null && (field.getFormat() == ObjectField.F_LINK || field.getFormat() == ObjectField.F_LINK_N) ) 
						obj = (ObjectDefinition)field.getSetting(ObjectField.E_LINK_OBJECT);
				}
			}
			return obj.getField(p[p.length-1]);
		} catch (Exception e) {
			Logger.warn("Could not locate field '" + name + "'.");
			return null;
		}
	}
	
	public ObjectDefinition getObject(String name){
		return objDefinition.get(name);
	}
	
	public Function getFunction(String name) {
		return functions.get(name);
	}

	public List<Variable> getVariables(){
		return variables;
	}

	public SequenceBucket getWidgetBucket() {
		return widgetBucket;
	}
	
	public SequenceBucket getFieldBucket() {
		return fieldBucket;
	}
	
	public String getLanguageEntry(String lang, String id) {
		return langEntries.getLanguageEntry(lang, id);
	}
	
	public void setLanguage(Language lang) {
		this.langEntries = lang;
	}

	public void addObjectDefinition(ObjectDefinition object) {
		objDefinition.put(object.getId(), object);
	}
	
	public void addService(Service service){
		services.put(service.getType(), service);
	}

	public String getName() {
		return appName;
	}
	
	public void setAppName(String appName) {
		this.appName = appName.toUpperCase();
	}
	
	// Widget to ID mappings
	
	public Widget getWidgetById(String id) {
		return idWidgetMap.get(id);
	}
	
	public void addWidgetMapping( String id, Widget widget) {
		idWidgetMap.put(id, widget);
	}
	
	// ID functions
	private synchronized void getIds() {
		DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
		SqlConnection conn = null;
		try {
			SqlDialect dialect = db.getDialect();
			conn = db.getConnection();

			mapIdString = new HashMap<Integer, String>();
			mapStringId = new HashMap<String, Integer>();
			
			if( db.hasTable( appName + "_ID") ) {
				SelectQuery select = dialect.createSelect();
				Table th = new Table(appName+"_ID");
				select.addTable(th);
				select.addColumn(th.getColumn("ID"));
				select.addColumn(th.getColumn("NAME"));
				
				PreparedStatement stmt = select.prepareStatement(conn.getConnection());
				ResultSet rs = stmt.executeQuery();
				
				while( rs.next() ) {
					mapIdString.put(rs.getInt(1), rs.getString(2));
					mapStringId.put(rs.getString(2), rs.getInt(1));
				}
				db.closeResultSet(rs);
			} else {
				// Create the table
				TableDefinition table = new TableDefinition(appName+"_ID");
				table.addColumn(new ColumnDefinition("ID", ColumnDefinition.TYPE_INT, 0, false, true, true));
				table.addColumn(new ColumnDefinition("NAME", ColumnDefinition.TYPE_VARCHAR, 50, false, false, false));
				db.createTable(table);
				conn.commit();
			}
		} catch (SQLException e) {
			Logger.debug(e);
		} finally {
			if( conn != null )
				db.releaseConnection(conn);
		}
	}
	
	//TODO [V2] Make Attribute IDs server-farm friendly.
	public int getId(String name) {
		if( mapStringId == null )
			getIds();
		Integer id = mapStringId.get(name);
		if( id != null )
			return id.intValue();
		synchronized (mapStringId) {
			int result = -1;
			DbInterface db = ((DbInterface)ServerSettings.getInterface(ServerSettings.XMLDB_IF));
			SqlConnection conn = null;
			try {
				conn = db.getConnection();

				// Update hierarchy table
				InsertQuery insert = db.getDialect().createInsert(appName+"_ID");
				insert.setValue("NAME", name);
				result = (int)db.executeInsert(conn.getConnection(), insert);
				conn.commit();
				mapIdString.put(result, name);
				mapStringId.put(name, result);
			} catch (SQLException e) {
				if( conn != null ) {
					try {
						conn.rollback();
					} catch (SQLException _) {}
				}
				Logger.debug(e);
			} finally {
				if( conn != null )
					db.releaseConnection(conn);
			}
			return result;
		}
	}
	
	public int _getId(String name) {
		if( mapStringId == null )
			getIds();
		Integer id = mapStringId.get(name);
		if( id != null )
			return id.intValue();
		else
			return -1;
	}
	
	public String getEncodedId(String name) {
		return Integer.toString(getId(name), 36);
	}
	
	public String getString( String name ) {
		return getString(Integer.parseInt(name, 36));
	}
	
	public String getString( int id ) {
		if( id < 0 )
			return null;
		if( mapIdString == null )
			getIds();
		String name = mapIdString.get(id);

		if( name == null )
			Logger.warn("Lookup of non-existant ID " + id + ".");
		
		return name;
	}

	public String getColumnName(ObjectField column) {
		return getColumnName(column.getId());
	}
	
	public String getColumnName(String column) {
		return "X"+Integer.toString(getId(column), 36).toUpperCase();
	}

	protected void parseElement(Element element, Application application) {
		if (!element.getNodeName().equalsIgnoreCase("application") )
			return;
		if( !element.hasAttribute("extends") )
			setAppName(element.getAttribute("id"));
		for( Element child : getChildrenElements(element) ) {
			String nodeName = child.getNodeName();

			if (nodeName.equals("objects")) {
				for( Element object : getChildrenElements(child) ) {
					if( object.getNodeName().equalsIgnoreCase("object") )
						addObjectDefinition(new ObjectDefinition(this, object, null, 0));
				}
			} else if (nodeName.equals("variables")) {
				for( Element var : getChildrenElements(child) ) {
					variables.add(new Variable(this, var));
				}
			} else if (nodeName.equals("functions")) {
				for( Element fnc : getChildrenElements(child) ) {
					functions.put(fnc.getAttribute("name").toLowerCase(), new Function(this, fnc));
				}
			} else if (nodeName.equals("events")) {
				for( Element fnc : getChildrenElements(child) ) {
					events.put(fnc.getAttribute("name").toLowerCase(), new Function(this, fnc));
				}
			} else if (nodeName.equals("languages")) {
				setLanguage(new Language(this, child));
			} else if (nodeName.equals("panels")) {
				for( Element panel : getChildrenElements(child) ) {
					if( panel.getNodeName().equalsIgnoreCase("window") )
						panels.put(panel.getAttribute("name"), new Window(this, panel));
					else
						panels.put(panel.getAttribute("name"), new RootPanel(this, panel));
				}
			} else if (nodeName.equals("services")){
				for( Element service : getChildrenElements(child) ){
					addService(new Service(this, service));
				}
			} else if (nodeName.equals("includes")) {
				for( Element include : getChildrenElements(child) ) {
					if( !include.getNodeName().equalsIgnoreCase("include") )
						continue;
					try {
						String fileName = include.getAttribute("file");
						DOMParser docParser = new DOMParser();
						docParser.setFeature("http://xml.org/sax/features/namespaces", false);
						docParser.parse("file:" + mainPath + "/" + fileName);
						parseElement(docParser.getDocument().getDocumentElement(), this);
					} catch (Exception e) {
						Logger.debug(e);
					}
				}
			}
		}		
	}
	
	private void resolveLinks(Collection<ObjectDefinition> objects) {
		for( ObjectDefinition object : objects ) {
			for( ObjectField field : object.getFields() ) {
				if( field.getFormat() == ObjectField.F_LINK || field.getFormat() == ObjectField.F_LINK_N ) {
					String linkName = (String)field.getSetting(ObjectField.E_LINK_FIELD);
					field.addSetting(ObjectField.E_LINK_FIELD, null);
					if( linkName != null ) {
						String[] fieldPath = linkName.split("\\/");
						ObjectDefinition objDef = getObject(fieldPath[0]);
						if( objDef != null ) {
							ObjectField sourceField = null;
							field.addSetting(ObjectField.E_LINK_OBJECT, objDef);
							for( int i = 1; (i < fieldPath.length - 1) && objDef != null; i++ )
								objDef = objDef.getObject(fieldPath[i]);
							if( objDef != null )
								sourceField = objDef.getField(fieldPath[fieldPath.length-1]);
							if( sourceField != null ) {
								if( field.getFormat() == ObjectField.F_LINK || (field.getFormat() == ObjectField.F_LINK_N && (sourceField.getFormat() == ObjectField.F_LINK || sourceField.getFormat() == ObjectField.F_LINK_N)))
									field.addSetting(ObjectField.E_LINK_FIELD, sourceField);
								else
									Logger.debug("LINK-N field '" + field.getId() + "' can only be linked to LINK or LINK-N fields..");
							} else
								Logger.debug("Undefined field '" + linkName + "' referenced by field '" + field.getId() + "'.");
							
						} else
							Logger.debug("Undefined object '" + fieldPath[0] + "' referenced by field '" + field.getId() + "'.");
					}
				}
			}
			
			// Resolve children objects
			if( object.getObjects() != null )
				resolveLinks(object.getObjects());
		}
	}
	
	protected class Language extends Item {
		private HashMap<String, String[]> langList;
		private Vector<String> langId;

		public Language(Item parent, Element element) {
			super(parent, element);
		}

		public String getLanguageEntry(String lang, String id) {
			try {
				return langList.get(id)[langId.indexOf(lang)];
			} catch (Exception e) {
				return "";
			}
		}

		public int addLanguage(String lang) {

			int index = langId.indexOf(lang); 

			if(index == -1){
				langId.add(lang);
				index = langId.indexOf(lang);
			}

			return index;
		}

		public void addLanguageEntry(int langId, String id, String value) {

			if(!langList.containsKey(id)){
				String[] values = new String[this.langId.size()];
				values[langId] = value;
				langList.put(id , values);
			}else{
				String[] values = langList.get(id);
				values[langId] = value;
			}

		}

		public String getDefaultLanguage() {
			return langId.firstElement();
		}

		protected void parseElement(Element element, Application application) {
			langList = new HashMap<String, String[]>();
			langId = new Vector<String>();
			
			List<Element> languages = getChildrenElements(element);
			for( Element lang : languages )
				addLanguage(lang.getAttribute("id"));
			for( Element lang : languages ) {
				int languageIndex = addLanguage(lang.getAttribute("id"));
				for( Element message : getChildrenElements(lang) )
					addLanguageEntry(languageIndex, message.getAttribute("id"), message.getTextContent());
			}
		}
	}

	
}

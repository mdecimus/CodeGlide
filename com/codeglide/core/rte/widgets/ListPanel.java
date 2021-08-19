package com.codeglide.core.rte.widgets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.interfaces.ClickHandler;
import com.codeglide.core.rte.interfaces.GetRecordHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.Record;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.interfaces.xmldb.DbRootNode;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public abstract class ListPanel extends AbstractPanel implements GetRecordHandler, ClickHandler {
	protected Expression displaySettingsExpression, bindExpression, ddGroupExpression, extendsExpression;
	protected HashMap<String, ListField> _fieldsMap;
	protected Function onClickFunction, onDoubleClickFunction;

	public ListPanel(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		displaySettingsExpression = getExpression(element, "displaySettings");
		bindExpression = getExpression(element, "bind");
		ddGroupExpression = getExpression(element, "ddgroup");
		_fieldsMap = null;
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("onclick") )
							onClickFunction = new Function(this, function);
						else if( function.getAttribute("name").equalsIgnoreCase("ondoubleclick") )
							onDoubleClickFunction = new Function(this, function);
					}
				}
			} else if(containers.getNodeName().equalsIgnoreCase("fields")) {
				String extendsAttr = containers.getAttribute("extends");
				if( extendsAttr != null && extendsAttr.startsWith("!") ) {
					// We will have to extend the fields in realtime
					
					extendsExpression = getExpression(containers, "extends");
				} else {
					_fieldsMap = new HashMap<String, ListField>();
					if( extendsAttr != null && !extendsAttr.isEmpty() ) {
						List<ObjectField> fields = application.getAllObjectFields(extendsAttr, ObjectField.T_INDEX);
						if( fields != null && fields.size() > 0 ) {
							for( ObjectField field : fields ) {
								ListField listField = new ListField(this, field);
								_fieldsMap.put(listField.getId(), listField);
							}
						} else
							Logger.debug("Could not extend List fields from object '" + extendsAttr + "', no INDEX fields found.");
					}
					for( Element child : getChildrenElements(containers) ) {
						ListField listField = new ListField(this, child);
						_fieldsMap.put(listField.getId(), listField);
					}
				}
			}
		}
		super.parseElement(element, application);
	}
	
	protected HashMap<String, ListField> getFieldsMap(Context context) throws ExpressionException {
		if( extendsExpression != null ) {
			String extendsAttr = extendsExpression.evaluate(context.getVariables(), context.getDocumentNode());
			List<ObjectField> fields = context.getApplication().getAllObjectFields(extendsAttr, ObjectField.T_INDEX);
			HashMap<String, ListField> fieldsMap = new HashMap<String, ListField>();
			if( fields != null && fields.size() > 0 ) {
				for( ObjectField field : fields ) {
					ListField listField = new ListField(this, field);
					fieldsMap.put(listField.getId(), listField);
				}
			} else
				Logger.debug("Could not extend List fields from object '" + extendsAttr + "', no INDEX fields found.");
			return fieldsMap;
		} else
			return _fieldsMap;
	}

	public List<Record> getRecords(Context context, int pageStart,
			int pageLimit, String sortBy, String groupBy, String filterBy)
			throws CodeGlideException {

		DynamicElement displaySettings = (DynamicElement)displaySettingsExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);

		// Set group by
		DynamicElement groupNode = (DynamicElement)displaySettings.getChildNode("Group");
		if( groupBy != null && groupNode != null ) {
			groupNode.setAttribute("Enabled", "1");
			groupNode.setAttribute("c1", context.getApplication().getString(groupBy) );
		}
		
		// Set sort by
		DynamicElement sortNode = (DynamicElement)displaySettings.getChildNode("Sort");
		if( sortBy != null && sortNode != null ) {
			boolean isDesc = false;
			if( sortBy.startsWith("@") ) {
				isDesc = true;
				sortBy = sortBy.substring(1);
			}
			sortNode.setAttribute("c1", context.getApplication().getString(sortBy));
			sortNode.setAttribute("c1d", (isDesc)?"0":"1");
		}
		
		// Build sort list
		StringBuffer sortList = new StringBuffer();
		if( groupNode != null && groupNode.getAttribute("Enabled").equals("1") ) {
			if( groupNode.getAttribute("c1d").equals("1") )
				sortList.append("@");
			sortList.append(groupNode.getAttribute("c1"));
		}
		if( sortNode != null ) {
			for( int i = 1; i <=4; i++ ) {
				String sortField = sortNode.getAttribute("c"+i);
				if( sortField != null && !sortField.isEmpty() ) {
					if( sortList.length() > 0 )
						sortList.append(",");
					if( sortNode.getAttribute("c"+i+"d").equals("1") )
						sortList.append("@");
					sortList.append(sortField);
				}
			}
		}
		sortBy = (sortList.length()>0) ? sortList.toString() : null;

		// Add search variables
		VariableHolder searchVariables = ((DbRootNode)context.getRootNode().getChildNode("Db")).setSearchVariables(sortBy, filterBy, pageStart, pageLimit);
		context.getVariables().addVariables("__$Search"+getSequenceId(), searchVariables);
		
		// Records list
		List<Record> records = new LinkedList<Record>();
		
		try {
			// Obtain results
			NodeList results = (NodeList)bindExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
			
			// Remove old elements
			removeAllElements((ContextUi)context, this);
			
			// Get fields
			HashMap<String, ListField> fieldsMap = getFieldsMap(context);
			
			for( int i = 0; i < results.getLength(); i++ ) {
				records.add(createRow(context, displaySettings, fieldsMap, (DynamicElement)results.item(i)));
			}
		} finally {
			context.getVariables().removeVariables("__$Search"+getSequenceId());
		}
		
		return records;
	}
	
	protected Record createRow(Context context, DynamicElement displaySettings, HashMap<String, ListField> fieldsMap, DynamicElement item) throws CodeGlideException {
		// Add node id
		Record row = new Record();
		row.addField("id", generateElementId((ContextUi)context, this, item));
		
		try {
			// Evaluate and add fields
			String fields[] = ((DynamicElement)displaySettings.getChildNode("Fields")).getAttribute("Include").split(",");
			for( int i = 0; i < fields.length; i++ ) {
				String fieldId = context.getApplication().getEncodedId(fields[i]), fieldValue = null;
				ListField listField = fieldsMap.get(fields[i]);
				try {
					try {
						DynamicAttr attr = (DynamicAttr)listField.getBind().evaluate(context.getVariables(), item, Expression.NODE); 
						if( attr.getFieldDefinition() != null && attr.getFieldDefinition().getFormat() == ObjectField.F_ENUM && listField.isType(ListField.T_IMAGEMAP))
							fieldValue = attr.getValue();
						else
							fieldValue = attr.getExpandedValue();
					} catch (Exception _) {
						fieldValue = listField.getBind().evaluate(context.getVariables(), item);
					}
					switch( listField.getFormat() ) {
						case ListField.F_BOOLEAN:
							row.addField( fieldId, (fieldValue != null && (fieldValue.equals("1"))) );
							break;
						case ListField.F_DOUBLE:
							row.addField( fieldId, Float.parseFloat(fieldValue));
							break;
						case ListField.F_INTEGER:
							row.addField( fieldId, Integer.parseInt(fieldValue));
							break;
						//case ListField.DATE:
						//	type = "date";
						//	break;
						default:
							row.addField( fieldId, fieldValue);
							break;
					}
				} catch (Exception _) {
				}
			}
		} catch (Exception _) {
		}
		return row;
	}

	public List<Record> getRecords(Context context) throws CodeGlideException {
		return getRecords(context, 0, 0, null, null, null);
	}
	
	protected List<Record> addParameters(ContextUi context, Action action, HashMap<String, ListField> fieldsMap, DynamicElement displaySettings ) throws ExpressionException {
		action.addParameter(ActionParameter.ID, generateWidgetId(context, this));
		
		try {
			if( Integer.parseInt(displaySettings.getAttribute("pageSize")) > 0 )
				action.addParameter(ActionParameter.PAGESIZE, displaySettings.getAttribute("pageSize"));
		} catch (Exception _) {
		}
		
		// Set onClick handler, if any.
		if( onClickFunction != null )
			action.addFlag(ActionFlag.CLICKABLE);
		if( onDoubleClickFunction != null )
			action.addFlag(ActionFlag.DOUBLECLICKABLE);
		
		// Obtain fields
		List<Record> records = new LinkedList<Record>();
		String[] fields = null;
		try {
			fields = ((DynamicElement)displaySettings.getChildNode("Fields")).getAttribute("Include").split(",");
			for( int i = 0; i < fields.length; i++ ) {
				String fieldId = fields[i];
				ListField listField = fieldsMap.get(fieldId);
				if( listField == null )
					continue;
				Record field = new Record();
				field.addField("id", context.getApplication().getEncodedId(fieldId));
				
				// Set format
				String type = null;
				switch( listField.getFormat() ) {
					case ListField.F_BOOLEAN:
						type = "bool";
						break;
					case ListField.F_DOUBLE:
						type = "float";
						break;
					case ListField.F_INTEGER:
						type = "int";
						break;
					case ListField.F_DATE:
						type = "date";
						break;
					default:
						type = "string";
						break;
				}
				field.addField("type", type);
				
				records.add(field);
			}
		} catch (Exception _) {
		}
		
		action.addRecords(records);
		
		try {
			// Set sorting order
			DynamicElement sort = (DynamicElement)((DynamicElement)displaySettings.getChildNode("Sort"));
			action.addParameter(ActionParameter.SORTBY, context.getApplication().getEncodedId(sort.getAttribute("c1")));
			if( sort.getAttribute("c1d").equals("1") )
				action.addFlag(ActionFlag.SORTDESC);
		} catch (Exception _) {
		}
		
		try {
			// Include drag and drop settings
			if( ddGroupExpression != null )
				action.addParameter(ActionParameter.DDGROUP, ddGroupExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
		} catch (Exception _) {
		}

		return records;
	}

	public void handleClick(ContextUi context, List<Action> result, Object input)
			throws CodeGlideException {
		if( onClickFunction != null )
			onClickFunction.run(context, result, input);
	}

	public void handleDoubleClick(ContextUi context, List<Action> result, Object input)
			throws CodeGlideException {
		if( onDoubleClickFunction != null )
			onDoubleClickFunction.run(context, result, input);
	}
	
	protected class ListField extends ObjectField {
		public static final int T_IMAGEMAP = 0x008;
		public static final short E_IMAGEMAP = 4;

		private Expression headerIcon;
		private boolean editable, autosave;
		private Widget editorWidget;
		
		public ListField(Item parent, Element element) {
			super(parent, element);
		}
		
		public ListField(Item parent, ObjectField extendField ) {
			super(parent);
			copyFields(extendField);
		}

		public Widget getEditorWidget() {
			return editorWidget;
		}

		public void setEditorWidget(Widget editorWidget) {
			this.editorWidget = editorWidget;
		}

		public boolean isEditable() {
			return editable;
		}

		public void setEditable(boolean editable) {
			this.editable = editable;
		}
		
		public Expression getHeaderIcon() {
			return headerIcon;
		}

		public void setHeaderIcon(Expression headerIcon) {
			this.headerIcon = headerIcon;
		}

		public boolean isAutosave() {
			return autosave;
		}

		public void setAutosave(boolean autosave) {
			this.autosave = autosave;
		}
		
		protected void copyFields( ObjectField extendField ) {
			setId(extendField.getId());
			setLocalId(extendField.getLocalId());
			setBind(extendField.getBind());
			setName(extendField.getName());
			setDefaultValue(extendField.getDefaultValue());
			setFormat(extendField.getFormat());
			setType(extendField.getType());
			if( getFormat() == ObjectField.F_ENUM && extendField.getSetting(ObjectField.E_LIST) != null )
				addSetting(ListField.E_LIST, extendField.getSetting(ObjectField.E_LIST));
		}
		
		protected void parseElement(Element element, Application application) {
			// Check if it extends an existing Object
			if( !element.getAttribute("extends").isEmpty() ) {
				String[] p = element.getAttribute("extends").split("\\/");
				ObjectDefinition objDef = application.getObject(p[0]);
				for( int i = 1; i < p.length - 1; i++ )
					objDef = objDef.getObject(p[i]);
				ObjectField extendField = objDef.getField(p[p.length-1]);
				copyFields(extendField);
			}

			super.parseElement(element, application);
			
			// Look for imageMap type
			String typeString = element.getAttribute("type");
			if( typeString != null && !typeString.isEmpty() ) {
				String[] typeArray = element.getAttribute("type").split(",");
				for( int i = 0; i < typeArray.length; i++ ) {
					if( typeArray[i].equals("imagemap") )
						setType(ListField.T_IMAGEMAP);
				}
			}

			// Add name
			if( !element.getAttribute("name").isEmpty() )
				setName(new Expression(element.getAttribute("name")));

			// The field may have these attributes or not. 
			String headerIcon = element.getAttribute("headericon");
			String editable = element.getAttribute("editable");

			if(headerIcon != null && !headerIcon.isEmpty() )
				setHeaderIcon(new Expression(headerIcon));

			if(editable != null && editable.equalsIgnoreCase("true")) {
				setEditable(true);
				setEditorWidget((Widget)Item.createItem(this, getChildrenElements(element).get(0)));
			} else
				setEditable(false);					

			if(element.getAttribute("autosave") != null && element.getAttribute("autosave").equalsIgnoreCase("true"))
				setAutosave(true);
			
			if( isType(ListField.T_IMAGEMAP) ) {
				HashMap<String, Expression> enumList = new HashMap<String, Expression>();
				for( Element enumElement : getChildrenElements(element) )
					enumList.put(enumElement.getAttribute("id"), new Expression(enumElement.getAttribute("value")));
				if( enumList.size() > 0 )
					addSetting(ListField.E_IMAGEMAP, enumList);
			}
			
			if( getLocalId() == null )
				setLocalId(getId());
			
			// Build bind variable if missing
			if( getBind() == null ) {
				int slashIdx = getId().lastIndexOf('/');
				String expression = null;
				if( slashIdx > -1 )
					expression = "!" + getId().substring(0, slashIdx) + "@" + getId().substring(slashIdx+1);
				else
					expression = "!@" + getId();
				setBind(new Expression(expression));
			}
		}
	}

	
}

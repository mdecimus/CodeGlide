package com.codeglide.core.rte.widgets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.ClickHandler;
import com.codeglide.core.rte.interfaces.DropHandler;
import com.codeglide.core.rte.interfaces.GetTreeHandler;
import com.codeglide.core.rte.interfaces.RenameItemHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.ExtendedElement;

/*
 * 
 * Flags
 * 
 * EDITABLE
 * 
 */

public class TreePanel extends AbstractPanel implements GetTreeHandler, ClickHandler, RenameItemHandler, DropHandler {
	private HashMap<String, Field> fieldsMap;
	private Expression ddGroupExpression, bindExpression, filterExpression;
	private Function onClickFunction, onDoubleClickFunction, onDropFunction;
	
	public TreePanel(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		fieldsMap = new HashMap<String, Field>();
		ddGroupExpression = getExpression(element, "ddgroup");
		bindExpression = getExpression(element, "bind");
		filterExpression = getExpression(element, "filter");
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("onclick") )
							onClickFunction = new Function(this, function);
						else if( function.getAttribute("name").equalsIgnoreCase("ondoubleclick") )
							onDoubleClickFunction = new Function(this, function);
						else if( function.getAttribute("name").equalsIgnoreCase("ondrop") )
							onDropFunction = new Function(this, function);
					}
				}
			} else if( containers.getNodeName().equalsIgnoreCase("fields") ) {
				for( Element child : getChildrenElements(containers) ) {
					Field field = new Field(this, child);
					fieldsMap.put(field.getId(), field);
				}
			} else if( containers.getNodeName().equalsIgnoreCase("menu") ) {
				addChild(new Menu(this, containers));
			}
		}
	}

	protected void render(ContextUi context, List<Action> result)
		throws CodeGlideException {
		
		Action action = new Action(ActionType.TREE);
		action.addParameter(ActionParameter.ID, generateWidgetId(context, this));
		setRenderParameters(context, action);
		
		// Include drag and drop settings
		if( ddGroupExpression != null )
			action.addParameter(ActionParameter.DDGROUP, ddGroupExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));

		// Set onClick handler, if any.
		if( onClickFunction != null )
			action.addFlag(ActionFlag.CLICKABLE);
		if( onDoubleClickFunction != null )
			action.addFlag(ActionFlag.DOUBLECLICKABLE);
		
		// Create CSS sheets
		Field format = fieldsMap.get("cls");
		if( format != null && format.getFormat() == Field.F_MAP) {
			int c = 0;
			String cssName = "cg-tree-" + Long.toString(getSequenceId(), 36);
			for( Field.Map map : format.getMappings() ) {
				if( !context.getWindowManager().hasStyleSheet(cssName+c) )
					context.getWindowManager().addStyleSheet(cssName+c, map.getTo().evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
				c++;
			}
		}
				
		result.add(action);
		List<Action> children = action.addChildren();
		super.render(context, children);
	}
	
	public void handleClick(ContextUi context, List<Action> result, Object input)
			throws CodeGlideException {
		if (onClickFunction != null)
			onClickFunction.run(context, result, input);
	}

	public void handleDoubleClick(ContextUi context, List<Action> result,
			Object input) throws CodeGlideException {
		if (onDoubleClickFunction != null)
			onDoubleClickFunction.run(context, result, input);
	}
	
	public void handleDrop(ContextUi context, List<Action> result,
			Node targetNode, NodeList sourceNodes) throws CodeGlideException {
		if (onDropFunction != null) {
			Vector<Object> input = new Vector<Object>(2);
			input.setSize(2);
			input.set(0, targetNode);
			input.set(1, sourceNodes);
			onDropFunction.run(context, result, input);
		}
		
	}
	public List<Record> getChildren(ContextUi context, String parentId)
			throws CodeGlideException {
		return getChildren(context, getElementById(context, parentId).getChildNodes());
	}

	public List<Record> getRootChildren(ContextUi context)
			throws CodeGlideException {
		removeAllElements(context, this);
		return getChildren(context, (NodeList)bindExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST));
	}
	
	protected List<Record> getChildren(ContextUi context, NodeList list ) throws CodeGlideException {
		List<Record> results = new LinkedList<Record>();
		
		// Build node array
		if( list != null && list.getLength() > 0 ) {
			for( int i = 0; i < list.getLength(); i++ ) {
				DynamicElement node = (DynamicElement)list.item(i);
				if( filterExpression != null && !(Boolean)filterExpression.evaluate(context.getVariables(),node, Expression.BOOLEAN) )
					continue;
				Record record = new Record();
				record.addField("id", generateElementId(context, this, node));
				for( Field field : fieldsMap.values() ) {
					if( field.getId().startsWith("_") )
						continue;
					if( field.getId().equals("cls") ) {
						String mapFrom = null;
						if( field.getBind() != null )
							mapFrom = field.getBind().evaluate(context.getVariables(),node);
						int c = 0;
						for( Field.Map map : field.getMappings() ) {
							boolean didMatch = false;
							if( map.isBooleanExpression() )
								didMatch = (Boolean)map.getFrom().evaluate(context.getVariables(),node, Expression.BOOLEAN);
							else if( mapFrom != null )
								didMatch = mapFrom.equals(map.getFrom().evaluate(context.getVariables(),node));
							if( didMatch ) {
								record.addField(field.getId(), "cg-tree-" + Long.toString(getSequenceId(), 36) + c);
								break;
							}
							c++;
						}
						//if( value == null )
						//	value = new JsonValue("cg-tree-" + Long.toString(((VisibleWidget)widget).getSequenceId(), 36) + (field.getMappings().size()-1) );
						
					} else {
						switch( field.getFormat() ) {
						case Field.F_BOOLEAN:
							record.addField(field.getId(), (Boolean)field.getBind().evaluate(context.getVariables(),node, Expression.BOOLEAN) );
							break;
						case Field.F_MAP:
						{
							String mapFrom = null;
							if( field.getBind() != null )
								mapFrom = field.getBind().evaluate(context.getVariables(),node);
							boolean didAdd = false;
							for( Field.Map map : field.getMappings() ) {
								boolean didMatch = false;
								if( map.isBooleanExpression() )
									didMatch = (Boolean)map.getFrom().evaluate(context.getVariables(),node, Expression.BOOLEAN);
								else if( mapFrom != null )
									didMatch = mapFrom.equals(map.getFrom().evaluate(context.getVariables(),node));
								if( didMatch ) {
									didAdd = true;
									record.addField(field.getId(), map.getTo().evaluate(context.getVariables(),node));
									break;
								}
							}
							if( !didAdd )
								record.addField(field.getId(), field.getMappings().get(field.getMappings().size()-1).getTo().evaluate(context.getVariables(),node));
						}
							break;
						default:
							record.addField(field.getId(),  field.getBind().evaluate(context.getVariables(),node) );
							break;
						}
					}
				}
				results.add(record);
			}
		}
		return results;
	}
	
	public void handleRenameItem( ContextUi context, String itemId, String itemName ) throws CodeGlideException {
		// Get node and set value
		DynamicElement node = getElementById(context, itemId);
		((DynamicAttr)fieldsMap.get("_name").getBind().evaluate(context.getVariables(),node, Expression.NODE)).setValue(itemName);
		if( node instanceof ExtendedElement )
			((ExtendedElement)node).update();
	}

	protected class Field extends Item {
		public static final short F_STRING = 0;
		public static final short F_INTEGER = 1;
		public static final short F_DOUBLE = 2;
		public static final short F_DATE = 3;
		public static final short F_BOOLEAN = 4;
		public static final short F_MAP = 5;

		private String id;
		private Expression bind;
		private short format;

		private java.util.List<Map> mappings;

		public Field(Item parent, Element element) {
			super(parent, element);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
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

		public void addMap( Expression from, Expression to, boolean isBoolean ) {
			if( mappings == null )
				mappings = new LinkedList<Map>();
			mappings.add(new Map(from, to, isBoolean));
		}
		
		public List<Map> getMappings() {
			return mappings;
		}
		
		public void setMappings(List<Map> mappings ) {
			this.mappings = mappings;
		}
		

		protected void parseElement(Element element, Application application) {
			// Set Id
			setId(element.getAttribute("id"));
			
			// Set bind variable
			if( !element.getAttribute("bind").isEmpty() )
				setBind(new Expression(element.getAttribute("bind")));

			// Set type
			String type = element.getAttribute("format");
			if(type.equalsIgnoreCase("string"))
				setFormat(F_STRING);						
			else if(type.equalsIgnoreCase("integer"))
				setFormat(F_INTEGER);						
			else if(type.equalsIgnoreCase("double"))
				setFormat(F_DOUBLE);						
			else if(type.equalsIgnoreCase("date"))
				setFormat(F_DATE);
			else if(type.equalsIgnoreCase("boolean"))
				setFormat(F_BOOLEAN);						
			else if(type.equalsIgnoreCase("map"))
				setFormat(F_MAP);						
			else if( !type.isEmpty() ){
				Logger.warn("Unknown format '" + type + "' for field '" + getId() + "'.");
				setFormat(F_STRING);
			}

			// Set maps
			if( getFormat() == F_MAP ) {
				for( Element mapElement : getChildrenElements(element) ){
					Expression expression = null;
					
					boolean isBooleanExpression = !mapElement.getAttribute("test").isEmpty();
					if( isBooleanExpression )
						expression = new Expression(mapElement.getAttribute("test"));
					else
						expression = new Expression(mapElement.getAttribute("from"));
					addMap( expression, new Expression(mapElement.getAttribute("to")), isBooleanExpression);							
				}
			}			
		}

		public class Map {
			private Expression from = null, to = null;
			private boolean booleanExpression;

			public Map(Expression from, Expression to, boolean booleanExpression) {
				this.from = from;
				this.to = to;
				this.booleanExpression = booleanExpression;
			}

			public boolean isBooleanExpression() {
				return booleanExpression;
			}
			
			public Expression getFrom() {
				return from;
			}

			public void setFrom(Expression from) {
				this.from = from;
			}

			public Expression getTo() {
				return to;
			}

			public void setTo(Expression to) {
				this.to = to;
			}

		}

	}

}


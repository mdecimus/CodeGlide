package com.codeglide.core.rte.widgets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.interfaces.UpdateRecordHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

public class GridPanel extends ListPanel implements UpdateRecordHandler {
	//private Expression addNodeExpression, addNodeNameExpression;
	private Function onItemCreateFunction, onItemAddFunction;
	
	public GridPanel(Item parent, Element element) {
		super(parent, element);
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		for( Element containers : getChildrenElements(element) ) {
			if( containers.getNodeName().equalsIgnoreCase("functions") ) {
				for( Element function : getChildrenElements(containers) ) {
					if( function.getNodeName().equals("function") ) {
						if( function.getAttribute("name").equalsIgnoreCase("onitemcreate") )
							onItemCreateFunction = new Function(this, function);
						else if( function.getAttribute("name").equalsIgnoreCase("onitemadd") )
							onItemAddFunction = new Function(this, function);
					}
				}
			}
		}
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		
		DynamicElement displaySettings = (DynamicElement)displaySettingsExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
		Action action = new Action(ActionType.GRID);
		setRenderParameters(context, action);
		HashMap<String, ListField> fieldsMap = getFieldsMap(context);
		List<Record> fields = addParameters(context, action, fieldsMap, displaySettings);

		// Set grouping for Editor and EditorGrid
		DynamicElement group = (DynamicElement)displaySettings.getChildNode("Group");
		if( group != null && group.getAttribute("Enabled").equals("1") ) {
			action.addParameter(ActionParameter.GROUPBY, context.getApplication().getEncodedId(group.getAttribute("c1")));
			if( group.getAttribute("Collapsed").equals("1") )
				action.addFlag(ActionFlag.GROUPCOLLAPSED);
			if( group.getAttribute("c1s").equals("0") )
				action.addFlag(ActionFlag.GROUPHIDDEN);
		}

		for( Record field : fields ) {
			String fieldId = context.getApplication().getString(Integer.parseInt(field.getStringField("id"),36));
			ListField listField = fieldsMap.get(fieldId);
			
			if( listField == null )
				continue;

			if( listField.getHeaderIcon() != null )
				field.addField("icon", listField.getHeaderIcon().evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
			
			// Add editable widget
			if( listField.isEditable() ) {
				if( listField.getDefaultValue() != null )
					field.addField("default", listField.getDefaultValue().evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
				if( listField.getEditorWidget() != null ) 
					field.addField("editor", listField.getEditorWidget().getSequenceId());
				if( listField.isAutosave() )
					field.addField("autosave", true);
			}

			boolean isImageMap = false;
			
			if( listField.isType(ListField.T_IMAGEMAP) ) {
				HashMap<String, Expression> imageMap = (HashMap<String, Expression>) listField.getSetting(ListField.E_IMAGEMAP);
				if( imageMap != null ) {
					List<Record> imageList = new LinkedList<Record>();
					for( String id : imageMap.keySet() ) {
						Record mapping = new Record();
						mapping.addField("_img", imageMap.get(id).evaluate(context.getVariables(),context.getRootNode().getDocumentNode()));
						mapping.addField("_val", id);
						imageList.add(mapping);
					}
					field.addField("imagemap", imageList);
					field.addField("filter", "imagemap");
					isImageMap = true;
				}
			}

			// Configure display format
			String columnName = null;
			DynamicElement formatNode = (DynamicElement)displaySettings.getChildNode("Format");
			if( formatNode != null && formatNode.getChildren() != null ) {
				for( Node child : formatNode.getChildren() ) {
					DynamicElement format = (DynamicElement)child;
					if( !format.getAttribute("Id").equals(fieldId) )
						continue;
					int width = 0;
					try {
						width = Integer.parseInt(format.getAttribute("Width"));
					} catch (Exception _) {}
					
					columnName = format.getAttribute("Name");
					field.addField("width", String.valueOf((width < 1) ? 20 : width));
					if( !isImageMap ) {
						field.addField("filter", format.getAttribute("Filter"));
						field.addField("filterparam", format.getAttribute("filterParam"));
					}
					field.addField("summary", format.getAttribute("Summary"));
					break;
				}
			}
			
			if( (columnName == null || columnName.isEmpty()) && listField.getName() != null )
				columnName = listField.getName().evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
			
			field.addField("name", columnName );
			
			// Generate CSS
			DynamicElement formatRules = ((DynamicElement)displaySettings.getChildNode("formatRules"));
			if( formatRules != null && formatRules.getChildren() != null ) {
				for( Node child : formatRules.getChildren() ) {
					DynamicElement format = (DynamicElement)child;
					if( format.getAttribute("Enabled").equals("0") )
						continue;
					String cssName = getCssName(format);
					if( !context.getWindowManager().hasStyleSheet(cssName) )
						context.getWindowManager().addStyleSheet(cssName, getCss(format));
				}
			}

		}
		
		result.add(action);
		List<Action> children = action.addChildren();
		super.render(context, children);
	}
	
	protected Record createRow(Context context, DynamicElement displaySettings, HashMap<String, ListField> fieldsMap, DynamicElement item) throws CodeGlideException {
		Record row = super.createRow(context, displaySettings, fieldsMap, item);
		
		// Add format rules
		DynamicElement formatRules = ((DynamicElement)displaySettings.getChildNode("formatRules"));
		if( formatRules != null && formatRules.getChildren() != null ) {
			for( Node node : formatRules.getChildren() ) {
				if( ((DynamicElement)node).getAttribute("Enabled").equals("0") )
					continue;
				try {
					if( (Boolean)context.getRootNode().getExpression(((DynamicElement)((DynamicElement)node).getFirstChild()).getAttribute("Expression")).evaluate(context.getVariables(),item, Expression.BOOLEAN) )
						row.addField("__style", getCssName(item));
				} catch (ExpressionException _) {
				}
			}
		}
		
		return row;
	}
	
	public boolean addRecord(Context context, Record record) throws CodeGlideException {
		HashMap<String, ListField> fieldsMap = getFieldsMap(context);
		
		Object result = onItemCreateFunction.run(context, new LinkedList<Action>(), null);
		DynamicElement node = null;
		if( result instanceof DynamicElement )
			node = (DynamicElement)result;
		else if( result instanceof NodeList )
			node = (DynamicElement)((NodeList)result).item(0);
			
		for( String fieldName : record.getFieldNames() ) 
			((DynamicAttr)fieldsMap.get(fieldName).getBind().evaluate(context.getVariables(), node, Expression.NODE)).setValue(record.getStringField(fieldName));
		
		Vector<Object> inputArgs = new Vector<Object>();
		inputArgs.add(node);
		onItemAddFunction.run(context, new LinkedList<Action>(), inputArgs);
		return true;
	}

	public boolean updateRecord(Context context, String id, Record record) throws CodeGlideException {
		HashMap<String, ListField> fieldsMap = getFieldsMap(context);
		DynamicElement node = getElementById((ContextUi)context, id);
		if( node == null )
			return false;
		for( String fieldName : record.getFieldNames() ) 
			((DynamicAttr)fieldsMap.get(fieldName).getBind().evaluate(context.getVariables(), node, Expression.NODE)).setValue(record.getStringField(fieldName));
		return true;
	}

	private String getCssName(DynamicElement format) {
		StringBuffer name = new StringBuffer();
		name.append("cg-gr-");
		if( format.getAttribute("Face") != null )
			name.append(format.getAttribute("Face").replace(' ', '-').toLowerCase());
		if( format.getAttribute("Color") != null )
			name.append(format.getAttribute("Color").toLowerCase());
		if( format.getAttribute("Size") != null )
			name.append(format.getAttribute("Size").toLowerCase());
		name.append(format.getAttribute("Bold"));
		name.append(format.getAttribute("Italic"));
		name.append(format.getAttribute("underLine"));
		name.append(format.getAttribute("strikeOver"));
		return name.toString();
	}

	private String getCss(DynamicElement format) {
		StringBuffer result = new StringBuffer();
		String fontFace = format.getAttribute("Face"), fontSize = format.getAttribute("Size"), 
		       fontColor = format.getAttribute("Color");
		if( (fontFace != null && !fontFace.isEmpty()) || (fontSize != null && !fontSize.isEmpty())) {
			result.append("font:");
			if( fontSize != null ) {
				result.append(fontSize).append("px");
				if( fontFace != null )
					result.append(" ");
			}
			if( fontFace != null )
				result.append(fontFace);
		}
		if( fontColor != null && !fontColor.isEmpty() ) {
			if(result.length() > 0)
				result.append("; ");
			result.append("color:").append(fontColor);
		}
		boolean isBold = format.getAttribute("Bold").equals("1"), isItalic = format.getAttribute("Italic").equals("1"),
				isUnderline = format.getAttribute("underLine").equals("1"), isStrikethru = format.getAttribute("strikeOver").equals("1");

		if( isUnderline || isStrikethru ) {
			if(result.length() > 0)
				result.append("; ");
			result.append("text-decoration:");
			if(isUnderline) 
				result.append("underline");
			if(isStrikethru) {
				if(isUnderline)
					result.append(" ");
				result.append("line");
			}
		}
		
		if( isBold ) {
			if(result.length() > 0)
				result.append("; ");
			result.append("font-weight:bold");
		}
		
		if( isItalic ) {
			if(result.length() > 0)
				result.append("; ");
			result.append("font-style:italic");
		}
		
		return result.toString();
	}
	

}

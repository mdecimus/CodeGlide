package com.codeglide.core.rte.widgets;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.interfaces.xmldb.DbRootNode;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrLink;

public class SelectLink extends Selectremote {

	public SelectLink(Item parent, Element element) {
		super(parent, element);
	}


	protected void render(ContextUi context, List<Action> result)
	throws CodeGlideException {
		Action action = new Action(ActionType.SELECTREMOTE);
		String value = null;
		Object[] data = addFieldProperties(context, action);

		// Obtain value from DynamicAttr
		if (data[0] != null) {
			value = ((DynamicAttr) data[0]).getValue();
			
			// Base36 encode the ID
			if( value != null && !value.isEmpty() )
				value = Long.toString(Long.parseLong(value), 36);
		}
		
		if( value != null )
			action.addParameter(ActionParameter.VALUE, value);
		if( pageSizeExpression != null )
			action.addParameter(ActionParameter.PAGESIZE, pageSizeExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
		action.addFlag(ActionFlag.EDITABLE);
		
		List<Record> records = new LinkedList<Record>();
		
		// Add ID and Value
		Record record = new Record();
		record.addField("id", "id");
		records.add(record);

		record = new Record();
		record.addField("id", "value");
		records.add(record);

		// Add extra fields
		if ( fieldsMap != null) {
			for( String fieldId : fieldsMap.keySet() ) {
				record = new Record();
				record.addField("id", fieldId);
				records.add(record);
			}
		}
		
		action.addRecords(records);
		result.add(action);
	}
	
	public void handleSet(ContextUi context, List<Action> result, String value ) {
		Node setNode = null;
		
		if( bindExpression != null ) {
			try {
				setNode = (Node)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
			} catch (Exception _) {}
		}
		
		if( setNode != null ) {
			if( setNode instanceof DynamicAttrLink ) {
				long setValue = -1;
				if( value != null && !value.isEmpty() )
					setValue = Long.parseLong(value, 36);
				((DynamicAttrLink)setNode).setValue(setValue);
			} else
				Logger.debug("Field bound to node '" + setNode.getNodeName() + "' which is not a Link attribute.");
		} else
			Logger.debug("Could not resolve a node to set values to for field " + this.getClass().getName() + ".");

	}

	public List<Record> getRecords(Context context, int pageStart,
			int pageLimit, String sortBy, String groupBy, String filterBy)
			throws CodeGlideException {

		// Handle get request
		List<Record> records = new LinkedList<Record>();

		ObjectField extendField = null;
		DynamicAttrLink attr = null;
		
		// Obtain DynamicAttr
		try {
			attr = (DynamicAttrLink)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
			extendField = attr.getFieldDefinition();
		} catch (Exception _) {
			Logger.debug("SelectLink can only be used on Link fields.");
			return records;
		}

		
		// Process options bind data
		if( optionsBindExpression != null ) {

			VariableHolder searchVariables = ((DbRootNode)context.getRootNode().getChildNode("Db")).setSearchVariables(sortBy, filterBy, pageStart, pageLimit);
			
			context.getVariables().addVariables("__$Search"+getSequenceId(), searchVariables);
			
			try {
				// Obtain results
				NodeList results = (NodeList)optionsBindExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
				
				// Set selected node
				DbNode linkedNode = attr.getLinkedNode();

				if( linkedNode != null ) {
					LinkedList<Node> items = new LinkedList<Node>();
					items.add(linkedNode);
					for( int i = 0 ; i < results.getLength(); i++ ) {
						DbNode item = (DbNode)results.item(i);
						if( item.getId() != linkedNode.getId() )
							items.add(item);
					}
					results = new DummyNodeList(items);
				}
				
				for( int i = 0; i < results.getLength(); i++ ) {
					DbNode item = (DbNode)results.item(i);

					try {
						// Evaluate and add fields
						Record record = new Record();
						record.addField("id", Long.toString(item.getId(), 36));
						record.addField("value", ((ObjectField)extendField.getSetting(ObjectField.E_LINK_FIELD)).getBind().evaluate(null, item) );

						for( String rowId : fieldsMap.keySet() ) {
							String rowValue = null;
							try {
								rowValue = ((DynamicAttr)fieldsMap.get(rowId).evaluate(context.getVariables(),item, Expression.NODE)).getExpandedValue();
							} catch (Exception _) {
								rowValue = fieldsMap.get(rowId).evaluate(context.getVariables(),item);
							}
							record.addField(rowId, rowValue);
						}
						records.add(record);
					} catch (Exception _) {
					}
				}
			} finally {
				context.getVariables().removeVariables("__$Search"+getSequenceId());
			}
		}

		return records;
	}

}

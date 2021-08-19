package com.codeglide.core.rte.widgets;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.GetRecordHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.interfaces.xmldb.DbRootNode;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;

/*
 * 
 * 
 * Flags
 * 
 * TEMPLATE
 * 
 */

public class Selectremote extends Itemlist implements GetRecordHandler {
	protected Expression pageSizeExpression;
	
	public Selectremote(Item parent, Element element) {
		super(parent, element);
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		pageSizeExpression = getExpression(element, "pageSize");
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.SELECTREMOTE);
		String value = null;
		{
			Object[] data = addFieldProperties(context, action);

			// Obtain value from DynamicAttr
			if (data[0] != null)
				value = ((DynamicAttr) data[0]).getValue();

			// If value is null, use the default
			if (value == null || value.isEmpty())
				value = (String) data[2];
		}
		
		if( value != null )
			action.addParameter(ActionParameter.VALUE, value);
		
		if( pageSizeExpression != null )
			action.addParameter(ActionParameter.PAGESIZE, pageSizeExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));

		List<Record> records = new LinkedList<Record>();
		if ( fieldsMap != null) {
			for( String fieldId : fieldsMap.keySet() ) {
				Record record = new Record();
				record.addField("id", fieldId);
				records.add(record);
			}
			if(fieldsMap.keySet().size() > 2 )
				action.addFlag(ActionFlag.TEMPLATE);
		} else {
			Record record = new Record();
			record.addField("id", "id");
			records.add(record);

			record = new Record();
			record.addField("id", "value");
			records.add(record);
		}

		action.addRecords(records);
		result.add(action);
	}
	
	public List<Record> getRecords(Context context, int pageStart,
			int pageLimit, String sortBy, String groupBy, String filterBy)
			throws CodeGlideException {

		// Handle get request
		List<Record> records = new LinkedList<Record>();
		
		if( optionValues != null ) {
			for( Expression[] option : optionValues ) {
				Record record = new Record();
				record.addField("id", option[0].evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
				record.addField("value", option[1].evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
				records.add(record);
			}
		}
		
		// Process options bind data
		if( optionsBindExpression != null ) {

			VariableHolder searchVariables = ((DbRootNode)context.getRootNode().getChildNode("Db")).setSearchVariables(sortBy, filterBy, pageStart, pageLimit);
			
			context.getVariables().addVariables("__$Search"+getSequenceId(), searchVariables);
			
			try {
				// Obtain results
				NodeList results = (NodeList)optionsBindExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
				
				for( int i = 0; i < results.getLength(); i++ ) {
					DynamicElement item = (DynamicElement)results.item(i);

					// Evaluate and add fields
					Record record = new Record();
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
				}
			} finally {
				context.getVariables().removeVariables("__$Search"+getSequenceId());
			}
		}
		
		return records;
	}

	public List<Record> getRecords(Context context) throws CodeGlideException {
		return getRecords(context, 0, 0, null, null, null );
	}

}

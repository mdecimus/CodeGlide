package com.codeglide.core.rte.widgets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.CommandGroup;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.xml.dom.DynamicElement;

public abstract class Itemlist extends Field {
	protected List<Expression[]> optionValues;
	protected List<CompositeOption> compositeOptions;
	protected HashMap<String, Expression> fieldsMap;
	
	protected Expression optionsBindExpression;
	
	public Itemlist(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		optionsBindExpression = getExpression(element, "optionsBind");
		optionValues = null;
		compositeOptions = null;
		for( Element container : getChildrenElements(element) ) {
			if( container.getNodeName().equalsIgnoreCase("options") ) {
				for( Element child : getChildrenElements(container) ) {
					if( getChildrenElements(child).size() > 0 ) {
						if( compositeOptions == null )
							compositeOptions = new LinkedList<CompositeOption>();
						compositeOptions.add(new CompositeOption(this, child));
					} else {
						if( optionValues == null )
							optionValues = new LinkedList<Expression[]>();
						optionValues.add(new Expression[] {new Expression(child.getAttribute("value")), new Expression(child.getTextContent())});
					}
				}
			} else if( container.getNodeName().equalsIgnoreCase("fields") ) {
				fieldsMap = new HashMap<String, Expression>();
				for( Element child : getChildrenElements(container) ) {
					fieldsMap.put(child.getAttribute("id"), new Expression(child.getAttribute("bind")));
				}
			}
		}
	}
	
	protected List<Record> getRecords(ContextUi context, ObjectField extendField) throws ExpressionException {
		
		// Result
		List<Record> result = new LinkedList<Record>();
		
		try {
			// Add static options
			if( optionValues != null ) {
				for( Expression[] option : optionValues ) {
					Record record = new Record();
					record.addField("id", option[0].evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
					record.addField("value", option[1].evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
					result.add(record);
				}
			}

			// Add extends fields options, if any
			if( extendField != null && extendField.getFormat() == ObjectField.F_ENUM ) {
				HashMap<String, Expression> values = (HashMap<String, Expression>) extendField.getSetting(ObjectField.E_LIST);
				if( values != null ) {
					for( String optionValue : values.keySet() ) {
						Record record = new Record();
						record.addField("id", optionValue);
						record.addField("value", values.get(optionValue).evaluate(context.getVariables(), context.getRootNode().getDocumentNode()));
						result.add(record);
					}
				}
			}

			if( optionsBindExpression != null && fieldsMap != null ) {
				NodeList results = (NodeList)optionsBindExpression.evaluate(context.getVariables(),context.getRootNode().getDocumentNode(), Expression.NODELIST);
				if( results != null && results.getLength() > 0 ) {
					for( int i = 0; i < results.getLength(); i++ ) {
						DynamicElement item = (DynamicElement)results.item(i);
						Record record = new Record();
						record.addField("id", fieldsMap.get("id").evaluate(context.getVariables(),item));
						record.addField("value", fieldsMap.get("value").evaluate(context.getVariables(),item));
						result.add(record);
					}
				}
			}
		} catch (Exception e) {
			Logger.debug(e);
		}

		return result;
	}
	
	protected class CompositeOption extends CommandGroup {
		private Expression valueExpression;
		
		public CompositeOption(Item parent, Element element) {
			super(parent, element);
		}

		protected void parseElement(Element element, Application application) {
			super.parseElement(element, application);
			valueExpression = getExpression(element, "value");
		}

		public void run(Context context, List<Action> result, String selectedValue )
				throws CodeGlideException {
			Action action = new Action(ActionType.ITEM);
			result.add(action);
			String value = valueExpression.evaluate(context.getVariables(), context.getDocumentNode());
			action.addParameter(ActionParameter.VALUE, value);
			if( value != null && selectedValue != null && value.equals(selectedValue) )
				action.addFlag(ActionFlag.CHECKED);
			List<Action> children = action.addChildren();
			super.run(context, children);
		}
		
	}
	

}

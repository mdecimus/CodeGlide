package com.codeglide.core.rte.widgets;

import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;

public class Panel extends AbstractPanel {
	protected ActionType panelType;
	
	public Panel(Item parent, Element element) {
		super(parent, element);
	}

	protected void parseElement(Element element, Application application) {
		String panelName = element.getNodeName().toLowerCase();
		if( panelName.equals("form") )
			panelType = ActionType.FORM;
		else if( panelName.equals("tabpanel") )
			panelType = ActionType.TABPANEL;
		else if( panelName.equals("fieldset") )
			panelType = ActionType.FIELDSET;
		else if( panelName.equals("window") )
			panelType = ActionType.WINDOW;
		else if( panelName.equals("columnpanel") )
			panelType = ActionType.COLUMNPANEL;
		else if( panelName.equals("accordionpanel") )
			panelType = ActionType.ACCORDIONPANEL;
		else if( panelName.equals("regionpanel") )
			panelType = ActionType.REGIONPANEL;
		else 
			panelType = ActionType.PANEL;
		super.parseElement(element, application);
		
		// Special treatment for HTML panels
		/*if( panelType == ActionType.HTMLPANEL ) {
			for( Element container : getChildrenElements(element) ) {
				if( container.getNodeName().equalsIgnoreCase("contents") ) {
					String htmlValue = null;
					NodeList childsList = container.getChildNodes();

					// Iterate over the tags and parse each kind.
					for (int i = 0; i < childsList.getLength() && htmlValue == null; i++) {
						Node child = childsList.item(i);
						if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE)
							htmlValue = child.getNodeValue();
					}

					if( htmlValue != null ) {
						if( actionParameters == null )
							actionParameters = new HashMap<ActionParameter, Expression>();
						actionParameters.put(ActionParameter.VALUE, new Expression(htmlValue));
					}
				}
			}
		}*/
	}
	
	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {

		Action action = new Action(panelType);
		action.addParameter(ActionParameter.ID, generateWidgetId(context));
		setRenderParameters(context, action);
		result.add(action);
		List<Action> children = action.addChildren();
		super.render(context, children);
	}


	
}

package com.codeglide.core.rte.widgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.commands.Command;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.windowmanager.PanelInstance;
import com.codeglide.core.rte.windowmanager.WindowInstance;
import com.codeglide.core.rte.windowmanager.WindowManager;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicElement;
import com.codeglide.xml.dom.NullElement;

/*
 * 
 * WIDTH
 * HEIGHT
 * 
 * 
 * 
 */

public abstract class Widget extends Command implements SequenceBucketizable {
	protected HashSet<ActionFlag> actionFlags;
	protected HashMap<ActionParameter, Expression> actionParameters;
	
	public Widget(Item parent, Element element) {
		super(parent, element);
		Application application = (Application)getAncestor(Application.class);
		seqId = -1;
		application.getWidgetBucket().registerObject(this);
		parseExtraParams(element, application);
	}

	public void run(Context context, List<Action> result) throws CodeGlideException {
		try {
			//Logger.debug("<"+this.getClass().getSimpleName()+">");
			render((ContextUi)context, result);
			//Logger.debug("</"+this.getClass().getSimpleName()+">");
		} catch (ClassCastException _) {
			throw new RuntimeError("@invalid-context");
		}
	}

	public boolean hasFlag(ActionFlag flag) {
		return (actionFlags != null && actionFlags.contains(flag));
	}
	
	protected abstract void render(ContextUi context, List<Action> result) throws CodeGlideException;
	
	public String generateWidgetId( ContextUi context ) {
		return generateWidgetId(context.getWindowId(), context.getPanelId(), this);
	}
	
	protected String generateWidgetId( ContextUi context, Widget widget ) {
		return generateWidgetId(context.getWindowId(), context.getPanelId(), widget);
	}
	
	protected String generateWidgetId( int windowId, int panelId, Widget widget ) {
		if( widget instanceof Window )
			return Integer.toString(windowId, 36);
		else if( widget instanceof Panel )
			return Integer.toString(windowId, 36) + "." + Integer.toString(widget.getSequenceId(), 36);
		else
			return Integer.toString(windowId, 36) + "." + Integer.toString(panelId, 36) + "." + Integer.toString(widget.getSequenceId(), 36);
	}
	
	protected String generateWidgetId( ContextUi context, String name ) throws RuntimeError {
		if( name.equals("_top") )
			return name;
		int windowId = context.getWindowId(), panelId = context.getPanelId();
		if( name.indexOf('/') != -1 ) {
			String[]p = name.split("\\/");
			name = p[1];
			PanelInstance pInstance = context.getWindowManager().getWindow(windowId).getPanelInstance(p[0]);
			if( pInstance == null )
				throw new RuntimeError("@undefined-panel-ref,"+p[1]);
			panelId = pInstance.getSequenceId();
		}
		Widget widget = context.getApplication().getWidgetById(name);
		if( widget == null )
			throw new RuntimeError("@undefined-widget-ref,"+name);
		return generateWidgetId(windowId, panelId, widget);
	}
	
	protected String generateElementId( ContextUi context, Widget widget, SequenceBucketizable element ) throws CodeGlideException {
		if( element instanceof NullElement )
			throw new RuntimeError("@null-object-operation");
		
		WindowManager windowManager = context.getWindowManager();
		WindowInstance window = windowManager.getWindow(context.getWindowId());
		PanelInstance panel = window.getPanelInstance(context.getPanelId());
		
		return Integer.toString(windowManager.registerObject(window, panel, widget, element), 36);
	}
	
	protected DynamicElement getElementById( ContextUi context, String id ) {
		return (DynamicElement)context.getWindowManager().getObject(Integer.parseInt(id, 36));
	}
	
	protected DynamicAttr getAttributeById( ContextUi context, String id ) {
		return (DynamicAttr)context.getWindowManager().getObject(Integer.parseInt(id, 36));
	}
	
	protected void removeAllElements( ContextUi context, Widget widget ) {
		WindowManager windowManager = context.getWindowManager();
		WindowInstance window = windowManager.getWindow(context.getWindowId());
		PanelInstance panel = window.getPanelInstance(context.getPanelId());
		windowManager.unregisterObjects(window, panel, widget);
	}
	
	protected void removeElement( ContextUi context, Widget widget, DynamicElement element ) {
		WindowManager windowManager = context.getWindowManager();
		WindowInstance window = windowManager.getWindow(context.getWindowId());
		PanelInstance panel = window.getPanelInstance(context.getPanelId());
		windowManager.unregisterObject(window, panel, widget, element);
	}
	
	protected String getVariableHolderName(ContextUi context) {
		return "P" + context.getWindowId() + "." + context.getPanelId();
	}

	protected void parseExtraParams(Element element, Application application) {
		if( !element.hasAttributes() )
			return;

		// Add render parameters
		addRenderParameter(element, ActionParameter.WIDTH, "width");
		addRenderParameter(element, ActionParameter.HEIGHT, "height");
		
		// Obtain internal ID, if any
		String widgetId = element.getAttribute("_id");
		if( widgetId != null && !widgetId.isEmpty() ) {
			if( application.getWidgetById(widgetId ) != null )
				Logger.warn("Duplicate _id \"" + widgetId  + "\".");
			else
				application.addWidgetMapping(widgetId , this);
		}
		
		// Parse flags
		String flags = element.getAttribute("flags");
		if( flags != null && !flags.isEmpty() ) {
			String[] flagItem = flags.split(",");
			for( int i = 0; i < flagItem.length; i++ ) {
				try{
					ActionFlag actionFlag = Enum.valueOf(ActionFlag.class, flagItem[i].toUpperCase());
					if( actionFlags == null )
						actionFlags = new HashSet<ActionFlag>();
					actionFlags.add(actionFlag);
				}catch(Exception e){
					Logger.debug("Flag \"" + flagItem[i] + "\" could not be found!");
				}			
			}
		}

	}
	
	protected void addRenderParameter(Element element, ActionParameter type, String attrName ) {
		Expression exp = getExpression(element, attrName);
		if( exp != null ) {
			if( actionParameters == null )
				actionParameters = new HashMap<ActionParameter, Expression>();
			actionParameters.put(type, exp);
		}
	}
	
	protected void setRenderParameters(ContextUi context, Action action) throws CodeGlideException {
		// Add flags
		action.addFlags(actionFlags);

		// Add render parameters
		if( actionParameters != null ) {
			for( ActionParameter type : actionParameters.keySet() ) {
				try {
					action.addParameter(type, actionParameters.get(type).evaluate(context.getVariables(), context.getDocumentNode()));
				} catch (ExpressionException _) {
				}
			}
		}
	}
	
	// Sequence ID
	private int seqId;
	
	public int getSequenceId() {
		return seqId;
	}

	public void setSequenceId(int id) {
		this.seqId = id;
	}

}

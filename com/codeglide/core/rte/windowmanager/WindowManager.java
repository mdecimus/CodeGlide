package com.codeglide.core.rte.windowmanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.codeglide.core.rte.sequencers.SequenceBucket;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.xml.dom.DynamicElement;

public class WindowManager {
	// Window Instances
	private SequenceBucket windowInstances = new SequenceBucket();
	
	// Cached elements
	private SequenceBucket objectBucket = new SequenceBucket();
	private HashSet<ObjectConsumer> objectConsumers = new HashSet<ObjectConsumer>();
	
	public int registerObject(WindowInstance window, PanelInstance panel, Widget widget, SequenceBucketizable element) {
		ObjectWrapper objectWrapper = null;
		
		if( element.getSequenceId() < 0 || (objectWrapper = (ObjectWrapper)objectBucket.getObject(element.getSequenceId())) == null ) {
			objectWrapper = new ObjectWrapper(element);
			objectBucket.registerObject(objectWrapper);
		}
		
		ObjectConsumer objectConsumer = new ObjectConsumer(window.getSequenceId(), panel.getSequenceId(), widget.getSequenceId(), element.getSequenceId());
		if( !objectConsumers.contains(objectConsumer) ) {
			objectWrapper.incrementUsageCount();
			objectConsumers.add(objectConsumer);
		}
		
		//Logger.debug("Registered object ["+element.getNodeName()+"] with ID " + element.getSequenceId() + " by ["+window.getSequenceId()+","+panel.getSequenceId()+","+widget.getSequenceId()+"]");
		
		return element.getSequenceId();
	}
	
	public void unregisterObjects(WindowInstance window) {
		HashSet<ObjectConsumer> removeSet = new HashSet<ObjectConsumer>();
		for( ObjectConsumer objectConsumer : objectConsumers ) {
			if( objectConsumer.windowId == window.getSequenceId() )
				removeSet.add(objectConsumer);
		}
		if( removeSet.size() > 0 ) 
			removeObjectConsumers(removeSet);
	}
	
	public void unregisterObjects(WindowInstance window, PanelInstance panel) {
		HashSet<ObjectConsumer> removeSet = new HashSet<ObjectConsumer>();
		for( ObjectConsumer objectConsumer : objectConsumers ) {
			if( objectConsumer.windowId == window.getSequenceId() && objectConsumer.panelId == panel.getSequenceId() )
				removeSet.add(objectConsumer);
		}
		if( removeSet.size() > 0 ) 
			removeObjectConsumers(removeSet);
	}
	
	public void unregisterObjects(WindowInstance window, PanelInstance panel, Widget widget) {
		HashSet<ObjectConsumer> removeSet = new HashSet<ObjectConsumer>();
		for( ObjectConsumer objectConsumer : objectConsumers ) {
			if( objectConsumer.windowId == window.getSequenceId() && objectConsumer.panelId == panel.getSequenceId() && objectConsumer.widgetId == widget.getSequenceId() )
				removeSet.add(objectConsumer);
		}
		if( removeSet.size() > 0 ) 
			removeObjectConsumers(removeSet);
	}
	
	public void unregisterObject(WindowInstance window, PanelInstance panel, Widget widget, DynamicElement element) {
		HashSet<ObjectConsumer> removeSet = new HashSet<ObjectConsumer>();
		for( ObjectConsumer objectConsumer : objectConsumers ) {
			if( objectConsumer.windowId == window.getSequenceId() && objectConsumer.panelId == panel.getSequenceId() && objectConsumer.widgetId == widget.getSequenceId() && objectConsumer.objectId == element.getSequenceId() )
				removeSet.add(objectConsumer);
		}
		if( removeSet.size() > 0 ) 
			removeObjectConsumers(removeSet);
	}
	
	private synchronized void removeObjectConsumers(HashSet<ObjectConsumer> removeSet) {
		this.objectConsumers.removeAll(removeSet);
		for( ObjectConsumer objectConsumer : removeSet ) {
			ObjectWrapper objectWrapper = (ObjectWrapper) objectBucket.getObject(objectConsumer.objectId);
			//int uCount;
			if( objectWrapper.decrementUsageCount() < 1 ) 
				objectBucket.unregisterObject(objectWrapper.getSequenceId());
			//Logger.debug("Removed consumer for object ["+objectWrapper.getObject().getNodeName()+"] by ["+objectConsumer.windowId+","+objectConsumer.panelId+","+objectConsumer.widgetId+"], usage count is now " + uCount + ".");
		}
	}
	
	public SequenceBucketizable getObject( int id ) {
		return ((ObjectWrapper)objectBucket.getObject(id)).getObject();
	}
	
	private class ObjectConsumer {
		int windowId, panelId, widgetId, objectId;
		
		public ObjectConsumer(int windowId, int panelId, int widgetId, int objectId) {
			this.windowId = windowId;
			this.panelId = panelId;
			this.widgetId = widgetId;
			this.objectId = objectId;
		}
		
		public boolean equals(Object obj) {
			return ((ObjectConsumer)obj).windowId == this.windowId && ((ObjectConsumer)obj).panelId == this.panelId && ((ObjectConsumer)obj).widgetId == this.widgetId && ((ObjectConsumer)obj).objectId == objectId;
		}
	}
	
	private class ObjectWrapper implements SequenceBucketizable {
		protected SequenceBucketizable object;
		protected int usageCount = 0;
		
		public ObjectWrapper(SequenceBucketizable object) {
			this.object = object;
		}
		
		public synchronized int getUsageCount() {
			return usageCount;
		}

		public synchronized void incrementUsageCount() {
			this.usageCount++;
		}
		
		public synchronized int decrementUsageCount() {
			return --this.usageCount;
		}

		public SequenceBucketizable getObject() {
			return object;
		}

		public int getSequenceId() {
			return object.getSequenceId();
		}

		public void setSequenceId(int id) {
			object.setSequenceId(id);
		}
	}
	
	public WindowInstance getWindow( int id ) {
		return (WindowInstance)windowInstances.getObject(id);
	}
	
	public int addWindow( WindowInstance window ) {
		return windowInstances.registerObject(window);
	}
	
	public void removeWindow( int id ) {
		WindowInstance window = getWindow(id);
		if( window != null )
			unregisterObjects(window);
		windowInstances.unregisterObject(id);
	}
	
	public List<WindowInstance> getWindows() {
		Collection<SequenceBucketizable> windows = windowInstances.getItems();
		LinkedList<WindowInstance> result = new LinkedList<WindowInstance>();
		if( windows.size() > 0 ) {
			Iterator<SequenceBucketizable>it = windows.iterator();
			while( it.hasNext() ) {
				result.add((WindowInstance)it.next());
			}
		}
		return result;
	}
	
	public SequenceBucket getWindowBucket() {
		return windowInstances;
	}
	
	//public void removeAllWindows() {
	//	windowInstances = new SequenceBucket();
	//}
	
	//TODO Implement stylesheets in a more generic way, also move to different class.
	private HashMap<String, String> styleSheets = null;
	private boolean hasNew = false;
	
	public void addStyleSheet(String name, String css) {
		if( styleSheets == null ) 
			styleSheets = new HashMap<String, String>();
		if( !styleSheets.containsKey(name) ) {
			styleSheets.put(name, css);
			hasNew = true;
		}
	}
	
	public boolean hasStyleSheet(String name) {
		return (styleSheets != null && styleSheets.containsKey(name));
	}
	
	public String getStyleSheet(String name) {
		return styleSheets.get(name);
	}
	
	public Collection<String> getStyleSheets() {
		hasNew = false;
		return styleSheets.keySet();
	}
	
	public boolean hasNewStylesheet() {
		return hasNew;
	}

}

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
package com.codeglide.core.rte.windowmanager;

import java.util.Collection;
import java.util.HashMap;

import com.codeglide.core.rte.commands.Function;
import com.codeglide.core.rte.sequencers.SequenceBucket;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;

public class WindowInstance implements SequenceBucketizable {
	// Instance id
	private int id = -1;

	// Lists of active panels in this instance
	private HashMap<String, PanelInstance> panelInstances = new HashMap<String, PanelInstance>();
	
	private SequenceBucket panelBucket = new SequenceBucket();

	private Function callbackFunction = null;
	private int callbackWindowId = -1;
	private int callbackPanelId = -1;
	
	private WindowManager windowManager;
	
	public WindowInstance( WindowManager windowManager ) {
		this.windowManager = windowManager;
	}
	
	public WindowManager getWindowManager() {
		return windowManager;
	}
	
	public int getCallbackPanelId() {
		return callbackPanelId;
	}

	public void setCallbackPanelId(int callbackPanelId) {
		this.callbackPanelId = callbackPanelId;
	}

	public int getCallbackWindowId() {
		return callbackWindowId;
	}

	public void setCallbackWindowId(int callbackWindowId) {
		this.callbackWindowId = callbackWindowId;
	}

	public Function getCallbackFunction() {
		return callbackFunction;
	}

	public void setCallbackFunction(Function callbackFunction) {
		this.callbackFunction = callbackFunction;
	}

	public PanelInstance getPanelInstance(String name) {
		return panelInstances.get(name);
	}
	
	public PanelInstance getPanelInstance(int id) {
		for( PanelInstance instance : panelInstances.values() ) {
			if( instance.getSequenceId() == id )
				return instance;
		}
		return null;
	}
	
	public Collection<PanelInstance> getPanelInstances() {
		return panelInstances.values();
	}
	
	public Collection<String> getPanelNames() {
		return panelInstances.keySet();
	}
	
	public int addPanelInstance(String name, PanelInstance instance) {
		removePanelInstance(name);
		panelBucket.registerObject(instance);
		panelInstances.put(name, instance);
		return instance.getSequenceId();
	}
	
	public boolean hasPanelInstance(String name) {
		return panelInstances.containsKey(name);
	}
	
	public void removePanelInstance(String name) {
		PanelInstance instance = getPanelInstance(name);
		if( instance != null ) {
			windowManager.unregisterObjects(this, instance);
			panelBucket.unregisterObject(instance.getSequenceId());
		}
	}
	
	public int getSequenceId() {
		return this.id;
	}

	public void setSequenceId(int id) {
		this.id = id;
	}

}

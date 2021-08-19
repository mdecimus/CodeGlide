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

import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.widgets.RootPanel;

public class PanelInstance implements SequenceBucketizable {

	int id = -1;
	
	// Panel reference
	private RootPanel panel = null;
	
	// Variables used by this instance
	private VariableHolder variables = null;

	public VariableHolder getVariables() {
		return variables;
	}

	public void setVariables(VariableHolder variables) {
		this.variables = variables;
	}

	public RootPanel getPanel() {
		return panel;
	}

	public void setPanel(RootPanel panel) {
		this.panel = panel;
	}

	public int getSequenceId() {
		return this.id;
	}

	public void setSequenceId(int id) {
		this.id = id;
	}

}

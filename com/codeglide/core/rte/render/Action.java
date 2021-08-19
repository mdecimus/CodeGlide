package com.codeglide.core.rte.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Action {
	private ActionType type;
	private HashSet<ActionFlag> flags = null;
	private List<Action> children = null;
	private List<Record> records = null;
	private HashMap<ActionParameter, String> parameters = new HashMap<ActionParameter, String>();
	private Action parent = null;
	
	public Action( ActionType type ) {
		this.type = type;
	}
	
	public ActionType getType() {
		return type;
	}
	
	public Action getParent() {
		return parent;
	}

	public void setParent(Action parent) {
		this.parent = parent;
	}
	
	public String getActionName() {
		return type.toString().toLowerCase();
	}
	
	public boolean isAncestorType(ActionType type) {
		Action runner = parent;
		while( runner != null && runner.getType() != type )
			runner = runner.getParent();
		return (runner != null && runner.getType() == type);
	}
	
	public boolean hasChildType(ActionType type) {
		if( children != null ) {
			for( Action child : children ) {
				if( child.getType() == type || child.hasChildType(type) )
					return true;
			}
		}
		return false;
	}

	public boolean isParentType(ActionType type) {
		return (parent != null && parent.getType() == type);
	}

	public void addFlag( ActionFlag name ) {
		if( flags == null )
			flags = new HashSet<ActionFlag>();
		flags.add(name);
	}
	
	public void addFlags( Collection<ActionFlag> flags ) {
		if( flags != null ) {
			if( this.flags == null )
				this.flags = new HashSet<ActionFlag>();
			this.flags.addAll(flags);
		}
	}
	
	public void addParameter( ActionParameter name, String parameter ) {
		parameters.put(name, parameter);
	}
	
	public List<Action> addChildren() {
		this.children = new ActionList<Action>(this);
		return this.children;
	}
	
	public void addRecords(List<Record> records) {
		this.records = records;
	}
	
	public String getParameter(ActionParameter name) {
		return parameters.get(name);
	}
	
	public void removeParameter(ActionParameter name) {
		parameters.remove(name);
	}
	
	public Collection<ActionFlag> getFlags() {
		return flags;
	}
	
	protected class ActionList<E> extends LinkedList<E> {
		private static final long serialVersionUID = 1L;
		private Action parent;
		
		public ActionList(Action parent) {
			super();
			this.parent = parent;
		}

		public boolean add(E arg0) {
			((Action)arg0).setParent(parent);
			return super.add(arg0);
		}
	}
	
	public List<Action> getChildren() {
		return (children != null) ? children : new LinkedList<Action>();
	}
	
	public List<Record> getRecords() {
		return (records != null) ? records : new LinkedList<Record>();
	}
	
	public boolean hasFlag(ActionFlag flag) {
		return (flags != null && flags.contains(flag));
	}
}

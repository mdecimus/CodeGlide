package com.codeglide.core.rte;

import java.util.HashMap;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;

public class Service extends Item {
	private HashMap<String, Expression> parameters;
	private HashMap<String, Map> mappings;
	private String type;
	
	public Service(Item parent, Element element) {
		super(parent, element);
	}

	public Expression getParameter(String name) {
		return parameters.get(name);
	}

	public Map getMap(String name) {
		return mappings.get(name);
	}
	
	public class Map {
		String name;
		HashMap<String, Expression> values = new HashMap<String, Expression>();
		
		public Expression getValue(String name) {
			return values.get(name);
		}
		
		public void addValue(String name, Expression value){
			values.put(name, value);
		}
		
		public void setName(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	public void addParameter(String name, Expression value){
		if(parameters == null)
			parameters = new HashMap<String, Expression>();
		parameters.put(name, value);		
	}
	
	public void addMapping(Map map){
		if(mappings == null)
			mappings = new HashMap<String, Map>();
		mappings.put(map.getName(), map);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	protected void parseElement(Element element, Application application) {
		setType(element.getAttribute("type"));
		
		for( Element child : getChildrenElements(element) ){
			
			if(child.getNodeName().equals("params")){
				for( Element param : getChildrenElements(child) ){
					addParameter( param.getAttribute("name"), new Expression(param.getAttribute("value")) );
				}
			}else if(child.getNodeName().equals("mappings")){
				
				for( Element map : getChildrenElements(child) ){
					
					Map subMap = new Map();
					subMap.setName(map.getAttribute("type"));
					
					for( Element define : getChildrenElements(map)){
						subMap.addValue(define.getAttribute("name"), new Expression(define.getAttribute("value")));
					}
					
					addMapping(subMap);
				}
			}
		}
		
	}
	
}

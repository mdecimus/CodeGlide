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
package com.codeglide.util.json;

import java.text.ParseException;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.xml.dom.DynamicElement;

public class Json2DynamicElement {

	public static void importNodes( DynamicElement node, String jsonString ) throws ParseException {
		processNode(JsonNode.parse(jsonString), node);
	}

	private static void processNode(JsonNode jsonNode, DynamicElement node) {

		node.disableTracking();

		for( ; jsonNode != null ; jsonNode = jsonNode.getNextNode() ) {

			String name = jsonNode.getName();
			JsonValue value = jsonNode.getValue();

			if( value.getType() == JsonValue.T_OBJECT ) {

				DynamicElement child = new DynamicElement(node.getDocumentNode(), name);
				processNode((JsonNode)value.getValue(), child);
				node._appendChild(child);

			} else if( value.getType() == JsonValue.T_STRING ) {

				node.setAttribute(name, (String) value.getValue());
			}
		}

		node.enableTracking();
	}

	public static String exportNodes(List<Node> nodes) {
		JsonNode result = null;
		for( Node node : nodes ) {
			JsonNode jnode = new JsonNode(node.getNodeName(), processNode(node));
			if( result != null )
				result.appendNext(jnode);
			else
				result = jnode;
		}
		return result.toString();
	}

	private static JsonNode processNode(Node node) {
		JsonNode result = null;
		
		if(node.hasAttributes()){
			NamedNodeMap attributes = node.getAttributes();
			for(int i = 0 ; i < attributes.getLength() ; i++){
				Node attribute = attributes.item(i);
				JsonNode jnode = new JsonNode(attribute.getNodeName(), attribute.getNodeValue());
				if( result != null )
					result.appendNext(jnode);
				else
					result = jnode;
			}
		}

		if(node.hasChildNodes()){
			NodeList childs = node.getChildNodes();
			for(int i = 0 ; i < childs.getLength() ; i++) {
				Node child = childs.item(i);
				JsonNode jnode = new JsonNode(child.getNodeName(), processNode(child));
				if( result != null )
					result.appendNext(jnode);
				else
					result = jnode;
			}
		}
		
		return result;
	}

}

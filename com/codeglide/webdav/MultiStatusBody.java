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

package com.codeglide.webdav;

import java.util.HashMap;
import java.util.LinkedList;

import com.codeglide.webdav.DavResponse.Status;

//Used for multi-status http responses
public class MultiStatusBody {
	//Inner class used for modeling a property
	static private class Property {
		//Property name and value, if any
		final private String namespace;
		final private String name;
		final private String value;		
		//Constructors
		Property(String namespace,String name) {
			this(namespace,name,null);
		}		
		Property(String namespace,String name,String value) {
			this.namespace = namespace;
			this.name = name;
			this.value = value;
		}		
		//Codifies itself as xml
		public String getContent() {
			if (value != null && !value.isEmpty())
				return "<" + name + " xmlnms='" + namespace + "'>" + value + "</" + name + ">";
			else
				return "<" + name + " xmlnms='" + namespace + "'/>";
		}
		
		public String toString() {
			return "\t\t\t\t" + this.getContent() + "\n";
		}
	}
	
	//Inner class used for modeling a property collection with the same status
	static public class PropertyStatus {
		final private DavResponse.Status status;
		final private LinkedList<Property> properties = new LinkedList<Property>();
		//Constructor
		PropertyStatus(DavResponse.Status status) {
			this.status = status;
		}
		//Add properties
		public void addProperty(String namespace,String name,String value) {
			properties.add(new Property(namespace,name,value));
		};		
		public void addProperty(String namespace,String name) {
			properties.add(new Property(namespace,name));
		};
		//Codifies itself as xml
		public String getContent() {
			String content = "<propstat>";
			content += "<prop>";
		    for(Property property : properties)
		    	content += property.getContent();
		    content += "</prop>";
		    content += "<status>" + status.getTag() + "</status>";
			content += "</propstat>";
			return content;
		}
		
		public String toString() {
			String content = "\t\t<propstat>\n";
			content += "\t\t\t<prop>\n";
		    for(Property property : properties)
		    	content += property.toString();
		    content += "\t\t\t</prop>\n";
		    content += "\t\t\t<status>" + status.getTag() + "</status>\n";
			content += "\t\t</propstat>\n";
			return content;
		}
	}
	//Inner class used for modeling a response node into the multi-status  
	static public class Response {
		//The subject of the response
		final private Status status;
		private final String href;
		private final HashMap<Status,PropertyStatus> propstats = new HashMap<Status,PropertyStatus>();		
		//Constructor
		Response (String href) {
			this(href,null);
		}
		Response (String href,Status status) {
			this.href = href;
			this.status = status;
		}
		//Returns the PropertyStatus for the provided status. Creates it if does not exist
		public PropertyStatus getPropertyStatus(Status status) {
			PropertyStatus propstat = propstats.get(status);
			if (propstat == null) {
				propstat = new PropertyStatus(status);
				propstats.put(status,propstat);
			}
			return propstat;
		}		
		//Codifies itself as xml
		public String getContent() {
			String content = "<response>";
			content += "<href>" + href + "</href>";
		    for(PropertyStatus status : propstats.values())
		    	content += status.getContent();
		    if (status != null)
		    	content += "<status>" + status.getTag() + "</status>";
			content += "</response>";
			return content;
		}
		
		public String toString() {
			String content = "\t<response>\n";
			content += "\t\t<href>" + href + "</href>\n";
		    for(PropertyStatus status : propstats.values())
		    	content += status.toString();
		    if (status != null)
		    	content += "\t\t<status>" + status.getTag() + "</status>\n";
			content += "\t</response>\n";
			return content;
		}
	}	
	//The responses
	final private LinkedList<Response> responses = new LinkedList<Response>();

	//Codifies its content as xml
	public String getContent() {
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><multistatus xmlns='DAV:'>";
		//Iterate over responses, adding them to the xml
	    for (Response response : responses)
	    	content += response.getContent();
		content += "</multistatus>";
		return content;
	}
	
	public String toString() {
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<multistatus xmlns='DAV:'>\n";
		//Iterate over responses, adding them to the xml
	    for (Response response : responses)
	    	content += response.toString();
		content += "</multistatus>\n";
		return content;
	}
	
	//Creates a new response and returns it
	public Response addResponse(String href) {
		Response response = new Response(href);
		responses.add(response);
		return response;
	}
	
	public Response addResponse(String href,Status status) {
		Response response = new Response(href,status);
		responses.add(response);
		return response;
	}

	public boolean isEmpty() {
		return responses.isEmpty();
	}
}

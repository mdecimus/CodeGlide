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
package com.codeglide.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.interfaces.Daemon;
import com.codeglide.interfaces.Interface;
import com.codeglide.interfaces.cookies.CookiesInterface;
import com.codeglide.interfaces.mail.MailInterface;
import com.codeglide.interfaces.messages.MessagesInterface;
import com.codeglide.interfaces.root.RootInterface;
import com.codeglide.interfaces.system.SystemInterface;
import com.codeglide.interfaces.xmldb.DbInterface;

public class ServerSettings {
	
	private static HashMap<String, Application> applications; 
	private static HashMap<Integer, Interface> interfaces;
	private static List<Daemon> daemons;

	private static boolean urlPackageMapping;
	private static boolean urlInstanceMapping;
	private static String defualtPackage;
	private static String defaultInstance;
	private static String applicationsDir;
	
	public final static int ROOT_IF = 0x001;
	public final static int XMLDB_IF = 0x002;
	public final static int COOKIES_IF = 0x004;
	public final static int MESSAGES_IF = 0x008;
	public final static int SYSTEM_IF = 0x010;
	public final static int MAIL_IF = 0x020;

	public final static int ALL_INTERFACES = ROOT_IF | XMLDB_IF | COOKIES_IF | MESSAGES_IF | SYSTEM_IF | MAIL_IF;
	
	public static void init(String path) throws SAXException, IOException {		

		// Initialize logger
		Logger.init(Logger.ALL, null);
		
		// Parse the XML settings file
		DOMParser docParser = new DOMParser();
		docParser.setFeature("http://xml.org/sax/features/namespaces",false);
		docParser.parse( "file:" + path);
		Document doc = docParser.getDocument();

		Element rootElement = doc.getDocumentElement();
		NodeList childsList = rootElement.getChildNodes();

		// Add all known interfaces
		interfaces = new HashMap<Integer, Interface>();
		interfaces.put(ROOT_IF, new RootInterface());
		interfaces.put(XMLDB_IF, new DbInterface());
		interfaces.put(COOKIES_IF, new CookiesInterface());
		interfaces.put(MESSAGES_IF, new MessagesInterface());
		interfaces.put(SYSTEM_IF, new SystemInterface());
		interfaces.put(MAIL_IF, new MailInterface());
		
		for(int i = 0 ; i < childsList.getLength() ; i ++){

			Node child = childsList.item(i);

			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String nodeName = child.getNodeName();

			if(nodeName.equalsIgnoreCase("interfaces")){
				parseInterfaces((Element) child);
			}else if(nodeName.equalsIgnoreCase("settings")){
				parseSettings((Element) child);
			}else if(nodeName.equalsIgnoreCase("daemons")){
				parseDaemons((Element) child);
			}
		}
		
		parseApplications();
	}

	private static int parseInterfaceName(String name) {
		if( name.equalsIgnoreCase("root") )
			return ROOT_IF;
		else if( name.equalsIgnoreCase("xmldb") )
			return XMLDB_IF;
		if( name.equalsIgnoreCase("cookies") )
			return COOKIES_IF;
		if( name.equalsIgnoreCase("messages") )
			return MESSAGES_IF;
		if( name.equalsIgnoreCase("system") )
			return SYSTEM_IF;
		if( name.equalsIgnoreCase("mail") )
			return MAIL_IF;
		else
			return -1;
	}
	
	private static void parseApplications() {
		
		// Get all the files from the specified folder.
		applications = new HashMap<String, Application>();
		File appDirectory = new File(applicationsDir);
				
		File[] files = appDirectory.listFiles(new FilenameFilter() {
		
			public boolean accept(File dir, String name) {

				if(dir != null){
					if(name.endsWith(".xml"))
						return true;
					else
						return false;
				}
				return false;
			}
		
		});
		
		for(int i = 0 ; i < files.length ; i++){
			try {
				String fileName = files[i].getPath();
				//FIXME remove this
				if( fileName.indexOf('_') > -1 )
					continue;
				Application application = new Application(new File(fileName));
				
				if(application.getName() != null){
					applications.put(application.getName(), application);
					
					for(Interface interfaceToInitialize : interfaces.values()){
						interfaceToInitialize.initApplication(application);
					}
				}
			} catch (Exception e) {
				Logger.debug(e);
			}
		}
	}

	private static void parseSettings(Element settingsElement) {

		NodeList settingsList = settingsElement.getChildNodes();

		for(int i = 0 ; i < settingsList.getLength() ; i ++){

			Node setting = settingsList.item(i);

			if(setting.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String name = ((Element) setting).getAttribute("name");
			String value = ((Element) setting).getAttribute("value");

			if (name.equalsIgnoreCase("urlpackagemapping"))
				urlPackageMapping = new Boolean(value);				
			else if(name.equalsIgnoreCase("urlinstancemapping"))
				urlInstanceMapping = new Boolean(value);
			else if(name.equalsIgnoreCase("defualtpackage"))
				defualtPackage = value;
			else if(name.equalsIgnoreCase("defaultinstance"))
				defaultInstance = value;
			else if(name.equalsIgnoreCase("appdir"))
				applicationsDir = value;
		}
	}

	private static void parseInterfaces(Element interfacesElement) {

		// Get all the "interface" elements
		NodeList interfacesList = interfacesElement.getElementsByTagName("interface");

		for(int i = 0 ; i < interfacesList.getLength() ; i++){

			Element interfaceElement = (Element) interfacesList.item(i);

			try {
				// List of Parameters, Schemas
				NodeList interfaceChilds = interfaceElement.getChildNodes();

				// Get name
				Interface interfaceInstance = interfaces.get(parseInterfaceName(interfaceElement.getAttribute("name")));
				if( interfaceInstance == null ) {
					Logger.error("Unknown interface '" + interfaceElement.getAttribute("name") +"'.");
					continue;
				}

				for(int j = 0 ; j < interfaceChilds.getLength() ; j++){

					Node child = interfaceChilds.item(j);

					if(child.getNodeType() != Node.ELEMENT_NODE)
						continue;

					String childName = child.getNodeName();

					if(childName.equals("parameters")){

						NodeList parametersList = child.getChildNodes();

						for(int k = 0 ; k < parametersList.getLength() ; k ++){
							Node parameter = parametersList.item(k);

							if(parameter.getNodeType() != Node.ELEMENT_NODE)
								continue;

							// Set corresponding parameters
							interfaceInstance.setParameter(((Element) parameter).getAttribute("name") , ((Element) parameter).getAttribute("value"));
						}

					}

				}

				// Initialize each interface.
				interfaceInstance.init();	

			} catch (Exception e) {
				//Logger.debug(e);
				Logger.debug((e.getLocalizedMessage()));
			}
		}

	}
	

	private static void parseDaemons(Element daemonsElement) {

		daemons = new ArrayList<Daemon>();
		
		NodeList daemonsList = daemonsElement.getChildNodes();

		for(int i = 0 ; i < daemonsList.getLength() ; i ++){

			Node daemon = daemonsList.item(i);

			if(daemon.getNodeType() != Node.ELEMENT_NODE)
				continue;

			try {
				Daemon daemonInstance =  (Daemon) Class.forName(((Element) daemon).getAttribute("driverclass")).newInstance();

				NodeList parametersList = daemon.getChildNodes();

				for(int m = 0 ; m < parametersList.getLength() ; m ++){
					Node parameter = parametersList.item(m);

					if(parameter.getNodeType() != Node.ELEMENT_NODE)
						continue;

					// Set corresponding parameters
					daemonInstance.setParameter(((Element) parameter).getAttribute("name") , ((Element) parameter).getAttribute("value"));
				}

				daemons.add(daemonInstance);

			} catch (Exception e) {
				Logger.debug(e);
			}
		}
	}

	public static boolean isUrlWithPackageName() {
		return urlPackageMapping;
	}

	public static boolean isUrlWithInstanceName() {
		return urlInstanceMapping;
	}

	public static String getDefaultPackageName() {
		return defualtPackage;
	}

	public static String getDefaultInstanceName() {
		return defaultInstance;
	}

	public static Application getApplication(String name) {
		return applications.get(name);
	}

	public static RootInterface getRootInterface() {
		return (RootInterface)interfaces.get(ROOT_IF);
	}

	public static Interface getInterface(int type) {
		return interfaces.get(type);
	}

	public static List<Daemon> getDaemons() {
		return daemons;
	}
	
	public static Application getApplicationByHostname(String hostname) throws CodeGlideException {
		// Obtain the package and site name from the URL
		String packageName = getDefaultPackageName();
		
		if( isUrlWithInstanceName() || isUrlWithPackageName() ) {
			String[] nameTokens = hostname.split(".");
			
			if( ServerSettings.isUrlWithInstanceName() && ServerSettings.isUrlWithPackageName() ) {
				if( nameTokens.length < 3 ) {
					throw new RuntimeError("The URL you are trying to access is not registered. Missing Package and Site name.");
				}
				packageName = nameTokens[1];
			} else if( ServerSettings.isUrlWithPackageName() ) {
				if( nameTokens.length < 2 ) {
					throw new RuntimeError("The URL you are trying to access is not registered. Missing Package name.");
				}
				packageName = nameTokens[0];
			} else {
				if( nameTokens.length < 2 ) {
					throw new RuntimeError("The URL you are trying to access is not registered. Missing Site name.");
				}
			}
		}
		
		return getApplication(packageName.toUpperCase());
	}
	
	public static String getInstanceName(String hostname) {
		String instanceName = getDefaultInstanceName();

		if( isUrlWithInstanceName() || isUrlWithPackageName() ) {
			String[] nameTokens = hostname.split(".");
			
			if( ServerSettings.isUrlWithInstanceName() && ServerSettings.isUrlWithPackageName() ) {
				instanceName = nameTokens[0].toLowerCase();
			} else if( ServerSettings.isUrlWithInstanceName() ) {
				instanceName = nameTokens[0].toLowerCase();
			}
		}
		
		return instanceName;
	}
	
}

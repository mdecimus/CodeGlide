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

import java.io.FileInputStream;

import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;



public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {		
		
		// Initialize CodeGlide Runtime Environment
		ServerSettings.init("conf/codeglide.xml");

		// Start Jetty
		Server server = new Server();
		XmlConfiguration config = new XmlConfiguration(new FileInputStream("conf/jetty/jetty.xml"));
		config.configure(server);
		server.start();
		server.join();
	}

}

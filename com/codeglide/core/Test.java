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
import java.util.LinkedList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.rte.variables.VariableHolder;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.interfaces.root.RootInterface;
import com.codeglide.interfaces.xmldb.DbInterface;
import com.codeglide.util.json.JsonNode;
import com.codeglide.xml.dom.DummyDocument;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;

public class Test {


	/*
	 * Parameters:
	 * 
	 * connstring      = Connection String to SQL
	 * user            = SQL User
	 * pass            = SQL Pass
	 * type            = SQL Server type
	 * poolsize        = SQL Connection pool size
	 * driverclass     = SQL Driver Class
	 * 
	 * serverid        = Numeric ID of the current server
	 * 
	 * fti_url         = Full text index URL
	 * fti_user        = Full text index auth username
	 * fti_pass        = Full text index auth pass
	 * fti_driverclass = Full text index Driver class (com.codeglide.interfaces.xmldb.SolrTextSearch)
	 * 
	 */
	
	public static void main(String[] args) throws Exception {
		
		Logger.init(Logger.ALL, null);
		
		// Create a dummy document
		Document document = new DummyDocument();
		
		DynamicElement root = new DynamicElement(document, "CG");
		((DummyDocument)document).setDocumentElement(root);
		root.appendChild("Folder");
		
		XPathFactory xf = XPathFactory.newInstance();
		XPath xp = xf.newXPath();
		VariableResolver vr = new VariableResolver();
		VariableHolder vh = new VariableHolder();
		xp.setXPathVariableResolver(vr);
		vr.addVariables("_g", vh);
		vh.defineVariable("Tmp", VariableHolder.OBJECT);
		vh.defineVariable("Str", VariableHolder.STRING);
		LinkedList<Node>l = new LinkedList<Node>();
		l.add(root.appendChild("Peper"));
		DummyNodeList nl = new DummyNodeList(l);
		vr.setVariable("Tmp", nl);
		vr.setVariable("Str", "dsdsd");
		
		XPathExpression x = xp.compile("name($Tmp)");
		System.out.println( x.evaluate(document) );
		
		
/*		RootInterface root = new RootInterface();
		root.setParameter("uid", "'1234:2040:1050'");
		root.setParameter("lang", "'en_US'");
		root.setParameter("tz", "'GMT-3'");
		root.setParameter("siteid", "'MySite'");
		//root.init("TestApp");
		
		DbInterface db = new DbInterface();
		db.setParameter("connstring", "jdbc:mysql://192.168.1.110/codeglide?allowMultiQueries=true");
		db.setParameter("user", "cg");
		db.setParameter("pass", "cg");
		db.setParameter("type", "mysql");
		db.setParameter("poolsize", "10");
		db.setParameter("driverclass", "com.mysql.jdbc.Driver");
		db.setParameter("serverid", "0");
		db.setParameter("binarystore", "c:/temp/cg_binary");
		db.setParameter("quota", "USER,UID,QUOTA");
		db.setParameter("fti_url", "http://127.0.0.1/solr");
		db.setParameter("fti_user", "admin");
		db.setParameter("fti_pass", "pass");
		db.setParameter("fti_driverclass", "com.codeglide.interfaces.xmldb.SolrTextSearch");*/
		/*
		// Create a schema for objects named 'Mail'
		Schema mailSchema = db.addSchema("Mail");
		InterfaceSchemaItem mailItem;

		// Add index for ID field
		mailItem = mailSchema.addItem();
		mailItem.setName("MID");
		mailItem.setType(InterfaceSchemaItem.T_INDEX);
		mailItem.setType(InterfaceSchemaItem.F_STRING);
		mailItem.setValue(new Expression("@Id"));
		
		// Add index for Subject field
		mailItem = mailSchema.addItem();
		mailItem.setName("SUBJECT");
		mailItem.setType(InterfaceSchemaItem.T_INDEX);
		mailItem.setFormat(InterfaceSchemaItem.F_STRING);
		mailItem.setValue(new Expression("@Subject"));
		
		// Add index for Subject field
		mailItem = mailSchema.addItem();
		mailItem.setName("TOF");
		mailItem.setType(InterfaceSchemaItem.T_INDEX);
		mailItem.setFormat(InterfaceSchemaItem.F_STRING);
		mailItem.setValue(new Expression("Addresses/Address[@Type='To' or @Type='Cc']/@Addr|Addresses/Address[@Type='To' or @Type='Cc']/@Name"));
		
		// Add full text index for Message Bodies
		mailItem = mailSchema.addItem();
		mailItem.setType(InterfaceSchemaItem.T_TEXTINDEX);
		mailItem.setValue(new Expression("//Contents/Body[1]"));
		mailItem.setAttrBin(new Expression("@Bin"));
		mailItem.setAttrCT(new Expression("@Type"));
		
		// Add full text index for Text attachments
		mailItem = mailSchema.addItem();
		mailItem.setType(InterfaceSchemaItem.T_TEXTINDEX);
		mailItem.setValue(new Expression("//File[contains(@Type,'text/') or @Type='ms-word']"));
		mailItem.setAttrBin(new Expression("@Bin"));
		mailItem.setAttrCT(new Expression("@Type"));

		// Register To line changes using a Changelog (just for testing)
		mailItem = mailSchema.addItem();
		mailItem.setType(InterfaceSchemaItem.T_CHANGELOG);
		mailItem.setName("TO");
		mailItem.setValue(new Expression("Addresses/Address[(@Type='To' or @Type='Cc') and position() = 1]/@Name"));

		// Create a autocalculated field (just for testing)
		mailItem = mailSchema.addItem();
		mailItem.setType(InterfaceSchemaItem.T_CALCULATED);
		mailItem.setName("Test");
		mailItem.setValue(new Expression("concat('[',@Subject,']')"));
		*/
		//db.init("TestApp");
		
/*		DynamicElement rootNode = null;//root.createRootElement(document);
		DynamicElement dbRootNode = null;//db.createRootElement(document);

		rootNode.disableTracking();
		rootNode.appendChild(dbRootNode);
		rootNode.enableTracking();

		DynamicElement myFolder = (DynamicElement) dbRootNode.getChildNode("Folder");
		System.out.println(((DynamicElement)myFolder.getChildNode("MailFolder")).getChildNode("Mail").getAttributes().item(3).getNodeValue());
		
		System.out.println("Done");
		
		if( false ) {
			
			DynamicElement testFolder = new DynamicElement(dbRootNode.getDocumentNode(), "MailFolder");
	
			testFolder.setAttribute("name", "This is my test mail folder");
			testFolder.setAttribute("_Container", "");
			
			DynamicElement testMail = new DynamicElement(dbRootNode.getDocumentNode(), "Mail");
			testMail.setAttribute("Subject", "hello world");
			testMail.setAttribute("Id", "<lfjdslfjsd@ldfldjf.host>");
			
			DynamicElement test = (DynamicElement)((DynamicElement)testMail.appendChild("Contents")).appendChild("Body");
			test.setAttribute("Type", "text/plain");
			test.setAttribute("Bin", new FileInputStream("c:/temp/Linksys.ico"));
			test = (DynamicElement)((DynamicElement)testMail.appendChild("Attachments")).appendChild("File");
			test.setAttribute("Type", "application/ms-word");
			test.setAttribute("Bin", new FileInputStream("c:/temp/test.doc"));
			test = (DynamicElement)((DynamicElement)testMail.appendChild("Addresses")).appendChild("Address");
			test.setAttribute("Type", "To");
			test.setAttribute("Name", "Jorge Arbusto");
			test.setAttribute("Addr", "jarbusto@casablanca.gob");
			
			testFolder.appendChild(testMail);
			
			myFolder.appendChild(testFolder);
		} */

		/*rootNode = root.createRootElement(document);
		dbRootNode = db.createRootElement(document);

		rootNode.disableTracking();
		rootNode.appendChild(dbRootNode);
		rootNode.enableTracking();

		myFolder = (DynamicElement) dbRootNode.getChildNode("Folder");
		
		System.out.println(((DynamicElement)myFolder.getChildNode("MailFolder")).getChildNode("Mail"));
		*/
		
	}
	
	
	

}

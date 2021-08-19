package com.codeglide.util.mail;

import java.io.IOException;
import java.util.Collection;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.interfaces.root.RootNode;
import com.codeglide.interfaces.xmldb.DbContainerNode;
import com.codeglide.interfaces.xmldb.DbInterface;
import com.codeglide.interfaces.xmldb.DbLeafNode;
import com.codeglide.interfaces.xmldb.DbNode;
import com.codeglide.interfaces.xmldb.DbRootNode;
import com.codeglide.util.mail.folder.FolderElement;
import com.codeglide.util.mail.folder.MailElement;
import com.codeglide.util.mail.folder.RootFolderElement;
import com.codeglide.webdav.exceptions.WebdavServerException;
import com.codeglide.xml.dom.DummyDocument;
import com.codeglide.xml.dom.DynamicElement;

@SuppressWarnings("serial")
public class MailSyncTestServlet extends HttpServlet {
	static protected final VariableResolver resolver = new VariableResolver();
	
	private Session getSession(HttpServletRequest request) throws WebdavServerException {
		try {
			Session session = null;//SessionManager.getSession("admin");
			if (session != null)
				//Existing session
				return session;
			else {
				Application application = ServerSettings.getApplicationByHostname(request.getServerName());
				//Get the user, check the password, create session
				//FIXME fix this
				/*long uid = ((DbInterface)ServerSettings.getInterface(DbInterface.class)).getUserNodeId(application,"admin");
				if( uid != -1 ) {
					Document doc = ((RootNode)ServerSettings.getRootInterface().createRootElement(application, new DummyDocument())).getDocumentNode();
					DbNode userNode = new DbLeafNode(doc, uid);
					if( userNode != null && userNode.getAttribute("Pass") != null && userNode.getAttribute("Pass").equals("pass")) {
						session = SessionManager.createSession(application, userNode);
						SessionManager.addSession("admin",session);
					}
				}*/
				return session;
			}
		} catch (CodeGlideException e) {
			throw new WebdavServerException(e.getMessage());
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DynamicElement dbr1 = (DynamicElement)((DynamicElement)((DynamicElement)((DbRootNode)this.getSession(request).getRootNode().getChildNode("Db")).getChildNode("UserFolder")).getChildNode("Folder")).getChildNode("Root1");
		DynamicElement dbr2 = (DynamicElement)((DynamicElement)((DynamicElement)((DbRootNode)this.getSession(request).getRootNode().getChildNode("Db")).getChildNode("UserFolder")).getChildNode("Folder")).getChildNode("Root2");
		DynamicElement dyn = new DynamicElement(this.getSession(request).getRootNode().getDocumentNode(),"Folder");
		dyn.setAttribute("Name","VirtualMailRoot");
		RootFolderElement root1 = new RootFolderElement(dbr1);
		RootFolderElement root2 = new RootFolderElement(dbr2);
/*
		root.setAttribute("Host","mail.codeglide.com");
		root.setAttribute("Protocol","imap");
		root.setAttribute("SSL",false);
		root.setAttribute("User","hzaccheo");
		root.setAttribute("Pass","purometal");
		root.setAttribute("Host","pop.gmail.com");
		root.setAttribute("Protocol","pop3");
		root.setAttribute("SSL",true);
		root.setAttribute("User","kaiser.zombie");
		root.setAttribute("Pass","voldem6");
		root.setAttribute("Host","mail.codeglide.com");
		root.setAttribute("Protocol","POP3");
		root.setAttribute("SSL",false);
		root.setAttribute("User","hzaccheo");
		root.setAttribute("Pass","purometal!");
*/
/*
		root1.setAttribute("Host","imap.gmail.com");
		root1.setAttribute("Protocol","imap");
		root1.setAttribute("SSL",true);
		root1.setAttribute("User","the.tattered.king");
		root1.setAttribute("Pass","ali baba");
*/
		root2.setAttribute("Host","imap.gmail.com");
		root2.setAttribute("Protocol","imap");
		root2.setAttribute("SSL",true);
		root2.setAttribute("User","hugo.zaccheo");
		root2.setAttribute("Pass","voldem6");

		response.getWriter().println("========================");
		response.getWriter().println(root2);
		response.getWriter().println(root2.nodeToString());
		response.getWriter().println("========================");
		DynamicElement x = new DynamicElement(this.getSession(request).getRootNode().getDocumentNode(),"Folder");
		x.setAttribute("Name","X");
		x.setAttribute("_Container","1");
		DynamicElement y = new DynamicElement(this.getSession(request).getRootNode().getDocumentNode(),"Folder");
		y.setAttribute("Name","Y");
		y.setAttribute("_Container","1");
		DynamicElement z = new DynamicElement(this.getSession(request).getRootNode().getDocumentNode(),"Folder");
		z.setAttribute("Name","Z");
		z.setAttribute("_Container","1");
		x.appendChild(y);
		x.appendChild(z);
		for (FolderElement folder : root2) {
			try {
				if (folder.getName().equals("INBOX") || folder.getName().equalsIgnoreCase("Yo"))
					folder.appendChild(x);
			} catch (Exception e) {
				response.getWriter().println("---" + e.getMessage() + "---");
				e.printStackTrace();				
			}
		}
		response.getWriter().println("========================");
		response.getWriter().println(root2);
		response.getWriter().println(root2.nodeToString());
		response.getWriter().println("========================");
		response.getWriter().println("========================");
		for (FolderElement folder : root2) {
			try {
				if (folder.getName().equals("INBOX")) {
					response.getWriter().println(folder.getName() + ":");
					folder.synchronizeMails();
					for (MailElement mail : folder.getMails())
						response.getWriter().println(mail);
					for (MailElement mail : folder.getMails()) {
						folder.removeChild(mail);
						break;
					}
				}
			} catch (Exception e) {
				response.getWriter().println("---" + e.getMessage() + "---");
				e.printStackTrace();
			}
		}

		for (FolderElement folder : root2) {
			try {
				if (folder.getName().equals("X"))
					folder.getContainer().removeChild(folder);
			} catch (Exception e) {
				response.getWriter().println("---" + e.getMessage() + "---");
				e.printStackTrace();
			}
		}
/*
		FolderElement inbox = null;
		for (FolderElement folder : root2) {
			try {
				if (folder.getName().equals("X"))
					x = folder;
				if (folder.getName().equals("INBOX"))
					inbox = folder;
			} catch (Exception e) {
				response.getWriter().println("---" + e.getMessage() + "---");
				e.printStackTrace();
			}
		}
		try {
			inbox.move((FolderElement)x);
		} catch (Exception e) {
			response.getWriter().println("---" + e.getMessage() + "---");
			e.printStackTrace();
		}
*/
		response.getWriter().println("========================");
		response.getWriter().println(root2);
		response.getWriter().println(root2.nodeToString());
		response.getWriter().println("========================");
		response.getWriter().println("========================");
		for (FolderElement folder : root2) {
			try {
				if (folder.getName().equals("INBOX")) {
					response.getWriter().println(folder.getName() + ":");
					folder.synchronizeMails();
					for (MailElement mail : folder.getMails())
						response.getWriter().println(mail);
				}
			} catch (Exception e) {
				response.getWriter().println("---" + e.getMessage() + "---");
				e.printStackTrace();
			}
		}
		response.getWriter().println("========================");
		
/*
		for (FolderElement folder : root1) {
			try {
				response.getWriter().println(folder.getName() + ":");
				folder.synchronizeMails();
				for (MailElement mail : folder.getMails())
					response.getWriter().println(mail.nodeToString());
			} catch (MessagingException e) {
				e.printStackTrace();
				response.getWriter().println(e.getMessage());
			}
		}
		response.getWriter().println("------------------------");
		response.getWriter().println("Kaiser Zombie:");
		for (FolderElement folder : root2) {
			try {
				response.getWriter().println(folder.getName() + ":");
				folder.synchronizeMails();
				for (MailElement mail : folder.getMails())
					response.getWriter().println(mail.nodeToString());
			} catch (MessagingException e) {
				e.printStackTrace();
				response.getWriter().println(e.getMessage());
			}
		}
		response.getWriter().println("========================");
		response.getWriter().println("Copiando mails");
		for (FolderElement folder : root1) {
			try {
				for (MailElement mail : folder.getMails())
					root2.getChildren().get(0).appendChild(mail);
			} catch (Exception e) {
				e.printStackTrace();
				response.getWriter().println(e.getMessage());
			}
		}
		response.getWriter().println("========================");
		response.getWriter().println("Mails");
		response.getWriter().println("Kaiser Zombie:");
		for (FolderElement folder : root2) {
			response.getWriter().println(folder.getName() + ":");
			for (MailElement mail : folder.getMails())
				response.getWriter().println(mail.nodeToString());
		}
		response.getWriter().println("========================");
*/
	}
}
package com.codeglide.core.rte.engines;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.DOMException;

import com.codeglide.core.Logger;
import com.codeglide.core.ServerSettings;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.contexts.Context;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.exceptions.ServerError;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.session.SessionManager;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.interfaces.xmldb.Transaction;

public abstract class Engine {
	protected Context context;
	protected Response response;
	
	protected HttpServletRequest servletRequest;
	protected HttpServletResponse servletResponse;
	
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.servletRequest = request;
		this.servletResponse = response;
		
		try {
			// Obtain session
			Session session = SessionManager.getSession(getSessionId());
			
			// Call sessionNotFound handler
			if( session == null )
				session = handleSessionNotFound(getApplication());

			if( session != null ) {
				// Initialize
				handleInit(session);
				
				if( session.hasExpired() )
					handleSessionExpired();
				else
					handleRequest();
				
				// Check if there are pending commits
				// Commit any open transactions
				Transaction transaction;
				if( ( transaction = Transaction.getActive()) != null ) {
					Logger.warn("Excution completed with uncommited transactions.");
					transaction.commit();
				}
			}
			
		} catch (Throwable e) {
			handleError(e);
		}
		handleFinalize();
		
		// Send response
		this.response.send(response);
	}
	
	protected void handleInit(Session session) throws Exception {
		
		// Create context if missing
		if( context == null )
			context = new Context(session);
		
		// Add global variables
		context.getVariables().addVariables("_g", session.getGlobalVariables());
		
		Context.setCurrent(context);
	}
	
	protected void handleFinalize() {
		Context.removeCurrent();
	}
	
	protected abstract void handleRequest() throws Exception;
	
	protected abstract void handleError(Throwable e);
	
	protected String[] errorToString( Throwable e ) {
		String message = "", title = "@runtime-error-title", failedOn = null, lang = null;
		if( e instanceof CodeGlideException ) {
			if( e instanceof ExpressionException ) {
				message = "@expression-error," + ((ExpressionException)e).getMessage();
			} else if( e instanceof ServerError ) {
				title = ((ServerError)e).getTitle();
				message = ((ServerError)e).getMessage();
			} else if( e instanceof RuntimeError ) {
				message = ((RuntimeError)e).getMessage();
			}
		} else if( e instanceof NullPointerException )
			message = "@runtime-null";
		else if( e instanceof DOMException )
			message = ((DOMException)e).getMessage();
		else if( e instanceof StackOverflowError )
			message = "@runtime-stackoverflow";
		else {
			title = "Internal Error";
			message = "The CodeGlide Runtime Environmet could not process your request. Please see the server error logs for more details.";
		}
		
		// Check where it failed
		if( message.startsWith("@") ) {//TODO fix this
			StackTraceElement[] stack = e.getStackTrace();
			for( int i = 0 ; i < stack.length ; i++ ) {
				if( stack[i].getClass().isAssignableFrom(Widget.class) ) {
					failedOn = stack[i].getClass().getSimpleName();
					i = -1;
				}
			}
			
			if( failedOn != null )
				message += "," + failedOn;
		}

		if( context != null ) {
			Session session = context.getCurrentSession();
			if( session != null )
				lang = session.getRootNode().getLanguage();
			//FIXME
			{
				lang = null;
			}
			
			if( lang != null && message.startsWith("@") )
				message = session.getRootNode().getApplication().getLanguageEntry(lang, message.substring(1));
			if( lang != null && title.startsWith("@") )
				title = session.getRootNode().getApplication().getLanguageEntry(lang, title.substring(1));
		}
		return new String[] {title, message};
	}
	
	protected abstract Session handleSessionNotFound(Application application) throws Exception;
	
	protected void handleSessionExpired() throws CodeGlideException {
		// Remove the session
		SessionManager.removeSession(context.getCurrentSession().getSessionId());
		setSessionId(null);
	}
	
	private Application getApplication() throws CodeGlideException {
		Application application = ServerSettings.getApplicationByHostname(servletRequest.getServerName());
		if( application == null ) 
			throw new ServerError("Internal Error", "Application does not exist in this server.");
		return application;
	}
	
	protected String getInstanceName(HttpServletRequest request) throws CodeGlideException {
		return ServerSettings.getInstanceName(request.getServerName());
	}
	
	protected abstract String getSessionId();
	
	protected abstract void setSessionId(String sessionId);

}

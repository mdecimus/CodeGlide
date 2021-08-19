package com.codeglide.core.rte.engines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.w3c.dom.Node;

import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.core.rte.commands.ui.ShowAlert;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.BeforeCloseHandler;
import com.codeglide.core.rte.interfaces.ClickHandler;
import com.codeglide.core.rte.interfaces.CloseHandler;
import com.codeglide.core.rte.interfaces.CustomEventHandler;
import com.codeglide.core.rte.interfaces.DropHandler;
import com.codeglide.core.rte.interfaces.GetRecordHandler;
import com.codeglide.core.rte.interfaces.GetTreeHandler;
import com.codeglide.core.rte.interfaces.RenameItemHandler;
import com.codeglide.core.rte.interfaces.SetStringHandler;
import com.codeglide.core.rte.interfaces.UpdateRecordHandler;
import com.codeglide.core.rte.interfaces.UploadHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.widgets.GridPanel;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.interfaces.cookies.CookiesNode;
import com.codeglide.util.json.JsonNode;
import com.codeglide.util.json.JsonValue;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;

public class AjaxRenderer extends GuiEngine {
	protected CookiesNode cookiesNode = null;
	
	protected void handleRequest() throws Exception {
		boolean doInit = false;
		// This is where events are stored
		List<Event> events = new LinkedList<Event>();
		
		response = new JsonResponseRenderer();
		ContextUi context = (ContextUi)this.context;
		List<Action> result = ((GuiResponse)response).getActionsList();
		Application application = context.getApplication();
	
		if( ServletFileUpload.isMultipartContent(servletRequest) ) {
			// Handle multipart request
			ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iter;
			try {
				// Create new event
				Event event = new Event();
				
				iter = upload.getItemIterator(servletRequest);

			    while (iter.hasNext()) {
				    FileItemStream item = iter.next();
				    String fieldName = item.getFieldName();
				    InputStream stream = item.openStream();
				    
				    if (item.isFormField()) {
				    	String fieldData = Streams.asString(stream);
				    	if( fieldName.equals("w") ) {
				    		event.parseComponentId(fieldData);
				    	} else if( fieldName.equals("e") ) {
				    		event.setEvent(fieldData);
				    	}
				    } else {
				    	ReceivedFile file = new ReceivedFile(File.createTempFile("cgweb", ".tmp"));
				    	file.setName(item.getName());
				    	file.setContentType(item.getContentType());
				    	
						InputStream in = item.openStream();
						FileOutputStream fos = new FileOutputStream(file.getFile());

						byte[] buf = new byte[256];
				        int read = 0;
				        int size = 0;
				        
				        while ((read = in.read(buf)) > 0) {
				            fos.write(buf, 0, read);
				            size += read;
				        }
				        //TODO delete uploaded files
				        file.setSize(size);
				        event.setPayload(file);
				    }
				}
			    
			    events.add(event);
			} catch (FileUploadException e) {
				Logger.debug(e);
			}
			
		} else {
			// Handle regular request
			Event event = new Event();
			event.setEvent(servletRequest.getParameter("e"));
			String componentId = servletRequest.getParameter("w");
			if( componentId != null )
				event.parseComponentId(componentId);
			
			// Handle eventGroup requests
			if( event.getEvent() != null && event.getEvent().equals("eventGroup") ) {
				JsonValue[] ajaxEvents = null;
				try {
					ajaxEvents = ((JsonValue[])JsonNode.parse(servletRequest.getParameter("p")).getValue().getValue());
				} catch (Exception e) {
				}
				for( int i = 0; i < ajaxEvents.length; i++ ) {
					Event subEvent = new Event(event);
					for( JsonNode ajaxEvent = (JsonNode)ajaxEvents[i].getValue(); ajaxEvent != null; ajaxEvent = ajaxEvent.getNextNode() ) {
						if( ajaxEvent.getName().equals("e") )
							subEvent.setEvent((String)ajaxEvent.getValue().getValue());
						else if( ajaxEvent.getName().equals("w") )
							subEvent.parseComponentId((String)ajaxEvent.getValue().getValue());
						else if( ajaxEvent.getName().equals("p") )
							subEvent.setPayload(ajaxEvent.getValue().getValue());
					}
					if( subEvent.getEvent().equals("submit") ) {
						Enumeration<String> hdrNames = servletRequest.getParameterNames();
						while( hdrNames.hasMoreElements() ) {
							String hdrName = hdrNames.nextElement();
							if( hdrName.indexOf('.') != -1 ) {
								subEvent = new Event(event);
								subEvent.parseComponentId(hdrName);
								subEvent.setPayload(servletRequest.getParameter(hdrName));
								subEvent.setEvent("set");
								events.add(subEvent);
							}
						}
					} else
						events.add(subEvent);
				}
			} else {
				event.setPayload(servletRequest.getParameter("p"));
				events.add(event);
			}
		}	
		
		try {
			// Process events
			boolean addedVariables = false;
			for( Event event : events ) {
				// Add panel variables
				if( !addedVariables && event.getWindowId() > -1 && event.getPanelId() > -1 ) {
					addPanelVariables(event.getWindowId(), event.getPanelId());
					addedVariables = true;
				}
				
				// Set context
				context.setPanelId(event.getPanelId());
				context.setWindowId(event.getWindowId());
				
				// Process events
				if( event.getEvent().equals("click") ) {
					// Handle click
					((ClickHandler)event.getWidget()).handleClick(context, result, getInputData(event));
				} else if( event.getEvent().equals("getRecords") ) { // TODO rename cg.js to support
					int pageStart = 0, pageLimit = 0;
					String groupBy = null, sortBy = null, filterBy = null;
					boolean sortDesc = false;
					
					// Update display settings with the information sent by the datastore
					for( JsonNode listInfo = JsonNode.parse((String)event.getPayload()); listInfo != null; listInfo = listInfo.getNextNode() ) {
						if( listInfo.getName().equals("groupBy") ) {
							groupBy = (String)listInfo.getValue().getValue();
						} else if( listInfo.getName().equals("sort") ) {
							sortBy = (String)listInfo.getValue().getValue();
						} else if( listInfo.getName().equals("query") ) {
							filterBy = (String)listInfo.getValue().getValue();
						} else if( listInfo.getName().equals("dir") ) {
							sortDesc = !((String)listInfo.getValue().getValue()).equalsIgnoreCase("ASC");
						} else if( listInfo.getName().equals("start") ) {
							if( listInfo.getValue().getType() == JsonValue.T_INTEGER )
								pageStart = (Integer)listInfo.getValue().getValue();
							else
								pageStart = Integer.parseInt((String)listInfo.getValue().getValue());
						} else if( listInfo.getName().equals("limit") ) {
							if( listInfo.getValue().getType() == JsonValue.T_INTEGER )
								pageLimit = (Integer)listInfo.getValue().getValue();
							else
								pageLimit = Integer.parseInt((String)listInfo.getValue().getValue());
						}
					}
					if( sortDesc && sortBy != null )
						sortBy = "@" + sortBy;
					response = new DataStoreRenderer( ((GetRecordHandler)event.getWidget()).getRecords(context, pageStart, pageLimit, sortBy, groupBy, filterBy) );
				} else if( event.getEvent().equals("setRecord") ) {
					boolean doRefresh = false;
					JsonNode payload = JsonNode.parse((String)event.getPayload());
					JsonValue[] items = ((JsonValue[])payload.getValue().getValue());
					for( int i = 0; i < items.length; i++ ) {
						String nodeId = null;
						Record record = new Record();
						
						for( JsonNode change = (JsonNode)items[i].getValue(); change != null; change = change.getNextNode() ) {
							if( change.getName().equals("id") ) {
								if( !((String)change.getValue().getValue()).equals("new") )
									nodeId = (String)change.getValue().getValue();
								
							} else {
								String fieldName = application.getString(Integer.parseInt(change.getName(), 36));
								switch( change.getValue().getType() ) {
									case JsonValue.T_BOOLEAN:
										record.addField(fieldName, ((Boolean)change.getValue().getValue()) ? "1" : "0");
										break;
									case JsonValue.T_FLOAT:
										record.addField(fieldName, Float.toString((Float)change.getValue().getValue()));
										break;
									case JsonValue.T_INTEGER:
										record.addField(fieldName, Integer.toString((Integer)change.getValue().getValue()));
										break;
									case JsonValue.T_STRING:
										record.addField(fieldName, (String)change.getValue().getValue());
										break;
								}
							}
						}

						// Add record
						if( nodeId == null )
							((UpdateRecordHandler)event.getWidget()).addRecord(context, record);
						else
							((UpdateRecordHandler)event.getWidget()).updateRecord(context, nodeId, record);
						doRefresh = true;
					}
					if( doRefresh ) {
						Action action = new Action(ActionType.RELOAD_COMPONENT);
						action.addParameter(ActionParameter.ID, event.getWidget().generateWidgetId(context));
						result.add(action);
					}
				} else if( event.getEvent().equals("getTree") ) {
					// Get node id
					String nodeId = (String)JsonNode.parse((String)event.getPayload()).getValue().getValue();
					
					response = new TreeStoreRenderer( ( nodeId.equals("root") ) ? ((GetTreeHandler)event.getWidget()).getRootChildren(context) :
																	   ((GetTreeHandler)event.getWidget()).getChildren(context, nodeId) );
				} else if( event.getEvent().equals("renameItem") ) {
					// Rename item
					String nodeId = null, nodeValue = null;
					for( JsonNode cmd = JsonNode.parse((String)event.getPayload()) ; cmd != null; cmd = cmd.getNextNode() ) {
						if( cmd.getName().equals("nodeId") )
							nodeId = (String)cmd.getValue().getValue();
						else if( cmd.getName().equals("value") )
							nodeValue = (String)cmd.getValue().getValue();
					}
					((RenameItemHandler)event.getWidget()).handleRenameItem(context, nodeId, nodeValue);
				} else if( event.getEvent().equals("set") ) {
					// Set string
					String input = null;
					if(event.getPayload() != null && event.getPayload() instanceof Boolean )
						input = ((Boolean)event.getPayload()) ? "1" : "0";
					else
						input = (String)event.getPayload();
					((SetStringHandler)event.getWidget()).handleSet(context, result, event.getItem(), input);
				} else if( event.getEvent().equals("message") ) {
					// Message box response
					String buttonName = null, inputString = null;
					for( JsonNode input = JsonNode.parse((String)event.getPayload()); input != null; input = input.getNextNode() ) {
						if( input.getName().equals("btn") )
							buttonName = (String)input.getValue().getValue();
						else if( input.getName().equals("input") )
							inputString = (String)input.getValue().getValue();
					}
					((CustomEventHandler)event.getWidget()).handleEvent(context, result, "on" + buttonName, inputString);
				} else if( event.getEvent().equals("fileUpload")) {
					String message = "<textarea>{success: true, message: \"OK\"}</textarea>";
					try {
						((UploadHandler)event.getWidget()).handleUpload(context, result, (ReceivedFile)event.getPayload());
					} catch( Exception e ) {
						message = "<textarea>{success: false, message: \"ERROR\"}</textarea>";
					}
					response = new HtmlRenderer(message);
				} else if( event.getEvent().equals("uploadImage") ) {
					// Handle an image upload request
					String imageUrl = "";
					try {
						imageUrl = ((UploadHandler)event.getWidget()).handleUpload(context, result, (ReceivedFile)event.getPayload());
					} catch( Exception e ) {
						Logger.debug(e);
					}
					response = new HtmlRenderer("<textarea>{imgUrl: '" + imageUrl  + "'}</textarea>");
				} else if( event.getEvent().equals("close") ) {
					((CloseHandler)event.getWidget()).handleClose(context, result);
				} else if( event.getEvent().equals("beforeclose") ) {
					((BeforeCloseHandler)event.getWidget()).handleBeforeClose(context, result);
				} else if( event.getEvent().equals("cellclick") ) {
					// Handle click
					((ClickHandler)event.getWidget()).handleClick(context, result, getInputData(event));
				} else if( event.getEvent().equals("celldblclick") ) {
					// Handle click
					((ClickHandler)event.getWidget()).handleDoubleClick(context, result, getInputData(event));
				} else if( event.getEvent().equals("onDrop") ) {
					if( event.getPayload() != null ) {
						DynamicElement targetNode = null;
						LinkedList<Node> sourceNodes = new LinkedList<Node>();
						JsonNode payload = null;
						if( event.getPayload() instanceof JsonNode )
							payload = (JsonNode)event.getPayload();
						else
							payload = JsonNode.parse((String)event.getPayload());
						for( JsonNode runner = payload; runner != null; runner = runner.getNextNode() ) {
							if( runner.getName().equals("parentId") ) {
								String value = (String)runner.getValue().getValue();
								if( !value.equals("root") )
									targetNode = getElementById(value);
							} else if( runner.getName().equals("childId") ) {
								JsonValue[] items = ((JsonValue[])runner.getValue().getValue());
								for( int i = 0; i < items.length; i++ ) {
									sourceNodes.add(getElementById((String)items[i].getValue()));
								}
							}
						}
						((DropHandler)event.getWidget()).handleDrop(context, result, targetNode, new DummyNodeList(sourceNodes));
					}
				} else if( event.getEvent().equals("init") ) {
					doInit = true;
					result.add(new Action(ActionType.INIT));
				} else
					Logger.warn("Unknown event '" + event.getEvent() + "'.");
			}
			
			// If we already have a renderer, exit.
			if( response instanceof JsonResponseRenderer ) {
				// Generate CSS request
				Session session = context.getCurrentSession();
				if( session.getWindowManager().hasNewStylesheet() ) {
					StringBuffer css = new StringBuffer();
					for( String cssName : session.getWindowManager().getStyleSheets() ) {
						css.append(".");
						css.append(cssName);
						if( cssName.startsWith("cg-gr") )
							css.append(" .x-grid3-cell-inner {");
						else
							css.append(" {");
						css.append(session.getWindowManager().getStyleSheet(cssName));
						css.append("} \\n");
					}
					Action addCss = new Action(ActionType.ADDCSS);
					addCss.addParameter(ActionParameter.ID, "cgcss");
					addCss.addParameter(ActionParameter.VALUE, css.toString());
					result.add(addCss);
				}

				// Call terminalInit event if needed
				if( events.size() < 1 || doInit ) {
					context.setWindowId(0);
					handleTerminalInit();
				} else
					handleTerminalRequest();
			}
			
		} catch (Throwable e) {
			handleError(e);
		}
	}
	
	protected void handleInit(Session session) throws Exception {
		super.handleInit(session);
		
		// Feed the cookies
		cookiesNode = (CookiesNode)session.getRootNode().getChildNode("Cookies");
		if( cookiesNode != null )
			cookiesNode.getCookies(servletRequest);
	}
	
	protected void handleFinalize() {
		super.handleFinalize();
		// Set cookies
		if( cookiesNode != null )
			cookiesNode.setCookies(servletResponse);
	}
	
	protected String getSessionId() {
		// Obtain session ID
		String sessionId = null;
		Cookie[] cookies = servletRequest.getCookies();
		if( cookies != null && cookies.length > 0 ) {
			for( int i = 0; i < cookies.length; i++ ) {
				if( cookies[i].getName().equals("ID") )
					sessionId = cookies[i].getValue();
			}
		}
		return sessionId;
	}
	
	protected void setSessionId(String sessionId) {
		Cookie cookie = new Cookie("ID", (sessionId!=null)?sessionId:"");
		if( sessionId == null )
			cookie.setMaxAge(0);
		cookie.setPath("/");
		//cookie.setDomain(hostName);
		servletResponse.addCookie(cookie);
	}
	
	// ID duplication controls
	private HashSet<String> usedIds = new HashSet<String>();
	private int idSequencer = 0;
	
	protected JsonValue generateJsonResponse(Vector<JsonValue> commands, Vector<JsonValue> handlers, List<Action> actions) {
		Application application = context.getApplication();
		Vector<JsonValue> result = new Vector<JsonValue>();
		for( Action action : actions ) {
			JsonNode node = null;
			
			// Process widget actions
			switch( action.getType() ) {
				case INIT:
					JsonNode params = new JsonNode("id","_top");
					params.appendNext("layout", "fit");
					node = new JsonNode("cmd","init");
					node.appendNext("cls", "viewport");
					node.appendNext("cfg", params);
					commands.add(new JsonValue(node));
					break;
				case SET_SESSION:
					setSessionId(action.getParameter(ActionParameter.ID));
					continue;
				case REPLACE_COMPONENT:
				case ADD_COMPONENT:
					if( action.getType() == ActionType.REPLACE_COMPONENT ) {
						node = new JsonNode("cmd","remove");
						node.appendNext("cmps", new JsonValue[] { new JsonValue(action.getParameter(ActionParameter.ID)) } );
						commands.add(0, new JsonValue(node));
					}
					node = new JsonNode("cmd","add");
					commands.add(new JsonValue(node));
					node.appendNext("target", ( action.getType() == ActionType.REPLACE_COMPONENT ) ? ("R"+action.getParameter(ActionParameter.ID)) : action.getParameter(ActionParameter.TARGET));
					//JsonNode cfg = new JsonNode();
					Action childAction = action.getChildren().get(0);
					//String panelType = childAction.getParameter(ActionParameter.TYPE);
					childAction.removeParameter(ActionParameter.TYPE);
					//childAction.removeParameter(ActionParameter.ID);
					JsonNode cfg = ((JsonNode)((JsonValue[])generateJsonResponse(commands, handlers, action.getChildren()).getValue())[0].getValue());
					JsonNode cmps = new JsonNode("cls", getPanelName(childAction, childAction.getType()));
					cmps.appendNext("cfg", cfg);
					node.appendNext("cmps", cmps);
					break;
				case REMOVE_COMPONENT:
					node = new JsonNode("cmd","remove");
					node.appendNext("cmps", new JsonValue[] { new JsonValue(action.getParameter(ActionParameter.ID)) } );
					commands.add(0, new JsonValue(node));
					break;
				case CLEAR_PANEL:
					node = new JsonNode("cmd","clear");
					node.appendNext("cnts", action.getParameter(ActionParameter.ID));
					commands.add(new JsonValue(node));
					break;
				case RELOAD_COMPONENT:
					node = new JsonNode("cmd","refreshcmp");
					node.appendNext("id", action.getParameter(ActionParameter.ID));
					commands.add(new JsonValue(node));
					break;
				case OPEN_WINDOW:
					node = new JsonNode("cmd","addwindow");
					commands.add(new JsonValue(node));
					childAction = action.getChildren().get(0);
					String windowId = childAction.getParameter(ActionParameter.ID);
					childAction.removeParameter(ActionParameter.ID);
					//childAction.removeParameter(ActionParameter.PANEL_ID);
					cfg = new JsonNode("id", windowId);
					cfg.appendNext("closable", childAction.hasFlag(ActionFlag.CLOSABLE));
					cfg.appendNext("draggable", childAction.hasFlag(ActionFlag.DRAGGABLE));
					cfg.appendNext("collapsible", childAction.hasFlag(ActionFlag.COLLAPSIBLE));
					cfg.appendNext("plain", true);
					cfg.appendNext((JsonNode)((JsonValue[])generateJsonResponse(commands, handlers, action.getChildren()).getValue())[0].getValue());
					node.appendNext("cfg", cfg);
					if( childAction.hasFlag(ActionFlag.BEFORECLOSABE) ) {
						JsonNode eventHdlr = new JsonNode("id", windowId);
						handlers.add(new JsonValue(eventHdlr));
						//node = new JsonNode("cmd","sethandler");
						//node.appendNext("cmps", new JsonValue[] {new JsonValue(eventHdlr)});
						//commands.add(new JsonValue(node));
						eventHdlr.appendNext("handlers", new JsonValue[] {new JsonValue(createEventHandlerNode("defaultHdlr", "beforeclose", "beforeclose", windowId, null, null, new JsonNode("bubbling", false)))} );
					} else if( childAction.hasFlag(ActionFlag.CLOSABLE) ) {
						JsonNode eventHdlr = new JsonNode("id", windowId);
						//node = new JsonNode("cmd","sethandler");
						//node.appendNext("cmps", new JsonValue[] {new JsonValue(eventHdlr)});
						//commands.add(new JsonValue(node));
						handlers.add(new JsonValue(eventHdlr));
						eventHdlr.appendNext("handlers", new JsonValue[] {new JsonValue(createEventHandlerNode("defaultHdlr", "close", "close", windowId, null, null, null))} );
					}
					break;
				case CLOSE_WINDOW:
					node = new JsonNode("cmd","removewindow");
					node.appendNext("id", action.getParameter(ActionParameter.ID));
					commands.add(new JsonValue(node));
					break;
				case SHOW_ALERT:
					node = new JsonNode("cmd","showmsg");
					commands.add(new JsonValue(node));
					node.appendNext("type", "msg");
					cfg = new JsonNode("title", action.getParameter(ActionParameter.TITLE));
					cfg.appendNext("msg", action.getParameter(ActionParameter.MESSAGE));
					if( action.getParameter(ActionParameter.TYPE) != null )
						cfg.appendNext("icon", action.getParameter(ActionParameter.TYPE));
					cfg.appendNext("buttons", "ok");
					node.appendNext("cfg", cfg);
					break;
				case SHOW_MESSAGE:
					node = new JsonNode("cmd","showmsg");
					commands.add(new JsonValue(node));
					node.appendNext("id", action.getParameter(ActionParameter.ID));
					node.appendNext("type", action.getParameter(ActionParameter.TYPE));
					cfg = new JsonNode("title", action.getParameter(ActionParameter.TITLE));
					cfg.appendNext("msg", action.getParameter(ActionParameter.MESSAGE));
					if( action.getParameter(ActionParameter.ICON) != null )
						cfg.appendNext("icon", action.getParameter(ActionParameter.ICON));
					if( action.getParameter(ActionParameter.BUTTONS) != null )
						cfg.appendNext("buttons", action.getParameter(ActionParameter.BUTTONS));
					node.appendNext("cfg", cfg);
					break;
				case ADDCSS:
					node = new JsonNode("cmd","addcss");
					node.appendNext("id", action.getParameter(ActionParameter.ID));
					node.appendNext("css", action.getParameter(ActionParameter.VALUE));
					commands.add(new JsonValue(node));
					break;
				case ITEM_RELOAD:
					node = new JsonNode("cmd", "refreshtreenode");
					node.appendNext("treeId", action.getParameter(ActionParameter.TARGET));
					node.appendNext("nodeId", (action.getParameter(ActionParameter.ID)!=null)?action.getParameter(ActionParameter.ID):"root");
					commands.add(new JsonValue(node));
					break;
				case ITEM_REMOVE:
					node = new JsonNode("cmd", "removetreenode");
					node.appendNext("treeId", action.getParameter(ActionParameter.TARGET));
					node.appendNext("nodeId", (action.getParameter(ActionParameter.ID)!=null)?action.getParameter(ActionParameter.ID):"root");
					commands.add(new JsonValue(node));
					break;
				case ITEM_RENAME:
					node = new JsonNode("cmd", "edittreenode");
					node.appendNext("treeId", action.getParameter(ActionParameter.TARGET));
					node.appendNext("nodeId", (action.getParameter(ActionParameter.ID)!=null)?action.getParameter(ActionParameter.ID):"root");
					commands.add(new JsonValue(node));
					break;
			}
			
			// We have a UI command, let's add it and skip widget processing
			if( node != null ) 
				continue;
			
			switch( action.getType() ) {
			case FORM:
			case TABPANEL:
			case FIELDSET:
			case PANEL:
			case WINDOW:
			case ACCORDIONPANEL:
			case COLUMNPANEL:
			case REGIONPANEL:
				{
					node = addPanelParameters(action);
					ActionType actionType = action.getType();
					
					boolean isRegion = action.getParameter(ActionParameter.REGION) != null;

					// Set proper layout, default is fit
					if( actionType == ActionType.FORM && action.isAncestorType(ActionType.FORM) ) {
						node.appendNext("layout","form");  // HTML doesn't allow to have forms within forms
						actionType = ActionType.PANEL;
					} else if( actionType == ActionType.ACCORDIONPANEL )
						node.appendNext("layout","accordion");
					else if( actionType == ActionType.COLUMNPANEL )
						node.appendNext("layout","column");
					else if( actionType == ActionType.REGIONPANEL )
						node.appendNext("layout","border");
					else if( actionType == ActionType.FIELDSET ) {
						Action childAction = action.getChildren().get(0);
						if( childAction != null && childAction.getType() == ActionType.FORM )
							node.appendNext("layout","column");
						else
							node.appendNext("layout","form");
						node.appendNext("collapsible", action.hasFlag(ActionFlag.COLLAPSIBLE));
						node.appendNext("autoHeight", true);
					}
					//else if( actionType != ActionType.TABPANEL && actionType != ActionType.FORM && actionType != ActionType.HTMLPANEL && actionType != ActionType.WINDOW )
					//	node.appendNext("layout","fit");
					
					if( action.hasFlag(ActionFlag.LABELSONTOP) )
						node.appendNext("labelAlign","top");
					
					// Set panel class name
					String panelClass = null;
					if( !action.isParentType(ActionType.ADD_COMPONENT) && actionType != ActionType.WINDOW && !isRegion )
						node.appendNext("xtype", panelClass = getPanelName(action, actionType));
						
					if( actionType == ActionType.FORM ) {
						node.appendNext("url","/AjaxEngine/");
					} else if( actionType == ActionType.TABPANEL) {
						node.appendNext("activeTab", 0);
						node.appendNext("deferredRender", false);
						node.appendNext("enableTabScroll", true);
						if( panelClass.equals("cg.tabpanel") )
							node.appendNext("layoutOnTabChange", true);
					} /*else if( actionType == ActionType.HTMLPANEL) {
						node.appendNext("html", action.getParameter(ActionParameter.VALUE));
					}*/
					
					// Add hack to force render on tab panels with richtext components
					if( action.isParentType(ActionType.CONTENTS) && action.getParent().getParent().getType() == ActionType.TABPANEL && action.hasChildType(ActionType.RICHTEXT) )
						node.appendNext("activeTabRender", true);
					
					// Force baseCls when rendering window sub-panels
					if( (action.isAncestorType(ActionType.WINDOW ) || action.isAncestorType(ActionType.RELOAD_COMPONENT)) && actionType != ActionType.FIELDSET )
						node.appendNext("baseCls", "x-windows-body");
					
					// Set borders
					if( actionType != ActionType.WINDOW ) {
						if( action.hasFlag(ActionFlag.BORDER ) ) {
							node.appendNext("border", true);
							if( !isRegion )
								node.appendNext("frame", true);
						} else
							node.appendNext("border", false);
					} else
						node.appendNext("autoScroll", true);
					
					// Add accordion panel options
					if( actionType == ActionType.ACCORDIONPANEL )
						node.appendNext("layoutConfig", new JsonNode("animate", true));
					
					for( Action childAction : action.getChildren() ) {
						switch(childAction.getType()) {
							case CONTENTS:
								if( /*action.getType() != ActionType.HTMLPANEL &&*/ childAction.getChildren() != null )
									node.appendNext("items", generateJsonResponse(commands, handlers, childAction.getChildren()));
								break;
							case BUTTONS:
							case BOTTOMBAR:
							case TOPBAR:
								String type = null;
								if( childAction.getType() == ActionType.TOPBAR )
									type = "tbar";
								else if( childAction.getType() == ActionType.BOTTOMBAR )
									type = "bbar";
								else
									type = "buttons";
								if( childAction.getChildren() != null )
									node.appendNext(type, generateJsonResponse(commands, handlers, childAction.getChildren()));
								break;
						}
					}
					
					if( action.hasFlag(ActionFlag.RELOADABLE) ) {
						JsonNode reloadPanel = new JsonNode("xtype", "panel");
						reloadPanel.appendNext("id", "R" + action.getParameter(ActionParameter.ID));
						reloadPanel.appendNext("layout", "fit");
						if( action.isAncestorType(ActionType.WINDOW ))
							reloadPanel.appendNext("baseCls", "x-windows-body");
						reloadPanel.appendNext("border", false);
						reloadPanel.appendNext("items", new JsonValue[] {new JsonValue(node)});
						node = reloadPanel;
					}
					
				}
					break;
				case MENU:
				{
					String buttonName = action.getParameter(ActionParameter.NAME);
					String buttonIcon = action.getParameter(ActionParameter.ICON);
					node = addParameter(null, "text", buttonName);
					node = addParameter(node, "icon", buttonIcon);

					if( buttonIcon != null && buttonName != null )
						node.appendNext("cls","x-btn-text-icon");
					else if( buttonIcon != null )
						node.appendNext("cls","x-btn-icon");
					else
						node.appendNext("cls","x-btn-text");
					node.appendNext("menu", new JsonNode("items", generateJsonResponse(commands, handlers, action.getChildren())));
				}
					break;
				case BUTTON:
				{
					String buttonId = action.getParameter(ActionParameter.ID);
					if( usedIds.contains(buttonId) ) {
						buttonId += "." + Integer.toString(idSequencer++, 36);
					} else
						usedIds.add(buttonId);
					JsonNode eventHdlr = new JsonNode("id", buttonId);
					handlers.add(new JsonValue(eventHdlr));
					Vector<JsonValue> buttonHandlers = new Vector<JsonValue>();
					boolean addDefaultHandler = false;
					if( action.getChildren().size() > 0 ) {
						for( Action buttonAction : action.getChildren() ) {
							switch( buttonAction.getType() ) {
								case FORM_SUBMIT:
									buttonHandlers.add(new JsonValue(createEventHandlerNode("submitForm", "click", "click", buttonId, "formId", buttonAction.getParameter(ActionParameter.TARGET), null)));
									break;
								case FORM_VALIDATE:
									buttonHandlers.add(new JsonValue(createEventHandlerNode("validateForm", "click", "click", buttonId, "formId", buttonAction.getParameter(ActionParameter.TARGET), null)));
									addDefaultHandler = true;
									break;
								case SHOW_FILE_DIALOG:
									JsonNode uploadCfg = new JsonNode("id", buttonAction.getParameter(ActionParameter.ID));
									uploadCfg.appendNext("url", "/AjaxEngine/");
									uploadCfg.appendNext("reset_on_hide", true);
									uploadCfg = addIntegerParameter(uploadCfg, "width", buttonAction.getParameter(ActionParameter.WIDTH));
									uploadCfg = addIntegerParameter(uploadCfg, "height", buttonAction.getParameter(ActionParameter.HEIGHT));
									//uploadCfg.appendNext(createJsonFields(buttonAction));
									buttonHandlers.add(new JsonValue(createEventHandlerNode("showUploadDialog", "click", "click", buttonId, null, null, new JsonNode("cfg", uploadCfg))));
									break;
								case ITEM_ADD:
									buttonHandlers.add(new JsonValue(createEventHandlerNode("gridInlineAdd", "click", "click", buttonId, "gridId", buttonAction.getParameter(ActionParameter.ID), null)));
									break;
								case COMPONENT_COMMIT:
									buttonHandlers.add(new JsonValue(createEventHandlerNode("submitGridChanges", "click", "set", buttonAction.getParameter(ActionParameter.ID), "gridId", buttonAction.getParameter(ActionParameter.ID), null)));
									break;
							}
							
						}
					} else if( action.getParameter(ActionParameter.INPUTLIST) != null ) {
						String eventName = null, listType = null;
	
						Widget targetWidget = (Widget)application.getWidgetBucket().getObject(Integer.parseInt(action.getParameter(ActionParameter.INPUTLIST).split("\\.")[2], 36));

						if( targetWidget instanceof ShowAlert ) {
							eventName = "submitViewSelections";
							listType = "viewId";
						} else if( targetWidget instanceof GridPanel ) {
							eventName = "submitGridSelections";
							listType = "gridId";
						} else {
							eventName = "submitTreeSelections";
							listType = "treeId";
						}
						JsonNode extraScope = null;
						if( action.getParameter(ActionParameter.PAYLOAD) != null ) 
							extraScope = new JsonNode("payload", new JsonNode("objs", new JsonValue[] {new JsonValue(action.getParameter(ActionParameter.PAYLOAD))} ));

						buttonHandlers.add(new JsonValue(createEventHandlerNode(eventName, "click", "click", buttonId, listType, action.getParameter(ActionParameter.INPUTLIST), extraScope)));

					} else 
						addDefaultHandler = true;
					
					if( addDefaultHandler ){
						String handlerName = "submitChanges";
						JsonNode extraScope = null;
						if( action.getParameter(ActionParameter.PAYLOAD) != null ) {
							handlerName = "defaultHdlr";
							extraScope = new JsonNode("payload", new JsonNode("objs", new JsonValue[] {new JsonValue(action.getParameter(ActionParameter.PAYLOAD))} ));
						}
						buttonHandlers.add(new JsonValue(createEventHandlerNode(handlerName, "click", "click", buttonId, null, null, extraScope)));
					}
					
					eventHdlr.appendNext("handlers", (JsonValue[]) buttonHandlers.toArray(new JsonValue[buttonHandlers.size()]));
					
					// Add node
					node = new JsonNode("id", buttonId);
					//node.appendNext(createJsonFields(action));
					String buttonName = action.getParameter(ActionParameter.NAME);
					if( buttonName != null )
						node.appendNext("text", buttonName);
					node = addParameter(node, "tooltip", action.getParameter(ActionParameter.TOOLTIP));
					node = addParameter(node, "group", action.getParameter(ActionParameter.GROUP));
					ActionType parentType = action.getParent().getType();
					if( parentType != ActionType.BUTTONS && parentType != ActionType.BOTTOMBAR && parentType != ActionType.TOPBAR && parentType != ActionType.MENU )
						node.appendNext("xtype", "button");
					else if( parentType == ActionType.MENU || parentType == ActionType.TOPBAR || parentType == ActionType.BOTTOMBAR ) {
						String buttonIcon = action.getParameter(ActionParameter.ICON);
						if( buttonIcon != null && buttonName != null )
							node.appendNext("cls","x-btn-text-icon");
						else if( buttonIcon != null )
							node.appendNext("cls","x-btn-icon");
						else
							node.appendNext("cls","x-btn-text");
						if( buttonIcon != null )
							node.appendNext("icon", buttonIcon);
					}
					
					if( action.hasFlag(ActionFlag.CHECKED) )
						node.appendNext("checked", true);
				}
					break;
				case TOGGLER:
				{
					node = new JsonNode("xtype", "cg.gridToggler");
					JsonNode map = new JsonNode("on", action.getParameter(ActionParameter.ON));
					map.appendNext("off", action.getParameter(ActionParameter.OFF));
					node.appendNext("imgMap", map);
					//node.appendNext(createJsonFields(action));
				}
					break;
				case DATEFIELD:
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					if( action.getParameter(ActionParameter.NAME) != null )
						node.appendNext("fieldLabel", action.getParameter(ActionParameter.NAME));
					else
						node.appendNext("labelSeparator", "");
					node.appendNext("xtype", (action.hasFlag(ActionFlag.DATETIME))?"cg.datetimefield":"datefield");
					if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
						createSaveFieldHandler(handlers, action.getParameter(ActionParameter.ID));
					break;
				case LABELFIELD:
				{
					node = new JsonNode("xtype", "cg.labelfield");
					node.appendNext(addFieldParameters(action));
					String labelName = action.getParameter(ActionParameter.NAME);
					if( labelName != null && !labelName.isEmpty() )
						node.appendNext("fieldLabel", labelName );
					else
						node.appendNext("labelSeparator", "");
				}
					break;
				case LABEL:
				{
					node = new JsonNode("xtype", "cg.labelfield");
					node.appendNext("value", action.getParameter(ActionParameter.NAME) );
					node.appendNext("labelSeparator", "");
				}
					break;
				case IMAGESELECTOR:
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					node.appendNext("xtype", "cg.imageselector");
					break;
				case COLORPICKER:
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					if( action.getParameter(ActionParameter.NAME) != null )
						node.appendNext("fieldLabel", action.getParameter(ActionParameter.NAME));
					else
						node.appendNext("labelSeparator", "");
					node.appendNext("xtype", "cg.colorpicker");
					if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
						createSaveFieldHandler(handlers, action.getParameter(ActionParameter.ID));
					break;
				case TEXTFIELD:
				{
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					String labelName = action.getParameter(ActionParameter.NAME);
					if( labelName != null && !labelName.isEmpty() )
						node.appendNext("fieldLabel", labelName);
					else
						node.appendNext("labelSeparator", "");
					if( action.hasFlag(ActionFlag.PASSWORDFIELD))
						node.appendNext("inputType", "password");
					node.appendNext("xtype", "textfield");
					if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
						createSaveFieldHandler(handlers, action.getParameter(ActionParameter.ID));
				}
					break;
				case RICHTEXT:
				{
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					String labelName = action.getParameter(ActionParameter.NAME);
					if( labelName != null && !labelName.isEmpty() )
						node.appendNext("fieldLabel", labelName );
					else
						node.appendNext("labelSeparator", "");
					node.appendNext("xtype", "cg.richtexteditor");
					if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
						createSaveFieldHandler(handlers, action.getParameter(ActionParameter.ID));
				}
					break;
				case CHECKBOX:
				{
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					node.appendNext("boxLabel", action.getParameter(ActionParameter.NAME));
					node.appendNext("xtype", "checkbox");
					node.appendNext("labelSeparator", "");
					node.appendNext("inputValue", "1");
					String v = action.getParameter(ActionParameter.VALUE);
					if( v != null && (v.equals("1") || v.equalsIgnoreCase("true")) )
						node.appendNext("checked", true);
					if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
						createSaveFieldHandler(handlers, action.getParameter(ActionParameter.ID));
				}
					break;
				case SEPARATOR:
					result.add(new JsonValue(action.getParameter(ActionParameter.VALUE)));
					break;
				case GRID:
				{
					// Set onClick handler, if any.
					if( action.hasFlag(ActionFlag.CLICKABLE) || action.hasFlag(ActionFlag.DOUBLECLICKABLE)) {
						JsonNode eventHdlr = new JsonNode("id", action.getParameter(ActionParameter.ID));
						//node = new JsonNode("cmd","sethandler");
						//node.appendNext("cmps", new JsonValue[] {new JsonValue(eventHdlr)});
						//commands.add(new JsonValue(node));
						handlers.add(new JsonValue(eventHdlr));
						eventHdlr.appendNext("handlers", new JsonValue[] {new JsonValue(createEventHandlerNode("gridCellClick", (action.hasFlag(ActionFlag.CLICKABLE)) ? "cellclick" : "celldblclick", (action.hasFlag(ActionFlag.CLICKABLE)) ? "cellclick" : "celldblclick", action.getParameter(ActionParameter.ID), null, null, null))} );
					}
					node = new JsonNode("xtype", (action.hasFlag(ActionFlag.EDITABLE))?"cg.editorgridpanel":"cg.gridpanel");
					//node.appendNext(createJsonFields(action));
					node.appendNext(addPanelParameters(action));
					node.appendNext(createStore(action));
					String selectionModel = "checkbox"; //action.hasFlag(ActionFlag.MULTIPLESELECT) ? "checkbox" : "single";
					node.appendNext("sm", new JsonNode("smType", selectionModel ));
					if( action.getParameter(ActionParameter.DDGROUP) != null ) {
						node.appendNext("enableDragDrop", true);
						node.appendNext("ddGroup", action.getParameter(ActionParameter.DDGROUP));
					}
					if( action.getParameter(ActionParameter.PAGESIZE) != null ) {
						if( action.getParameter(ActionParameter.PAGESIZE) != null ) {
							node.appendNext("enablePaging", true);
							node.appendNext("pagingTbCfg", addIntegerParameter(null, "pageSize", action.getParameter(ActionParameter.PAGESIZE)));
						}
					}
					node.appendNext("border", action.hasFlag(ActionFlag.BORDER));
					node.appendNext("autoWidth", true);
					node.appendNext("monitorResize", true);
					JsonNode vc = new JsonNode("autoFill", true);
					vc.appendNext("forceFit", true);
					if( action.getParameter(ActionParameter.GROUPBY) != null ) {
						vc.appendNext("viewType", "groupingView");
						vc.appendNext("hideGroupedColumn", action.hasFlag(ActionFlag.GROUPHIDDEN) );
						vc.appendNext("startCollapsed", action.hasFlag(ActionFlag.GROUPCOLLAPSED) );
					} else
						vc.appendNext("viewType", "defaultView");
					
					node.appendNext("viewConfig", vc);

					// Create column model
					Vector<JsonValue> rd = new Vector<JsonValue>();
					JsonNode defaults = null;
					for( Record field : action.getRecords() ) {
						JsonNode item = new JsonNode("id",field.getStringField("id"));
						item.appendNext("header", field.getStringField("name"));
						item.appendNext("dataIndex", field.getStringField("id"));
						if( field.getStringField("icon") != null )
							item.appendNext("stylizedHeader", "<img src=\"" + field.getStringField("icon") + "\"/>");
						if( field.getBooleanField("autosave") )
							item.appendNext("autosave", true );
						String width = field.getStringField("width");
						if( width == null )
							width = "20";
						item.appendNext("width", Integer.parseInt(width));
						item.appendNext("sortable", true);
						if( field.getStringField("filter") != null ) {
							if( field.getStringField("filterparam") != null ) {
								JsonNode param = new JsonNode("func", field.getStringField("filter"));
								param.appendNext("param",field.getStringField("filterparam"));
								item.appendNext("renderer", param );
							} else if( field.getStringField("filter").equals("imagemap") ) {
								JsonNode param = new JsonNode("func", "imageMap" );
								Vector<JsonValue> mappings = new Vector<JsonValue>();
								for( Record map : field.getRecordListField("imagemap") ) {
									JsonNode mapItem = new JsonNode("val", map.getStringField("_val"));
									mapItem.appendNext("img",map.getStringField("_img"));
									mappings.add(new JsonValue(mapItem));
								}
								param.appendNext("param", (JsonValue[]) mappings.toArray(new JsonValue[mappings.size()]));
								item.appendNext("renderer", param );
							} else
								item.appendNext("renderer", field.getStringField("filter") );
						}
						if( field.getStringField("summary") != null )
							item.appendNext("summaryType", field.getStringField("summary") );

						// Render editor widget
						int editorId = field.getIntegerField("editor");
						if( editorId > -1 ) {
							try {
								List<Action> editorActions = new LinkedList<Action>();
								((Widget)application.getWidgetBucket().getObject(editorId)).run(context, editorActions);
								item.appendNext("editor", ((JsonValue[])generateJsonResponse(commands, handlers, editorActions).getValue())[0] );
							} catch (CodeGlideException _) {
							}
						}
						
						//TODO format default values if they are float, bool
						if( field.getStringField("default") != null ) {
							JsonNode def = new JsonNode(field.getStringField("id"), field.getStringField("default"));
							if( defaults != null )
								defaults.appendNext(def);
							else
								defaults = def;
						}
						
						rd.add(new JsonValue(item));
					}
					node.appendNext("cm", (JsonValue[]) rd.toArray(new JsonValue[rd.size()]));

					if( defaults != null )
						node.appendNext("defaultRowValues", defaults);

					for( Action childAction : action.getChildren() ) {
						switch(childAction.getType()) {
						case BUTTONS:
						case BOTTOMBAR:
						case TOPBAR:
							String type;
							if( childAction.getType() == ActionType.TOPBAR )
								type = "tbar";
							else if( childAction.getType() == ActionType.BOTTOMBAR )
								type = "bbar";
							else
								type = "buttons";
							if( childAction.getChildren() != null )
								node.appendNext(type, generateJsonResponse(commands, handlers, childAction.getChildren()));
							break;
						}
					}
				}					
					break;
				case TREE:
				{
					// Set onClick handler, if any.
					if( action.hasFlag(ActionFlag.CLICKABLE) ) {
						JsonNode eventHdlr = new JsonNode("id", action.getParameter(ActionParameter.ID));
						//node = new JsonNode("cmd","sethandler");
						//node.appendNext("cmps", new JsonValue[] {new JsonValue(eventHdlr)});
						//commands.add(new JsonValue(node));
						handlers.add(new JsonValue(eventHdlr));
						Vector<JsonValue> hdlrs = new Vector<JsonValue>();
						//if( action.getParameter("_as") != null )
						//	hdlrs.add(new JsonValue(createEventHandlerNode("submitChanges", "click", "click", action.getParameter(ActionParameter.ID), null, null, null)));
						hdlrs.add(new JsonValue(createEventHandlerNode("treeNodeClick", "click", "click", action.getParameter(ActionParameter.ID), null, null, null)));
						eventHdlr.appendNext("handlers", (JsonValue[]) hdlrs.toArray(new JsonValue[hdlrs.size()]) );
					}

					node = new JsonNode("xtype", "cg.treepanel");
					//node.appendNext(createJsonFields(action));
					node.appendNext(addPanelParameters(action));
					node.appendNext("editable", action.hasFlag(ActionFlag.EDITABLE));
					node.appendNext("autoScroll", true);
					node.appendNext("animate", true);
					node.appendNext("containerScroll", true);
					node.appendNext("rootVisible", false);
					//node.appendNext("baseCls", "x-plain");
					node.appendNext("border", action.hasFlag(ActionFlag.BORDER));
					node.appendNext("loader", new JsonNode("dataUrl", "/AjaxEngine/"));
					if( action.getParameter(ActionParameter.DDGROUP) != null ) {
						node.appendNext("enableDD", true);
						node.appendNext("ddGroup", action.getParameter(ActionParameter.DDGROUP));
					}
					JsonNode root = new JsonNode("text", "CG");
					root.appendNext("id", "root");
					root.appendNext("draggable", false);
					root.appendNext("text", "CG");
					node.appendNext("root", root);
					for( Action childAction : action.getChildren() ) {
						switch(childAction.getType()) {
							case MENU:
								if( childAction.getChildren() != null )
									node.appendNext("ctxMenu", new JsonNode("items", generateJsonResponse(commands, handlers, childAction.getChildren())));
								break;
						}
					}
				}
					break;
				case RADIOGROUP:
				{
					String radioId = action.getParameter(ActionParameter.ID);
					int c = 0;
					for( Record field : action.getRecords() ) {
						node = new JsonNode("xtype", "radio");
						if( c == 0 ) {
							String fieldLabel = action.getParameter(ActionParameter.NAME);
							if( fieldLabel != null )
								node.appendNext("fieldLabel", fieldLabel);
							else
								node.appendNext("labelSeparator", "");
						} else
							node.appendNext("labelSeparator", "");
						node.appendNext("id", radioId + "." + c);
						node.appendNext("name", radioId);
						node.appendNext("boxLabel", field.getStringField("value"));
						node.appendNext("inputValue", field.getStringField("id"));
						if( field.getBooleanField("selected")  )
							node.appendNext("checked", true);
						result.add(new JsonValue(node));
						if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
							createSaveFieldHandler(handlers, radioId+"."+c);
						c++;
					}

					node = null;
				}
					break;
				case ITEMSELECTOR:
				case MULTISELECT:
				case SELECT:
				case SELECTREMOTE:
				{
					node = addFieldParameters(action);
					node.appendNext("name", action.getParameter(ActionParameter.ID));
					if( action.getParameter(ActionParameter.NAME) != null )
						node.appendNext("fieldLabel", action.getParameter(ActionParameter.NAME));
					else
						node.appendNext("labelSeparator", "");
					String xType = null;
					if( action.getType() == ActionType.SELECT || action.getType() == ActionType.SELECTREMOTE ) {
						xType = "cg.combobox";
						node.appendNext("editable", action.hasFlag(ActionFlag.EDITABLE));
						node.appendNext("triggerAction", "all");
						node.appendNext("listWidth", 300);
						node.appendNext("typeAhead", true);
					} else if( action.getType() == ActionType.ITEMSELECTOR ) {
						xType = "cg.itemselector";
						node = addIntegerParameter(node, "msWidth", action.getParameter(ActionParameter.WIDTH));
						node = addIntegerParameter(node, "msHeight", action.getParameter(ActionParameter.HEIGHT));
					} else if( action.getType() == ActionType.MULTISELECT )
						xType = "cg.multiselect";
					node.appendNext("xtype", xType );
					//if( action.getParameter("msgTarget") == null )
					if( action.hasFlag(ActionFlag.AUTOSUBMIT) )
						createSaveFieldHandler(handlers, action.getParameter(ActionParameter.ID));

					// Use a store if pagination is present
					if( action.getType() == ActionType.SELECTREMOTE ) {
						if( action.getParameter(ActionParameter.PAGESIZE) != null ) {
							node.appendNext("enablePaging", true);
							node.appendNext("pagingTbCfg", addIntegerParameter(null, "pageSize", action.getParameter(ActionParameter.PAGESIZE)));
						}
						node.appendNext(createStore(action));
						for( Record field : action.getRecords() ) {
							if( field.getStringField("id").equals("id") )
								node.appendNext("valueField", "id");
							else if( field.getStringField("id").equals("value") )
								node.appendNext("displayField", "value");
						}				
						if( action.getRecords().size() > 2 ) {
							node.appendNext("tpl", "<div class='combo-item'><h3><span>{subtitle}</span>{value}</h3>{details}</div>");
							node.appendNext("itemSelector", "div.combo-item");
						}
					} else {
						// If it is a combobox, specify mode local
						if( action.getType() == ActionType.SELECT )
							node.appendNext("mode", "local");
						Vector<JsonValue> data = new Vector<JsonValue>(), selData = new Vector<JsonValue>();
						for( Record field : action.getRecords()  ) {
							JsonValue value = new JsonValue(new JsonValue[] {new JsonValue(field.getStringField("id")), new JsonValue(field.getStringField("value"))} );
							if( field.getBooleanField("selected") )
								selData.add(value);
							else
								data.add(value);
						}
						node.appendNext("data", (JsonValue[]) data.toArray(new JsonValue[data.size()]));
						if( action.getType() == ActionType.ITEMSELECTOR )
							node.appendNext("selData", (JsonValue[]) selData.toArray(new JsonValue[selData.size()]));
						else if( action.getType() == ActionType.MULTISELECT ) {
							node.appendNext("dataFields", new JsonValue[] {new JsonValue("id"), new JsonValue("value")});
							node.appendNext("valueField","id");
							node.appendNext("displayField","value");
						}
					}

				}
					break;
			}
			if( node != null )
				result.add(new JsonValue(node));
			
		}
		if( result.size() == 0 )
			return null;
		/*else if( result.size() == 1 )
			return result.get(0);*/
		else
			return new JsonValue((JsonValue[]) result.toArray(new JsonValue[result.size()]));
	}
	
	private String getPanelName(Action action, ActionType type) {
		if( type == ActionType.FORM ) {
			return "form";
		} else if( type == ActionType.TABPANEL) {
			if( action.hasChildType(ActionType.RICHTEXT) )
				return "cg.tabpanel";
			else
				return "tabpanel";
		} else if( type == ActionType.FIELDSET) {
			return "fieldset";
		} else  {
			return "panel";
		}
	}
	
	private JsonNode createStore(Action data) {
		JsonNode items = new JsonNode("proxy",new JsonNode("url","/AjaxEngine/"));
		//items.appendNext("remoteSort", true);
		JsonNode tmp = new JsonNode("totalProperty", "results");
		tmp.appendNext("root","rows");
		tmp.appendNext("id", "id");
		items.appendNext("reader",tmp);
		
		// Specify sorting and grouping models
		String sortBy = data.getParameter(ActionParameter.SORTBY), groupBy = data.getParameter(ActionParameter.GROUPBY);
		if( sortBy != null || groupBy != null ) {
			if( sortBy != null ) {
				tmp = new JsonNode("field", sortBy);
				tmp.appendNext("direction", (data.hasFlag(ActionFlag.SORTDESC)) ? "DESC" : "ASC" );
				tmp = new JsonNode("sortInfo", tmp);
			} else
				tmp = null;
			if( groupBy != null ) {
				if( tmp != null )
					tmp.appendNext("groupField",groupBy);
				else
					tmp = new JsonNode("groupField",groupBy);
				tmp.appendNext("remoteGroup", true);
			}
			items.appendNext("extraParams", tmp);
		}
		
		// Create a row definition
		Vector<JsonValue> rd = new Vector<JsonValue>();
		for( Record field : data.getRecords() ) {
			JsonNode item = new JsonNode("name",field.getStringField("id"));
			String type = field.getStringField("type");
			if( type != null ) {
				if( type.equals("date") ) {
					item.appendNext("type",type);
					//TODO should we use 'c' instead? Y-m-d\\TH:i:s
					item.appendNext("dateFormat", "Y-m-d\\TH:i:s\\Z");
				} else if( !type.equals("string") ) {
					item.appendNext("type",type);
				}
			}
			
			rd.add(new JsonValue(item));
		}
		items.appendNext("rd", (JsonValue[]) rd.toArray(new JsonValue[rd.size()]));
		
		return new JsonNode("store", items);
	}
	
	private JsonNode addFieldParameters(Action action) {
		JsonNode result = null;
		if( action.getType() != ActionType.ITEMSELECTOR ) {
			result = addWidgetParameters(action);
			if( action.getType() != ActionType.CHECKBOX ) {
				String value = action.getParameter(ActionParameter.VALUE);
				result = addParameter(result, "value", value);
			}
		}
		String buttonId = action.getParameter(ActionParameter.ID);
		if( usedIds.contains(buttonId) ) {
			buttonId += "." + Integer.toString(idSequencer++, 36);
		} else
			usedIds.add(buttonId);
		result = addParameter(result, "id", buttonId);
		result = addParameter(result, "allowBlank", !action.hasFlag(ActionFlag.REQUIRED));
		result.appendNext("msgTarget", "side");
		return result;
	}
	
	private JsonNode addWidgetParameters(Action action) {
		JsonNode result = addIntegerParameter(null, "width", action.getParameter(ActionParameter.WIDTH));
		result = addIntegerParameter(result, "height", action.getParameter(ActionParameter.HEIGHT));
		return result;
	}
	
	private JsonNode addPanelParameters(Action action) {
		JsonNode result = addWidgetParameters(action);
		result = addParameter(result, "id", action.getParameter(ActionParameter.ID));
		result = addParameter(result, "title", action.getParameter(ActionParameter.TITLE));
		//result = addParameter(result, "layout", action.getParameter(ActionParameter.LAYOUT));
		
		// Add padding
		if( action.getParameter(ActionParameter.PADDING) != null || action.getParameter(ActionParameter.BACKGROUNDCOLOR) != null ) {
			//result.appendNext("defaults", new JsonNode("bodyStyle", "padding:" + action.getParameter(ActionParameter.PADDING)));
			StringBuffer style = new StringBuffer();
			if( action.getParameter(ActionParameter.PADDING) != null )
				style.append("padding:" + action.getParameter(ActionParameter.PADDING));
			if( action.getParameter(ActionParameter.BACKGROUNDCOLOR) != null ) {
				if( style.length() > 0 )
					style.append(";");
				style.append("background-color:" + action.getParameter(ActionParameter.BACKGROUNDCOLOR));
			}
			result.appendNext("bodyStyle", style.toString());
		}
		
		if( action.getParameter(ActionParameter.REGION) != null ) {
			result = addParameter(result, "region", action.getParameter(ActionParameter.REGION));
			if( action.getParameter(ActionParameter.MARGINS) != null )
				result.appendNext("margins", action.getParameter(ActionParameter.MARGINS));
			result.appendNext("split", action.hasFlag(ActionFlag.SPLIT ));
			result.appendNext("collapsible", action.hasFlag(ActionFlag.COLLAPSIBLE));
			result.appendNext("layout", "fit");
			/*if( action.getParameter(ActionParameter.REGION).equals("west") ) {
				node.appendNext("minSize", 200);
				node.appendNext("maxSize", 400);
			}*/
		}
		

		
		return result;
	}
	
	private JsonNode addParameter(JsonNode result, String name, String value) {
		if( value != null )
			return addParameter(result, name, new JsonValue(value));
		else
			return result;
	}

	private JsonNode addIntegerParameter(JsonNode result, String name, String value) {
		if( value != null && !value.isEmpty() )
			return addParameter(result, name, new JsonValue(Integer.parseInt(value)));
		else
			return result;
	}

	private JsonNode addParameter(JsonNode result, String name, boolean value) {
		return addParameter(result, name, new JsonValue(value));
	}
	
	/*private JsonNode addParameter(JsonNode result, String name, int value) {
		return addParameter(result, name, new JsonValue(value));
	}*/
	
	private JsonNode addParameter(JsonNode result, String name, JsonValue value ) {
		JsonNode node = new JsonNode(name, value);
		if( result == null )
			result = node;
		else
			result.appendNext(node);
		return result;
	}
	
	
	/*private JsonValue detectJsonValue(String value) {
		if( value == null || value.isEmpty() )
			return new JsonValue("");
		boolean isNumber = true, isBoolean = true;
		for( int i = 0; i < value.length() && (isBoolean || isNumber); i++ ) {
			char ch = value.charAt(i);
			if( !Character.isDigit(ch) ){
				if( isNumber )
					isNumber = false;
				if( !((ch == 't' && i == 0) || (ch == 'r' && i == 1) || (ch == 'u' && i == 2) ||
						(ch == 'e' && i == 3) || (ch == 'f' && i == 0) || (ch == 'a' && i == 1) ||
						(ch == 'l' && i == 2) || (ch == 's' && i == 3) || (ch == 'e' && i == 4)) )
					isBoolean = false;
			} else if( isBoolean )
				isBoolean = false;
		}
		if( isBoolean )
			return new JsonValue(value.equals("true"));
		else if( isNumber )
			return new JsonValue(Integer.parseInt(value));
		else
			return new JsonValue(value);
	}*/
	
	private void createSaveFieldHandler(Vector<JsonValue> handlers, String widgetId) {
		JsonNode eventHdlr = new JsonNode("id", widgetId);
		//JsonNode node = new JsonNode("cmd","sethandler");
		//node.appendNext("cmps", new JsonValue[] {new JsonValue(eventHdlr)});
		eventHdlr.appendNext("handlers", new JsonValue[]{new JsonValue(createEventHandlerNode("saveFieldChange", "change", "change", widgetId, null, null, null))});
		handlers.add(new JsonValue(eventHdlr));
		//return node;
	}
	
	private JsonNode createEventHandlerNode(String handlerName, String eventName, String scopeEvent, String widgetId, String targetType, String targetId, JsonNode extraScope) {
		JsonNode result = new JsonNode("event", eventName);
		result.appendNext("hdlr", handlerName);
		JsonNode scope = new JsonNode("id",widgetId);
		scope.appendNext("event", scopeEvent);
		if(targetType != null)
			scope.appendNext(targetType, targetId);
		if(extraScope != null)
			scope.appendNext(extraScope);
		result.appendNext("scope", scope);
		return result;
	}
		
	private Object getInputData(Event event) {
		Object inputData = null;

		if( event.getPayload() != null ) {
			LinkedList<Node> list = new LinkedList<Node>();
			JsonNode payload = null;
			if( event.getPayload() instanceof JsonNode )
				payload = (JsonNode)event.getPayload();
			else
				payload = JsonNode.parse((String)event.getPayload());
			if( payload.getValue().getType() == JsonValue.T_ARRAY ) {
				JsonValue[] items = ((JsonValue[])payload.getValue().getValue());
				for( int i = 0; i < items.length; i++ ) {
					list.add(getElementById((String)items[i].getValue()));
				}
			} else //TODO support comma separated
				list.add(getElementById((String)payload.getValue().getValue()));
			inputData = new DummyNodeList(list);
		}
		return inputData;
	}

	// Event class
	
	protected class Event {
		String event = null;
		Widget widget = null;
		int windowId = -1, panelId = -1;
		Object payload = null;
		SequenceBucketizable item = null;
		
		public Event() {
			
		}
		public Event(Event event) {
			this.windowId = event.getWindowId();
			this.panelId = event.getPanelId();
			this.payload = event.getPayload();
			this.widget = event.getWidget();
			this.event = event.getEvent();
		}
		public String getEvent() {
			return event;
		}
		public void setEvent(String event) {
			this.event = event;
		}
		public Widget getWidget() {
			return widget;
		}
		public void setWidget(Widget widget) {
			this.widget = widget;
		}
		public int getWindowId() {
			return windowId;
		}
		public void setWindowId(int windowId) {
			this.windowId = windowId;
		}
		public int getPanelId() {
			return panelId;
		}
		public void setPanelId(int panelId) {
			this.panelId = panelId;
		}
		public Object getPayload() {
			return payload;
		}
		public SequenceBucketizable getItem() {
			return item;
		}
		public void setPayload(Object payload) {
			this.payload = payload;
		}
		public void parseComponentId(String componentId) {
			Object[] elements = parseId(componentId);
			windowId = (Integer)elements[0];
			panelId = (Integer)elements[1];
			widget = (Widget)elements[2];
			item = (SequenceBucketizable)elements[3];
		}
		
	}

	// AJAX Engine renderers
	
	protected abstract class Renderer extends GuiResponse {
		protected String contentType = "text/plain", resultString = null;
		public void send(HttpServletResponse response) throws IOException {
			// Render response
			/*FileReader fr = new FileReader("test/test.json");
			StringBuffer sb = new StringBuffer();
			int c;
			while( (c = fr.read()) != -1)
				sb.append((char)c);
			fr.close();
			resultString = sb.toString();*/
			
			response.setContentType(contentType);
			response.setContentLength(resultString.length());
			PrintWriter out = response.getWriter();
			out.write(resultString);
			out.close();
		}
	}
	
	protected class HtmlRenderer extends Renderer {
		
		public HtmlRenderer(String htmlString) {
			this.resultString = htmlString;
			this.contentType = "text/html";
		}
	}
	
	protected abstract class RecordsRenderer extends Renderer {
		protected Vector<JsonValue> rows = new Vector<JsonValue>();		
		
		public RecordsRenderer(List<Record> records) {
			for( Record record : records ) {
				JsonNode row = null;
				for( String fieldName : record.getFieldNames() ) {
					Object value = record.getField(fieldName);
					JsonValue jValue = null;
					if( value == null )
						jValue = new JsonValue((String)null);
					else if( value instanceof String )
						jValue = new JsonValue((String)value);
					else if( value instanceof Integer )
						jValue = new JsonValue((Integer)value);
					else if( value instanceof Boolean )
						jValue = new JsonValue((Boolean)value);
					else if( value instanceof Double )
						jValue = new JsonValue(((Double)value).floatValue());
					else
						jValue = new JsonValue((String)null);
					if( row == null )
						row = new JsonNode(fieldName, jValue);
					else
						row.appendNext(fieldName, jValue);
				}
				rows.add(new JsonValue(row));
			}
		}
		
	}
	
	protected class DataStoreRenderer extends RecordsRenderer {

		public DataStoreRenderer(List<Record> records) {
			super(records);
		}
		
		public void send(HttpServletResponse response) throws IOException {
			JsonNode jsonResponse = new JsonNode("results", rows.size());
			jsonResponse.appendNext("rows", (JsonValue[]) rows.toArray(new JsonValue[rows.size()]));
			resultString = jsonResponse.toString();
			
			super.send(response);
		}
		
	}
	
	protected class TreeStoreRenderer extends RecordsRenderer {

		public TreeStoreRenderer(List<Record> records) {
			super(records);
		}
		
		public void send(HttpServletResponse response) throws IOException {
			resultString = new JsonNode("r", (JsonValue[]) rows.toArray(new JsonValue[rows.size()])).toString();
			resultString = resultString.substring(resultString.indexOf('['), resultString.length()-1);
			
			super.send(response);
		}
	}
	
	protected class JsonResponseRenderer extends Renderer {
		protected Vector<JsonValue> commands = new Vector<JsonValue>();
		
		public void send(HttpServletResponse response) throws IOException {
			// Create renderer
			Vector<JsonValue> handlers = new Vector<JsonValue>();

			// Start rendering
			generateJsonResponse(commands, handlers, actions);
			if( handlers.size() > 0) {
				JsonNode node = new JsonNode("cmd","sethandler");
				node.appendNext("cmps", (JsonValue[]) handlers.toArray(new JsonValue[handlers.size()]));
				commands.add(new JsonValue(node));
			}
			if( commands.size() > 0 ) 
				resultString = new JsonNode("commands", (JsonValue[]) commands.toArray(new JsonValue[commands.size()])).toString();
			else
				resultString = "{}";
			super.send(response);
		}
		
	}
	
}

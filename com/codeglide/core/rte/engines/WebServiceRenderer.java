package com.codeglide.core.rte.engines;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.codeglide.core.Logger;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.BeforeCloseHandler;
import com.codeglide.core.rte.interfaces.CloseHandler;
import com.codeglide.core.rte.interfaces.CustomEventHandler;
import com.codeglide.core.rte.interfaces.RenameItemHandler;
import com.codeglide.core.rte.interfaces.SetStringHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.render.Record;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.core.rte.session.Session;
import com.codeglide.core.rte.variables.VariableResolver;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.interfaces.cookies.CookiesNode;
import com.codeglide.xml.dom.DummyNodeList;

public class WebServiceRenderer extends GuiEngine {
	/***********************************************************************************************/
	private String NtoS(Element node) {
		return NtoS(node,"",true);		
	}

	private String NtoS(Node node, String prefix, boolean last) {
		String txt;
		if (node.getNodeName().equals("#text"))
			txt = prefix + ((prefix.isEmpty())?"":"|_") + node.getTextContent() + "\n";
		else {
			txt = prefix + ((prefix.isEmpty())?"":"|_") + node.getNodeName() + " [";
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0;attributes != null && i < attributes.getLength();i++) {
				txt += (i==0 ? "" : ",") + AtoS(attributes.item(i));
			}
			txt += "]\n";
			NodeList children = node.getChildNodes();
			for (int i = 0;children != null && i < children.getLength();i++)
				txt += NtoS(children.item(i),prefix + (last?"  ":"| "),i == children.getLength() - 1);
		}
		return txt;
	}
	
	private String AtoS(Node attr) {
		return attr.getNodeName() + " = '" + attr.getTextContent() + "'";
	}
	
	//TODO use SAX
	protected void handleRequest() throws Exception {
		if (servletRequest.getInputStream().available() == 0) 
			response = new WsdlDefinitionResponse();
		else try {
			// Create a web services response
			response = new WebServiceResponse();
			
			// Define rendering variables
			ContextUi context = (ContextUi)this.context;
			List<Action> result = ((GuiResponse)response).getActionsList();
			Session session = context.getCurrentSession();
			
			NodeList events;
			try {
				InputSource inputSource = new InputSource(servletRequest.getInputStream());
				DOMParser docParser = new DOMParser();				
				docParser.parse(inputSource);
				Element x = docParser.getDocument().getDocumentElement();
				System.out.println(NtoS(x));
				events = x.getChildNodes();
			} catch (Exception e) {
				e.printStackTrace();
				events = new DummyNodeList(new LinkedList<Node>());
			}
			
			boolean doInit = false;
			int lastWindowId = -1, lastPanelId = -1;
			
			for( int i = 0; i < events.getLength(); i++ ) {
				Node node = events.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE)
					continue;
				Element event = (Element)node;
				String eventName = event.getNodeName();
				int windowId = -1, panelId = -1;
				Widget widget = null;
				SequenceBucketizable item = null;
				
				// Obtain windowId, panelId and widget
				if( !event.getAttribute("w").isEmpty() ) {
					Object[] elements = parseId(event.getAttribute("w"));
					windowId = (Integer)elements[0];
					panelId = (Integer)elements[1];
					widget = (Widget)elements[2];
					item = (SequenceBucketizable)elements[3];
				}
				
				// Set IDs and add variables if they changed
				if( lastWindowId != windowId || lastPanelId != panelId ) {
					VariableResolver resolver = new VariableResolver();
					resolver.addVariables("_g", session.getGlobalVariables());
					if( panelId > -1 && windowId > -1 ) 
						addPanelVariables(windowId, panelId);
					context.setVariables(resolver);
					context.setWindowId(lastWindowId = windowId);
					context.setPanelId(lastPanelId = panelId);
				}

				// Process events
				if( eventName.equals("click") ) {
					// Handle click
//				((ClickHandler)widget).handleClick(context, result, getInputData(event));
				} else if( eventName.equals("setCookie") ) {
					if( cookiesNode != null ) {
						String cookieId = event.getAttribute("id"), cookieValue = event.getAttribute("value");
						if( cookieId.equals("OBJ") )
							cookiesNode.getCookies(cookieValue);
					}
				} else if( eventName.equals("getRecords") ) { 
//				int pageStart = 0, pageLimit = 0;
//				String groupBy = null, sortBy = null, filterBy = null;
//				boolean sortDesc = false;
//				
//				// Update display settings with the information sent by the datastore
//				for( JsonNode listInfo = JsonNode.parse((String)event.getPayload()); listInfo != null; listInfo = listInfo.getNextNode() ) {
//					if( listInfo.getName().equals("groupBy") ) {
//						groupBy = (String)listInfo.getValue().getValue();
//					} else if( listInfo.getName().equals("sort") ) {
//						sortBy = (String)listInfo.getValue().getValue();
//					} else if( listInfo.getName().equals("query") ) {
//						filterBy = (String)listInfo.getValue().getValue();
//					} else if( listInfo.getName().equals("dir") ) {
//						sortDesc = !((String)listInfo.getValue().getValue()).equalsIgnoreCase("ASC");
//					} else if( listInfo.getName().equals("start") ) {
//						if( listInfo.getValue().getType() == JsonValue.T_INTEGER )
//							pageStart = (Integer)listInfo.getValue().getValue();
//						else
//							pageStart = Integer.parseInt((String)listInfo.getValue().getValue());
//					} else if( listInfo.getName().equals("limit") ) {
//						if( listInfo.getValue().getType() == JsonValue.T_INTEGER )
//							pageLimit = (Integer)listInfo.getValue().getValue();
//						else
//							pageLimit = Integer.parseInt((String)listInfo.getValue().getValue());
//					}
//				}
//				if( sortDesc && sortBy != null )
//					sortBy = "@" + sortBy;
//				renderer = new DataStoreRenderer( ((GetRecordHandler)widget).getRecords(context, pageStart, pageLimit, sortBy, groupBy, filterBy) );
				} else if( eventName.equals("setRecord") ) {
//				boolean doRefresh = false;
//				JsonNode payload = JsonNode.parse((String)event.getPayload());
//				JsonValue[] items = ((JsonValue[])payload.getValue().getValue());
//				for( int i = 0; i < items.length; i++ ) {
//					String nodeId = null;
//					Record record = new Record();
//					
//					for( JsonNode change = (JsonNode)items[i].getValue(); change != null; change = change.getNextNode() ) {
//						if( change.getName().equals("id") ) {
//							if( !((String)change.getValue().getValue()).equals("new") )
//								nodeId = (String)change.getValue().getValue();
//							
//						} else {
//							String fieldName = application.getString(Integer.parseInt(change.getName(), 36));
//							switch( change.getValue().getType() ) {
//								case JsonValue.T_BOOLEAN:
//									record.addField(fieldName, ((Boolean)change.getValue().getValue()) ? "1" : "0");
//									break;
//								case JsonValue.T_FLOAT:
//									record.addField(fieldName, Float.toString((Float)change.getValue().getValue()));
//									break;
//								case JsonValue.T_INTEGER:
//									record.addField(fieldName, Integer.toString((Integer)change.getValue().getValue()));
//									break;
//								case JsonValue.T_STRING:
//									record.addField(fieldName, (String)change.getValue().getValue());
//									break;
//							}
//						}
//					}
//
//					// Add record
//					if( nodeId == null )
//						((UpdateRecordHandler)widget).addRecord(context, record);
//					else
//						((UpdateRecordHandler)widget).updateRecord(context, nodeId, record);
//					doRefresh = true;
//				}
//				if( doRefresh ) {
//					Action action = new Action(ActionType.RELOAD_COMPONENT);
//					action.addParameter(ActionParameter.ID, widget.generateWidgetId(context));
//					result.add(action);
//				}
				} else if( eventName.equals("getTree") ) {
					// Get node id
//				String nodeId = (String)JsonNode.parse((String)event.getPayload()).getValue().getValue();
//				
//				renderer = new TreeStoreRenderer( ( nodeId.equals("root") ) ? ((GetTreeHandler)widget).getRootChildren(context) :
//																   ((GetTreeHandler)widget).getChildren(context, nodeId) );
				} else if( eventName.equals("renameItem") ) {
					// Rename item
					String nodeId = null, nodeValue = null;
//				for( JsonNode cmd = JsonNode.parse((String)event.getPayload()) ; cmd != null; cmd = cmd.getNextNode() ) {
//					if( cmd.getName().equals("nodeId") )
//						nodeId = (String)cmd.getValue().getValue();
//					else if( cmd.getName().equals("value") )
//						nodeValue = (String)cmd.getValue().getValue();
//				}
					((RenameItemHandler)widget).handleRenameItem(context, nodeId, nodeValue);
				} else if( eventName.equals("set") ) {
					// Set string
					String input = null;
//				if(event.getPayload() != null && event.getPayload() instanceof Boolean )
//					input = ((Boolean)event.getPayload()) ? "1" : "0";
//				else
//					input = (String)event.getPayload();
					((SetStringHandler)widget).handleSet(context, result, item, input);
				} else if( eventName.equals("message") ) {
					// Message box response
					String buttonName = null, inputString = null;
//				for( JsonNode input = JsonNode.parse((String)event.getPayload()); input != null; input = input.getNextNode() ) {
//					if( input.getName().equals("btn") )
//						buttonName = (String)input.getValue().getValue();
//					else if( input.getName().equals("input") )
//						inputString = (String)input.getValue().getValue();
//				}
					((CustomEventHandler)widget).handleEvent(context, result, "on" + buttonName, inputString);
				} else if( eventName.equals("fileUpload")) {
//				String message = "<textarea>{success: true, message: \"OK\"}</textarea>";
//				try {
//					((UploadHandler)widget).handleUpload(context, result, (ReceivedFile)event.getPayload());
//				} catch( Exception e ) {
//					message = "<textarea>{success: false, message: \"ERROR\"}</textarea>";
//				}
//				renderer = new HtmlRenderer(message);
				} else if( eventName.equals("uploadImage") ) {
//				// Handle an image upload request
//				String imageUrl = "";
//				try {
//					imageUrl = ((UploadHandler)widget).handleUpload(context, result, (ReceivedFile)event.getPayload());
//				} catch( Exception e ) {
//					Logger.debug(e);
//				}
//				renderer = new HtmlRenderer("<textarea>{imgUrl: '" + imageUrl  + "'}</textarea>");
				} else if( eventName.equals("close") ) {
					((CloseHandler)widget).handleClose(context, result);
				} else if( eventName.equals("beforeclose") ) {
					((BeforeCloseHandler)widget).handleBeforeClose(context, result);
				} else if( eventName.equals("cellclick") ) {
//				// Handle click
//				((ClickHandler)widget).handleClick(context, result, getInputData(event));
				} else if( eventName.equals("celldblclick") ) {
//				// Handle click
//				((ClickHandler)widget).handleDoubleClick(context, result, getInputData(event));
				} else if( eventName.equals("onDrop") ) {
//				if( event.getPayload() != null ) {
//					DynamicElement targetNode = null;
//					LinkedList<Node> sourceNodes = new LinkedList<Node>();
//					JsonNode payload = null;
//					if( event.getPayload() instanceof JsonNode )
//						payload = (JsonNode)event.getPayload();
//					else
//						payload = JsonNode.parse((String)event.getPayload());
//					for( JsonNode runner = payload; runner != null; runner = runner.getNextNode() ) {
//						if( runner.getName().equals("parentId") ) {
//							String value = (String)runner.getValue().getValue();
//							if( !value.equals("root") )
//								targetNode = getElementById(value);
//						} else if( runner.getName().equals("childId") ) {
//							JsonValue[] items = ((JsonValue[])runner.getValue().getValue());
//							for( int i = 0; i < items.length; i++ ) {
//								sourceNodes.add(getElementById((String)items[i].getValue()));
//							}
//						}
//					}
//					((DropHandler)widget).handleDrop(context, result, targetNode, new DummyNodeList(sourceNodes));
//				}
				} else if( eventName.equals("init") ) {
					doInit = true;
					result.add(new Action(ActionType.INIT));
				} else
					Logger.warn("Unknown event '" + eventName + "'.");
			}

			// Generate CSS request
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
			if( events.getLength() < 1 || doInit ) {
				context.setWindowId(0);
				handleTerminalInit();
			} else
				handleTerminalRequest();
		} catch (Throwable e) {
			handleError(e);
		}
	}

	protected CookiesNode cookiesNode = null;
	
	protected void handleInit(Session session) throws Exception {
		super.handleInit(session);
		
		// Feed the cookies
		cookiesNode = (CookiesNode)session.getRootNode().getChildNode("Cookies");
	}
	
	protected void handleFinalize() {
		super.handleFinalize();
		
		// Set cookies
		if( cookiesNode != null && response instanceof GuiResponse ) {
			String cookiesString = cookiesNode.getCookiesString();
			if( cookiesString != null ) {
				Action action = new Action(ActionType.SAVE_COOKIE);
				action.addParameter(ActionParameter.ID, "OBJ");
				action.addParameter(ActionParameter.VALUE, cookiesString);
				((GuiResponse)response).getActionsList().add(action);
			}
		}
	}
	
	protected Session handleSessionNotFound(Application application) throws Exception {
		if (servletRequest.getInputStream().available() == 0) {
			response = new WsdlDefinitionResponse();
			return null;
		} else
			return super.handleSessionNotFound(application);
	}
		
	protected String getSessionId() {
		try {
			String requestPath[] = servletRequest.getServletPath().split("\\/");
			return requestPath[requestPath.length-1];
		} catch (Exception _) {
		}
		return null;
	}
	
	protected void setSessionId(String sessionId) {
		Action action = new Action(ActionType.SET_SESSION);
		action.addParameter(ActionParameter.ID, sessionId);
		((GuiResponse)response).getActionsList().add(action);
	}

	protected class WebServiceResponse extends GuiResponse {
		// ID duplication controls
		private HashSet<String> usedIds = new HashSet<String>();
		private int idSequencer = 0;

		public void send(HttpServletResponse response) throws IOException {
			response.setContentType("text/xml");
			
			PrintWriter out = response.getWriter();
			out.print("<?xml version='1.0' encoding='utf-8'?>");
			out.print("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>");
				out.print("<soapenv:Body>");
					out.print("<CodeGlideResponse xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns='http://www.codeglide.com' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>");
			sendCommands(out, actions);
					out.print("</CodeGlideResponse>");
				out.print("</soapenv:Body>");
			out.print("</soapenv:Envelope>");
			out.close();
		}
		
		protected void sendCommands(PrintWriter out, List<Action> actions) throws IOException {
			for( Action action : actions ) {
				
				// Process widget actions
				switch( action.getType() ) {
					case INIT:
						out.append("<").append(action.getActionName()).append("/>");
						break;
					case SET_SESSION:
					case REMOVE_COMPONENT:
					case CLEAR_PANEL:
					case RELOAD_COMPONENT:
					case CLOSE_WINDOW:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ID);
						out.append("/>");
						continue;
					case REPLACE_COMPONENT:
					case ADD_COMPONENT:
						out.append("<").append(action.getActionName());
						if( action.getType() == ActionType.REPLACE_COMPONENT )
							writeTagParameter(out, action, ActionParameter.ID);
						else
							writeTagParameter(out, action, ActionParameter.TARGET);
						out.append(">");
						sendCommands(out, action.getChildren());
						out.append("</").append(action.getActionName()).append(">");
						break;
					case OPEN_WINDOW:
					{
						Action childAction = action.getChildren().get(0);
						out.append("<").append(action.getActionName());
						writeTagParameter(out, childAction, ActionParameter.ID);
						out.append(">");
						sendCommands(out, action.getChildren());
						out.append("</").append(action.getActionName()).append(">");
					}
						break;
					case SHOW_MESSAGE:
					case SHOW_ALERT:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ID);
						writeTagParameter(out, action, ActionParameter.TITLE);
						writeTagParameter(out, action, ActionParameter.MESSAGE);
						writeTagParameter(out, action, ActionParameter.TYPE);
						writeTagParameter(out, action, ActionParameter.ICON);
						writeTagParameter(out, action, ActionParameter.BUTTONS);
						out.append("/>");
						break;
					case ADDCSS:
					case SAVE_COOKIE:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ID);
						writeTagParameter(out, action, ActionParameter.VALUE);
						out.append("/>");
						break;
					case ITEM_RELOAD:
					case ITEM_REMOVE:
					case ITEM_RENAME:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ID);
						writeTagParameter(out, action, ActionParameter.TARGET);
						out.append("/>");
						break;
					case BUTTONS:
					case BOTTOMBAR:
					case TOPBAR:
					case CONTENTS:
						out.append("<").append(action.getActionName()).append(">");
						sendCommands(out, action.getChildren());
						out.append("</").append(action.getActionName()).append(">");
						break;
					case FORM:
					case TABPANEL:
					case FIELDSET:
					case PANEL:
					case WINDOW:
					case ACCORDIONPANEL:
					case COLUMNPANEL:
					case REGIONPANEL:
						{
							out.append("<").append(action.getActionName());
							writePanelParameters(out, action);
							writeFlags(out, action);
							out.append(">");
							sendCommands(out, action.getChildren());
							out.append("</").append(action.getActionName()).append(">");
						}
						break;
					case MENU:
					{
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.NAME);
						writeTagParameter(out, action, ActionParameter.ICON);
						out.append(">");
						sendCommands(out, action.getChildren());
						out.append("</").append(action.getActionName()).append(">");
					}
						break;
					case BUTTON:
					{
						String buttonId = action.getParameter(ActionParameter.ID);
						if( usedIds.contains(buttonId) ) {
							buttonId += "." + Integer.toString(idSequencer++, 36);
						} else
							usedIds.add(buttonId);
						out.append("<").append(action.getActionName()).append(" id=\"").append(buttonId).append("\"");

						writeTagParameter(out, action, ActionParameter.NAME);
						writeTagParameter(out, action, ActionParameter.ICON);
						writeTagParameter(out, action, ActionParameter.TOOLTIP);
						writeTagParameter(out, action, ActionParameter.GROUP);
						writeTagParameter(out, action, ActionParameter.INPUTLIST);
						writeTagParameter(out, action, ActionParameter.PAYLOAD); //TODO support comma separated inputlists and payloads
						writeFlags(out, action);

						if( action.getChildren().size() > 0 ) {
							out.append(">");
							for( Action buttonAction : action.getChildren() ) {
								out.append("<").append(buttonAction.getActionName());
								writeTagParameter(out, buttonAction, ActionParameter.TARGET);
								writeTagParameter(out, buttonAction, ActionParameter.ID);
								if( buttonAction.getType() == ActionType.SHOW_FILE_DIALOG ) {
									writeTagParameter(out, buttonAction, ActionParameter.WIDTH);
									writeTagParameter(out, buttonAction, ActionParameter.HEIGHT);
								}
								out.append("/>");
							}
							out.append("</").append(action.getActionName()).append(">");
						} else
							out.append("/>");
					}
						break;
					case TOGGLER:
					{
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ON);
						writeTagParameter(out, action, ActionParameter.OFF);
						out.append("/>");
					}
						break;
					case DATEFIELD:
					case LABELFIELD:
					case IMAGESELECTOR:
					case COLORPICKER:
					case TEXTFIELD:
					case RICHTEXT:
					case CHECKBOX:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ID);
						writeTagParameter(out, action, ActionParameter.NAME);
						writeTagParameter(out, action, ActionParameter.VALUE);
						writeTagParameter(out, action, ActionParameter.WIDTH);
						writeTagParameter(out, action, ActionParameter.HEIGHT);
						writeFlags(out, action);
						out.append("/>");
						break;
					case SEPARATOR:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.VALUE);
						writeFlags(out, action);
						out.append("/>");
						break;
					case LABEL:
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.NAME);
						writeFlags(out, action);
						out.append("/>");
						break;
					case GRID:
					{
						out.append("<").append(action.getActionName());
						writePanelParameters(out, action);
						writeTagParameter(out, action, ActionParameter.DDGROUP);
						writeTagParameter(out, action, ActionParameter.PAGESIZE);
						writeTagParameter(out, action, ActionParameter.SORTBY);
						writeTagParameter(out, action, ActionParameter.GROUPBY);
						writeFlags(out, action);
						out.append("><fields>");

						// Create column model
						for( Record field : action.getRecords() ) {
							out.append("<field");
							writeTag(out, "id", field.getStringField("id"));
							writeTag(out, "headerName", field.getStringField("name"));
							writeTag(out, "headerIcon", field.getStringField("icon"));
							writeTag(out, "width", field.getStringField("width"));
							if( field.getBooleanField("autosave") )
								writeTag(out, "autosave", "");
							writeTag(out, "summary", field.getStringField("summary"));
							writeTag(out, "default", field.getStringField("default"));
			
							boolean closedTag = false;
							if( field.getStringField("filter") != null ) {
								writeTag(out, "filter", field.getStringField("filter"));
								if( field.getStringField("filterparam") != null ) {
									writeTag(out, "filterParam", field.getStringField("filterparam"));
								} else if( field.getStringField("filter").equals("imagemap") ) {
									out.append("><imageMappings>");
									closedTag = true;
									for( Record map : field.getRecordListField("imagemap") ) {
										out.append("<map");
										writeTag(out, "val", map.getStringField("_val"));
										writeTag(out, "img", map.getStringField("_img"));
										out.append("/>");
									}
									out.append("</imageMappings>");
								}
							}

							// Render editor widget
							int editorId = field.getIntegerField("editor");
							if( editorId > -1 ) {
								try {
									List<Action> editorActions = new LinkedList<Action>();
									((Widget)context.getApplication().getWidgetBucket().getObject(editorId)).run(context, editorActions);
									if( !closedTag ) {
										out.append(">");
										closedTag = true;
									}
									sendCommands(out, editorActions);
								} catch (CodeGlideException _) {
								}
							}
							
							if( closedTag )
								out.append("</field>");
							else
								out.append("/>");
							
						}
						out.append("</fields>");

						sendCommands(out, action.getChildren());
						out.append("</").append(action.getActionName()).append(">");
					}					
						break;
					case TREE:
					{
						out.append("<").append(action.getActionName());
						writePanelParameters(out, action);
						writeTagParameter(out, action, ActionParameter.DDGROUP);
						writeFlags(out, action);
						out.append(">");
						sendCommands(out, action.getChildren());
						out.append("</").append(action.getActionName()).append(">");
					}
						break;
					case RADIOGROUP:
					case ITEMSELECTOR:
					case MULTISELECT:
					case SELECT:
					case SELECTREMOTE:
					{
						out.append("<").append(action.getActionName());
						writeTagParameter(out, action, ActionParameter.ID);
						writeTagParameter(out, action, ActionParameter.NAME);
						writeTagParameter(out, action, ActionParameter.VALUE);
						writeTagParameter(out, action, ActionParameter.WIDTH);
						writeTagParameter(out, action, ActionParameter.HEIGHT);
						writeFlags(out, action);

						// Use a store if pagination is present
						if( action.getType() == ActionType.SELECTREMOTE ) {
							writeTagParameter(out, action, ActionParameter.PAGESIZE);
							out.append("/>");
						} else {
							out.append(">");
							if( action.getType() == ActionType.RADIOGROUP ) {
								for( Record field : action.getRecords() ) {
									out.append("<radio");
									writeTag(out, "id", field.getStringField("id"));
									if( field.getBooleanField("selected")  )
										writeTag(out, "checked", "");
									out.append("><label");
									writeTag(out, "name", field.getStringField("value"));
									out.append("/></radio>");
								}
								for( Action radio : action.getChildren() ) {
									out.append("<radio");
									writeTag(out, "id", radio.getParameter(ActionParameter.VALUE));
									if( action.hasFlag(ActionFlag.CHECKED)  )
										writeTag(out, "checked", "");
									out.append(">");
									sendCommands(out, radio.getChildren());
									out.append("</radio>");
								}
							} else {
								for( Record field : action.getRecords() ) {
									out.append("<option");
									writeTag(out, "id", field.getStringField("id"));
									writeTag(out, "name", field.getStringField("value"));
									if( field.getBooleanField("selected")  )
										writeTag(out, "checked", "");
									out.append("/>");
								}
							}
							out.append("</").append(action.getActionName()).append(">");
						}

					}
						break;
				}
			}
		}

		protected void writePanelParameters(PrintWriter out, Action action) {
			writeTagParameter(out, action, ActionParameter.ID);
			//writeTagParameter(out, action, ActionParameter.PANEL_ID);
			writeTagParameter(out, action, ActionParameter.TITLE);
			writeTagParameter(out, action, ActionParameter.WIDTH);
			writeTagParameter(out, action, ActionParameter.HEIGHT);
			writeTagParameter(out, action, ActionParameter.LAYOUT);
			writeTagParameter(out, action, ActionParameter.PADDING);
			if( action.getParameter(ActionParameter.REGION) != null ) {
				writeTagParameter(out, action, ActionParameter.REGION);
				writeTagParameter(out, action, ActionParameter.MARGINS);
			}
		}
		
		protected void writeFlags(PrintWriter out, Action action ) {
			Collection<ActionFlag> flags = action.getFlags();
			if( flags != null && flags.size() > 0 ) {
				out.append(" flags=\"");
				boolean isFirst = true;
				for( ActionFlag flag : flags ) {
					if( isFirst )
						isFirst = false;
					else
						out.append(",");
					out.append(flag.toString().toLowerCase());
				}
				out.append("\"");
			}
		}
		
		protected void writeTagParameter(PrintWriter out, Action action, ActionParameter parameter ) {
			String value = action.getParameter(parameter);
			if( value != null ) 
				out.append(" ").append(parameter.toString().toLowerCase()).append("=\"").append(value).append("\""); //TODO escape
		}
		
		protected void writeTag(PrintWriter out, String name, String value ) {
			if( value != null ) 
				out.append(" ").append(name).append("=\"").append(value).append("\""); //TODO escape
		}

	}
	
	protected class WsdlDefinitionResponse extends Response {

		public void send(HttpServletResponse response) throws IOException {
			response.setContentType("text/xml");
			
			PrintWriter out = response.getWriter();
			out.print("<?xml version='1.0' encoding='utf-8'?>");
			out.print("<wsdl:definitions xmlns:http='http://schemas.xmlsoap.org/wsdl/http/' xmlns:soap='http://schemas.xmlsoap.org/wsdl/soap/' xmlns:s='http://www.w3.org/2001/XMLSchema' xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/' xmlns:tns='http://www.codeglide.com' xmlns:tm='http://microsoft.com/wsdl/mime/textMatching/' xmlns:mime='http://schemas.xmlsoap.org/wsdl/mime/' targetNamespace='http://www.codeglide.com' xmlns:wsdl='http://schemas.xmlsoap.org/wsdl/'>");
			
			  //Types
			  out.print("<wsdl:types>");
			    out.print("<s:schema elementFormDefault='qualified' targetNamespace='http://www.codeglide.com'>");
			    
			      //Type
			      out.print("<s:element name='CodeGlideRequest'>");
			        out.print("<s:complexType>");
			          out.print("<s:sequence>");
			            out.print("<s:element minOccurs='0' maxOccurs='1' name='Event' type='s:string' />");
			          out.print("</s:sequence>");
			        out.print("</s:complexType>");
			      out.print("</s:element>");
			      
			      //Type
			      out.print("<s:element name='CodeGlideResponse'>");
			        out.print("<s:complexType>");
			          out.print("<s:sequence>");
			            out.print("<s:element minOccurs='1' maxOccurs='1' name='GetWeatherByZipCodeResult' type='tns:WeatherForecasts' />");
			          out.print("</s:sequence>");
			        out.print("</s:complexType>");
			      out.print("</s:element>");
			      
			      //Type
			      out.print("<s:complexType name='WeatherForecasts'>");
			        out.print("<s:sequence>");
			          out.print("<s:element minOccurs='1' maxOccurs='1' name='Latitude' type='s:float' />");
			          out.print("<s:element minOccurs='1' maxOccurs='1' name='Longitude' type='s:float' />");
			          out.print("<s:element minOccurs='1' maxOccurs='1' name='AllocationFactor' type='s:float' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='FipsCode' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='PlaceName' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='StateCode' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='Status' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='Details' type='tns:ArrayOfWeatherData' />");
			        out.print("</s:sequence>");
			      out.print("</s:complexType>");
			      
			      //Type
			      out.print("<s:complexType name='ArrayOfWeatherData'>");
			        out.print("<s:sequence>");
			          out.print("<s:element minOccurs='0' maxOccurs='unbounded' name='WeatherData' type='tns:WeatherData' />");
			        out.print("</s:sequence>");
			      out.print("</s:complexType>");
			      
			      //Type
			      out.print("<s:complexType name='WeatherData'>");
			        out.print("<s:sequence>");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='Day' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='WeatherImage' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='MaxTemperatureF' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='MinTemperatureF' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='MaxTemperatureC' type='s:string' />");
			          out.print("<s:element minOccurs='0' maxOccurs='1' name='MinTemperatureC' type='s:string' />");
			        out.print("</s:sequence>");
			      out.print("</s:complexType>");
			      out.print("<s:element name='WeatherForecasts' type='tns:WeatherForecasts' />");
			    out.print("</s:schema>");
			  out.print("</wsdl:types>");
			  
			  //Messages
			  out.print("<wsdl:message name='CodeGlideEngineEventsSoap'>");
			    out.print("<wsdl:part name='parameters' element='tns:CodeGlideRequest' />");
			  out.print("</wsdl:message>");
			  
			  out.print("<wsdl:message name='CodeGlideEngineActionsSoap'>");
			    out.print("<wsdl:part name='parameters' element='tns:CodeGlideResponse' />");
			  out.print("</wsdl:message>");
			  
			  //Port types
			  out.print("<wsdl:portType name='CodeGlideEngineSoap'>");
			    out.print("<wsdl:operation name='CodeGlideRequest'>");
			      out.print("<wsdl:input message='tns:CodeGlideEngineEventsSoap' />");
			      out.print("<wsdl:output message='tns:CodeGlideEngineActionsSoap' />");
			    out.print("</wsdl:operation>");
			  out.print("</wsdl:portType>");
			  
			  //Bindings
			  out.print("<wsdl:binding name='CodeGlideEngineSoap' type='tns:CodeGlideEngineSoap'>");
			    out.print("<soap:binding transport='http://schemas.xmlsoap.org/soap/http' style='document' />");
			    out.print("<wsdl:operation name='CodeGlideRequest'>");
			      out.print("<soap:operation soapAction='http://www.codeglide.com/CodeGlideEngine' style='document' />");
			      out.print("<wsdl:input>");
			        out.print("<soap:body use='literal' />");
			      out.print("</wsdl:input>");
			      out.print("<wsdl:output>");
			        out.print("<soap:body use='literal' />");
			      out.print("</wsdl:output>");
			    out.print("</wsdl:operation>");
			  out.print("</wsdl:binding>");
			  
			  //Service
			  out.print("<wsdl:service name='CodeGlideEngine'>");
			    out.print("<wsdl:port name='CodeGlideEngineSoap' binding='tns:CodeGlideEngineSoap'>");
			      out.print("<soap:address location='http://localhost:8080/cgRte' />");
			    out.print("</wsdl:port>");
			  out.print("</wsdl:service>");
			  
			out.print("</wsdl:definitions>");
			out.close();
		}
		
	}
	
}

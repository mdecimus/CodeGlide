package com.codeglide.core.rte.widgets;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.objects.ObjectField;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.contexts.ContextWriter;
import com.codeglide.core.rte.engines.FileStreamEngine;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.ExpressionException;
import com.codeglide.core.rte.interfaces.UploadHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionFlag;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.sequencers.SequenceBucketizable;
import com.codeglide.util.converter.HtmlCoder;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrStream;
import com.codeglide.xml.dom.DynamicElement;

/*
 * 
 * Input Flags
 * 
 * LOCALMAILTO, INVALIDURI, LOADEXTIMG, LOADDYNAMIC, REPLACEBODY, REMOVESTYLE
 * 
 */

public class Richtext extends Field implements UploadHandler {
	protected Expression photoList;
	
	public Richtext(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		if( context instanceof ContextWriter ) {
			try {
				Writer writer = ((ContextWriter)context).getWriter();
				DynamicAttr source = (DynamicAttr)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
				RichTextReader reader = new RichTextReader(context, this, source, getPhotoList(context, source));
				reader.setFlags(this.actionFlags);
				int c;
				while( (c = reader.read()) != -1 )
					writer.write((char)c);
				reader.close();
			} catch (IOException _) {
			}
		} else {
			Action action = new Action(ActionType.RICHTEXT);
			DynamicAttr body = (DynamicAttr)addFieldProperties(context, action)[0];
			StringBuffer rteResult = new StringBuffer();
			try {
				if( body != null ) {
					RichTextReader reader = new RichTextReader(context, this, body, getPhotoList(context, body));
					reader.setFlags(this.actionFlags);
					int c;
					while( (c = reader.read()) != -1 )
						rteResult.append((char)c);
				}
			} catch (Exception e) {Logger.debug(e);}
			action.addParameter(ActionParameter.VALUE, rteResult.toString());
			result.add(action);
		}
	}
	
	protected void parseElement(Element element, Application application) {
		super.parseElement(element, application);
		photoList = getExpression(element, "imagesnode");
	}
	
	public void handleSet(ContextUi context, List<Action> result, SequenceBucketizable target, String value ) {
		super.handleSet(context, result, target, generateCid(context, value));
	}
	
	public String handleUpload(ContextUi context, List<Action> result, ReceivedFile file )  throws CodeGlideException {
		DynamicElement image = null;
		ObjectDefinition objDef = context.getApplication().getObject("File");
		if( objDef != null )
			image = objDef.buildObject(context.getRootNode().getDocumentNode());
		else
			image = new DynamicElement(context.getDocumentNode(), "File");
		
		image.setAttribute("Name", file.getName());
		image.setAttribute("Type", file.getContentType());
		image.setAttribute("Size", file.getSize());
		image.setAttribute("Bin", file);
		try {
			image.setAttribute("Id", "<" + UUID.randomUUID().toString() + "@" + InetAddress.getLocalHost().getCanonicalHostName() + ">");
		} catch (UnknownHostException _) {
		}

		DynamicAttr body = null;
		if( bindExpression != null ) {
			try {
				body = (DynamicAttr)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
			} catch (Exception _) {}
		}

		DynamicElement photoList = getPhotoList(context, body);
		if( photoList == null ) {
			photoList = (DynamicElement)((DynamicElement)body.getParentNode()).appendChild("_" + body.getNodeName());
		}
		
		photoList.appendChild(image);
		
		return FileStreamEngine.FILESTREAM_URL + generateElementId(context, this, (DynamicElement)image);
	}
	
	protected DynamicElement getPhotoList(ContextUi context, DynamicAttr body) {
		try {
			if( photoList != null )
				return (DynamicElement)photoList.evaluate(context.getVariables(), body.getParentNode(), Expression.NODE);
			else
				return (DynamicElement)((DynamicElement)body.getParentNode()).getChildNode("_"+body.getNodeName());
		} catch (ExpressionException e) {
			return null;
		}
	}

	private String generateCid( ContextUi context, String body ) {
		StringBuffer result = new StringBuffer( body.length() );
		int idx = 0, lastIdx = 0;
		while( ( idx = body.indexOf(FileStreamEngine.FILESTREAM_URL, lastIdx) ) > -1 ) {
			result.append( body.substring(lastIdx, idx ) );
			lastIdx = body.indexOf('"',idx);
			DynamicElement file = getElementById(context, body.substring(idx + FileStreamEngine.FILESTREAM_URL.length(), lastIdx));
			String fileId = file.getAttribute("Id");
			if( fileId.startsWith("<") )
				fileId = fileId.substring(1, fileId.length() - 1);
			result.append("cid:").append(fileId);
		}
		if( result.length() > 0 ) {
			result.append(body.substring(lastIdx));
			return result.toString();
		} else
			return body;
		
	}
	
	/*
	 * 
	 * 			if( request.().equals("set") ) {
				setFieldValue((GuiRequest)request, rootDoc, (VisibleWidget)widget, rteGenerateCid((GuiRequest)request, (FieldsWidget)widget, (String)request.getPayload()));
				result.setExitBlock();
				return;
			} else if( request.().equals("uploadImage")) {

				CustomAction action = new CustomAction(WidgetElement.NULL);
				action.setPayload("<textarea>{imgUrl: '/cgrte/?p=" + generateElementId((GuiRequest)request, (VisibleWidget)widget, (DynamicElement)image) + "&e=dl" + "'}</textarea>");
				action.setContentType("text/html");
				result.addAction(action);
				result.setExitBlock();
				return;
			} else {
				if( widget.getValue(WidgetElement.RENDER_TO) != null ) {
				} else {
				}
			}

	 * 
	 */
	
	protected class RichTextReader extends Reader {
		public final static int FL_HTML_SOURCE   = 0x00001;
		public final static int FL_LOAD_EXTIMG   = 0x00002;
		public final static int FL_LOAD_DYNAMIC  = 0x00004;
		public final static int FL_TXT_HIGHLIGHT = 0x00008;
		public final static int FL_SKIP_TAGS     = 0x00010;
		public final static int FL_REMOVE_STYLE  = 0x00020;
		public final static int FL_LOCAL_MAILTO  = 0x00040;
		public final static int FL_INVALID_URI   = 0x00080;
		public final static int FL_EXTRACT_BODY  = 0x00100;
		public final static int FL_REPLACE_BODY  = 0x00200;
		public final static int FL_SKIP_CID      = 0x00400;
		public final static int FL_LOCAL_CHARSET = 0x00800;

		private int flags = FL_HTML_SOURCE | FL_TXT_HIGHLIGHT | FL_REMOVE_STYLE | FL_LOCAL_MAILTO;
		private boolean insideTag = false;
		private HashMap<String, String> bodyAttr = null;
		private Reader bodySource = null, buffer = null;
		
		private ContextUi context = null;
		private DynamicElement photoList = null;
		private DynamicAttr body = null;
		private Widget rteWidget = null;
		
		public void setFlags( Collection<ActionFlag> flags ) {
			if( flags.contains(ActionFlag.LOCALMAILTO) )
				set(FL_LOCAL_MAILTO);
			else
				clear(FL_LOCAL_MAILTO);
			if( flags.contains(ActionFlag.INVALIDURI) )
				set(FL_INVALID_URI);
			else
				clear(FL_INVALID_URI);
			if( flags.contains(ActionFlag.LOADEXTIMG) )
				set(FL_LOAD_EXTIMG);
			else
				clear(FL_LOAD_EXTIMG);
			if( flags.contains(ActionFlag.LOADDYNAMIC) )
				set(FL_LOAD_DYNAMIC);
			else
				clear(FL_LOAD_DYNAMIC);
			if( flags.contains(ActionFlag.REPLACEBODY) )
				set(FL_REPLACE_BODY);
			else
				clear(FL_REPLACE_BODY);
			if( flags.contains(ActionFlag.REMOVESTYLE) )
				set(FL_REMOVE_STYLE);
			else
				clear(FL_REMOVE_STYLE);
			
		}
		
		public RichTextReader( ContextUi context, Widget rteWidget, DynamicAttr body, DynamicElement photoList ) {
			this.context = context;
			this.photoList = photoList;
			this.rteWidget = rteWidget;
			this.body = body;
		}

		public void set( int flag ) {
			flags |= flag;
		}

		public void clear( int flag ) {
			flags &= ~flag;
		}

		public boolean check( int flag ) {
			return( (flags & flag ) != 0 );
		}
		
		private String getCidAsCookie( String id ) throws CodeGlideException {
			if( photoList == null || photoList.getChildren() == null )
				return null;
			for( Node node : photoList.getChildren() ) {
				String cid = ((DynamicElement)node).getAttribute("Id");
				if( cid != null && cid.equals(id) ) {
					return FileStreamEngine.FILESTREAM_URL + generateElementId(context, rteWidget, (DynamicElement)node);
				}
			}
			return null;
		}
		
		private String getMailtoAsCookie( String mail ) {
			//TODO implement
			return null;
		}
		
		private void addBodyAttribute( String attr ) {
			int index = attr.indexOf("=");
			if( index < 0 )
				return;
			String key = attr.substring(0,index).trim().toLowerCase();
			String val = attr.substring(index+1).trim();
			if( val.charAt(0) == '"' && val.charAt(val.length()-1) == '"')
				val = val.substring(1,val.length()-1);
			if( bodyAttr == null )
				bodyAttr = new HashMap<String, String>();
			bodyAttr.put( key, val );
		}
		
		private String cleanAttr( String attr ) throws CodeGlideException {
			int index = attr.indexOf("=");
			if( index < 0 )
				return attr;
			String key = attr.substring(0,index).trim();
			if( key.equalsIgnoreCase("target") || !check( FL_LOAD_DYNAMIC ) && (
			     key.equalsIgnoreCase("code") || key.equalsIgnoreCase("action") ||
			     key.equalsIgnoreCase("codetype") || key.equalsIgnoreCase("language") ||
			     key.toLowerCase().startsWith("on")))
				return null;
			if( !check( FL_SKIP_CID ) && (key.equalsIgnoreCase("href") || key.equalsIgnoreCase("src") ||
			    key.equalsIgnoreCase("lowsrc") || key.equalsIgnoreCase("background")) ) {
					String val = attr.substring(index+1).trim();
					if( val.charAt(0) == '"' && val.charAt(val.length()-1) == '"')
						val = val.substring(1,val.length()-1);
					String uri = val.toLowerCase(), cleanValue = null;
					if( uri.startsWith("cid:") ) {
						cleanValue = getCidAsCookie( "<" + val.substring(4) + ">" );
					} else if( uri.startsWith("mailto:") && check( FL_LOCAL_MAILTO ) ) {
						cleanValue = getMailtoAsCookie(uri.substring(7)).toString();
					} else if( uri.startsWith("http:") || uri.startsWith("https:") ||
					           uri.startsWith("ftp:") || uri.startsWith("gopher:") || 
					           uri.startsWith("wais:") || uri.startsWith("telnet:")) {
						cleanValue = getCidAsCookie( val );
						if( cleanValue == null )
							cleanValue = val;
					} else if( uri.startsWith("#") )
						cleanValue = val;
						
					if( cleanValue == null ) {
						if( check( FL_INVALID_URI ) )
							cleanValue = val;
						else
							cleanValue = "#";
					}

					return key.toLowerCase() + "=\"" + cleanValue + "\""; 
			} else
				return attr;
		}
		
		private String cleanTag( String tag ) throws CodeGlideException  {
			String parseTag = tag.trim();
			try {
				parseTag = parseTag.split(" ")[0].toLowerCase();
			} catch (Exception e) {
				parseTag.toLowerCase();
			}
			if( parseTag.equals("meta") || parseTag.equals("html") || parseTag.equals("/html") ||
			    parseTag.equals("head") || parseTag.equals("/head") || parseTag.equals("link") ||
			    parseTag.equals("server") || parseTag.equals("/server") || parseTag.equals("frame") ||
			    parseTag.equals("/frame") || ( !check( FL_LOAD_DYNAMIC) && ( parseTag.equals("app") ||
			     parseTag.equals("/app") || parseTag.equals("object") || parseTag.equals("/object") ||
			     parseTag.equals("iframe") || parseTag.equals("/iframe") || parseTag.equals("applet") ||
			     parseTag.equals("/applet") || parseTag.equals("script") || parseTag.equals("/script") ))) {
			    	return null;
			    }
			else if( parseTag.equals("style") && check(FL_REMOVE_STYLE) )
				return "<!--";
			else if( parseTag.equals("/style") && check(FL_REMOVE_STYLE) )
				return "-->";
			else if( (parseTag.startsWith("!") || parseTag.endsWith("-")) && check(FL_REMOVE_STYLE) )
				return null;
			
			boolean isBody = false, addTarget = false;
			StringBuffer tidyTag = new StringBuffer(tag.length());
			tidyTag.append("<");
			if( (parseTag.equals("body") || parseTag.equals("/body")) && check(FL_REPLACE_BODY) )
				return null;
			else if( parseTag.equals("body") && (check(FL_REMOVE_STYLE) || check(FL_EXTRACT_BODY) ) ) {
				if( check(FL_REMOVE_STYLE ) )
					tidyTag.append("table border=\"0\" width=\"100%\" height=\"100%\"");
				isBody = true;
			} else if( parseTag.equals("/body") && (check(FL_REMOVE_STYLE) || check(FL_EXTRACT_BODY) ) ) {
				if( check(FL_EXTRACT_BODY ) )
					return null;
				else 
					tidyTag.append("/td></tr></table");
			} else if( parseTag.equals("a") ) {
				tidyTag.append("a");
				addTarget = true;
			} else
				tidyTag.append(parseTag);

			final int S_INTAG = 1, S_INQUOTE = 2, S_SEENPARAM = 3, S_SEENPARAMEQUAL = 4, S_SEENPARAMEND = 5;
			int state = S_INTAG;
			String unsecureImgAlt = null;
			boolean attrReady = false, seenTagName = false, unsecureImage = false, addImageLink = false;

			if( !check( FL_LOAD_EXTIMG ) && parseTag.equals("img") )
				unsecureImage = true;

			StringBuffer attr = new StringBuffer();		
			for( int i = 0; i < tag.length(); i++ ) {
				attrReady = false;
				if( tag.charAt(i) == '"' ) {
					if( state == S_INQUOTE )
						state = S_SEENPARAMEND;
					else
						state = S_INQUOTE;
					attr.append("\"");
				} else if( tag.charAt(i) == '=') {
					if( state == S_SEENPARAM || state == S_SEENPARAMEND ) 
						state = S_SEENPARAMEQUAL;
					attr.append("=");
				} else {
					if( Character.isWhitespace( tag.charAt(i) ) ) {
						if( state == S_INQUOTE )
							attr.append(tag.charAt(i));
						else
							state = S_SEENPARAMEND;
					} else {
						if( state == S_SEENPARAMEND ) {
							attrReady = true;
							state = S_SEENPARAM;
						} else if( state == S_INQUOTE ) {
							attr.append( tag.charAt(i) );
						} else {
							state = S_SEENPARAM;
							attr.append( tag.charAt(i) );
						}
					}
				}
				if( attrReady || i == tag.length() - 1 ) {
					if( seenTagName ) {
						String param = null;
						if( isBody && check( FL_EXTRACT_BODY ) ) {
							addBodyAttribute(attr.toString());
						} else if( (param = cleanAttr(attr.toString())) != null ) {
							if( unsecureImage && param.startsWith("alt=") )
								unsecureImgAlt = param;
							else if( unsecureImage && param.startsWith("src=") && param.indexOf("://") > -1 ) {
								addImageLink = true; //TODO we might want to put a custom text here
								tidyTag.append(" border=\"0\" src=\"#\" alt=\"").append("[image]").append("\"");
							} else {
								tidyTag.append(" ").append(param);
								if( param.startsWith("href=\"#"))
									addTarget = false;
							}
						}
					}
					else
						seenTagName = true;
					attr.setLength(0);
					attr.append(tag.charAt(i));
				}
			}

			if( isBody && check( FL_EXTRACT_BODY ) )
				return null;
			if( addImageLink ) {
				/*Cookie link = new Cookie( h.cookie );
				link.setCmd(h.cookie.getCmd());
				link.setForm(h.cookie.getForm());
				link.setTarget(h.cookie.getTarget());
				link.setParent(h.cookie.getParent());
				link.addKey("sp","");
				tidyTag.insert(0, "<a href=\"" + link.toString() + "\">" );*/
			} else if( addTarget )
				tidyTag.append(" target=\"_blank\"");		
			else if( isBody ) 
				tidyTag.append("><tr><td width=\"100%\" valign=\"top\"");
			else if( unsecureImgAlt != null )
				tidyTag.append(" ").append(unsecureImgAlt);
			
			return tidyTag.append(">").toString();
		}

		public int read() throws IOException {
			try {
				if( bodySource == null ) {
					
					// Get content type
					String contentType = null;
					
					Expression expContentType = (Expression)body.getFieldDefinition().getSetting(ObjectField.E_CONTENT_TYPE);
					try {
						if( expContentType != null )
							contentType = expContentType.evaluate(context.getVariables(), body.getParentNode());
					} catch (ExpressionException _) {
					}
					
					if( body instanceof DynamicAttrStream ) {
						InputStream stream = ((DynamicAttrStream)body).getInputStream();
						if( stream != null )
							bodySource = new InputStreamReader( new BufferedInputStream(stream, 4096), "utf-8");
						else
							bodySource = new StringReader("");
					} else
						bodySource = new StringReader(body.getValue());	
					
					if( contentType == null || contentType.isEmpty() || !contentType.toLowerCase().startsWith("text/"))
						contentType = "text/plain";
					if( contentType.equalsIgnoreCase("text/html") )
						set( FL_HTML_SOURCE );
					else
						clear( FL_HTML_SOURCE );
					
					/*NodeList list = ((DynamicElement)rte.getField("body-array").getBind().evaluate(context.getVariables(), mail, Expression.NODE)).getChildNodes();
					if( list == null || list.getLength() < 1)
						return -1;
					for( int i = 0; i < list.getLength() && bodySource == null; i++ ) {
						DynamicElement body = (DynamicElement)list.item(i);
						String type = rte.getField("body-type").getBind().evaluate(context.getVariables(), body);
						if( type != null ) {
							type = type.toLowerCase();
							if( type.equals("text/plain") && !check(FL_HTML_SOURCE) )
								bodySource = wrapStream( body );
							else if( type.equals("text/html") && check(FL_HTML_SOURCE) )
								bodySource = wrapStream( body );
						}
					}*/

					if( check( FL_REPLACE_BODY ) ) {
						StringBuffer tag = new StringBuffer("<body");
						if( bodyAttr != null ) {
							for( Iterator<String> it = bodyAttr.keySet().iterator(); it.hasNext(); ) {
								String key = (String) it.next();
								String value = (String) bodyAttr.get(key);
								if( key.equals("background") ) {
									if( value.toLowerCase().startsWith("cid:") )
										value = getCidAsCookie( "<" + value.substring(4) + ">" );
									else
										value = getCidAsCookie(value);
								}
								tag.append(" ").append(key).append("=\"").append(value).append("\"");
							}
						}
						if( bodyAttr == null || !bodyAttr.containsKey("bgcolor") )
							tag.append(" bgcolor=\"#ffffff\"");
						if( bodyAttr == null || !bodyAttr.containsKey("topmargin") )
							tag.append(" topmargin=\"0\"");
						if( bodyAttr == null || !bodyAttr.containsKey("leftmargin") )
							tag.append(" leftmargin=\"0\"");
						tag.append(">");
						buffer = new StringReader( tag.toString() );
					}
				}
				
				int c;
				if( buffer != null && ((c = buffer.read()) > -1 ) ) 
					return c;
				else if( buffer != null )
					buffer = null;

				if( check( FL_HTML_SOURCE ) ) {
					if( insideTag || (c = bodySource.read()) == '<' ) {
						StringBuffer tag = new StringBuffer();
						insideTag = false;
						while( true ) {
							c = bodySource.read();
							if( c == '<' ) {
								insideTag = true;
								if( !check(FL_SKIP_TAGS) ) {
									buffer = new StringReader( tag.toString() );
									return '<';
								} else
									return read();
							} else if( c == '>' ) {
								String tidyTag = cleanTag( tag.toString() );
								if( !check(FL_SKIP_TAGS) && tidyTag != null ) {
									buffer = new StringReader( tidyTag );
									return buffer.read();
								} else
									return read();
							} else if( c == -1 ) {
								buffer = new StringReader( tag.toString() );
								return '<';
							} else if( c == '\r' || c == '\n' )
								tag.append(' ');
							else
								tag.append((char)c);
						}
					} else if( c == -1 && check( FL_REPLACE_BODY ) ) {
						buffer = new StringReader("</body>");
						clear( FL_REPLACE_BODY );
						return buffer.read();
					} else
						return c;
				} else {
					c = bodySource.read();
					if( c == '\n' ) {
						buffer = new StringReader("<br/>");
						return buffer.read();
					} else if( c == '<' || c == '>' || c == '&' || c == '"' ) {
						buffer = new StringReader(HtmlCoder.encodeHtmlEntity(c));
						return buffer.read();
					} else if( check(FL_TXT_HIGHLIGHT) && (c == 'h' || c == 'f' || c == 'm' || c == 'n' || c == 't')) {
						StringBuffer link = new StringBuffer(50);
						link.append((char)c);
						int maxCount = 8;
						while( (c = bodySource.read()) > -1 && c != ':' && (maxCount-- > 0) )
							link.append((char)c);
						if( c == ':' ) {
							int nextC = bodySource.read(), followingC = bodySource.read();
							String uri = link.toString();
							link.append(":");
							if( nextC != -1 )
								link.append( (char)nextC );
							if( followingC != -1 )
								link.append( (char)followingC );
							if( uri.equalsIgnoreCase("mailto") || ( nextC == '/' && followingC == '/' &&
							    (uri.equalsIgnoreCase("http") || uri.equalsIgnoreCase("https") ||
							     uri.equalsIgnoreCase("ftp") || uri.equalsIgnoreCase("telnet") ||
							     uri.equalsIgnoreCase("news")) ) ) {
							     	maxCount = 512;
							     	while( (c = bodySource.read()) > -1 && !Character.isWhitespace((char)c)
							     	       && "<>'\"[]{}*".indexOf(c) < 0 && (maxCount-- > 0) )
							     		link.append((char)c);
									String linkName = link.toString();
									if( uri.equalsIgnoreCase("mailto") ) {
										uri = getMailtoAsCookie(linkName.substring(7)).toString();
									} else
										uri = linkName;
									link.setLength(0);
									link.append("<a href=\"").append(uri).append("\" target=\"_blank\">").append(linkName).append("</a>");
									if( c != -1 )
										link.append(HtmlCoder.encodeHtmlEntity(c));
							}
						} else if( c != -1 )
							link.append((char)c);
						buffer = new StringReader( link.toString().replaceAll("\n", "<br/>" ));
						return buffer.read();
					} else
						return c;
				}
			} catch( CodeGlideException e ) {
				Logger.debug(e);
				return -1;
			}
		}

		public void setBodyAttribute( String key, String value ) {
			if( bodyAttr == null )
				bodyAttr = new HashMap<String, String>();
			bodyAttr.put( key, value );
		}

		public String getBodyAttribute( String key ) {
			if( bodyAttr != null )
				return (String) bodyAttr.get(key);	
			else
				return null;
		}

		public Collection<String> getBodyAttributes() {
			if( bodyAttr != null)
				return bodyAttr.keySet();
			else
				return null;
		}
		
		public boolean hasBodyAttributes() {
			return( bodyAttr != null );	
		}

		public boolean markSupported() {
			return false;	
		}

		public void close() throws IOException {
			
		}

		public int read(char[] arg0, int arg1, int arg2) throws IOException {
			throw new IOException("Not implemented");
		}
	}


}

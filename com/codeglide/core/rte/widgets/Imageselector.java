package com.codeglide.core.rte.widgets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.codeglide.core.Expression;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.engines.FileStreamEngine;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.exceptions.RuntimeError;
import com.codeglide.core.rte.interfaces.UploadHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.xml.dom.DynamicAttr;
import com.codeglide.xml.dom.DynamicAttrStream;
import com.codeglide.xml.dom.DynamicElement;

public class Imageselector extends Widget implements UploadHandler {
	private Expression bindExpression, defaultExpression;
	
	public Imageselector(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
	
		Node rawImage = null;
		String imageUrl = null;
		try {
			rawImage = (Node)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
		} catch (Exception _) {}
		if( rawImage != null ) {
			if( rawImage instanceof DynamicElement ) {
				imageUrl = getUrl(context, (DynamicElement)rawImage);
			} else if( rawImage instanceof DynamicAttrStream ) {
				imageUrl = getUrl(context, (DynamicElement)((DynamicAttr)rawImage).getParentNode());
			}
		}
		if( defaultExpression != null ) {
			try {
				imageUrl = bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode());
			} catch (Exception _) {}
		}
		
		Action action = new Action(ActionType.IMAGESELECTOR);
		action.addParameter(ActionParameter.ID, generateWidgetId(context, this));
		action.addParameter(ActionParameter.VALUE, imageUrl);
		result.add(action);
	
	}

	protected void parseElement(Element element, Application application) {
		bindExpression = getExpression(element, "bind");
		defaultExpression = getExpression(element, "default");
	}

	public String handleUpload(ContextUi context, List<Action> result,
			ReceivedFile file)  throws CodeGlideException {

		Node rawImage = null;
		try {
			rawImage = (Node)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
		} catch (Exception _) {}

		if( file.getContentType() == null || !file.getContentType().toLowerCase().startsWith("image/") )
			throw new RuntimeError("@invalid-content-type");
		
		if( rawImage != null ) {
			if( rawImage instanceof DynamicElement ) {
				DynamicElement image = (DynamicElement)rawImage;
				image.setAttribute("Name", file.getName());
				image.setAttribute("Type", file.getContentType());
				image.setAttribute("Size", file.getSize());
				image.setAttribute("Bin", file);
				try {
					image.setAttribute("Id", "<" + UUID.randomUUID().toString() + "@" + InetAddress.getLocalHost().getCanonicalHostName() + ">");
				} catch (UnknownHostException _) {
				}
				return getUrl(context, (DynamicElement)image);
			} else if( rawImage instanceof DynamicAttrStream ) {
				((DynamicAttrStream)rawImage).setInputStream(file);
				return getUrl(context, (DynamicElement)((DynamicAttr)rawImage).getParentNode());
			}
		}
		return null;
	}

	private String getUrl( ContextUi context, DynamicElement image ) throws CodeGlideException {
		return FileStreamEngine.FILESTREAM_URL + generateElementId(context, this, image);
	}
	
	/*
	 * 
	 * 				CustomAction action = new CustomAction(WidgetElement.NULL);
				action.setPayload("<textarea>{imgUrl: '/cgrte/?p=" + generateElementId((GuiRequest)request, (VisibleWidget)widget, (DynamicElement)image) + "&e=dl" + "'}</textarea>");
				action.setContentType("text/html");
				result.addAction(action);
				result.setExitBlock();

	 */
	
}

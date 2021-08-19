package com.codeglide.core.rte.commands.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Element;

import com.codeglide.core.Expression;
import com.codeglide.core.objects.ObjectDefinition;
import com.codeglide.core.rte.Application;
import com.codeglide.core.rte.Item;
import com.codeglide.core.rte.ReceivedFile;
import com.codeglide.core.rte.contexts.ContextUi;
import com.codeglide.core.rte.engines.FileStreamEngine;
import com.codeglide.core.rte.exceptions.CodeGlideException;
import com.codeglide.core.rte.interfaces.UploadHandler;
import com.codeglide.core.rte.render.Action;
import com.codeglide.core.rte.render.ActionParameter;
import com.codeglide.core.rte.render.ActionType;
import com.codeglide.core.rte.widgets.Widget;
import com.codeglide.xml.dom.DynamicElement;

public class ShowFileDialog extends Widget implements UploadHandler {
	private Expression bindExpression;

	public ShowFileDialog(Item parent, Element element) {
		super(parent, element);
	}

	protected void render(ContextUi context, List<Action> result)
			throws CodeGlideException {
		Action action = new Action(ActionType.SHOW_FILE_DIALOG);
		action.addParameter(ActionParameter.ID, generateWidgetId(context, this));
		setRenderParameters(context, action);
		result.add(action);
	}

	protected void parseElement(Element element, Application application) {
		bindExpression = getExpression(element, "bind");
	}

	public String handleUpload(ContextUi context, List<Action> result,
			ReceivedFile file) throws CodeGlideException {
		DynamicElement fileNode = null;
		ObjectDefinition objDef = context.getApplication().getObject("File");
		if( objDef != null )
			fileNode = objDef.buildObject(context.getRootNode().getDocumentNode());
		else
			fileNode = new DynamicElement(context.getDocumentNode(), "File");
		fileNode.setAttribute("Name", file.getName());
		fileNode.setAttribute("Type", file.getContentType());
		fileNode.setAttribute("Size", file.getSize());
		fileNode.setAttribute("Bin", file);
		try {
			fileNode.setAttribute("Id", "<" + UUID.randomUUID().toString() + "@" + InetAddress.getLocalHost().getCanonicalHostName() + ">");
		} catch (UnknownHostException _) {
		}

		DynamicElement filesNode = (DynamicElement)bindExpression.evaluate(context.getVariables(), context.getRootNode().getDocumentNode(), Expression.NODE);
		if( filesNode != null )
			filesNode.appendChild(fileNode);

		return FileStreamEngine.FILESTREAM_URL + generateElementId(context, this, fileNode);
	}
	
}

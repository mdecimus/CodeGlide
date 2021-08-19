package com.codeglide.core.rte;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.codeglide.core.Expression;
import com.codeglide.core.Logger;
import com.codeglide.core.rte.commands.Break;
import com.codeglide.core.rte.commands.Call;
import com.codeglide.core.rte.commands.Case;
import com.codeglide.core.rte.commands.Exit;
import com.codeglide.core.rte.commands.For;
import com.codeglide.core.rte.commands.If;
import com.codeglide.core.rte.commands.ObjAdd;
import com.codeglide.core.rte.commands.ObjCreate;
import com.codeglide.core.rte.commands.ObjRemove;
import com.codeglide.core.rte.commands.ObjUpdate;
import com.codeglide.core.rte.commands.Setfield;
import com.codeglide.core.rte.commands.Setvar;
import com.codeglide.core.rte.commands.StrConcat;
import com.codeglide.core.rte.commands.StrCopy;
import com.codeglide.core.rte.commands.Sudo;
import com.codeglide.core.rte.commands.While;
import com.codeglide.core.rte.commands.ui.ComponentCommit;
import com.codeglide.core.rte.commands.ui.ComponentReload;
import com.codeglide.core.rte.commands.ui.ComponentRemove;
import com.codeglide.core.rte.commands.ui.FormSubmit;
import com.codeglide.core.rte.commands.ui.FormValidate;
import com.codeglide.core.rte.commands.ui.ItemAdd;
import com.codeglide.core.rte.commands.ui.ItemReload;
import com.codeglide.core.rte.commands.ui.ItemRemove;
import com.codeglide.core.rte.commands.ui.ItemRename;
import com.codeglide.core.rte.commands.ui.PanelAdd;
import com.codeglide.core.rte.commands.ui.PanelClear;
import com.codeglide.core.rte.commands.ui.PanelReload;
import com.codeglide.core.rte.commands.ui.ShowAlert;
import com.codeglide.core.rte.commands.ui.ShowMessage;
import com.codeglide.core.rte.commands.ui.ShowFileDialog;
import com.codeglide.core.rte.commands.ui.StartSession;
import com.codeglide.core.rte.commands.ui.StrWriter;
import com.codeglide.core.rte.commands.ui.WindowClose;
import com.codeglide.core.rte.commands.ui.WindowOpen;
import com.codeglide.core.rte.widgets.Button;
import com.codeglide.core.rte.widgets.Checkbox;
import com.codeglide.core.rte.widgets.Colorpicker;
import com.codeglide.core.rte.widgets.DataViewPanel;
import com.codeglide.core.rte.widgets.DateTimeField;
import com.codeglide.core.rte.widgets.GridPanel;
import com.codeglide.core.rte.widgets.Imageselector;
import com.codeglide.core.rte.widgets.Itemselector;
import com.codeglide.core.rte.widgets.Label;
import com.codeglide.core.rte.widgets.Labelfield;
import com.codeglide.core.rte.widgets.Link;
import com.codeglide.core.rte.widgets.Menu;
import com.codeglide.core.rte.widgets.MultiSelect;
import com.codeglide.core.rte.widgets.Panel;
import com.codeglide.core.rte.widgets.Radiogroup;
import com.codeglide.core.rte.widgets.Richtext;
import com.codeglide.core.rte.widgets.Select;
import com.codeglide.core.rte.widgets.SelectLink;
import com.codeglide.core.rte.widgets.Selectremote;
import com.codeglide.core.rte.widgets.Separator;
import com.codeglide.core.rte.widgets.Textfield;
import com.codeglide.core.rte.widgets.Toggler;
import com.codeglide.core.rte.widgets.TreePanel;

public abstract class Item {
	protected Item parent;
	
	public Item(Item parent) {
		this.parent = parent;
	}
	
	public Item( Item parent, Element element ) {
		this.parent = parent;
		parseElement(element, (Application)getAncestor(Application.class));
	}
	
	protected void setParent(Item parent) {
		this.parent = parent;
	}
	
	public Item getParent() {
		return parent;
	}
	
	public Item getAncestor( Class<?> className ) {
		Item runner = parent;
		while( runner != null && !(className.isInstance(runner)) )
			runner = runner.getParent();
		return runner;
	}
	
	// DOM parsing functions
	
	protected abstract void parseElement( Element element, Application application );
	
	protected List<Element> getChildrenElements(Element element) {
		LinkedList<Element> list = new LinkedList<Element>();
		NodeList childsList = element.getChildNodes();

		// Iterate over the tags and parse each kind.
		for (int i = 0; i < childsList.getLength(); i++) {
			Node child = childsList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
				list.add((Element)child);
		}
		return list;
	}
	
	protected Expression getExpression(Element element, String attrName) {
		return (element.hasAttribute(attrName)) ? new Expression(element.getAttribute(attrName)) : null;
	}
	
	protected boolean hasAttribute(Element element, String attrName) {
		return element.getAttribute(attrName) != null && !element.getAttribute(attrName).isEmpty();
	}

	public static Item createItem(Item parent, Element element) {
		String itemName = element.getNodeName().toLowerCase();
		if( itemName.equals("break") )
			return new Break(parent, element);
		else if( itemName.equals("call") )
			return new Call(parent, element);
		else if( itemName.equals("case") )
			return new Case(parent, element);
		else if( itemName.equals("exit") )
			return new Exit(parent, element);
		else if( itemName.equals("for") )
			return new For(parent, element);
		else if( itemName.equals("if") )
			return new If(parent, element);
		else if( itemName.equals("addobject") )
			return new ObjAdd(parent, element);
		else if( itemName.equals("createobject") )
			return new ObjCreate(parent, element);
		else if( itemName.equals("removeobject") )
			return new ObjRemove(parent, element);
		else if( itemName.equals("updateobject") )
			return new ObjUpdate(parent, element);
		else if( itemName.equals("setfield") )
			return new Setfield(parent, element);
		else if( itemName.equals("setvar") )
			return new Setvar(parent, element);
		else if( itemName.equals("strconcat") )
			return new StrConcat(parent, element);
		else if( itemName.equals("strcopy") )
			return new StrCopy(parent, element);
		else if( itemName.equals("sudo") )
			return new Sudo(parent, element);
		else if( itemName.equals("while") )
			return new While(parent, element);
		else if( itemName.equals("commitcomponent") )
			return new ComponentCommit(parent, element);
		else if( itemName.equals("reloadcomponent") )
			return new ComponentReload(parent, element);
		else if( itemName.equals("removecomponent") )
			return new ComponentRemove(parent, element);
		else if( itemName.equals("submitform") )
			return new FormSubmit(parent, element);
		else if( itemName.equals("validateform") )
			return new FormValidate(parent, element);
		else if( itemName.equals("additem") )
			return new ItemAdd(parent, element);
		else if( itemName.equals("reloaditem") )
			return new ItemReload(parent, element);
		else if( itemName.equals("removeitem") )
			return new ItemRemove(parent, element);
		else if( itemName.equals("renameitem") )
			return new ItemRename(parent, element);
		else if( itemName.equals("addpanel") )
			return new PanelAdd(parent, element);
		else if( itemName.equals("clearpanel") )
			return new PanelClear(parent, element);
		else if( itemName.equals("reloadpanel") )
			return new PanelReload(parent, element);
		else if( itemName.equals("showalert") )
			return new ShowAlert(parent, element);
		else if( itemName.equals("showmessage") )
			return new ShowMessage(parent, element);
		else if( itemName.equals("showfiledialog") )
			return new ShowFileDialog(parent, element);
		else if( itemName.equals("startsession") )
			return new StartSession(parent, element);
		else if( itemName.equals("stringwriter") )
			return new StrWriter(parent, element);
		else if( itemName.equals("closewindow") )
			return new WindowClose(parent, element);
		else if( itemName.equals("openwindow") )
			return new WindowOpen(parent, element);
		else if( itemName.equals("button") )
			return new Button(parent, element);
		else if( itemName.equals("checkbox") )
			return new Checkbox(parent, element);
		else if( itemName.equals("colorpicker") )
			return new Colorpicker(parent, element);
		else if( itemName.equals("dataviewpanel") )
			return new DataViewPanel(parent, element);
		else if( itemName.equals("gridpanel") )
			return new GridPanel(parent, element);
		else if( itemName.equals("imageselector") )
			return new Imageselector(parent, element);
		else if( itemName.equals("datefield") )
			return new DateTimeField(parent, element);
		else if( itemName.equals("itemselector") )
			return new Itemselector(parent, element);
		else if( itemName.equals("list") )
			return new MultiSelect(parent, element);
		else if( itemName.equals("linkselect") )
			return new SelectLink(parent, element);
		else if( itemName.equals("labelfield") )
			return new Labelfield(parent, element);
		else if( itemName.equals("label") )
			return new Label(parent, element);
		else if( itemName.equals("link") )
			return new Link(parent, element);
		else if( itemName.equals("menu") )
			return new Menu(parent, element);
		else if( itemName.equals("panel") || itemName.equals("form") || itemName.equals("fieldset") || itemName.equals("tabpanel") || itemName.equals("regionpanel") || itemName.equals("columnpanel") || itemName.equals("accordionpanel"))
			return new Panel(parent, element);
		else if( itemName.equals("radiogroup") )
			return new Radiogroup(parent, element);
		else if( itemName.equals("richtext") )
			return new Richtext(parent, element);
		else if( itemName.equals("select") )
			return new Select(parent, element);
		else if( itemName.equals("remoteselect") )
			return new Selectremote(parent, element);
		else if( itemName.equals("separator") )
			return new Separator(parent, element);
		else if( itemName.equals("textfield") )
			return new Textfield(parent, element);
		else if( itemName.equals("toggler") )
			return new Toggler(parent, element);
		else if( itemName.equals("treepanel") )
			return new TreePanel(parent, element);
		else
			Logger.debug("Unknown command '" + element.getNodeName() + "'.");
		
		return null;
	}

}

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
package com.codeglide.util.mimedir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.TimeZone;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import com.codeglide.util.ImageUtil;
import com.codeglide.xml.dom.DummyDocument;
import com.codeglide.xml.dom.DummyNamedNodeMap;
import com.codeglide.xml.dom.DummyNodeList;
import com.codeglide.xml.dom.DynamicElement;

public class VCard extends MimeDir {

	public VCard(DynamicElement contact) throws IOException {
		mimeType = "VCARD";
		// Evaluate attributes
		DummyNamedNodeMap listAttr = (DummyNamedNodeMap) contact
				.getAttributes();
		if (listAttr.getLength() >= 1) {

			// Last name - first name - middle name - h prefix - h suffix
			String[] names = new String[] { null, null, null, null, null };
			// Longitude - latitude
			String[] geo = new String[] { null, null };

			for (int i = 0; i < listAttr.getLength(); i++) {
				Attr attr = (Attr) listAttr.item(i);
				String attrName = attr.getName();

				if (attrName.equalsIgnoreCase("VCardName")) {
					addElement("FN", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("LastName")) {
					names[0] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("FirstName")) {
					names[1] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("MiddleName")) {
					names[2] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("HonorificPrefix")) {
					names[3] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("HonorificSuffix")) {
					names[4] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("NickName")) {
					addElement("NICKNAME", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("BirthDate")) {
					addElement("BDAY", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Photo")) {
					addElement("PHOTO", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Logo")) {
					addElement("LOGO", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Mailer")) {
					addElement("MAILER", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("TimeZone")) {
					addElement("TZ", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("GeoLongitude")) {
					geo[0] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("GeoLatitude")) {
					geo[1] = attr.getValue();
				} else if (attrName.equalsIgnoreCase("Title")) {
					addElement("TITLE", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Role")) {
					addElement("ROLE", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Category")) {
					addElement("CATEGORIES", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Note")) {
					addElement("NOTE", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("SortString")) {
					addElement("SORT-STRING", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Sound")) {
					addElement("SOUND", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("UID")) {
					addElement("UID", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("URL")) {
					addElement("URL", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Class")) {
					addElement("CLASS", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("XObject")) {
					addElement("X_OBJECT", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Source")) {
					addElement("SOURCE", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Revision")) {
					addElement("REV", null, attr.getValue());
				} else if (attrName.equalsIgnoreCase("Key")) {
					addElement("KEY", null, attr.getValue());
				}
			}
			addElement("N", null, names);
			if (geo != null)
				addElement("GEO", null, geo);
		}

		//Evaluate child
		DummyNodeList listChild = (DummyNodeList) contact.getChildNodes();

		if (listChild.getLength() >= 1) {
			for (int i = 0; i < listChild.getLength(); i++) {
				DynamicElement node = (DynamicElement) listChild.item(i);
				String nodeName = node.getNodeName();
				if (nodeName.equalsIgnoreCase("Addresses")) {

					DummyNodeList childs = (DummyNodeList) node.getChildNodes();
					for (int g = 0; g < childs.getLength(); g++) {
						DynamicElement nodeChild = (DynamicElement) childs
								.item(g);
						String[] address = new String[] { null, null, null,
								null, null, null, null };
						address[0] = nodeChild.getAttribute("PoBox");
						address[1] = nodeChild.getAttribute("ExtendedAddress");
						address[2] = nodeChild.getAttribute("StreetAddress");
						address[3] = nodeChild.getAttribute("Locality");
						address[4] = nodeChild.getAttribute("Region");
						address[5] = nodeChild.getAttribute("PostalCode");
						address[6] = nodeChild.getAttribute("CountryName");

						String type = nodeChild.getAttribute("Type");

						addElement("ADR", type, address);
					}
				} else if (nodeName.equalsIgnoreCase("Labels")) {
					DummyNodeList childs = (DummyNodeList) node.getChildNodes();
					for (int g = 0; g < childs.getLength(); g++) {
						DynamicElement nodeChild = (DynamicElement) childs
								.item(g);
						String type = nodeChild.getAttribute("Type");
						addElement("LABEL", type, nodeChild
								.getAttribute("Value"));
					}

				} else if (nodeName.equalsIgnoreCase("Phones")) {
					DummyNodeList childs = (DummyNodeList) node.getChildNodes();
					for (int g = 0; g < childs.getLength(); g++) {
						DynamicElement nodeChild = (DynamicElement) childs
								.item(g);
						String type = nodeChild.getAttribute("Type");
						addElement("TEL", type, nodeChild.getAttribute("Value"));
					}

				} else if (nodeName.equalsIgnoreCase("Mails")) {
					DummyNodeList childs = (DummyNodeList) node.getChildNodes();
					for (int g = 0; g < childs.getLength(); g++) {
						DynamicElement nodeChild = (DynamicElement) childs
								.item(g);
						addElement("EMAIL", nodeChild.getAttribute("Type"),
								nodeChild.getAttribute("Value"));
					}
				} else if (nodeName.equalsIgnoreCase("URLs")) {
					DummyNodeList childs = (DummyNodeList) node.getChildNodes();
					for (int g = 0; g < childs.getLength(); g++) {
						DynamicElement nodeChild = (DynamicElement) childs
								.item(g);
						addElement("URL", nodeChild.getAttribute("Type"),
								nodeChild.getAttribute("Value"));
					}
				} else if (nodeName.equalsIgnoreCase("Organization")) {
					String[] org = new String[] { null, null, null };
					org[0] = node.getAttribute("OrgName");
					org[1] = node.getAttribute("OrgUnit_1");
					org[2] = node.getAttribute("OrgUnit_2");
					addElement("ORG", null, org);
				}
			}
		}

	}

	public VCard(Reader in) throws Exception {
		super(in);
	}

	public DynamicElement getXml() throws Exception {
		return getXml(TimeZone.getTimeZone("GMT"), new DummyDocument());
	}

	public DynamicElement getXml(TimeZone tz, Document doc) throws Exception {
		if (mimeType == null || !mimeType.equalsIgnoreCase("vcard"))
			throw new Exception("This is not a vCard element: '" + mimeType
					+ "'");
		String fileAs = null, firstName = null, middleName = null, lastName = null, hPrefix = null, hSuffix = null;

		DynamicElement contact = new DynamicElement(doc, "vcard");
		DynamicElement phones = null, mails = null, addresses = null, labels = null, urls = null;
		for (Iterator it = elements.iterator(); it.hasNext();) {
			Object item = it.next();
			if (item instanceof MimeDir)
				continue;
			VElement ve = (VElement) item;
			item = ve.getValues();
			String textElement = null;
			if (item instanceof String)
				textElement = (String) item;
			else if (item instanceof String[])
				textElement = ((String[]) item)[0];

			if (ve.getName().equalsIgnoreCase("fn")) {
				fileAs = textElement;
				if (fileAs != null)
					contact.setAttribute("VCardName", fileAs);

			} else if (ve.getName().equalsIgnoreCase("n")) {
				if (item instanceof String)
					lastName = (String) item;
				else if (item instanceof String[]) {
					lastName = ((String[]) item)[0];
					if (((String[]) item).length > 1)
						firstName = ((String[]) item)[1];
					if (((String[]) item).length > 2)
						middleName = ((String[]) item)[2];
					if (((String[]) item).length > 3)
						hPrefix = ((String[]) item)[3];
					if (((String[]) item).length > 4)
						hSuffix = ((String[]) item)[4];
				}
				if (lastName != null) {
					contact.setAttribute("LastName", lastName);
				}
				if (firstName != null) {
					contact.setAttribute("FirstName", firstName);
				}
				if (middleName != null) {
					contact.setAttribute("MiddleName", middleName);
				}
				if (hPrefix != null) {
					contact.setAttribute("HonorificPrefix", hPrefix);
				}
				if (hSuffix != null) {
					contact.setAttribute("HonorificSuffix", hPrefix);
				}
			} else if (ve.getName().equalsIgnoreCase("nickname")) {
				contact.setAttribute("NickName", textElement);
			} else if (ve.getName().equalsIgnoreCase("photo")) {
				if (item instanceof byte[]) {
					InputStream imageStream = null;
					imageStream = ImageUtil.resampleImage(
							new ByteArrayInputStream((byte[]) item), 95, 95);
					contact.setAttribute("Photo", imageStream);
				}
			} else if (ve.getName().equalsIgnoreCase("bday")) {
				contact.setAttribute("BirthDate", textElement);
			} else if (ve.getName().equalsIgnoreCase("adr") && item instanceof String[]) {

				Iterator<String> itParams = ve.getParamKeys();
				while (itParams.hasNext()) {
					String[] params = ve.getParam((String) itParams.next());
					String adrType = null;

					if (params != null && params.length > 0) {
						for (int c = 0; c < params.length && adrType == null; c++) {
							if (params[c].equalsIgnoreCase("home"))
								adrType = "home";
							else if (params[c].equalsIgnoreCase("work"))
								adrType = "work";
							else if (params[c].equalsIgnoreCase("dom"))
								adrType = "dom";
							else if (params[c].equalsIgnoreCase("intl"))
								adrType = "intl";
							else if (params[c].equalsIgnoreCase("postal"))
								adrType = "postal";
							else if (params[c].equalsIgnoreCase("parcel"))
								adrType = "parcel";
							else if (params[c].equalsIgnoreCase("pref"))
								adrType = "pref";
							else if (params[c].equalsIgnoreCase("other"))
								adrType = "other";

							if (adrType != null) {
								if (addresses == null)
									addresses = (DynamicElement) contact
											.appendChild("Addresses");

								DynamicElement address = (DynamicElement) addresses
										.appendChild("Address");

								address.setAttribute("Type", adrType);

								// 0: pobox
								// 1: extended address
								// 2: street address
								// 3: city -- Locality
								// 4: state -- Region
								// 5: zip -- PostalCode
								// 6: country

								if (((String[]) item).length >= 1)
									address.setAttribute("PoBox",
											((String[]) item)[0]);
								if (((String[]) item).length >= 2)
									address.setAttribute("ExtendedAddress",
											((String[]) item)[1]);
								if (((String[]) item).length >= 3)
									address.setAttribute("StreetAddress",
											((String[]) item)[2]);
								if (((String[]) item).length >= 4)
									address.setAttribute("Locality",
											((String[]) item)[3]);
								if (((String[]) item).length >= 5)
									address.setAttribute("Region",
											((String[]) item)[4]);
								if (((String[]) item).length >= 6)
									address.setAttribute("PostalCode",
											((String[]) item)[5]);
								if (((String[]) item).length >= 7)
									address.setAttribute("CountryName",
											((String[]) item)[6]);

								adrType = null;
							}
						}
					}

				}

			} else if (ve.getName().equalsIgnoreCase("tel")) {

				Iterator<String> itParams = ve.getParamKeys();
				while (itParams.hasNext()) {
					String phoneType = null;
					String[] params = ve.getParam((String) itParams.next());
					if (params != null && params.length > 0) {
						for (int c = 0; c < params.length && phoneType == null; c++) {
							if (params[c].equalsIgnoreCase("home")) {
								phoneType = "home";
							} else if (params[c].equalsIgnoreCase("work")) {
								phoneType = "work";
							} else if (params[c].equalsIgnoreCase("fax"))
								phoneType = "fax";
							else if (params[c].equalsIgnoreCase("cell"))
								phoneType = "mobile";
							else if (params[c].equalsIgnoreCase("video"))
								phoneType = "otherfax";
							else if (params[c].equalsIgnoreCase("pager"))
								phoneType = "pager";
							else if (params[c].equalsIgnoreCase("bbs"))
								phoneType = "telex";
							else if (params[c].equalsIgnoreCase("modem"))
								phoneType = "ttytdd";
							else if (params[c].equalsIgnoreCase("car"))
								phoneType = "car";
							else if (params[c].equalsIgnoreCase("isdn"))
								phoneType = "isdn";
							else if (params[c].equalsIgnoreCase("pcs"))
								phoneType = "radio";
							else if (params[c].equalsIgnoreCase("pref"))
								phoneType = "pref";
							else if (params[c].equalsIgnoreCase("voice"))
								phoneType = "voice";
							else if (params[c].equalsIgnoreCase("msg"))
								phoneType = "msg";
							else if (params[c].equalsIgnoreCase("other"))
								phoneType = "other";

							if (phoneType != null) {
								if (phones == null)
									phones = (DynamicElement) contact
											.appendChild("Phones");

								DynamicElement phone = (DynamicElement) phones
										.appendChild("Phone");

								phone.setAttribute("Type", phoneType);
								phone.setAttribute("Value", textElement);
								phoneType = null;
							}
						}
					}
				}
			} else if (ve.getName().equalsIgnoreCase("email") && textElement != null) {

				Iterator<String> itParams = ve.getParamKeys();
				while (itParams.hasNext()) {
					String[] params = ve.getParam((String) itParams.next());
					String mailType = null;
					if (params != null && params.length > 0) {
						for (int c = 0; c < params.length && mailType == null; c++) {
							if (params[c].equalsIgnoreCase("pref"))
								mailType = "pref";
							else if (params[c].equalsIgnoreCase("x400"))
								mailType = "x400";
							else if (params[c].equalsIgnoreCase("internet"))
								mailType = "internet";
							else if (params[c].equalsIgnoreCase("home"))
								mailType = "home";
							else if (params[c].equalsIgnoreCase("work"))
								mailType = "work";
							else if (params[c].equalsIgnoreCase("other"))
								mailType = "other";

							if (mailType != null) {

								if (mails == null)
									mails = (DynamicElement) contact
											.appendChild("Mails");

								DynamicElement mail = (DynamicElement) mails
										.appendChild("Mail");

								mail.setAttribute("Type", mailType);
								mail.setAttribute("Value", textElement);

								mailType = null;
							}
						}
					}
				}
			} else if (ve.getName().equalsIgnoreCase("title") && textElement != null) {
				contact.setAttribute("Title", textElement);
			} else if (ve.getName().equalsIgnoreCase("role") && textElement != null) {
				contact.setAttribute("Role", textElement);
			} else if (ve.getName().equalsIgnoreCase("org") && textElement != null) {
				DynamicElement org = (DynamicElement) contact
						.appendChild("Organization");
				
				if (item instanceof String[]){
					if (((String[]) item).length >= 1)
						org.setAttribute("OrgName", ((String[]) item)[0]);
					if (((String[]) item).length >= 2)
						org.setAttribute("OrgUnit_1", ((String[]) item)[1]);
					if (((String[]) item).length >= 3)
						org.setAttribute("OrgUnit_2", ((String[]) item)[2]);
				}else{
					org.setAttribute("OrgName", textElement);
				}
				
			} else if (ve.getName().equalsIgnoreCase("categories") && textElement != null) {
				contact.setAttribute("category", textElement);
			} else if (ve.getName().equalsIgnoreCase("note") && textElement != null) {
				contact.setAttribute("Note", textElement);
			} else if (ve.getName().equalsIgnoreCase("url") && textElement != null) {

				String[] params = ve.getParam("url");
				String urlType = null;
				if (params != null && params.length > 0) {
					for (int c = 0; c < params.length && urlType == null; c++) {
						if (params[c].equalsIgnoreCase("personal"))
							urlType = "personal";
						else if (params[c].equalsIgnoreCase("work"))
							urlType = "work";
						else if (params[c].equalsIgnoreCase("organization"))
							urlType = "organization";
						else if (params[c].equalsIgnoreCase("other"))
							urlType = "other";
						else if (params[c].equalsIgnoreCase("home"))
							urlType = "home";

						if (urlType != null) {

							if (urls == null)
								urls = (DynamicElement) contact
										.appendChild("URLs");

							DynamicElement url = (DynamicElement) urls
									.appendChild("URL");

							url.setAttribute("Type", urlType);
							url.setAttribute("Value", textElement);

							urlType = null;
						}
					}
				}
				//if only is a URL (no type), type is "work"
				else {
					if (urls == null)
						urls = (DynamicElement) contact.appendChild("URLs");

					DynamicElement url = (DynamicElement) urls
							.appendChild("URL");
					url.setAttribute("Type", "work");
					url.setAttribute("Value", textElement);
				}

			} else if (ve.getName().equalsIgnoreCase("key")) {
				if (item instanceof byte[]) {
					try {
						CertificateFactory factory = CertificateFactory
								.getInstance("X.509");
						Certificate cert = factory
								.generateCertificate(new ByteArrayInputStream(
										(byte[]) item));
						contact.setAttribute("Key", new ByteArrayInputStream(
								cert.getEncoded()));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (ve.getName().equalsIgnoreCase("label") && textElement != null) {
				Iterator<String> itParams = ve.getParamKeys();
				while (itParams.hasNext()) {
					String[] params = ve.getParam((String) itParams.next());
					String labelType = null;
					if (params != null && params.length > 0) {
						for (int c = 0; c < params.length && labelType == null; c++) {
							if (params[c].equalsIgnoreCase("home"))
								labelType = "home";
							else if (params[c].equalsIgnoreCase("work"))
								labelType = "work";
							else if (params[c].equalsIgnoreCase("dom"))
								labelType = "dom";
							else if (params[c].equalsIgnoreCase("intl"))
								labelType = "intl";
							else if (params[c].equalsIgnoreCase("postal"))
								labelType = "postal";
							else if (params[c].equalsIgnoreCase("parcel"))
								labelType = "parcel";
							else if (params[c].equalsIgnoreCase("pref"))
								labelType = "pref";
							else if (params[c].equalsIgnoreCase("other"))
								labelType = "other";

							if (labelType != null) {

								if (labels == null)
									labels = (DynamicElement) contact
											.appendChild("Labels");

								DynamicElement label = (DynamicElement) labels
										.appendChild("Label");
								label.setAttribute("Type", labelType);
								label.setAttribute("Value", textElement);

								labelType = null;
							}
						}
					}
				}
			} else if (ve.getName().equalsIgnoreCase("rev") && textElement != null) {
				contact.setAttribute("Revision", textElement);
			} else if (ve.getName().equalsIgnoreCase("mailer") && textElement != null) {
				contact.setAttribute("Mailer", textElement);
			} else if (ve.getName().equalsIgnoreCase("geo") && textElement != null) {
				if (((String[]) item).length >= 1)
					contact.setAttribute("GeoLongitude", ((String[]) item)[0]);
				if (((String[]) item).length >= 2)
					contact.setAttribute("GeoLatitude", ((String[]) item)[1]);
			} else if (ve.getName().equalsIgnoreCase("tz") && textElement != null) {
				contact.setAttribute("TimeZone", textElement);
			} else if (ve.getName().equalsIgnoreCase("logo") && textElement != null) {
				if (item instanceof byte[]) {
					InputStream imageStream = null;
					imageStream = ImageUtil.resampleImage(
							new ByteArrayInputStream((byte[]) item), 95, 95);
					contact.setAttribute("Logo", imageStream);
				}
			} else if (ve.getName().equalsIgnoreCase("sort-string")
					&& textElement != null) {
				contact.setAttribute("SortString", textElement);
			} else if (ve.getName().equalsIgnoreCase("sound") && textElement != null) {
				InputStream soundStream = null;
				soundStream = new ByteArrayInputStream((byte[]) item);
				contact.setAttribute("Sound", soundStream);
			} else if (ve.getName().equalsIgnoreCase("UID") && textElement != null) {
				contact.setAttribute("UID", textElement);
			} else if (ve.getName().equalsIgnoreCase("class") && textElement != null) {
				contact.setAttribute("Class", textElement);
			} else if (ve.getName().equals("x_object") && textElement != null) {
				contact.setAttribute("XObject", textElement);
			} else if (ve.getName().equals("source") && textElement != null) {
				contact.setAttribute("Source", textElement);
			}
		}
		if (fileAs == null
				&& (firstName != null || middleName != null || lastName != null)) {
			if (lastName != null) {
				fileAs = lastName;
				if (firstName != null || middleName != null)
					fileAs += ", ";
			} else
				fileAs = "";
			if (firstName != null) {
				fileAs += firstName;
				if (middleName != null)
					fileAs += " ";
			}
			if (middleName != null)
				fileAs += middleName;
			contact.setAttribute("VCardName", fileAs);
		}

		return contact;
	}

	public void getVcard(Writer out) throws IOException {
		out.write("BEGIN:VCARD\r\n");
		out.write("VERSION:3.0\r\n");
		out.write("PRODID:-//CodeGlide MIMEDIR//EN\r\n");
		export(out);
		out.write("END:VCARD\r\n\r\n");
	}

}

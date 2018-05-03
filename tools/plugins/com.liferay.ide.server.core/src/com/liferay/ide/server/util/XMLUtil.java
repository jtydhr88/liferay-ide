/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.server.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author IBM
 */
public class XMLUtil {

	/**
	 * Create a child of the given node at the given index.
	 *
	 * @param doc
	 *            a document
	 * @param element
	 *            an element
	 * @param index
	 *            an index
	 * @param nodeName
	 *            a node name
	 * @return Element
	 */
	public static Element createChildElement(Document doc, Element element, int index, String nodeName) {
		Element element2 = doc.createElement(nodeName);

		try {
			NodeList childList = element.getElementsByTagName(nodeName);

			Node child = childList.item(index);

			element.insertBefore(element2, child);
		}
		catch (Exception e) {
			element.appendChild(element2);
		}

		return element2;
	}

	/**
	 * Create a child of the given node.
	 *
	 * @param doc
	 *            a document
	 * @param node
	 *            a node
	 * @param nodeName
	 *            a node name
	 * @return Element
	 */
	public static Element createChildElement(Document doc, Node node, String nodeName) {
		Element element = doc.createElement(nodeName);

		node.appendChild(element);

		return element;
	}

	/**
	 * Set the value of the given node to the given text.
	 */
	public static void createTextChildElement(Document doc, Node node, String name, String value) {
		Element element = createChildElement(doc, node, name);

		element.appendChild(doc.createTextNode(value));
	}

	/**
	 * Return the attribute value.
	 *
	 * @return String
	 * @param element
	 *            Element
	 * @param attr
	 *            String
	 */
	public static String getAttributeValue(Element element, String attr) {
		Attr attributeNode = element.getAttributeNode(attr);

		return attributeNode.getValue();
	}

	public static byte[] getContents(Document document) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream print = new PrintStream(out, true, "UTF-8")) {

			print(print, document);

			return out.toByteArray();
		}
		catch (Exception ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
	}

	public static DocumentBuilder getDocumentBuilder() {
		if (_documentBuilder == null) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

				factory.setValidating(false);
				factory.setNamespaceAware(false);
				factory.setExpandEntityReferences(false);

				// In case we happen to have a Xerces parser, try to set the feature that allows
				// Java encodings to be
				// used

				try {
					factory.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
				}
				catch (ParserConfigurationException pce) {

					// Ignore if feature isn't supported

				}

				// factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",
				// new
				// Boolean(false));

				_documentBuilder = factory.newDocumentBuilder();

				_documentBuilder.setEntityResolver(
					new EntityResolver() {

						public InputSource resolveEntity(String publicId, String systemId)
							throws IOException, SAXException {

							return new InputSource(new ByteArrayInputStream(new byte[0]));
					}

				});
			}
			catch (Exception e) {
			}
		}

		return _documentBuilder;
	}

	/**
	 * Return an iterator for the subelements.
	 *
	 * @return Iterator
	 * @param element
	 *            Element
	 * @param name
	 *            String
	 */
	@SuppressWarnings("rawtypes")
	public static Iterator getNodeIterator(Element element, String name) {
		List<Node> list = new ArrayList<>();
		NodeList nodeList = element.getElementsByTagName(name);

		int length = nodeList.getLength();

		for (int i = 0; i < length; i++) {
			list.add(nodeList.item(i));
		}

		return list.iterator();
	}

	/**
	 * Get the value of this node. Will return "" instead of null.
	 *
	 * @return String
	 * @param node
	 *            Node
	 */
	public static String getNodeValue(Node node) {
		NodeList nodeList = node.getChildNodes();

		int length = nodeList.getLength();

		for (int i = 0; i < length; i++) {
			Node n = nodeList.item(i);

			if (n instanceof Text) {
				Text t = (Text)n;

				return t.getNodeValue();
			}
		}

		return "";
	}

	/**
	 * Get the value of a subnode.
	 *
	 * @return String
	 */
	public static String getsubNodeValue(Element element, String name) {
		NodeList nodeList = element.getElementsByTagName(name);

		return getNodeValue(nodeList.item(0)).trim();
	}

	/**
	 * Insert the given text.
	 */
	public static void insertText(Document doc, Node node, String text) {
		node.appendChild(doc.createCDATASection(text));
	}

	public static void save(String filename, Document document) throws IOException {
		try (OutputStream outStream = new FileOutputStream(filename);
			BufferedOutputStream buffer = new BufferedOutputStream(outStream);
			PrintStream out = new PrintStream(buffer, true, "UTF-8")) {

			print(out, document);
		}
		catch (Exception ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
	}

	public static void save(String filename, Node node) throws IOException {
		try (OutputStream outStream = new FileOutputStream(filename);
			BufferedOutputStream buffer = new BufferedOutputStream(outStream);
			PrintStream out = new PrintStream(buffer, true, "UTF-8")) {

			print(out, node);
		}
		catch (Exception ex) {
			throw new IOException(ex.getLocalizedMessage());
		}
	}

	public static void setAttributeValue(Element element, String attr, String attValue) {
		Attr attributeNode = element.getAttributeNode(attr);

		attributeNode.setNodeValue(attValue);
	}

	/**
	 * Set the value of the subnode
	 *
	 * @param name
	 *            String
	 *
	 * @param value
	 *            String
	 */
	public static void setNodeValue(Node node, String name, String value) {
		String s = node.getNodeValue();

		if (s != null) {
			node.setNodeValue(value);
			return;
		}

		NodeList nodelist = node.getChildNodes();

		for (int i = 0; i < nodelist.getLength(); i++) {
			if (nodelist.item(i) instanceof Text) {
				Text text = (Text)nodelist.item(i);

				text.setData(value);
				return;
			}
		}

		return;
	}

	public static String toString(Document document) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
			PrintStream out = new PrintStream(baos)) {

			print(out, document);

			return new String(baos.toByteArray(), "UTF-8");
		}
		catch (Exception ex) {
		}

		return null;
	}

	protected static String getDocumentTypeData(DocumentType doctype) {
		String data = doctype.getName();

		if (doctype.getPublicId() != null) {
			data += " PUBLIC \"" + doctype.getPublicId() + "\"";
			String systemId = doctype.getSystemId();

			if (systemId == null) {
				systemId = "";
			}

			data += " \"" + systemId + "\"";
		}
		else {
			data += " SYSTEM \"" + doctype.getSystemId() + "\"";
		}

		return data;
	}

	protected static String normalize(String s) {
		StringBuffer stringbuffer = new StringBuffer();
		int i = s == null ? 0 : s.length();

		for (int j = 0; j < i; j++) {
			char c = s.charAt(j);

			switch (c) {
				case 60: /* '<' */
					stringbuffer.append("&lt;");

					break;

				case 62: /* '>' */
					stringbuffer.append("&gt;");

					break;

				case 38: /* '&' */
					stringbuffer.append("&amp;");

					break;

				case 34: /* '"' */
					stringbuffer.append("&quot;");

					break;

				case 10: /* '\n' */
				case 13: /* '\r' */
				default:
					stringbuffer.append(c);

					break;
			}
		}

		return stringbuffer.toString();
	}

	protected static void print(PrintStream out, Node node) {
		if (node == null) {
			return;
		}

		short type = node.getNodeType();

		switch (type) {
			case Node.DOCUMENT_NODE:
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

				NodeList nodelist = node.getChildNodes();

				int size = nodelist.getLength();

				for (int i = 0; i < size; i++)print(out, nodelist.item(i));

				break;

			case Node.DOCUMENT_TYPE_NODE: DocumentType docType = (DocumentType)node;

				out.print("<!DOCTYPE " + getDocumentTypeData(docType) + ">\n");

				break;

			case Node.ELEMENT_NODE:
				out.print('<');
				out.print(node.getNodeName());
				NamedNodeMap map = node.getAttributes();

				if (map != null) {
					int nodeSize = map.getLength();

					for (int i = 0; i < nodeSize; i++) {
						Attr attr = (Attr)map.item(i);
						out.print(' ');
						out.print(attr.getNodeName());
						out.print("=\"");
						out.print(normalize(attr.getNodeValue()));
						out.print('"');
					}
				}

				if (!node.hasChildNodes()) {
					out.print("/>");
				}
				else {
					out.print('>');
					NodeList nodeList = node.getChildNodes();

					int numChildren = nodeList.getLength();

					for (int i = 0; i < numChildren; i++)print(out, nodeList.item(i));

					out.print("</");
					out.print(node.getNodeName());
					out.print('>');
				}

				break;

			case Node.ENTITY_REFERENCE_NODE: NodeList referenceNodeList = node.getChildNodes();

				if (referenceNodeList != null) {
					int referenceNodeSize = referenceNodeList.getLength();

					for (int i = 0; i < referenceNodeSize; i++) {
						print(out, referenceNodeList.item(i));
					}
				}

				break;

			case Node.CDATA_SECTION_NODE: out.print(normalize(node.getNodeValue()));

				break;

			case Node.TEXT_NODE: out.print(normalize(node.getNodeValue()));

				break;

			case Node.PROCESSING_INSTRUCTION_NODE:
				out.print("<?");
				out.print(node.getNodeName());
				String s = node.getNodeValue();

				if ((s != null) && (s.length() > 0)) {
					out.print(' ');
					out.print(s);
				}

				out.print("?>");

				break;

			case Node.COMMENT_NODE:
				out.print("<!--");
				out.print(node.getNodeValue());
				out.print("-->");

				break;

			default: String originalValue = node.getNodeValue();

				String normalizeValue = normalize(originalValue);

				out.print(normalizeValue);

				break;
		}

		out.flush();
	}

	private static DocumentBuilder _documentBuilder;

}
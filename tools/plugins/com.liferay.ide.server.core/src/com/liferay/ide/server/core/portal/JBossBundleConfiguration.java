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

package com.liferay.ide.server.core.portal;

import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.util.XMLUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Simon Jiang
 */
public class JBossBundleConfiguration extends PortalBundleConfiguration {

	public JBossBundleConfiguration(PortalBundle bundle) {
		super(bundle);

		IPath appServerDir = bundle.getAppServerDir();

		IPath configurationDir = appServerDir.append("/standalone/configuration/");

		_configurationFilePath = configurationDir.append("standalone.xml");
	}

	@Override
	public void applyBundleChange(LiferayServerPort port) {
		if (LiferayServerPort.defaultStoreInXML.equals(port.getStoreLocation())) {
			Node attributeNode = _getAttributeNode("socket-binding", port.getId());

			if (attributeNode != null) {
				XMLUtil.setNodeValue(attributeNode, port.getId(), String.valueOf(port.getPort()));
			}
		}
	}

	@Override
	public List<LiferayServerPort> getConfiguredServerPorts() {
		List<LiferayServerPort> ports = new ArrayList<>();

		if (serverDirty == true) {
			if (FileUtil.exists(_configurationFilePath)) {
				try (InputStream input = new FileInputStream(_configurationFilePath.toFile())) {
					DocumentBuilder documentBuilder = XMLUtil.getDocumentBuilder();

					Document refreshDocument = documentBuilder.parse(new InputSource(input));

					ports = _getPortsValue(refreshDocument, "socket-binding");
				}
				catch (IOException | SAXException e) {
					LiferayServerCore.logError(e);
				}
			}
		}
		else {
			ports = _getPortsValue(configurationDocument, "socket-binding");
		}

		return ports;
	}

	@Override
	public int getHttpPort() {
		List<LiferayServerPort> ports = new ArrayList<>();

		if (serverDirty == true) {
			if (FileUtil.exists(_configurationFilePath)) {
				try (InputStream input = new FileInputStream(_configurationFilePath.toFile())) {
					DocumentBuilder documentBuilder = XMLUtil.getDocumentBuilder();

					Document refreshDocument = documentBuilder.parse(new InputSource(input));

					ports = _getPortsValue(refreshDocument, "socket-binding");
				}
				catch (IOException | SAXException e) {
					LiferayServerCore.logError(e);
				}
			}
		}
		else {
			ports = getConfiguredServerPorts();
		}

		for (LiferayServerPort port : ports) {
			if ("http".equalsIgnoreCase(port.getProtocol())) {
				return port.getPort();
			}
		}

		return -1;
	}

	@Override
	public void loadBundleConfiguration(IProgressMonitor monitor) throws CoreException {
		try {
			if (FileUtil.exists(_configurationFilePath)) {
				try (InputStream input = new FileInputStream(_configurationFilePath.toFile())) {
					DocumentBuilder documentBuilder = XMLUtil.getDocumentBuilder();

					configurationDocument = documentBuilder.parse(new InputSource(input));
				}
			}

			if (FileUtil.notExists(_configurationFilePath)) {
				throw new CoreException(
					new Status(
						IStatus.WARNING, LiferayServerCore.PLUGIN_ID, 0,
						"Could not load the JBoss server configuration", null));
			}
		}
		catch (Exception e) {
			throw new CoreException(LiferayServerCore.createErrorStatus(e));
		}
	}

	@Override
	public void saveBundleConfiguration(IProgressMonitor monitor) throws CoreException {
		try {
			if (FileUtil.exists(_configurationFilePath)) {
				if (serverDirty) {
					XMLUtil.save(_configurationFilePath.toOSString(), configurationDocument);
				}
			}
		}
		catch (Exception e) {
			throw new CoreException(LiferayServerCore.createErrorStatus(e));
		}
	}

	private Node _getAttributeNode(String tagName, String attrName) {
		try {
			Document document = configurationDocument;

			NodeList connectorNodes = document.getElementsByTagName(tagName);

			for (int i = 0; i < connectorNodes.getLength(); i++) {
				Node node = connectorNodes.item(i);

				NamedNodeMap attributes = node.getAttributes();

				for (int j = 0; j < attributes.getLength(); j++) {
					Node itemNode = attributes.item(j);

					if (itemNode != null) {
						String nodeName = itemNode.getNodeName();

						if (nodeName.equals("name")) {
							String nodeValue = itemNode.getNodeValue();

							if (nodeValue.equals(attrName.toLowerCase())) {
								return attributes.getNamedItem("port");
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			LiferayServerCore.logError(e);
		}

		return null;
	}

	private List<LiferayServerPort> _getPortsValue(Document document, String tagName) {
		List<LiferayServerPort> retPorts = new ArrayList<>();

		try {
			NodeList connectorNodes = document.getElementsByTagName(tagName);

			for (int i = 0; i < connectorNodes.getLength(); i++) {
				Node node = connectorNodes.item(i);

				NamedNodeMap attributes = node.getAttributes();

				for (int j = 0; j < attributes.getLength(); j++) {
					Node itemNode = attributes.item(j);

					if (itemNode != null) {
						String nodeName = itemNode.getNodeName();

						if (nodeName.equals("name")) {
							boolean existed = false;

							for (LiferayServerPort port : retPorts) {
								String portId = port.getId();

								if (portId.equals(itemNode.getNodeValue())) {
									existed = true;

									break;
								}
							}

							if (!existed) {
								Node portNode = attributes.getNamedItem("port");

								LiferayServerPort createServerPort = createLiferayServerPort(
									itemNode.getNodeValue(), itemNode.getNodeValue(), portNode.getNodeValue());

								retPorts.add(createServerPort);
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			LiferayServerCore.logError(e);
		}

		return retPorts;
	}

	private IPath _configurationFilePath;

}
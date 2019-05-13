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

package com.liferay.ide.upgrade.plugins.ui.internal.customjsps;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Andy Wu
 * @author Terry Jia
 */
public interface CustomJSPSupport {

	public default String[] get70FilePaths(File file) {
		IProject project = CoreUtil.getProject(file);

		IFolder resourceFolder = project.getFolder("/src/main/resources/META-INF/resources/");

		File newFile = FileUtil.getFile(resourceFolder);

		Path resourcePath = newFile.toPath();

		Path relativePath = resourcePath.relativize(file.toPath());

		String[] paths = new String[2];

		IFile original62File = resourceFolder.getFile("/.ignore/" + relativePath.toString() + ".62");
		IFile original70File = resourceFolder.getFile(relativePath.toString());

		if (original62File.exists() && original70File.exists()) {
			paths[0] = FileUtil.getLocationPortableString(original62File);
			paths[1] = FileUtil.getLocationPortableString(original70File);
		}

		return paths;
	}

	public default File[] getCompareFiles62(File file, File project) {
		File customJspDir = getCustomJspDir(project);

		Path customJspPath = customJspDir.toPath();

		Path relativePath = customJspPath.relativize(file.toPath());

		File[] files = new File[2];

		files[0] = null;
		files[1] = file;

		File originalJspFile = new File(
			getLiferay62ServerRootDirPath(getLiferay62ServerLocation()), relativePath.toString());

		if (originalJspFile.exists()) {
			files[0] = originalJspFile;
		}

		return files;
	}

	public default File getCustomJspDir(File project) {
		File hookFile = new File(project, "/docroot/WEB-INF/liferay-hook.xml");

		String customJspPath = null;

		try (InputStream inputStream = Files.newInputStream(hookFile.toPath())) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder domBuilder = documentBuilderFactory.newDocumentBuilder();

			domBuilder.setEntityResolver(
				new EntityResolver() {

					public InputSource resolveEntity(String publicId, String systemId)
						throws IOException, SAXException {

						// don't connect internet to fetch dtd for validation

						try (InputStream inputStream = new ByteArrayInputStream(new String("").getBytes())) {
							return new InputSource(inputStream);
						}
					}

				});

			Document document = domBuilder.parse(inputStream);

			Element root = document.getDocumentElement();

			NodeList nodeList = root.getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				if ((node.getNodeType() == Node.ELEMENT_NODE) && "custom-jsp-dir".equals(node.getNodeName())) {
					Node child = node.getFirstChild();

					customJspPath = child.getNodeValue();
				}
			}
		}
		catch (Exception e) {
		}

		if (CoreUtil.isNullOrEmpty(customJspPath)) {
			return null;
		}

		return new File(project, "/docroot/" + customJspPath);
	}

	public default File getLiferay62ServerLocation() {
		return new File("/Users/terryjia/work/portal/liferay-portal-6.2-ee-sp20");
	}

	public default File getLiferay62ServerRootDirPath(File serverLocation) {
		String[] names = serverLocation.list(
			(dir, name) -> {
				if (name.startsWith("tomcat-")) {
					return true;
				}

				return false;
			});

		File rootDir = null;

		if ((names != null) && (names.length == 1)) {
			rootDir = new File(serverLocation, names[0] + "/webapps/ROOT/");
		}

		return rootDir;
	}

}
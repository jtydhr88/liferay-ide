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

package com.liferay.ide.server.tomcat.core;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.tomcat.core.internal.TomcatRuntimeClasspathProvider;
import org.eclipse.wst.server.core.IRuntime;

/**
 * @author Greg Amerson
 */
@SuppressWarnings("restriction")
public class LiferayTomcatRuntimeClasspathProvider extends TomcatRuntimeClasspathProvider {

	public LiferayTomcatRuntimeClasspathProvider() {
	}

	@Override
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime)
	{

		IPath installPath = runtime.getLocation();

		if (installPath == null) {
			return new IClasspathEntry[0];
		}

		String runtimeId = runtime.getRuntimeType().getId();

		IClasspathEntry[] entries = resolveClasspathContainerForPath(installPath, runtimeId);

		// IDE-483

		ILiferayTomcatRuntime liferayTomcatRuntime = (ILiferayTomcatRuntime)runtime.loadAdapter(
			ILiferayTomcatRuntime.class, null);

		if (liferayTomcatRuntime != null) {
			if (liferayTomcatRuntime.getJavadocURL() != null) {
				entries = _getUpdatedJavadocEntries(entries, liferayTomcatRuntime);
			}

			if (liferayTomcatRuntime.getSourceLocation() != null) {
				entries = _getUpdatedSourceEntries(entries, liferayTomcatRuntime);
			}
		}

		return entries;
	}

	protected IClasspathEntry[] resolveClasspathContainerForPath(IPath installPath, String runtimeTypeId)
	{

		List<IClasspathEntry> list = new ArrayList<>();

		if (runtimeTypeId.endsWith("60") || runtimeTypeId.endsWith("70") ||
			FileUtil.exists(installPath.append("lib").toFile())) {

			IPath path = installPath.append("lib");

			addLibraryEntries(list, path.toFile(), true);
		}

		// go through all classpath entries and remove some unneeded ones

		List<IClasspathEntry> optimizedList = new ArrayList<>();

		List<String> excludes = Arrays.asList(ILiferayTomcatConstants.LIB_EXCLUDES);

		for (IClasspathEntry entry : list) {
			if (!excludes.contains(entry.getPath().lastSegment())) {
				optimizedList.add(entry);
			}
		}

		return (IClasspathEntry[])optimizedList.toArray(new IClasspathEntry[optimizedList.size()]);
	}

	protected void updateClasspath(IProject project, IRuntime runtime)
	{

		// IJavaProject javaProject = JavaCore.create(project);

	}

	private IClasspathEntry[] _getUpdatedJavadocEntries(
		IClasspathEntry[] entries, ILiferayTomcatRuntime liferayTomcatRuntime)
			{

		List<IClasspathEntry> updatedEntries = new ArrayList<>();

		String javadocURL = liferayTomcatRuntime.getJavadocURL();

		if (javadocURL != null) {
			for (IClasspathEntry existingEntry : entries)
			{
				IPath path = existingEntry.getPath();

				IClasspathEntry newEntry = null;

				for (String javadocJar : _JARS)
				{
					if (path.lastSegment().equalsIgnoreCase(javadocJar)) {
						IClasspathAttribute[] extraAttrs = existingEntry.getExtraAttributes();

						List<IClasspathAttribute> newExtraAttrs = new ArrayList<>();

						IClasspathAttribute javadocAttr = _newJavadocAttr(javadocURL);

						newExtraAttrs.add(javadocAttr);

						if (!CoreUtil.isNullOrEmpty(extraAttrs)) {
							for (IClasspathAttribute attr : extraAttrs)
							{
								if (!attr.getName().equals(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME)) {
									newExtraAttrs.add(attr);
								}
							}
						}

						newEntry = JavaCore.newLibraryEntry(
							existingEntry.getPath(), existingEntry.getSourceAttachmentPath(),
							existingEntry.getSourceAttachmentRootPath(), existingEntry.getAccessRules(),
							newExtraAttrs.toArray(new IClasspathAttribute[0]), existingEntry.isExported());
						break;
					}
				}

				if (newEntry != null) {
					updatedEntries.add(newEntry);
				}
				else {
					updatedEntries.add(existingEntry);
				}
			}
		}
		else {
			Collections.addAll(updatedEntries, entries);
		}

		return updatedEntries.toArray(new IClasspathEntry[0]);
	}

	private IClasspathEntry[] _getUpdatedSourceEntries(
		IClasspathEntry[] entries, ILiferayTomcatRuntime liferayTomcatRuntime)
			{

		List<IClasspathEntry> updatedEntries = new ArrayList<>();

		IPath sourceLocation = liferayTomcatRuntime.getSourceLocation();

		if (sourceLocation != null) {
			for (IClasspathEntry existingEntry : entries)
			{
				IPath path = existingEntry.getPath();

				IClasspathEntry newEntry = null;

				for (String sourceJar : _JARS)
				{
					if (path.lastSegment().equalsIgnoreCase(sourceJar)) {
						IPath sourcePath = existingEntry.getSourceAttachmentPath();

						if (sourcePath == null) {
							sourcePath = sourceLocation;
						}

						newEntry = JavaCore.newLibraryEntry(
							existingEntry.getPath(), sourcePath, existingEntry.getSourceAttachmentRootPath(),
							existingEntry.getAccessRules(), existingEntry.getExtraAttributes(),
							existingEntry.isExported());

						break;
					}
				}

				if (newEntry != null) {
					updatedEntries.add(newEntry);
				}
				else {
					updatedEntries.add(existingEntry);
				}
			}
		}
		else {
			Collections.addAll(updatedEntries, entries);
		}

		return updatedEntries.toArray(new IClasspathEntry[0]);
	}

	private IClasspathAttribute _newJavadocAttr(String url)
	{

		return JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, url);
	}

	private static final String[] _JARS = {"portal-impl.jar", "portal-service.jar", "support-tomcat.jar", };

}
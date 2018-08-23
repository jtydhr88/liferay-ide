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

package com.liferay.ide.project.core.modules;

import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.project.core.ProjectCore;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.java.JavaPackageName;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author Simon Jiang
 */
@SuppressWarnings("restriction")
public class NewLiferayComponentValidationService extends ValidationService {

	@Override
	public void dispose() {
		NewLiferayComponentOp op = _op();

		if (_listener != null) {
			Value<Object> projectNameProperty = op.property(NewLiferayComponentOp.PROP_PROJECT_NAME);

			projectNameProperty.detach(_listener);

			Value<Object> packageNameProperty = op.property(NewLiferayComponentOp.PROP_PACKAGE_NAME);

			packageNameProperty.detach(_listener);

			Value<Object> templateNameProperty = op.property(NewLiferayComponentOp.PROP_COMPONENT_CLASS_TEMPLATE_NAME);

			templateNameProperty.detach(_listener);

			_listener = null;
		}

		super.dispose();
	}

	@Override
	protected Status compute() {
		Status retval = Status.createOkStatus();

		NewLiferayComponentOp op = _op();

		Value<String> componentClassName = op.getComponentClassName();

		String className = componentClassName.content(true);

		if (!CoreUtil.isNullOrEmpty(className)) {
			IStatus status = JavaConventions.validateJavaTypeName(
				className, CompilerOptions.VERSION_1_7, CompilerOptions.VERSION_1_7);

			int classNameStatus = status.getSeverity();

			if (className.indexOf('.') != -1) {
				classNameStatus = IStatus.ERROR;
			}

			if (classNameStatus == IStatus.ERROR) {
				retval = Status.createErrorStatus("Invalid class name");
			}
		}

		Value<String> projectNameValue = op.getProjectName();

		String projectName = projectNameValue.content(true);

		if (projectName != null) {
			IProject project = CoreUtil.getProject(projectName);

			if (project != null) {
				try {
					Value<JavaPackageName> javaPackageName = op.getPackageName();

					JavaPackageName pack = javaPackageName.content(true);

					if (pack != null) {
						String packageName = pack.toString();

						IPath packageFullPath = new Path(packageName.replace('.', IPath.SEPARATOR));

						String fileName = className + ".java";

						IPath javaPath = packageFullPath.append(fileName);

						ILiferayProject liferayProject = LiferayCore.create(project);

						IFolder sourceFolder = liferayProject.getSourceFolder("java");

						IFile file = sourceFolder.getFile(javaPath);

						IPath javaFileLocation = file.getLocation();

						File javaFile = javaFileLocation.toFile();

						if (FileUtil.exists(javaFile)) {
							retval = Status.createErrorStatus("Type with same name but different case exists.");
						}

						IJavaProject javaProject = JavaCore.create(project);

						IType type = javaProject.findType(packageName + "." + className);

						if (type != null) {
							retval = Status.createErrorStatus(packageName + "." + className + " already exists.");
						}
					}
				}
				catch (Exception e) {
					ProjectCore.logError("Checking component class name failed.", e);
				}
			}
		}

		return retval;
	}

	@Override
	protected void initValidationService() {
		super.initValidationService();

		_listener = new FilteredListener<PropertyContentEvent>() {

			@Override
			protected void handleTypedEvent(PropertyContentEvent event) {
				refresh();
			}

		};

		NewLiferayComponentOp op = _op();

		Value<Object> projectNameProperty = op.property(NewLiferayComponentOp.PROP_PROJECT_NAME);

		projectNameProperty.attach(_listener);

		Value<Object> packageNameProperty = op.property(NewLiferayComponentOp.PROP_PACKAGE_NAME);

		packageNameProperty.attach(_listener);

		Value<Object> templateNameProperty = op.property(NewLiferayComponentOp.PROP_COMPONENT_CLASS_TEMPLATE_NAME);

		templateNameProperty.attach(_listener);
	}

	private NewLiferayComponentOp _op() {
		return context(NewLiferayComponentOp.class);
	}

	private FilteredListener<PropertyContentEvent> _listener;

}
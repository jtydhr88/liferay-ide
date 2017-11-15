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

package com.liferay.ide.layouttpl.ui.wizard;

import com.liferay.ide.core.ILiferayConstants;
import com.liferay.ide.core.ILiferayPortal;
import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.StringPool;
import com.liferay.ide.layouttpl.core.model.LayoutTplElement;
import com.liferay.ide.layouttpl.core.operation.INewLayoutTplDataModelProperties;
import com.liferay.ide.layouttpl.core.operation.LayoutTplDescriptorHelper;
import com.liferay.ide.layouttpl.core.util.LayoutTplUtil;
import com.liferay.ide.layouttpl.ui.LayoutTplUI;
import com.liferay.ide.layouttpl.ui.util.LayoutTemplatesFactory;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.project.ui.wizard.LiferayDataModelOperation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URL;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * @author Gregory Amerson
 * @author Cindy Li
 * @author Kuo Zhang
 */
@SuppressWarnings("restriction")
public class AddLayoutTplOperation extends LiferayDataModelOperation implements INewLayoutTplDataModelProperties {

	public AddLayoutTplOperation(IDataModel model, TemplateStore templateStore, TemplateContextType contextType) {
		super(model, templateStore, contextType);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus retval = null;

		IDataModel dm = getDataModel();

		String diagramClassName = dm.getStringProperty(LAYOUT_TEMPLATE_ID);

		LayoutTplElement diagramModel = createLayoutTplDigram(dm, _isBootstrapStyle(), diagramClassName);

		try {
			IFile templateFile = null;

			String templateFileName = getDataModel().getStringProperty(LAYOUT_TEMPLATE_FILE);

			if (!CoreUtil.isNullOrEmpty(templateFileName)) {
				templateFile = createTemplateFile(templateFileName, diagramModel);
			}

			getDataModel().setProperty(LAYOUT_TPL_FILE_CREATED, templateFile);

			String wapTemplateFileName = getDataModel().getStringProperty(LAYOUT_WAP_TEMPLATE_FILE);

			diagramModel.setClassName(diagramClassName + ".wap");

			if (!CoreUtil.isNullOrEmpty(wapTemplateFileName)) {
				createTemplateFile(wapTemplateFileName, diagramModel);
			}

			String thumbnailFileName = getDataModel().getStringProperty(LAYOUT_THUMBNAIL_FILE);

			if (!CoreUtil.isNullOrEmpty(thumbnailFileName)) {
				createThumbnailFile(thumbnailFileName);
			}
		}
		catch (CoreException ce) {
			LayoutTplUI.logError(ce);

			return LayoutTplUI.createErrorStatus(ce);
		}
		catch (IOException ioe) {
			LayoutTplUI.logError(ioe);

			return LayoutTplUI.createErrorStatus(ioe);
		}

		LayoutTplDescriptorHelper layoutTplDescHelper = new LayoutTplDescriptorHelper(getTargetProject());

		retval = layoutTplDescHelper.addNewLayoutTemplate(dm);

		return retval;
	}

	public IProject getTargetProject() {
		String projectName = model.getStringProperty(PROJECT_NAME);

		return ProjectUtil.getProject(projectName);
	}

	protected LayoutTplElement createLayoutTplDigram(IDataModel dm, boolean bootstrapStyle, String className) {
		LayoutTplElement layoutTpl = LayoutTplElement.TYPE.instantiate();

		layoutTpl.setBootstrapStyle(bootstrapStyle);
		layoutTpl.setClassName(className);

		if (dm.getBooleanProperty(LAYOUT_IMAGE_1_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_1(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_1_2_I_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_1_2_I(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_1_2_II_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_1_2_II(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_1_2_1_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_1_2_1(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_2_I_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_2_I(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_2_II_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_2_II(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_2_III_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_2_III(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_2_2_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_2_2(layoutTpl);
		}
		else if (dm.getBooleanProperty(LAYOUT_IMAGE_3_COLUMN)) {
			LayoutTemplatesFactory.add_Layout_3(layoutTpl);
		}

		return layoutTpl;
	}

	protected IFile createTemplateFile(String templateFileName, LayoutTplElement element) throws CoreException {
		IFolder defaultDocroot = CoreUtil.getDefaultDocrootFolder(getTargetProject());

		IFile templateFile = defaultDocroot.getFile(templateFileName);

		if (element != null) {
			LayoutTplUtil.saveToFile(element, templateFile, null);
		}
		else {
			ByteArrayInputStream input = new ByteArrayInputStream(StringPool.EMPTY.getBytes());

			if (templateFile.exists()) {
				templateFile.setContents(input, IResource.FORCE, null);
			}
			else {
				templateFile.create(input, true, null);
			}
		}

		return templateFile;
	}

	protected void createThumbnailFile(String thumbnailFileName) throws CoreException, IOException {
		IFolder defaultDocroot = CoreUtil.getDefaultDocrootFolder(getTargetProject());

		IFile thumbnailFile = defaultDocroot.getFile(thumbnailFileName);

		LayoutTplUI defaultUI = LayoutTplUI.getDefault();

		Bundle bundle = defaultUI.getBundle();

		URL iconFileURL = bundle.getEntry("/icons/blank_columns.png");

		CoreUtil.prepareFolder((IFolder)thumbnailFile.getParent());

		if (thumbnailFile.exists()) {
			thumbnailFile.setContents(iconFileURL.openStream(), IResource.FORCE, null);
		}
		else {
			thumbnailFile.create(iconFileURL.openStream(), true, null);
		}
	}

	private boolean _isBootstrapStyle() {
		ILiferayProject lrproject = LiferayCore.create(getTargetProject());

		ILiferayPortal portal = lrproject.adapt(ILiferayPortal.class);

		if (portal != null) {
			Version version = new Version(portal.getVersion());

			if (CoreUtil.compareVersions(version, ILiferayConstants.V620) >= 0) {
				return true;
			}

			return false;
		}

		return true;
	}

}
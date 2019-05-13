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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * @author Andy Wu
 * @author Terry Jia
 */
public class CompareItem extends BufferedContent implements IEditableContent, IModificationDate, ITypedElement {

	public CompareItem(File file) {
		_file = file;
	}

	public Image getImage() {
		return CompareUI.DESC_CTOOL_NEXT.createImage();
	}

	public long getModificationDate() {
		return System.currentTimeMillis();
	}

	public String getName() {
		return _file.getName();
	}

	public String getType() {
		return ITypedElement.TEXT_TYPE;
	}

	public boolean isEditable() {
		return false;
	}

	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return null;
	}

	protected InputStream createStream() throws CoreException {
		try {
			return Files.newInputStream(_file.toPath());
		}
		catch (IOException ioe) {
		}

		return new ByteArrayInputStream(new byte[0]);
	}

	private File _file;

}
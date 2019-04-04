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

package com.liferay.ide.project.ui.repl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * @author Terry Jia
 */
public class SnippetMessages {

	private static final String RESOURCE_BUNDLE = "org.eclipse.jdt.internal.debug.ui.snippeteditor.SnippetMessages";//$NON-NLS-1$

	private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private SnippetMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		}
	}

	public static String getFormattedString(String key, Object arg) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";
		}
		if (arg == null)
			arg = "";
		return NLS.bind(format, new Object[] { arg });
	}

	static ResourceBundle getBundle() {
		return fgResourceBundle;
	}
}

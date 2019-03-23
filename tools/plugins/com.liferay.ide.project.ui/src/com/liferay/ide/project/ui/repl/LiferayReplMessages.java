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
 * @author Gregory Amerson
 */
public class LiferayReplMessages {

	public static ResourceBundle getBundle() {
		return _resourceBundle;
	}

	public static String getFormattedString(String key, Object arg) {
		String format = null;

		try {
			format = _resourceBundle.getString(key);
		}
		catch (MissingResourceException mre) {
			return "!" + key + "!";
		}

		if (arg == null) {
			arg = "";
		}

		return NLS.bind(format, new Object[] {arg});
	}

	public static String getString(String key) {
		try {
			return _resourceBundle.getString(key);
		}
		catch (MissingResourceException mre) {
			return "!" + key + "!";
		}
	}

	private LiferayReplMessages() {
	}

	private static final ResourceBundle _resourceBundle = ResourceBundle.getBundle(
		"com.liferay.ide.project.ui.repl.LiferayReplMessages");

}
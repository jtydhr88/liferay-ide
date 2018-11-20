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

package com.liferay.ide.project.core.model.internal;

import org.eclipse.sapphire.ConversionException;
import org.eclipse.sapphire.ConversionService;

import org.osgi.framework.Version;

/**
 * @author Charles Wu
 */
public class StringToOSGiVersionConversionService extends ConversionService<String, Version> {

	public StringToOSGiVersionConversionService() {
		super(String.class, Version.class);
	}

	@Override
	public Version convert(String version) throws ConversionException {
		Version result = null;

		try {
			result = Version.parseVersion(version);
		}
		catch (IllegalArgumentException iae) {

			// Intentionally ignored.

		}

		if (result == Version.emptyVersion) {
			return null;
		}

		return result;
	}

}
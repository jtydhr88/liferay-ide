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

package com.liferay.ide.core;

import com.liferay.ide.core.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

/**
 * @author Charles Wu
 */
public class TargetPlatformManager implements Serializable {

	public static boolean shouldSerialize = false;

	public static TargetPlatformManager deserialize() {
		File objectLocation = _getObjectLocation();

		if (FileUtil.notExists(objectLocation)) {
			return null;
		}

		TargetPlatformManager instance = null;

		try (FileInputStream fis = new FileInputStream(objectLocation);
			ObjectInputStream oi = new ObjectInputStream(fis)) {

			instance = (TargetPlatformManager)oi.readObject();
		}
		catch (Exception e) {
			LiferayCore.logError(e);
		}

		return instance;
	}

	public static TargetPlatformManager getInstance() {
		if (_instance == null) {
			TargetPlatformManager object = deserialize();

			if (object != null) {
				_instance = object;

				// makesure this map would be initialized when this class was modified.

				if (_instance._targetPlatformArtifacts == null) {
					_instance._targetPlatformArtifacts = new HashMap<>();
				}
			}
			else {
				_instance = new TargetPlatformManager();
			}
		}

		return _instance;
	}

	public List<Artifact> getArtifactsByVersion(String version) {
		List<Artifact> artifacts = _targetPlatformArtifacts.get(version);

		if (artifacts == null) {
			artifacts = Collections.emptyList();
		}

		return artifacts;
	}

	public void put(String version, List<Artifact> artifacts) {
		_targetPlatformArtifacts.put(version, artifacts);

		shouldSerialize = true;
	}

	public void serialize() {
		try (FileOutputStream fos = new FileOutputStream(_getObjectLocation());
			ObjectOutputStream oos = new ObjectOutputStream(fos)) {

			oos.writeObject(_instance);
		}
		catch (Exception e) {
			LiferayCore.logError(e);
		}
	}

	private static File _getObjectLocation() {
		LiferayCore plugin = LiferayCore.getDefault();

		IPath location = plugin.getStateLocation();

		return new File(location.toFile(), "TargetPlatformManager.object");
	}

	private TargetPlatformManager() {
		_targetPlatformArtifacts = new HashMap<>();
	}

	private static TargetPlatformManager _instance;

	private static final long serialVersionUID = 4178530885085379265L;

	private Map<String, List<Artifact>> _targetPlatformArtifacts;

}
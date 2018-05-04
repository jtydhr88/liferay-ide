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

import com.liferay.ide.core.properties.PortalPropertiesConfiguration;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.server.core.LiferayServerCore;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.Bundle;

import org.w3c.dom.Document;

/**
 * @author Simon Jiang
 */
public abstract class PortalBundleConfiguration implements IPortalBundleConfiguration {

	public static final String MODIFY_PORT_PROPERTY = "modifyPort";

	public static final String NAME_PROPERTY = "name";

	public static final String PORT_PROPERTY = "port";

	public PortalBundleConfiguration(PortalBundle bundle) {
		this.bundle = bundle;
	}

	/**
	 * Adds a property change listener to this server.
	 *
	 * @param listener
	 *            PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (_propertyListeners == null) {
			_propertyListeners = new ArrayList<>();
		}

		_propertyListeners.add(listener);
	}

	public abstract void applyBundleChange(LiferayServerPort port) throws CoreException;

	public void applyChange(LiferayServerPort port) {
		try {
			String storeLocation = port.getStoreLocation();

			if (storeLocation.equals(LiferayServerPort.defaultStoreInProperties)) {
				if (!portalExtProperties.containsKey(FRAMEWORK_OSGI_CONSOLE_NAME)) {
					portalExtProperties.addProperty(FRAMEWORK_OSGI_CONSOLE_NAME, "localhost:" + port.getPort());
				}
				else {
					portalExtProperties.setProperty(FRAMEWORK_OSGI_CONSOLE_NAME, "localhost:" + port.getPort());
				}
			}

			applyBundleChange(port);
		}
		catch (Exception e) {
			LiferayServerCore.logError(e);
		}
	}

	public LiferayServerPort createLiferayServerPort(final String id, final String portName, final String portValue) {
		String retVal = null;

		if (!CoreUtil.empty(portValue)) {
			if (portValue.lastIndexOf(":") == -1) {
				retVal = portValue;
			}
			else {
				retVal = portValue.substring(portValue.lastIndexOf(":") + 1, portValue.length() - 1);
			}
		}

		return new LiferayServerPort(
			id, StringUtils.capitalize(StringUtils.replace(portName, "-", " ")), Integer.parseInt(retVal),
			StringUtils.capitalize(portName));
	}

	@Override
	public int getTelnetPort() {
		String retVal = "11311";

		if (portalExtProperties.containsKey(FRAMEWORK_OSGI_CONSOLE_NAME)) {
			String osginUrl = (String)portalExtProperties.getProperty(FRAMEWORK_OSGI_CONSOLE_NAME);

			String urlRemoveLocalhost = osginUrl.replaceAll("localhost:", "");

			String telnetPort = urlRemoveLocalhost.trim();

			if (!CoreUtil.empty(telnetPort)) {
				retVal = telnetPort;
			}
		}

		return Integer.parseInt(retVal);
	}

	public void importFromPath(IProgressMonitor monitor) throws CoreException {
		load(monitor);
	}

	// property change listeners

	public void load(IProgressMonitor monitor) {
		try {
			IPath liferayHomePath = bundle.getLiferayHome();

			IPath portalExtPropertiesPath = liferayHomePath.append(PORTAL_EXT_PROPERTIES);

			if (FileUtil.exists(portalExtPropertiesPath)) {
				try (FileInputStream sream = new FileInputStream(portalExtPropertiesPath.toFile())) {
					portalExtProperties = new PortalPropertiesConfiguration();

					portalExtProperties.load(sream);
				}
			}
			else {
				portalExtProperties = new PortalPropertiesConfiguration();
			}

			loadBundleConfiguration(monitor);
		}
		catch (Exception e) {
			LiferayServerCore.logError(e);
		}
	}

	public abstract void loadBundleConfiguration(IProgressMonitor monitor) throws CoreException;

	/**
	 * Modify the port with the given id.
	 *
	 * @param id
	 *            String
	 * @param port
	 *            int
	 */
	public void modifyServerPort(String id, int port) {
		try {
			serverDirty = true;
			firePropertyChangeEvent(MODIFY_PORT_PROPERTY, id, Integer.valueOf(port));
		}
		catch (Exception e) {
			LiferayServerCore.logError(e);
		}
	}

	public List<LiferayServerPort> readDefaultPorts(PortalBundle bundle) {
		List<LiferayServerPort> deaultPorts = null;

		try {
			LiferayServerCore serverCorePlugin = LiferayServerCore.getDefault();

			Bundle serverBundle = serverCorePlugin.getBundle();

			URL bundlesFodlerUrl = FileLocator.toFileURL(serverBundle.getEntry("bundles"));

			final File bundleFolder = new File(bundlesFodlerUrl.getFile());

			File[] bundleConfigurationFiles = bundleFolder.listFiles(
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						String bundleType = bundle.getType();

						if (name.startsWith(bundleType)) {
							return true;
						}

						return false;
					}

				});

			if (bundleConfigurationFiles[0].exists()) {
				final ObjectMapper mapper = new ObjectMapper();

				deaultPorts = mapper.readValue(
					bundleConfigurationFiles[0], new TypeReference<List<LiferayServerPort>>() {});
			}
		}
		catch (Exception e) {
			LiferayServerCore.logError("Can't find bundle deafult ports configurations.", e);
		}

		return deaultPorts;
	}

	/**
	 * Removes a property change listener from this server.
	 *
	 * @param listener
	 *            PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (_propertyListeners != null) {
			_propertyListeners.remove(listener);
		}
	}

	public void save(IProgressMonitor monitor) {
		try {
			if (serverDirty) {
				IPath liferayHomePath = bundle.getLiferayHome();

				IPath extPropertiesFile = liferayHomePath.append(PORTAL_EXT_PROPERTIES);

				File portalExtPropertiesFile = extPropertiesFile.toFile();

				if (!portalExtPropertiesFile.exists()) {
					portalExtPropertiesFile.createNewFile();
				}

				portalExtProperties.save(portalExtPropertiesFile);
			}

			saveBundleConfiguration(monitor);

			serverDirty = false;
		}
		catch (Exception e) {
			LiferayServerCore.logError(e);
		}
	}

	public abstract void saveBundleConfiguration(IProgressMonitor monitor) throws CoreException;

	/**
	 * Return a string representation of this object.
	 *
	 * @return String
	 */
	public String toString() {
		Class<? extends PortalBundleConfiguration> classRef = getClass();

		String className = classRef.getName();

		return className;
	}

	protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		if (_propertyListeners == null) {
			return;
		}

		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);

		try {
			Iterator<PropertyChangeListener> iterator = _propertyListeners.iterator();

			while (iterator.hasNext()) {
				try {
					PropertyChangeListener listener = (PropertyChangeListener)iterator.next();

					listener.propertyChange(event);
				}
				catch (Exception e) {
				}
			}
		}
		catch (Exception e) {
		}
	}

	protected static final String FRAMEWORK_OSGI_CONSOLE_NAME = "module.framework.properties.osgi.console";

	protected static final String PORTAL_EXT_PROPERTIES = "portal-ext.properties";

	protected PortalBundle bundle;
	protected IFolder configPath;
	protected Document configurationDocument;
	protected PortalPropertiesConfiguration portalExtProperties;
	protected boolean serverDirty;

	private transient List<PropertyChangeListener> _propertyListeners;

}
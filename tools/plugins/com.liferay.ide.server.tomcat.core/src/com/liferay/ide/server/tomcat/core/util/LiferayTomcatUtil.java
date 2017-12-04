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

package com.liferay.ide.server.tomcat.core.util;

import com.liferay.ide.core.ILiferayConstants;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileListing;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.StringPool;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.server.core.ILiferayRuntime;
import com.liferay.ide.server.core.IPluginPublisher;
import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.tomcat.core.ILiferayTomcatConstants;
import com.liferay.ide.server.tomcat.core.ILiferayTomcatRuntime;
import com.liferay.ide.server.tomcat.core.ILiferayTomcatServer;
import com.liferay.ide.server.tomcat.core.LiferayTomcatPlugin;
import com.liferay.ide.server.tomcat.core.LiferayTomcatRuntime70;
import com.liferay.ide.server.tomcat.core.LiferayTomcatServerBehavior;
import com.liferay.ide.server.util.LiferayPortalValueLoader;
import com.liferay.ide.server.util.ServerUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jst.server.tomcat.core.internal.TomcatVersionHelper;
import org.eclipse.jst.server.tomcat.core.internal.xml.Factory;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.Context;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.ServerInstance;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;

import org.osgi.framework.Version;

/**
 * @author Greg Amerson
 * @author Simon Jiang
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class LiferayTomcatUtil {

	public static final String CONFIG_TYPE_SERVER = "server";

	public static final String CONFIG_TYPE_VERSION = "version";

	public static void addRuntimeVMArgments(
		List<String> runtimeVMArgs, IPath installPath, IPath configPath, IPath deployPath, boolean testEnv,
		IServer currentServer, ILiferayTomcatServer liferayTomcatServer) {

		runtimeVMArgs.add("-Dfile.encoding=UTF8");
		runtimeVMArgs.add("-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false");
		runtimeVMArgs.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");

		ILiferayRuntime runtime = ServerUtil.getLiferayRuntime(currentServer);

		Version portalVersion = new Version(getVersion(runtime));

		if (CoreUtil.compareVersions(portalVersion, LiferayTomcatRuntime70.leastSupportedVersion) < 0) {
			runtimeVMArgs.add("-Djava.security.auth.login.config=\"" + configPath.toOSString() + "/conf/jaas.config\"");
		}
		else {
			runtimeVMArgs.add("-Djava.net.preferIPv4Stack=true");
		}

		runtimeVMArgs.add(
			"-Djava.util.logging.config.file=\"" + installPath.toOSString() + "/conf/logging.properties\"");
		runtimeVMArgs.add("-Djava.io.tmpdir=\"" + installPath.toOSString() + "/temp\"");

		boolean useDefaultPortalServerSettings = liferayTomcatServer.getUseDefaultPortalServerSettings();

		if (useDefaultPortalServerSettings) {
			_addUserDefaultVMArgs(runtimeVMArgs);
		}
		else {
			_addUserVMArgs(runtimeVMArgs, currentServer, liferayTomcatServer);

			File externalPropertiesFile = _getExternalPropertiesFile(
				installPath, configPath, currentServer, liferayTomcatServer);

			runtimeVMArgs.add("-Dexternal-properties=\"" + externalPropertiesFile.getAbsolutePath() + "\"");
		}
	}

	public static IStatus canAddModule(IModule module, IServer currentServer) {
		IProject project = module.getProject();

		if (project != null) {
			IFacetedProject facetedProject = ProjectUtil.getFacetedProject(project);

			if (facetedProject != null) {
				IProjectFacet liferayFacet = ProjectUtil.getLiferayFacet(facetedProject);

				if (liferayFacet != null) {
					String facetId = liferayFacet.getId();

					IRuntime runtime = null;

					try {
						runtime = ServerUtil.getRuntime(project);
					}
					catch (CoreException ce) {
					}

					if (runtime != null) {
						IPluginPublisher pluginPublisher = LiferayServerCore.getPluginPublisher(
							facetId, runtime.getRuntimeType().getId());

						if (pluginPublisher != null) {
							IStatus status = pluginPublisher.canPublishModule(currentServer, module);

							if (!status.isOK()) {
								return status;
							}
						}
					}
				}
			}
		}

		return Status.OK_STATUS;
	}

	public static void displayToggleMessage(String msg, String key) {
	}

	public static IPath[] getAllUserClasspathLibraries(IPath runtimeLocation, IPath portalDir) {
		List<IPath> libs = new ArrayList<>();
		IPath libFolder = runtimeLocation.append("lib");
		IPath extLibFolder = runtimeLocation.append("lib/ext");
		IPath webinfLibFolder = portalDir.append("WEB-INF/lib");

		try {
			List<File> libFiles = FileListing.getFileListing(new File(libFolder.toOSString()));

			for (File lib : libFiles) {
				if (FileUtil.exists(lib) && lib.getName().endsWith(".jar")) {
					libs.add(new Path(lib.getPath()));
				}
			}

			List<File> extLibFiles = FileListing.getFileListing(new File(extLibFolder.toOSString()));

			for (File lib : extLibFiles) {
				if (FileUtil.exists(lib) && lib.getName().endsWith(".jar")) {
					libs.add(new Path(lib.getPath()));
				}
			}

			libFiles = FileListing.getFileListing(new File(webinfLibFolder.toOSString()));

			for (File lib : libFiles) {
				if (FileUtil.exists(lib) && lib.getName().endsWith(".jar")) {
					libs.add(new Path(lib.getPath()));
				}
			}
		}
		catch (FileNotFoundException fnfe) {
			LiferayTomcatPlugin.logError(fnfe);
		}

		return libs.toArray(new IPath[0]);
	}

	// to read liferay info from manifest need at least version 6.2.0

	public static String getConfigInfoFromCache(String configType, IPath portalDir) {
		IPath configInfoPath = null;

		if (configType.equals(CONFIG_TYPE_VERSION)) {
			configInfoPath = _liferayTomcatPluginLocation.append("version.properties");
		}
		else if (configType.equals(CONFIG_TYPE_SERVER)) {
			configInfoPath = _liferayTomcatPluginLocation.append("serverInfos.properties");
		}
		else {
			return null;
		}

		File configInfoFile = configInfoPath.toFile();

		String portalDirKey = CoreUtil.createStringDigest(portalDir.toPortableString());

		Properties properties = new Properties();

		if (FileUtil.exists(configInfoFile)) {
			try (InputStream fileInput = Files.newInputStream(configInfoFile.toPath())) {
				properties.load(fileInput);

				String configInfo = (String)properties.get(portalDirKey);

				if (!CoreUtil.isNullOrEmpty(configInfo)) {
					return configInfo;
				}
			}
			catch (Exception e) {
			}
		}

		return null;
	}

	public static String getConfigInfoFromManifest(String configType, IPath portalDir) {
		File implJar = portalDir.append("WEB-INF/lib/portal-impl.jar").toFile();

		String version = null;
		String serverInfo = null;

		if (FileUtil.exists(implJar)) {
			try {
				@SuppressWarnings("resource")
				JarFile jar = new JarFile(implJar);

				Manifest manifest = jar.getManifest();

				Attributes attributes = manifest.getMainAttributes();

				version = attributes.getValue("Liferay-Portal-Version");
				serverInfo = attributes.getValue("Liferay-Portal-Server-Info");

				if (CoreUtil.compareVersions(Version.parseVersion(version), _MANIFEST_VERSION_REQUIRED) < 0) {
					version = null;
					serverInfo = null;
				}
			}
			catch (IOException ioe) {
				LiferayTomcatPlugin.logError(ioe);
			}
		}

		if (configType.equals(CONFIG_TYPE_VERSION)) {
			return version;
		}

		if (configType.equals(CONFIG_TYPE_SERVER)) {
			return serverInfo;
		}

		return null;
	}

	public static ILiferayTomcatRuntime getLiferayTomcatRuntime(IRuntime runtime) {
		if (runtime != null) {
			return (ILiferayTomcatRuntime)runtime.createWorkingCopy().loadAdapter(ILiferayTomcatRuntime.class, null);
		}

		return null;
	}

	public static IPath getPortalDir(IPath appServerDir) {
		return checkAndReturnCustomPortalDir(appServerDir);
	}

	public static String getVersion(ILiferayRuntime runtime) {
		String version = getConfigInfoFromCache(CONFIG_TYPE_VERSION, runtime.getAppServerPortalDir());

		if (version == null) {
			version = getConfigInfoFromManifest(CONFIG_TYPE_VERSION, runtime.getAppServerPortalDir());

			if (version == null) {
				LiferayPortalValueLoader loader = new LiferayPortalValueLoader(runtime.getUserLibs());

				Version loadedVersion = loader.loadVersionFromClass();

				if (loadedVersion != null) {
					version = loadedVersion.toString();
				}
			}

			if (version != null) {
				saveConfigInfoIntoCache(CONFIG_TYPE_VERSION, version, runtime.getAppServerPortalDir());
			}
		}

		return version;
	}

	public static boolean isExtProjectContext(Context context) {
		return false;
	}

	public static boolean isLiferayModule(IModule module) {
		boolean retval = false;

		if (module != null) {
			IProject project = module.getProject();

			retval = ProjectUtil.isLiferayFacetedProject(project);
		}

		return retval;
	}

	public static Context loadContextFile(File contextFile) {
		Context context = null;

		if (FileUtil.exists(contextFile)) {
			try (InputStream fis = Files.newInputStream(contextFile.toPath())) {
				Factory factory = new Factory();

				factory.setPackageName("org.eclipse.jst.server.tomcat.core.internal.xml.server40");
				context = (Context)factory.loadDocument(fis);

				if (context != null) {
					String path = context.getPath();

					// If path attribute is not set, derive from file name

					if (path == null) {
						String fileName = contextFile.getName();

						path = fileName.substring(0, fileName.length() - ".xml".length());

						if ("ROOT".equals(path)) {
							path = StringPool.EMPTY;
						}

						context.setPath(StringPool.FORWARD_SLASH + path);
					}
				}
			}
			catch (Exception e) {

				// may be a spurious xml file in the host dir?

			}
		}

		return context;
	}

	public static IPath modifyLocationForBundle(IPath currentLocation) {
		IPath modifiedLocation = null;

		if ((currentLocation == null) || CoreUtil.isNullOrEmpty(currentLocation.toOSString())) {
			return null;
		}

		File location = currentLocation.toFile();

		if (FileUtil.exists(location) && location.isDirectory()) {

			// check to see if this location contains tomcat dir *tomcat*

			File[] files = location.listFiles();
			boolean matches = false;

			String pattern = ".*tomcat.*";

			File tomcatDir = null;

			for (File file : files) {
				if (file.isDirectory() && file.getName().matches(pattern)) {
					matches = true;

					tomcatDir = file;

					break;
				}
			}

			if (matches && (tomcatDir != null)) {
				modifiedLocation = new Path(tomcatDir.getPath());
			}
		}

		return modifiedLocation;
	}

	public static void saveConfigInfoIntoCache(String configType, String configInfo, IPath portalDir) {
		IPath versionsInfoPath = null;

		if (configType.equals(CONFIG_TYPE_VERSION)) {
			_liferayTomcatPluginLocation.append("version.properties");
		}
		else if (configType.equals(CONFIG_TYPE_SERVER)) {
			versionsInfoPath = _liferayTomcatPluginLocation.append("serverInfos.properties");
		}

		if (versionsInfoPath != null) {
			File versionInfoFile = versionsInfoPath.toFile();

			if (configInfo != null) {
				String portalDirKey = CoreUtil.createStringDigest(portalDir.toPortableString());
				Properties properties = new Properties();

				try (InputStream fileInput = Files.newInputStream(versionInfoFile.toPath())) {
					properties.load(fileInput);
				}
				catch (FileNotFoundException fnfe) {

					// ignore filenotfound we likely just haven't had a file written yet.

				}
				catch (IOException ioe) {
					LiferayTomcatPlugin.logError(ioe);
				}

				properties.put(portalDirKey, configInfo);

				try (OutputStream fileOutput = Files.newOutputStream(versionInfoFile.toPath())) {
					properties.store(fileOutput, StringPool.EMPTY);
				}
				catch (Exception e) {
					LiferayTomcatPlugin.logError(e);
				}
			}
		}
	}

	public static void syncStopServer(IServer server) {
		if (server.getServerState() != IServer.STATE_STARTED) {
			return;
		}

		LiferayTomcatServerBehavior serverBehavior = (LiferayTomcatServerBehavior)server.loadAdapter(
			LiferayTomcatServerBehavior.class, null);

		Thread shutdownThread = new Thread() {

			@Override
			public void run() {
				serverBehavior.stop(true);

				synchronized (server) {
					try {
						server.wait(5000);
					}
					catch (InterruptedException ie) {
					}
				}
			}

		};

		IServerListener shutdownListener = new IServerListener() {

			@Override
			public void serverChanged(ServerEvent event) {
				if (event.getState() == IServer.STATE_STOPPED) {
					synchronized (server) {
						server.notifyAll();
					}
				}
			}

		};

		server.addServerListener(shutdownListener);

		try {
			shutdownThread.start();
			shutdownThread.join();
		}
		catch (InterruptedException ie) {
		}

		server.removeServerListener(shutdownListener);
	}

	public static IStatus validateRuntimeStubLocation(String runtimeTypeId, IPath runtimeStubLocation) {
		try {
			IRuntimeWorkingCopy runtimeStub = ServerCore.findRuntimeType(runtimeTypeId).createRuntime(null, null);

			runtimeStub.setLocation(runtimeStubLocation);
			runtimeStub.setStub(true);

			return runtimeStub.validate(null);
		}
		catch (Exception e) {
			return LiferayTomcatPlugin.createErrorStatus(e);
		}
	}

	/**
	 * Added for IDE-646
	 */
	protected static IPath checkAndReturnCustomPortalDir(IPath appServerDir) {
		IPath retval = null;

		if (appServerDir != null) {
			IPath serverDir = appServerDir.append(_CONFIG_DIR).append(_SERVICE_NAME);

			IPath portalContextFileDir = serverDir.append(_HOST_NAME).append(_DEFAULT_PORTAL_CONTEXT_FILE);

			File contextFile = portalContextFileDir.toFile();

			if (FileUtil.exists(contextFile)) {
				Context tcPortalContext = loadContextFile(contextFile);

				if (tcPortalContext != null) {
					String docBase = tcPortalContext.getDocBase();

					if (docBase != null) {
						return new Path(docBase);
					}
				}
			}

			if (retval == null) {
				retval = appServerDir.append(_DEFAULT_PORTAL_DIR);
			}
		}

		return retval;
	}

	private static void _addUserDefaultVMArgs(List<String> runtimeVMArgs) {
		String[] memoryArgs = ILiferayTomcatConstants.DEFAULT_MEMORY_ARGS.split(StringPool.SPACE);

		if (memoryArgs != null) {
			for (String arg : memoryArgs) {
				runtimeVMArgs.add(arg);
			}
		}
	}

	private static void _addUserVMArgs(
		List<String> runtimeVMArgs, IServer currentServer, ILiferayTomcatServer portalTomcatServer) {

		String[] memoryArgs = ILiferayTomcatConstants.DEFAULT_MEMORY_ARGS.split(StringPool.SPACE);
		String userTimezone = ILiferayTomcatConstants.DEFAULT_USER_TIMEZONE;

		if ((currentServer != null) && (portalTomcatServer != null)) {
			memoryArgs = DebugPlugin.parseArguments(portalTomcatServer.getMemoryArgs());

			userTimezone = portalTomcatServer.getUserTimezone();
		}

		if (memoryArgs != null) {
			for (String arg : memoryArgs) {
				runtimeVMArgs.add(arg);
			}
		}

		runtimeVMArgs.add("-Duser.timezone=" + userTimezone);
	}

	private static File _ensurePortalIDEPropertiesExists(
		IPath installPath, IPath configPath, IServer currentServer, ILiferayTomcatServer portalServer) {

		IPath idePropertiesPath = installPath.append("../portal-ide.properties");

		String hostName = "localhost";

		try {
			ServerInstance server = TomcatVersionHelper.getCatalinaServerInstance(
				configPath.append("conf/server.xml"), null, null);

			hostName = server.getHost().getName();
		}
		catch (Exception e) {
			LiferayTomcatPlugin.logError(e);
		}

		Properties props = new Properties();

		if ((portalServer != null) &&
			(portalServer.getServerMode() == ILiferayTomcatConstants.DEVELOPMENT_SERVER_MODE)) {

			props.put("include-and-override", "portal-developer.properties");
		}

		props.put("com.liferay.portal.servlet.filters.etag.ETagFilter", "false");
		props.put("com.liferay.portal.servlet.filters.header.HeaderFilter", "false");
		props.put("json.service.auth.token.enabled", "false");

		props.put("auto.deploy.tomcat.conf.dir", configPath.append("conf/Catalina/" + hostName).toOSString());

		if ((currentServer != null) && (portalServer != null)) {
			IPath runtimLocation = currentServer.getRuntime().getLocation();

			String autoDeployDir = portalServer.getAutoDeployDirectory();

			if (!ILiferayTomcatConstants.DEFAULT_AUTO_DEPLOYDIR.equals(autoDeployDir)) {
				IPath autoDeployDirPath = new Path(autoDeployDir);

				if (autoDeployDirPath.isAbsolute() && FileUtil.exists(autoDeployDirPath.toFile())) {
					props.put("auto.deploy.deploy.dir", portalServer.getAutoDeployDirectory());
				}
				else {
					File autoDeployDirFile = new File(runtimLocation.toFile(), autoDeployDir);

					if (FileUtil.exists(autoDeployDirFile)) {
						props.put("auto.deploy.deploy.dir", autoDeployDirFile.getPath());
					}
				}
			}

			props.put("auto.deploy.interval", portalServer.getAutoDeployInterval());
		}

		// props.put( "json.service.public.methods", "*" );

		props.put("jsonws.web.service.public.methods", "*");

		File file = idePropertiesPath.toFile();

		try {
			props.store(Files.newOutputStream(file.toPath()), null);
		}
		catch (Exception e) {
			LiferayTomcatPlugin.logError(e);
		}

		return file;
	}

	private static File _getExternalPropertiesFile(
		IPath installPath, IPath configPath, IServer currentServer, ILiferayTomcatServer portalServer) {

		File retval = null;

		if (portalServer != null) {
			File portalIdePropFile = _ensurePortalIDEPropertiesExists(
				installPath, configPath, currentServer, portalServer);

			retval = portalIdePropFile;

			String externalProperties = portalServer.getExternalProperties();

			if (!CoreUtil.isNullOrEmpty(externalProperties)) {
				File externalPropertiesFile = _setupExternalPropertiesFile(portalIdePropFile, externalProperties);

				if (externalPropertiesFile != null) {
					retval = externalPropertiesFile;
				}
			}
		}

		return retval;
	}

	private static File _setupExternalPropertiesFile(File portalIdePropFile, String externalPropertiesPath) {
		File retval = null;

		// first check to see if there is an external properties file

		File externalPropertiesFile = new File(externalPropertiesPath);

		if (FileUtil.exists(externalPropertiesFile)) {
			ExternalPropertiesConfiguration props = new ExternalPropertiesConfiguration();

			try {
				props.load(Files.newInputStream(externalPropertiesFile.toPath()));

				props.setProperty("include-and-override", portalIdePropFile.getAbsolutePath());

				props.setHeader("# Last modified by Liferay IDE " + new Date());

				props.save(Files.newOutputStream(externalPropertiesFile.toPath()));

				retval = externalPropertiesFile;
			}
			catch (Exception e) {
				retval = null;
			}
		}
		else {

			// don't setup an external properties file

			retval = null;
		}

		return retval;
	}

	private static final String _CONFIG_DIR = "conf";

	private static final String _DEFAULT_PORTAL_CONTEXT_FILE = "ROOT.xml";

	private static final String _DEFAULT_PORTAL_DIR = "/webapps/ROOT";

	private static final String _HOST_NAME = "localhost";

	private static final Version _MANIFEST_VERSION_REQUIRED = ILiferayConstants.V620;

	private static final String _SERVICE_NAME = "Catalina";

	private static IPath _liferayTomcatPluginLocation = LiferayTomcatPlugin.getDefault().getStateLocation();

}
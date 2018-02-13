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

package com.liferay.ide.maven.ui;

import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.maven.core.ILiferayMavenConstants;
import com.liferay.ide.maven.core.MavenUtil;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * @author Simon Jiang
 */
public class LeagcyMavenProjectPropertyTester extends PropertyTester {

	private static String leagcyGroupId = "com.liferay.portal";
	private static String[] leagcyArtifactId =
		{"liferay-maven-plugin", "portal-service", "util-java", "util-bridges", "util-taglib", "util-slf4j"};
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IProject) {
			IProject project = (IProject)receiver;

			try {
				IMavenProjectFacade mavenProjectFacade = MavenUtil.getProjectFacade(project);

				if ( mavenProjectFacade == null ) {
					return false;
				}

				MavenProject mavenProject = mavenProjectFacade.getMavenProject(new NullProgressMonitor());
				if ( mavenProject == null ) {
					return false;
				}

				List<Dependency> dependencies = mavenProject.getDependencies();

				for (Dependency dependency : dependencies) {
					if (leagcyGroupId.equals(dependency.getGroupId()) && ListUtil.contains(leagcyArtifactId,dependency.getArtifactId())) {
						return true;
					}
				}

				List<Plugin> buildPlugins = mavenProject.getBuildPlugins();
				for(int i=0;i<buildPlugins.size();i++) {
					Plugin plugin = buildPlugins.get(i);

					if (plugin.getGroupId().equals(ILiferayMavenConstants.LIFERAY_MAVEN_PLUGINS_GROUP_ID)) {
						return true;
					}
				}

				return false;
			}
			catch (Exception e) {
			}
		}

		return false;
	}

}

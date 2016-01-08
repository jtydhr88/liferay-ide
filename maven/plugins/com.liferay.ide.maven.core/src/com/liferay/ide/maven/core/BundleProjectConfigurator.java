/*******************************************************************************
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
 *
 *******************************************************************************/

package com.liferay.ide.maven.core;

import com.liferay.ide.core.LiferayProjectNature;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
public class BundleProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator
{

    public BundleProjectConfigurator()
    {
        super();
    }

    public void configureClasspath( IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor )
        throws CoreException
    {
    }

    public void configureRawClasspath(
        ProjectConfigurationRequest request, IClasspathDescriptor classpath, IProgressMonitor monitor )
        throws CoreException
    {
    }

    @Override
    public void configure( ProjectConfigurationRequest request, IProgressMonitor monitor ) throws CoreException
    {
        if( monitor == null )
        {
            monitor = new NullProgressMonitor();
        }

        IProject project = request.getProject();

        LiferayProjectNature liferayProjectNature = new LiferayProjectNature();

        if( isMavenBundlePlugin( project ) )
        {
            liferayProjectNature.addLiferayNature( project, monitor );
        }

        monitor.worked( 100 );
        monitor.done();
    }

    private boolean isMavenBundlePlugin( IProject project )
    {
        final NullProgressMonitor monitor = new NullProgressMonitor();
        final IMavenProjectFacade facade = MavenUtil.getProjectFacade( project, monitor );

        if( facade != null )
        {
            try
            {
                final MavenProject mavenProject = facade.getMavenProject( new NullProgressMonitor() );

                if( mavenProject != null && "bundle".equals( mavenProject.getPackaging() ) )
                {
                    final Plugin mavenBundlePlugin =
                        MavenUtil.getPlugin( facade, ILiferayMavenConstants.MAVEN_BUNDLE_PLUGIN_KEY, monitor );

                    if( mavenBundlePlugin != null )
                    {
                        return true;
                    }

                }
                else if( mavenProject != null && "jar".equals( mavenProject.getPackaging() ) )
                {
                    final Plugin bndMavenPlugin =
                        MavenUtil.getPlugin( facade, ILiferayMavenConstants.BND_MAVEN_PLUGIN_KEY, monitor );

                    if( bndMavenPlugin != null )
                    {
                        return true;
                    }
                }
            }
            catch( CoreException e )
            {
            }
        }

        return false;
    }

}

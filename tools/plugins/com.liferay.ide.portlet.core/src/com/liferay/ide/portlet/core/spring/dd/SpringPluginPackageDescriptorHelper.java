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

package com.liferay.ide.portlet.core.spring.dd;

import com.liferay.ide.core.ILiferayConstants;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.StringPool;
import com.liferay.ide.portlet.core.IPluginPackageModel;
import com.liferay.ide.portlet.core.PluginPropertiesConfiguration;
import com.liferay.ide.portlet.core.PortletCore;
import com.liferay.ide.portlet.core.dd.PluginPackagesDescriptorHelper;

import java.io.File;
import java.io.FileWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

/**
 * @author Terry Jia
 */
public class SpringPluginPackageDescriptorHelper extends PluginPackagesDescriptorHelper
{

    public SpringPluginPackageDescriptorHelper()
    {
        super();
    }

    public SpringPluginPackageDescriptorHelper( IProject project )
    {
        super( project );
    }

    @Override
    public IStatus doAddNewPortlet( IDataModel dataModel )
    {
        if( canAddNewPortlet( dataModel ) )
        {
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "aopalliance.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-aop.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-beans.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-context.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-core.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-expression.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-web.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-webmvc.jar" );
            addPortalDependency( IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS, "spring-webmvc-portlet.jar" );
        }

        return Status.OK_STATUS;
    }

    public IStatus addPortalDependency( String propertyName, String value )
    {
        if( CoreUtil.isNullOrEmpty( value ) )
        {
            return Status.OK_STATUS;
        }

        try
        {
            final IFile pluginPackageFile = getDescriptorFile();

            if( !pluginPackageFile.exists() )
            {
                IStatus warning =
                    PortletCore.createWarningStatus( "No " + ILiferayConstants.LIFERAY_PLUGIN_PACKAGE_PROPERTIES_FILE +
                        " file in the project." );

                PortletCore.getDefault().getLog().log( warning );

                return Status.OK_STATUS;
            }

            File osfile = new File( pluginPackageFile.getLocation().toOSString() );

            PluginPropertiesConfiguration pluginPackageProperties = new PluginPropertiesConfiguration();
            pluginPackageProperties.load( osfile );

            String existingDeps = pluginPackageProperties.getString( propertyName, StringPool.EMPTY );

            String[] existingValues = existingDeps.split( "," );

            for( String existingValue : existingValues )
            {
                if( value.equals( existingValue ) )
                {
                    return Status.OK_STATUS;
                }
            }

            String newPortalDeps = null;

            if( CoreUtil.isNullOrEmpty( existingDeps ) )
            {
                newPortalDeps = value;
            }
            else
            {
                newPortalDeps = existingDeps + "," + value;
            }

            pluginPackageProperties.setProperty( propertyName, newPortalDeps );

            FileWriter output = new FileWriter( osfile );

            try
            {
                pluginPackageProperties.save( output );
            }
            finally
            {
                output.close();
            }

            // refresh file
            pluginPackageFile.refreshLocal( IResource.DEPTH_ZERO, new NullProgressMonitor() );
        }
        catch( Exception e )
        {
            PortletCore.logError( e );

            return PortletCore.createErrorStatus( "Could not add dependency in " +
                ILiferayConstants.LIFERAY_PLUGIN_PACKAGE_PROPERTIES_FILE );
        }

        return Status.OK_STATUS;
    }

    private boolean canAddNewPortlet( IDataModel dataModel )
    {
        return dataModel.getID().contains( "NewSpringPortlet" );
    }
}
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

package com.liferay.ide.project.core.tests;

import static org.junit.Assert.assertNotNull;

import com.liferay.ide.core.IWebProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.model.NewLiferayPluginProjectOp;
import com.liferay.ide.project.core.model.PluginType;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.junit.Test;

/**
 * @author Terry Jia
 */
public class NewLiferayWebPluginProjectTests extends ProjectCoreBase
{

    @Override
    protected IPath getLiferayPluginsSdkDir()
    {
        return ProjectCore.getDefault().getStateLocation().append(
            "com.liferay.portal.plugins.sdk-1.0.16-withdependencies" );
    }

    @Override
    protected IPath getLiferayPluginsSDKZip()
    {
        return getLiferayBundlesPath().append(
            "com.liferay.portal.plugins.sdk-1.0.16-withdependencies.zip" );
    }

    @Override
    protected String getLiferayPluginsSdkZipFolder()
    {
        return "com.liferay.portal.plugins.sdk-1.0.16-withdependencies/";
    }

    @Test
    public void testNewWebAntProject() throws Exception
    {
        if( shouldSkipBundleTests() )
            return;

        final String projectName = "test-web-project-sdk";
        final NewLiferayPluginProjectOp op = newProjectOp( projectName );

        op.setPluginType( PluginType.web );

        final IProject webProject = createAntProject( op );

        assertNotNull( LiferayCore.create( IWebProject.class, webProject ).getDefaultDocrootFolder() );
    }

}

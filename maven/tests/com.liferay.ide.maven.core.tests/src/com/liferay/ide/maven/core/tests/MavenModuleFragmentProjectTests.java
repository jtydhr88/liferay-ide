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

package com.liferay.ide.maven.core.tests;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.fragment.NewModuleFragmentFilesOp;
import com.liferay.ide.project.core.modules.fragment.NewModuleFragmentOp;
import com.liferay.ide.project.core.modules.fragment.NewModuleFragmentOpMethods;
import com.liferay.ide.project.core.modules.fragment.OverrideFilePath;
import com.liferay.ide.server.core.tests.ServerCoreBase;
import com.liferay.ide.server.util.ServerUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.ProgressMonitorBridge;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.junit.Test;

/**
 * @author Joye Luo
 */
public class MavenModuleFragmentProjectTests extends ServerCoreBase
{

    @Override
    protected IPath getLiferayRuntimeDir()
    {
        return ProjectCore.getDefault().getStateLocation().append( "liferay-ce-portal-7.0-ga5/tomcat-8.0.32" );
    }

    @Override
    protected IPath getLiferayRuntimeZip()
    {
        return getLiferayBundlesPath().append( "liferay-ce-portal-tomcat-7.0-ga5-20171018150113838.zip" );
    }

    @Override
    protected String getRuntimeId()
    {
        return "com.liferay.ide.server.portal.runtime";
    }

    @Override
    public void setupRuntime() throws Exception
    {
        if( shouldSkipBundleTests() )
            return;

        extractRuntime( getLiferayRuntimeZip(), getLiferayRuntimeDir() );
    }

    @Test
    public void testNewModuleFragmentFileProjectValidation() throws Exception
    {
        deleteAllWorkspaceProjects();

        NewModuleFragmentFilesOp fop = NewModuleFragmentFilesOp.TYPE.instantiate();

        Status projectValidationStatus = fop.getProjectName().validation();

        assertEquals("No suitable Liferay fragment project.", projectValidationStatus.message() );

        NewModuleFragmentOp op = NewModuleFragmentOp.TYPE.instantiate();
        final String runtimeName = "liferay-portal-7.0";
        final NullProgressMonitor npm = new NullProgressMonitor();

        IRuntime runtime = ServerCore.findRuntime( runtimeName );

        if( runtime == null )
        {
            final IRuntimeWorkingCopy runtimeWC =
                ServerCore.findRuntimeType( getRuntimeId() ).createRuntime( runtimeName, npm );

            runtimeWC.setName( runtimeName );
            runtimeWC.setLocation( getLiferayRuntimeDir() );

            runtime = runtimeWC.save( true, npm );
        }

        assertNotNull( runtime );

        List<String> bundles = ServerUtil.getModuleFileListFrom70Server( runtime );

        assertNotNull( bundles );

        for( String hostOsgiBundle : bundles )
        {
            if( hostOsgiBundle.contains( "com.liferay.asset.display.web" ) )
            {
                op.setProjectName( "test-project-validation" );
                op.setProjectProvider( "gradle-module-fragment" );
                op.setLiferayRuntimeName( runtimeName );
                op.setHostOsgiBundle( hostOsgiBundle );
                OverrideFilePath overrideFilePath = op.getOverrideFiles().insert();
                overrideFilePath.setValue( "META-INF/resources/view.jsp" );

                Status gradleExeStatus =
                    NewModuleFragmentOpMethods.execute( op, ProgressMonitorBridge.create( new NullProgressMonitor() ) );

                assertTrue( gradleExeStatus.ok() );

                IProject existedGradleProject = CoreUtil.getProject( op.getProjectName().content() );

                assertNotNull( existedGradleProject );

                IFile bndFile = existedGradleProject.getFile( "bnd.bnd" );

                bndFile.delete( true, true, new NullProgressMonitor() );

                fop.setProjectName( op.getProjectName().content() );

                projectValidationStatus = fop.getProjectName().validation();

                assertEquals( "Can't find bnd.bnd file in the project.", projectValidationStatus.message() );
            }
        }
    }

    @Test
    public void testNewModuleFragmentProjectOpProjectName()
    {
        NewModuleFragmentOp op = NewModuleFragmentOp.TYPE.instantiate();

        op.setProjectName( "test-module-fragment" );
        Status projectNameOkValidationStatus1 = op.getProjectName().validation();
        assertEquals( "ok", projectNameOkValidationStatus1.message() );

        op.setProjectName( "#test-module-fragment" );
        Status projectNameErrorValidationStatus = op.getProjectName().validation();
        assertEquals( "The project name is invalid.", projectNameErrorValidationStatus.message() );

        op.setProjectName( "test_module_fragment" );
        Status projectNameOkValidationStatus2 = op.getProjectName().validation();
        assertEquals( "ok", projectNameOkValidationStatus2.message() );
    }

    @Test
    public void testNewModuleFragmentProjectOpProject() throws Exception
    {
        NewModuleFragmentOp op = NewModuleFragmentOp.TYPE.instantiate();
        final String runtimeName = "liferay-portal-7.0";
        final NullProgressMonitor npm = new NullProgressMonitor();

        IRuntime runtime = ServerCore.findRuntime( runtimeName );

        if( runtime == null )
        {
            final IRuntimeWorkingCopy runtimeWC =
                ServerCore.findRuntimeType( getRuntimeId() ).createRuntime( runtimeName, npm );

            runtimeWC.setName( runtimeName );
            runtimeWC.setLocation( getLiferayRuntimeDir() );

            runtime = runtimeWC.save( true, npm );
        }

        assertNotNull( runtime );

        List<String> bundles = ServerUtil.getModuleFileListFrom70Server( runtime );

        assertNotNull( bundles );

        for( String hostOsgiBundle : bundles )
        {
            if( hostOsgiBundle.contains( "com.liferay.asset.display.web" ) )
            {
                op.setProjectName( "test-gradle-module-fragment" );
                op.setProjectProvider( "gradle-module-fragment" );
                op.setLiferayRuntimeName( runtimeName );
                op.setHostOsgiBundle( hostOsgiBundle );
                OverrideFilePath overrideFilePath = op.getOverrideFiles().insert();
                overrideFilePath.setValue( "META-INF/resources/view.jsp" );

                Status gradleExeStatus =
                    NewModuleFragmentOpMethods.execute( op, ProgressMonitorBridge.create( new NullProgressMonitor() ) );

                assertTrue( gradleExeStatus.ok() );

                IProject existedGradleProject = CoreUtil.getProject( op.getProjectName().content() );

                assertNotNull( existedGradleProject );

                IFile gradleFile = existedGradleProject.getFile( "build.gradle" );

                assertTrue( gradleFile.exists() );

                IFile overrideFile = existedGradleProject.getFile( "src/main/resources/META-INF/resources/view.jsp" );

                assertTrue( overrideFile.exists() );
            }

            if( hostOsgiBundle.contains( "com.liferay.login.web" ) )
            {
                op.setProjectName( "test-maven-module-fragment" );
                op.setProjectProvider( "maven-module-fragment" );
                op.setLiferayRuntimeName( runtimeName );
                op.setHostOsgiBundle( hostOsgiBundle );
                OverrideFilePath file = op.getOverrideFiles().insert();
                file.setValue( "META-INF/resources/login.jsp" );

                Status mavenExeStatus =
                    NewModuleFragmentOpMethods.execute( op, ProgressMonitorBridge.create( new NullProgressMonitor() ) );

                assertTrue( mavenExeStatus.ok() );

                IProject existedMavenProject = CoreUtil.getProject( op.getProjectName().content() );

                assertNotNull( existedMavenProject );

                IFile pomFile = existedMavenProject.getFile( "pom.xml" );

                assertTrue( pomFile.exists() );

                IFile overrideFile1 = existedMavenProject.getFile( "src/main/resources/META-INF/resources/login.jsp" );

                assertTrue( overrideFile1.exists() );
            }
        }
    }
}

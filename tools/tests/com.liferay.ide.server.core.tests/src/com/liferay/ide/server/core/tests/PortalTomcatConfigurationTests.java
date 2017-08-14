/*******************************************************************************
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved./
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

package com.liferay.ide.server.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.junit.Before;
import org.junit.Test;

import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.server.core.ILiferayServer;
import com.liferay.ide.server.core.portal.LiferayServerPort;
import com.liferay.ide.server.core.portal.PortalBundleConfiguration;
import com.liferay.ide.server.core.portal.PortalRuntime;
import com.liferay.ide.server.core.portal.PortalServerDelegate;

/**
 * @author Simon Jiang
 */
public class PortalTomcatConfigurationTests extends ServerCoreBase
{
    protected IPath getLiferayRuntimeDir()
    {
        return ProjectCore.getDefault().getStateLocation().append( "liferay-ce-portal-7.0-ga3/tomcat-8.0.32" );
    }

    protected IPath getLiferayRuntimeZip()
    {
        return getLiferayBundlesPath().append( "liferay-ce-portal-tomcat-7.0-ga3-20160804222206210.zip" );
    }

    public String getRuntimeVersion()
    {
        return "7.0.2";
    }

    @Before
    public void setupRuntime() throws Exception
    {
        if( shouldSkipBundleTests() ) return;

        assertEquals(
            "Expected liferayBundlesPath to exist: " + getLiferayBundlesPath().toOSString(), true,
            getLiferayBundlesPath().toFile().exists() );

        extractRuntime( getLiferayRuntimeZip(), getLiferayRuntimeDir() );

        final NullProgressMonitor npm = new NullProgressMonitor();

        final String runtimeName = getRuntimeVersion();

        runtime = ServerCore.findRuntime( runtimeName );

        if( runtime == null )
        {
            final IRuntimeWorkingCopy runtimeWC =
                ServerCore.findRuntimeType( getRuntimeId() ).createRuntime( runtimeName, npm );

            runtimeWC.setName( runtimeName );
            runtimeWC.setLocation( getLiferayRuntimeDir() );

            runtime = runtimeWC.save( true, npm );
        }

        assertNotNull( runtime );
        final PortalRuntime liferayRuntime =
            (PortalRuntime) ServerCore.findRuntime( runtimeName ).loadAdapter( PortalRuntime.class, npm );

        assertNotNull( liferayRuntime );
    }
    @Override
    protected String getRuntimeId()
    {
        return "com.liferay.ide.server.portal.runtime";
    }
    
    protected void checkPortValue( PortalServerDelegate wc, LiferayServerPort newPort ) throws Exception
    {
        int retVal = -1;
        List<LiferayServerPort> liferayServerPortsBeforeChanged = wc.getLiferayServerPorts();

        retVal = getPortValue( liferayServerPortsBeforeChanged, newPort.getId());
        assertNotEquals( retVal, newPort.getPort() );

        PortalBundleConfiguration initBundleConfiguration = wc.initBundleConfiguration();
        initBundleConfiguration.modifyServerPort( newPort.getId(), newPort.getPort() );
        wc.applyChange( newPort, new NullProgressMonitor() );
        wc.saveConfiguration( new NullProgressMonitor() );
        initBundleConfiguration.load( new NullProgressMonitor() );
        
        List<LiferayServerPort> liferayServerPortsAfterChanged = wc.getLiferayServerPorts();

        retVal = getPortValue( liferayServerPortsAfterChanged, newPort.getId());
        assertEquals( retVal, newPort.getPort() );
    }

    protected int getPortValue( List<LiferayServerPort> ports, String portId )
    {
        int retVal = -1;
        for( LiferayServerPort port : ports )
        {
            if ( port.getId().toLowerCase().equals( portId.toLowerCase() ) )
            {
                retVal = port.getPort();
                break;
            }
        }

        return retVal;
    }
    
    @Test
    public void testGetPortalServerConfigurationForTomcat() throws Exception
    {
        if( shouldSkipBundleTests() ) return;

        if( runtime == null )
        {
            setupRuntime();
        }

        assertNotNull( runtime );

        final IServerWorkingCopy serverWC = createServerForRuntime( "testTomcatConfiguration", runtime );
        final PortalServerDelegate portalServerDelegate =
            (PortalServerDelegate) serverWC.loadAdapter( PortalServerDelegate.class, new NullProgressMonitor());

        LiferayServerPort agentPort = new LiferayServerPort( ILiferayServer.ATTR_AGENT_PORT, "Bnd Agent", 27998, "TCPIP", LiferayServerPort.defayltStoreInServer );
        LiferayServerPort httpPort = new LiferayServerPort( "HTTP/1.1", "HTTP/1.1", 8982, "HTTP/1.1", LiferayServerPort.defaultStoreInXML );

        checkPortValue( portalServerDelegate, agentPort );
        checkPortValue( portalServerDelegate, httpPort );
    }
}


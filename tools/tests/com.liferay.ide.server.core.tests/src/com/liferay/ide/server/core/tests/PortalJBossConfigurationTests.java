package com.liferay.ide.server.core.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.junit.Test;

import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.server.core.ILiferayServer;
import com.liferay.ide.server.core.portal.LiferayServerPort;
import com.liferay.ide.server.core.portal.PortalServerDelegate;

public class PortalJBossConfigurationTests extends PortalTomcatConfigurationTests
{

    protected IPath getLiferayRuntimeDir()
    {
        return ProjectCore.getDefault().getStateLocation().append( "liferay-ce-portal-7.0-ga4/wildfly-10.0.0" );
    }

    protected IPath getLiferayRuntimeZip()
    {
        return getLiferayBundlesPath().append( "liferay-ce-portal-wildfly-7.0-ga4-20170613175008905.zip" );
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

        final IServerWorkingCopy serverWC = createServerForRuntime( "testJBossConfiguration", runtime );
        final PortalServerDelegate portalServerDelegate =
            (PortalServerDelegate) serverWC.loadAdapter( PortalServerDelegate.class, new NullProgressMonitor());

        LiferayServerPort agentPort = new LiferayServerPort( ILiferayServer.ATTR_AGENT_PORT, "Bnd Agent", 27998, "TCPIP", LiferayServerPort.defayltStoreInServer );
        LiferayServerPort httpPort = new LiferayServerPort( "HTTP/1.1", "HTTP/1.1", 8982, "HTTP/1.1", LiferayServerPort.defaultStoreInXML );

        checkPortValue( portalServerDelegate, agentPort );
        checkPortValue( portalServerDelegate, httpPort );
    }
}

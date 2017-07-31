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

package com.liferay.ide.server.core.portal;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.StringPool;
import com.liferay.ide.server.core.ILiferayServer;
import com.liferay.ide.server.core.LiferayServerCore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerDelegate;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 * @author Simon Jiang
 */
@SuppressWarnings( "restriction" )
public class PortalServerDelegate extends ServerDelegate implements PortalServerWorkingCopy
{
    private Object versionLock = new Object(); 
    private int currentVersion;
    private int loadedVersion;
    private final static List<String> SUPPORT_TYPES_LIST = Arrays.asList( "liferay.bundle", "jst.web", "jst.utility" );
    protected PortalBundleConfiguration bundleConfiguration;
    public PortalServerDelegate()
    {
        super();
    }

    @Override
    public IStatus canModifyModules( IModule[] add, IModule[] remove )
    {
        IStatus retval = Status.OK_STATUS;

        if( !CoreUtil.isNullOrEmpty( add ) )
        {
            for( IModule module : add )
            {
                if( !SUPPORT_TYPES_LIST.contains( module.getModuleType().getId() ) )
                {
                    retval =
                        LiferayServerCore.error( "Unable to add module with type " + module.getModuleType().getName() );
                    break;
                }
            }
        }

        return retval;
    }

    public int getAutoPublishTime()
    {
        return getAttribute( Server.PROP_AUTO_PUBLISH_TIME, 0 );
    }

    @Override
    public String getHttpPort()
    {
        return getAttribute( ATTR_HTTP_PORT, DEFAULT_HTTP_PORT );
    }

    @Override
    public IModule[] getChildModules( IModule[] module )
    {
        IModule[] retval = null;

        if( module != null )
        {
            final IModuleType moduleType = module[0].getModuleType();

            if( module.length == 1 && moduleType != null && SUPPORT_TYPES_LIST.contains( moduleType.getId() ) )
            {
                retval = new IModule[0];
            }
        }

        return retval;
    }

    @Override
    public boolean getDeveloperMode()
    {
        return getAttribute( PROPERTY_DEVELOPER_MODE, PortalServerConstants.DEFAULT_DEVELOPER_MODE );
    }

    public String getExternalProperties()
    {
        return getAttribute( PROPERTY_EXTERNAL_PROPERTIES, StringPool.EMPTY );
    }

    @Override
    public boolean getLaunchSettings()
    {
        return getAttribute( PROPERTY_LAUNCH_SETTINGS, PortalServerConstants.DEFAULT_LAUNCH_SETTING );
    }

    public String getHost()
    {
        return getServer().getHost();
    }

    public String getId()
    {
        return getServer().getId();
    }

    @Override
    public void setLaunchSettings( boolean launchSettings )
    {
        setAttribute( PROPERTY_LAUNCH_SETTINGS, launchSettings );
    }

    public String[] getMemoryArgs()
    {
        String[] retval = null;

        final String args = getAttribute( PROPERTY_MEMORY_ARGS, PortalServerConstants.DEFAULT_MEMORY_ARGS );

        if( !CoreUtil.isNullOrEmpty( args ) )
        {
            retval = args.split( " " );
        }

        return retval;
    }

    public String getPassword()
    {
        return getAttribute( ATTR_PASSWORD, DEFAULT_PASSWORD );
    }

    @Override
    public URL getPluginContextURL( String context )
    {
        try
        {
            return new URL( getPortalHomeUrl(), StringPool.FORWARD_SLASH + context );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    @Override
    public URL getPortalHomeUrl()
    {
        try
        {
            return new URL( "http://localhost:" + getHttpPort() );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    @Override
    public IModule[] getRootModules( IModule module ) throws CoreException
    {
        final IStatus status = canModifyModules( new IModule[] { module }, null );

        if( status == null || !status.isOK() )
        {
            throw new CoreException( status );
        }

        return new IModule[] { module };
    }

    public String getUsername()
    {
        return getAttribute( ATTR_USERNAME, DEFAULT_USERNAME );
    }

    @Override
    public URL getWebServicesListURL()
    {
        try
        {
            return new URL( getPortalHomeUrl(), "/tunnel-web/axis" ); //$NON-NLS-1$
        }
        catch( MalformedURLException e )
        {
            LiferayServerCore.logError( "Unable to get web services list URL", e ); //$NON-NLS-1$
        }

        return null;
    }

    @Override
    public void modifyModules( IModule[] add, IModule[] remove, IProgressMonitor monitor ) throws CoreException
    {
    }

    @Override
    public void setDefaults( IProgressMonitor monitor )
    {
        setAttribute( Server.PROP_AUTO_PUBLISH_TIME, getAutoPublishTime() );
        //setAttribute( ILiferayServerBehavior.AGENT_PORT, Agent.DEFAULT_PORT );
    }

    @Override
    public void setDeveloperMode( boolean developerMode )
    {
        setAttribute( PROPERTY_DEVELOPER_MODE, developerMode );
    }

    public void setExternalProperties( String externalProperties )
    {
        setAttribute( PROPERTY_EXTERNAL_PROPERTIES, externalProperties );
    }

    public void setMemoryArgs( String memoryArgs )
    {
        setAttribute( PROPERTY_MEMORY_ARGS, memoryArgs );
    }

    public void setPassword( String password )
    {
        setAttribute( ATTR_PASSWORD, password );
    }

    public void setUsername( String username )
    {
        setAttribute( ATTR_USERNAME, username );
    }

    @Override
    public void saveConfiguration( IProgressMonitor monitor ) throws CoreException
    {
        PortalRuntime runtime = (PortalRuntime) getServer().getRuntime().loadAdapter( PortalRuntime.class, new NullProgressMonitor() );
        if ( runtime != null )
        {
            bundleConfiguration.save(runtime.getAppServerDir(), monitor);            
        }
    }

    public void applyChange( LiferayServerPort port, IProgressMonitor monitor )
    {
        if ( port.getStoreLocation().equals( LiferayServerPort.defayltStoreInServer )  )
        {
            setAttribute( port.getId(), port.getPort() );
        }
        else
        {
            bundleConfiguration.applyChange( port );
        }
    }
    
    public List<LiferayServerPort> getLiferayServerPorts()
    {
        List<LiferayServerPort> liferayServerPorts = new ArrayList<LiferayServerPort>();

        liferayServerPorts.addAll( createServerPort() );
        liferayServerPorts.addAll( getBundleConfiguration().getServerPorts() );

        return liferayServerPorts;
    }

    private List<LiferayServerPort> createServerPort()
    {
        List<LiferayServerPort> serverPorts = new ArrayList<LiferayServerPort>();
        if ( getServer() != null )
        {
            
            serverPorts.add( new LiferayServerPort( ATTR_AGENT_PORT, "Bnd Agent", getAgentPort(), "TCPIP", LiferayServerPort.defayltStoreInServer ) );
            serverPorts.add( new LiferayServerPort( ATTR_JMX_PORT, "Jmx Client", getJmxPort(), "TCPIP", LiferayServerPort.defayltStoreInServer ) );
            serverPorts.add( new LiferayServerPort( ATTR_TELNET_PORT, "Telnet", getTelnetPort(), "TCPIP", LiferayServerPort.defayltStoreInServer ) );
        }
        
        return serverPorts;
    }
  
    @Override
    public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException 
    {
        try 
        {
            synchronized (versionLock) 
            {
                bundleConfiguration = null;
                currentVersion = 0;
                loadedVersion = 0;
            }
            
            PortalRuntime portalRuntime = (PortalRuntime) runtime.loadAdapter( PortalRuntime.class, new NullProgressMonitor() );
            bundleConfiguration = portalRuntime.getPortalBundle().getBundleConfiguration();
            bundleConfiguration.importFromPath( portalRuntime.getPortalBundle().getAppServerDir(), monitor );

        } catch (CoreException ce) {
            throw ce;
        }
    }
    
    public PortalBundleConfiguration getBundleConfiguration()
    {
        int current;
        PortalBundleConfiguration bundleConfig;
        // Grab current state
        synchronized (versionLock) 
        {
            current = currentVersion;
            bundleConfig = bundleConfiguration;
        }
        // If configuration needs loading
        if (bundleConfig == null || loadedVersion != current) 
        {
            try {
                PortalRuntime portalRuntime = (PortalRuntime) getServer().getRuntime().loadAdapter( PortalRuntime.class, new NullProgressMonitor() );
                bundleConfiguration = portalRuntime.getPortalBundle().getBundleConfiguration();

                // If not yet loaded
                if (bundleConfig == null) 
                {
                    bundleConfig = bundleConfiguration;
                }                
                bundleConfiguration.load( portalRuntime.getAppServerDir(), new NullProgressMonitor() );
                // Update loaded version
                synchronized (versionLock) 
                {
                    // If newer version not already loaded, update version
                    if (bundleConfiguration == null || loadedVersion < current) {
                        bundleConfiguration = bundleConfig;
                        loadedVersion = current;
                    }
                }
            } 
            catch (CoreException ce) 
            {
                LiferayServerCore.logError( ce );
            }
        }
        return bundleConfig;
    }

    @Override
    public int getAgentPort()
    {
        return getAttribute( ATTR_AGENT_PORT, ILiferayServer.DEFAULT_AGENT_PORT );
    }

    @Override
    public int getJmxPort()
    {
        return getAttribute( ATTR_JMX_PORT, ILiferayServer.DEFAULT_JMX_PORT );
    }

    @Override
    public int getTelnetPort()
    {
        return getAttribute( ATTR_TELNET_PORT, ILiferayServer.DEFAULT_TELNET_PORT );
    }

    @Override
    public void configurationChanged() 
    {
        synchronized (versionLock) 
        {
            currentVersion++;
        }
    }
    
}

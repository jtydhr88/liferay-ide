/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

package com.liferay.ide.server.core.portal;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.server.core.LiferayServerCore;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.ServerPort;
import org.w3c.dom.Document;

public abstract class PortalBundleConfiguration implements IPortalBundleConfiguration
{
    
    protected List<LiferayServerPort> ports = new ArrayList<LiferayServerPort>();
    protected PortalBundle bundle;
    public static final String NAME_PROPERTY = "name";
    public static final String PORT_PROPERTY = "port";
    public static final String MODIFY_PORT_PROPERTY = "modifyPort";
    protected IFolder configPath;
    protected boolean isServerDirty;
    protected Document configurationDocument;

    // property change listeners
    private transient List<PropertyChangeListener> propertyListeners;

    public PortalBundleConfiguration( PortalBundle bundle )
    {
        this.bundle = bundle;
    }

    /**
     * PortalBundleConfiguration constructor.
     * 
     * @param path
     *            a path
     */
    public PortalBundleConfiguration( IFolder path )
    {
        super();
        this.configPath = path;
        /*
         * try { load(configPath, new NullProgressMonitor()); } catch (Exception e) { // ignore }
         */
    }
 
    
    protected IFolder getFolder()
    {
        return configPath;
    }

    protected ServerPort getPort( final String id )
    {
        List<LiferayServerPort> serverPorts = getServerPorts();

        for( LiferayServerPort port : serverPorts )
        {
            if( port.getId().equals( id ) )
            {
                return new ServerPort( port.getId(), port.getName(), port.getPort(), port.getProtocol() );
            }
        }

        return null;
    }

    protected void firePropertyChangeEvent( String propertyName, Object oldValue, Object newValue )
    {
        if( propertyListeners == null )
            return;

        PropertyChangeEvent event = new PropertyChangeEvent( this, propertyName, oldValue, newValue );
        try
        {
            Iterator<PropertyChangeListener> iterator = propertyListeners.iterator();
            while( iterator.hasNext() )
            {
                try
                {
                    PropertyChangeListener listener = (PropertyChangeListener) iterator.next();
                    listener.propertyChange( event );
                }
                catch( Exception e )
                {
                }
            }
        }
        catch( Exception e )
        {
        }
    }

    /**
     * Adds a property change listener to this server.
     *
     * @param listener
     *            java.beans.PropertyChangeListener
     */
    public void addPropertyChangeListener( PropertyChangeListener listener )
    {
        if( propertyListeners == null )
        {
            propertyListeners = new ArrayList<PropertyChangeListener>();
        }
        propertyListeners.add( listener );
    }

    /**
     * Removes a property change listener from this server.
     *
     * @param listener
     *            java.beans.PropertyChangeListener
     */
    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        if( propertyListeners != null )
        {
            propertyListeners.remove( listener );
        }
    }

    protected abstract void save( IPath configruationPath, IProgressMonitor monitor ) throws CoreException;

    // protected abstract void save( IFolder folder, IProgressMonitor monitor ) throws CoreException;
    // protected abstract void load(IFolder folder, IProgressMonitor monitor) throws CoreException;
    protected abstract void load( IPath configruationPath, IProgressMonitor monitor ) throws CoreException;

    public abstract void applyChange( LiferayServerPort port );
    public abstract ServerPort getMainPort();

    /**
     * Return a string representation of this object.
     * 
     * @return java.lang.String
     */
    public String toString()
    {
        return getClass().getName().toString();
    }

    @Override
    public void addAddtionalPorts( LiferayServerPort port )
    {
        ports.add( port );
    }

    public void importFromPath( IPath path, IProgressMonitor monitor ) throws CoreException
    {
        load( path, monitor );
    }

    /**
     * Modify the port with the given id.
     *
     * @param id
     *            java.lang.String
     * @param port
     *            int
     */
    public void modifyServerPort( String id, int port )
    {
        try
        {
            isServerDirty = true;
            firePropertyChangeEvent( MODIFY_PORT_PROPERTY, id, new Integer( port ) );
        }
        catch( Exception e )
        {
            LiferayServerCore.logError( e );
        }
    }
    
    
    protected LiferayServerPort createServerPort( final String id, final String portName, final String portValue )
    {
        String retVal = null;
        
        if( !CoreUtil.empty( portValue ) )
        {
            if( portValue.lastIndexOf( ":" ) == -1 )
            {
                retVal = portValue;
            }
            else
            {
                retVal = portValue.substring( portValue.lastIndexOf( ":" ) + 1, portValue.length() - 1 );
            }
        }
        return new LiferayServerPort( id, StringUtils.capitalize( StringUtils.replace( portName, "-", " " ) ), Integer.parseInt( retVal ), StringUtils.capitalize( portName ) );
    }    
}

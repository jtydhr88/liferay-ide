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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.server.core.LiferayServerCore;

/**
 * @author Simon Jiang
 */

public abstract class PortalBundleConfiguration implements IPortalBundleConfiguration
{
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

    public abstract void save( IProgressMonitor monitor ) throws CoreException;
    public abstract void load( IProgressMonitor monitor );
    public abstract void applyChange( LiferayServerPort port );

    /**
     * Return a string representation of this object.
     * 
     * @return java.lang.String
     */
    public String toString()
    {
        return getClass().getName().toString();
    }

    public void importFromPath( IProgressMonitor monitor ) throws CoreException
    {
        load( monitor );
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
    
    
    public LiferayServerPort createLiferayServerPort( final String id, final String portName, final String portValue )
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

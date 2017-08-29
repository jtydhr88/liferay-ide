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

package com.liferay.ide.server.ui.editor;

import com.liferay.ide.server.core.ILiferayServerWorkingCopy;
import com.liferay.ide.server.core.portal.LiferayServerPort;
import com.liferay.ide.server.core.portal.PortalBundleConfiguration;
import com.liferay.ide.server.core.portal.PortalRuntime;
import com.liferay.ide.server.core.portal.PortalServerDelegate;
import com.liferay.ide.server.ui.LiferayServerUI;
import com.liferay.ide.server.ui.cmd.ModifyPortCommand;
import com.liferay.ide.server.util.ServerUtil;
import com.liferay.ide.server.util.SocketUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * @author Simon Jiang
 */

public class ConfigurationPortEditorSection extends ServerEditorSection
{

    protected PortalBundleConfiguration portalBundleConfiguration;

    protected boolean updating;

    protected Table ports;
    protected TableViewer viewer;

    protected PropertyChangeListener listener;
    protected ILiferayServerWorkingCopy liferayServer;
    protected PortalRuntime portalRuntime;

    protected PortalServerDelegate portalSeverDelgate;

    // property change listeners
    private transient List<PropertyChangeListener> propertyListeners;

    /**
     * ConfigurationPortEditorSection constructor comment.
     */
    public ConfigurationPortEditorSection()
    {
        super();
    }

    /**
     * 
     */
    protected void addChangeListener()
    {
        listener = new PropertyChangeListener()
        {

            public void propertyChange( PropertyChangeEvent event )
            {
                if( PortalBundleConfiguration.MODIFY_PORT_PROPERTY.equals( event.getPropertyName() ) )
                {
                    String id = (String) event.getOldValue();
                    Integer i = (Integer) event.getNewValue();
                    changePortNumber( id, i.intValue() );
                }
            }
        };
        portalBundleConfiguration.addPropertyChangeListener( listener );
    }

    /**
     * @param id
     *            java.lang.String
     * @param port
     *            int
     */
    protected void changePortNumber( String id, int port )
    {
        TableItem[] items = ports.getItems();
        int size = items.length;
        for( int i = 0; i < size; i++ )
        {
            try
            {
                LiferayServerPort sp = (LiferayServerPort) items[i].getData();
                if( sp.getId().equals( id ) )
                {
                    LiferayServerPort changedPort =
                        new LiferayServerPort( id, sp.getName(), port, sp.getProtocol(), sp.getStoreLocation() );

                    boolean checkPort = validPort( items[i], changedPort );

                    boolean portAvailable = SocketUtil.isPortAvailable( String.valueOf( port ) );

                    boolean originalPort = getOriginalPort( changedPort );

                    if( ( !checkPort || !portAvailable ) && !originalPort )
                    {
                        items[i].setImage( LiferayServerUI.getImage( LiferayServerUI.IMG_PORT_WARNING ) );
                    }
                    else
                    {
                        items[i].setImage( LiferayServerUI.getImage( LiferayServerUI.IMG_PORT ) );
                        getManagedForm().getMessageManager().removeMessage( items[i] );
                    }

                    items[i].setData( changedPort );
                    items[i].setText( 1, port + "" );

                    // modify document
                    portalSeverDelgate.applyChange( changedPort, new NullProgressMonitor() );
                    return;
                }
            }
            catch( Exception e )
            {
                LiferayServerUI.logError( e );
            }
        }
    }

    private boolean getOriginalPort( final LiferayServerPort newPort )
    {
        List<LiferayServerPort> liferayServerPorts = portalSeverDelgate.getLiferayServerPorts();

        for( LiferayServerPort port : liferayServerPorts )
        {
            if( port.getId().equals( newPort.getId() ) )
            {
                return port.getPort() == newPort.getPort();
            }
        }

        return false;
    }

    /**
     * Creates the SWT controls for this workbench part.
     *
     * @param parent
     *            the parent control
     */
    public void createSection( Composite parent )
    {
        super.createSection( parent );
        FormToolkit toolkit = getFormToolkit( parent.getDisplay() );

        Section section = toolkit.createSection(
            parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR |
                Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE );
        section.setText( "Ports" );
        section.setDescription( "Modify the server ports." );
        section.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL ) );

        // ports
        Composite composite = toolkit.createComposite( section );
        GridLayout layout = new GridLayout();
        layout.marginHeight = 8;
        layout.marginWidth = 8;
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL ) );
        toolkit.paintBordersFor( composite );
        section.setClient( composite );

        ports = toolkit.createTable( composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION );
        ports.setHeaderVisible( true );
        ports.setLinesVisible( true );

        TableLayout tableLayout = new TableLayout();

        TableColumn col = new TableColumn( ports, SWT.NONE );
        col.setText( "Port Name" );
        ColumnWeightData colData = new ColumnWeightData( 15, 150, true );
        tableLayout.addColumnData( colData );

        col = new TableColumn( ports, SWT.NONE );
        col.setText( "Port Number" );
        colData = new ColumnWeightData( 8, 80, true );
        tableLayout.addColumnData( colData );

        GridData data = new GridData( GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL );
        data.widthHint = 230;
        data.heightHint = 100;
        ports.setLayoutData( data );
        ports.setLayout( tableLayout );

        viewer = new TableViewer( ports );
        viewer.setColumnProperties( new String[] { "name", "port" } );

        ColumnViewerToolTipSupport.enableFor( viewer );
        viewer.setLabelProvider( new ColumnLabelProvider()
        {

            @Override
            public String getToolTipText( Object element )
            {
                LiferayServerPort port = (LiferayServerPort) element;
                String toolTip = getTableItemValidationTooltip( port );
                return toolTip;
            }

        } );

        initialize();
    }

    private String getTableItemValidationTooltip( LiferayServerPort serverPort )
    {

        boolean portAvailable = SocketUtil.isPortAvailable( String.valueOf( serverPort.getPort() ) );
        boolean originalPort = getOriginalPort( serverPort );

        if( !portAvailable && !originalPort )
        {
            StringBuffer sb = new StringBuffer();
            sb.append( serverPort.getPort() );
            sb.append( " is being used by other application." );
            return sb.toString();
        }

        final List<String> serverLists = ServerUtil.checkUsingPorts( server.getName(), serverPort );

        if( serverLists.size() > 0 )
        {
            StringBuffer sb = new StringBuffer();

            sb.append( serverPort.getPort() );
            sb.append( " is being used at: " );

            for( String serverName : serverLists )
            {
                sb.append( "<" );
                sb.append( serverName );
                sb.append( ">" );
            }

            return sb.toString();
        }

        return null;
    }

    protected void setupPortEditors()
    {
        viewer.setCellEditors( new CellEditor[] { null, new TextCellEditor( ports ) } );

        ICellModifier cellModifier = new ICellModifier()
        {

            public Object getValue( Object element, String property )
            {
                LiferayServerPort sp = (LiferayServerPort) element;
                if( sp.getPort() < 0 )
                    return "-";
                return sp.getPort() + "";
            }

            public boolean canModify( Object element, String property )
            {
                if( "port".equals( property ) )
                    return true;

                return false;
            }

            public void modify( Object element, String property, Object value )
            {
                try
                {
                    Item item = (Item) element;
                    LiferayServerPort sp = (LiferayServerPort) item.getData();
                    int port = Integer.parseInt( (String) value );

                    if( sp.getPort() != port )
                    {
                        execute(
                            new ModifyPortCommand( portalBundleConfiguration, portalSeverDelgate, sp.getId(), port ) );
                    }
                }
                catch( Exception ex )
                {
                }
            }
        };
        viewer.setCellModifier( cellModifier );

        // preselect second column (Windows-only)
        String os = System.getProperty( "os.name" );
        if( os != null && os.toLowerCase().indexOf( "win" ) >= 0 )
        {
            ports.addSelectionListener( new SelectionAdapter()
            {

                public void widgetSelected( SelectionEvent event )
                {
                    try
                    {
                        int n = ports.getSelectionIndex();
                        viewer.editElement( ports.getItem( n ).getData(), 1 );
                    }
                    catch( Exception e )
                    {
                        // ignore
                    }
                }
            } );
        }
    }

    public void dispose()
    {
        if( portalBundleConfiguration != null )
        {
            portalBundleConfiguration.removePropertyChangeListener( listener );
        }
    }

    /*
     * (non-Javadoc) Initializes the editor part with a site and input.
     */
    public void init( IEditorSite site, IEditorInput input )
    {
        super.init( site, input );
        portalRuntime =
            (PortalRuntime) server.getRuntime().loadAdapter( PortalRuntime.class, new NullProgressMonitor() );

        try
        {
            portalSeverDelgate = (PortalServerDelegate) server.getAdapter( PortalServerDelegate.class );
            portalBundleConfiguration = portalSeverDelgate.initBundleConfiguration();
        }
        catch( Exception e )
        {
        }

        addChangeListener();
        initialize();
    }

    private boolean validPort( TableItem item, LiferayServerPort serverPort )
    {
        try
        {
            int port = serverPort.getPort();

            if( port < 0 || port > 65535 )
            {
                getManagedForm().getMessageManager().addMessage(
                    item, "Port must to be a number from 1~65535.", null, IMessageProvider.ERROR );

                return false;
            }
        }
        catch( Exception e )
        {
            getManagedForm().getMessageManager().addMessage( item, "Port is invalid", null, IMessageProvider.ERROR );

            return false;
        }

        final List<String> serverLists = ServerUtil.checkUsingPorts( server.getName(), serverPort );

        if( serverLists.size() > 0 )
        {
            StringBuffer sb = new StringBuffer();

            sb.append( serverPort.getPort() );
            sb.append( " is being used at: " );

            for( String serverName : serverLists )
            {
                sb.append( "<" );
                sb.append( serverName );
                sb.append( ">" );
            }

            getManagedForm().getMessageManager().addMessage( item, sb.toString(), null, IMessageProvider.WARNING );

            return false;
        }
        else
        {
            getManagedForm().getMessageManager().removeMessage( item );
        }

        return true;
    }

    /**
     * Initialize the fields in this editor.
     */
    protected void initialize()
    {
        try
        {
            boolean checkPort = true;
            if( ports == null )
            {
                return;
            }

            ports.removeAll();

            Iterator<LiferayServerPort> iterator = portalSeverDelgate.getLiferayServerPorts().iterator();
            while( iterator.hasNext() )
            {
                LiferayServerPort port = (LiferayServerPort) iterator.next();
                TableItem item = new TableItem( ports, SWT.NONE );
                String portStr = "-";

                if( port.getPort() >= 0 )
                {
                    portStr = port.getPort() + "";
                }
                String[] s = new String[] { port.getName(), portStr };
                item.setText( s );

                checkPort = validPort( item, port );

                if( !checkPort )
                {
                    item.setImage( LiferayServerUI.getImage( LiferayServerUI.IMG_PORT_WARNING ) );
                }
                else
                {
                    item.setImage( LiferayServerUI.getImage( LiferayServerUI.IMG_PORT ) );
                }

                item.setData( port );
            }

            if( readOnly )
            {
                viewer.setCellEditors( new CellEditor[] { null, null } );
                viewer.setCellModifier( null );
            }
            else
            {
                setupPortEditors();
            }
        }
        catch( Exception e )
        {
            LiferayServerUI.logError( e );
        }

    }

    protected void firePropertyChangeEvent( String propertyName, Object oldValue, Object newValue )
    {
        if( propertyListeners == null )
        {
            return;
        }

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
                    LiferayServerUI.logError( "Error firing property change event", e );
                }
            }
        }
        catch( Exception e )
        {
            LiferayServerUI.logError( "Error in property event", e );
        }
    }
}

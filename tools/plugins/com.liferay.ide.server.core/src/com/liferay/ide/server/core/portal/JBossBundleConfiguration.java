package com.liferay.ide.server.core.portal;

import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.util.XMLUtil;

import java.io.FileInputStream;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.ServerPort;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class JBossBundleConfiguration extends PortalBundleConfiguration
{
    public JBossBundleConfiguration( PortalBundle bundle )
    {
        super( bundle );
    }
    
    public JBossBundleConfiguration( IFolder path )
    {
        super( path );
    }
    
    @Override
    public List<LiferayServerPort> getServerPorts()
    {
        return getPortsValue( "socket-binding" );
    }

    @Override
    public void applyChange( LiferayServerPort port )
    {
        if ( port.getStoreLocation().equals( LiferayServerPort.defaultStoreInXML ) )
        {
            Node attributeNode = getAttributeNode( "socket-binding", port.getId() );
            
            if ( attributeNode != null )
            {
                XMLUtil.setNodeValue( attributeNode, port.getId(), String.valueOf( port.getPort() ) );    
            }
        }
    }
    
    @Override
    protected void save( IPath configruationPath, IProgressMonitor monitor ) throws CoreException
    {
        try
        {
            IPath filePath = configruationPath.append( "/standalone/configuration/" ).append("standalone.xml");
            if( filePath.toFile().exists() )
            {
                if( isServerDirty )
                {
                    XMLUtil.save( filePath.toOSString(), configurationDocument );
                }
            }
            isServerDirty = false;
        }
        catch( Exception e )
        {
            throw new CoreException( LiferayServerCore.createErrorStatus( e ) );
        }
    }
    
    @Override
    protected void load( IPath configruationPath, IProgressMonitor monitor ) throws CoreException
    {
        try
        {
            IPath file = configruationPath.append( "/standalone/configuration/" ).append("standalone.xml");

            if (file.toFile().exists())
            {
                configurationDocument = XMLUtil.getDocumentBuilder().parse(new InputSource(new FileInputStream(file.toFile())));
            }
            
            if ( !file.toFile().exists() )
            {
                throw new CoreException( new Status(IStatus.WARNING, LiferayServerCore.PLUGIN_ID, 0, "Could not load the JBoss server configuration" , null ) );
            }
        }
        catch( Exception e)
        {
            throw new CoreException( LiferayServerCore.createErrorStatus( e ) );
        }
    }
    
    private Node getAttributeNode( String tagName, String attrName )
    {
        try
        {
            Document document = configurationDocument;

            NodeList connectorNodes = document.getElementsByTagName( tagName );

            for( int i = 0; i < connectorNodes.getLength(); i++ )
            {
                Node node = connectorNodes.item( i );

                NamedNodeMap attributes = node.getAttributes();

                for( int j = 0; j < attributes.getLength(); j++ )
                {
                    Node itemNode = attributes.item( j );
                    
                    if( itemNode != null )
                    {
                        if( itemNode.getNodeName().equals( "name" ) )
                        {
                            if ( itemNode.getNodeValue().equals( attrName.toLowerCase()  ) )
                            {
                                return attributes.getNamedItem( "port" );
                            }
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            LiferayServerCore.logError( e );
        }

        return null;
    }
    
    private List<LiferayServerPort> getPortsValue( String tagName )
    {
        try
        {
            Document document = configurationDocument;

            NodeList connectorNodes = document.getElementsByTagName( tagName );

            for( int i = 0; i < connectorNodes.getLength(); i++ )
            {
                Node node = connectorNodes.item( i );

                NamedNodeMap attributes = node.getAttributes();

                for( int j = 0; j < attributes.getLength(); j++ )
                {
                    Node itemNode = attributes.item( j );
                    
                    if( itemNode != null )
                    {
                        if( itemNode.getNodeName().equals( "name" ) )
                        {
                            boolean existed = false;
                            for( LiferayServerPort port : ports )
                            {
                                if ( port.getId().equals( itemNode.getNodeValue() ) )
                                {
                                    existed = true;
                                    break;
                                }
                            }
                            
                            if ( !existed )
                            {
                                Node portNode = attributes.getNamedItem( "port" );
                                
                                LiferayServerPort createServerPort = createServerPort( itemNode.getNodeValue(),itemNode.getNodeValue(), portNode.getNodeValue() );
                                
                                ports.add( createServerPort );                               
                            }
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            LiferayServerCore.logError( e );
        }
        return ports;
    }    

    @Override
    public ServerPort getMainPort()
    {
        return getPort( "http" );
    }
}

package com.liferay.ide.server.tomcat.core;

import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.core.portal.LiferayServerPort;
import com.liferay.ide.server.core.portal.PortalBundle;
import com.liferay.ide.server.core.portal.PortalBundleConfiguration;
import com.liferay.ide.server.util.XMLUtil;

import java.io.FileInputStream;
import java.util.List;

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


public class TomcatBundleConfiguration extends PortalBundleConfiguration
{

    public TomcatBundleConfiguration( PortalBundle bundle )
    {
        super( bundle );
    }
    
    @Override
    public List<LiferayServerPort> getServerPorts()
    {
        getConfigurationPortsValue( "Connector", "port", "protocol" );
        getConfigurationPortsValue( "Server", "port", "shutdown" );

        return ports;
    }
    
    @Override
    protected void load( IPath configruationPath, IProgressMonitor monitor ) throws CoreException
    {
        try
        {
            IPath file = configruationPath.append( "/conf/" ).append("server.xml");

            if ( !file.toFile().exists() )
            {
                throw new CoreException( new Status(IStatus.WARNING, LiferayServerCore.PLUGIN_ID, 0, "Could not load the Tomcat server configuration" , null ) );
            }
            
            if (file.toFile().exists())
            {
                configurationDocument = XMLUtil.getDocumentBuilder().parse(new InputSource(new FileInputStream(file.toFile())));
            }
        }
        catch ( Exception e )
        {
            LiferayServerCore.logError( e );
            throw new CoreException( LiferayServerCore.createErrorStatus( e ) );
        }
        
    }

    private Node getAttributeNode( String tagName, String nodeTagName, String nodeTagValue )
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
                        if( itemNode.getNodeName().equals( nodeTagName ) )
                        {
                            if ( itemNode.getNodeValue().equals( nodeTagValue  ) )
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
    
    private List<LiferayServerPort> getConfigurationPortsValue( String tagName, String portName, String protocolName  )
    {
        try
        {
            Document document = configurationDocument;

            NodeList connectorNodes = document.getElementsByTagName( tagName );

            for( int i = 0; i < connectorNodes.getLength(); i++ )
            {
                Node node = connectorNodes.item( i );

                NamedNodeMap attributes = node.getAttributes();
                String portValue = null;
                String protocolValue = null;

                for( int j = 0; j < attributes.getLength(); j++ )
                {
                    Node itemNode = attributes.item( j );
                    
                    if( itemNode != null )
                    {
                        if( itemNode.getNodeName().equals( portName ) )
                        {
                            portValue = itemNode.getNodeValue();
                        }
                        
                        if( itemNode.getNodeName().equals( protocolName ) )
                        {
                            protocolValue = itemNode.getNodeValue();
                        }                        
                    }
                }
                boolean existed = false;

                for( LiferayServerPort port : ports )
                {
                    if ( port.getId().equals( protocolValue ) && protocolValue != null )
                    {
                        existed = true;
                        break;
                    }
                }

                if ( !existed )
                {
                    LiferayServerPort createServerPort = createServerPort( protocolValue,protocolValue, portValue );
                    ports.add( createServerPort );                               
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
    protected void save( IPath configruationPath, IProgressMonitor monitor ) throws CoreException
    {
        try
        {
            IPath filePath = configruationPath.append( "conf/" ).append("server.xml");
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
    public void applyChange( LiferayServerPort port )
    {
        if ( port.getStoreLocation().equals( LiferayServerPort.defaultStoreInXML ) )
        {
            Node connectorNode = getAttributeNode( "Connector", "protocol", port.getId()  );
            
            if ( connectorNode != null )
            {
                XMLUtil.setNodeValue( connectorNode, port.getId(), String.valueOf( port.getPort() ) );    
            }

            Node serverNode = getAttributeNode( "Server", "shutdown", port.getId() );
            
            if ( serverNode != null )
            {
                XMLUtil.setNodeValue( serverNode, port.getId(), String.valueOf( port.getPort() ) );    
            }            
        }
    }

    @Override
    public ServerPort getMainPort()
    {
        return getPort( "HTTP/1.1" );
    }
}

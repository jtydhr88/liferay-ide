
package com.liferay.ide.server.core.portal;

import org.eclipse.wst.server.core.ServerPort;

public class LiferayServerPort extends ServerPort
{

    public static String defaultStoreInXML = "xml";
    public static String defayltStoreInServer = "server";
    public static String defayltStoreInProperties = "properties";

    private String storeLocation;

    public LiferayServerPort( String id, String name, int port, String protocol )
    {
        super( id, name, port, protocol );
        this.setStoreLocation( defaultStoreInXML );
    }

    public LiferayServerPort( String id, String name, int port, String protocol, String storeLocation )
    {
        super( id, name, port, protocol );
        this.setStoreLocation( storeLocation );
    }

    public LiferayServerPort( ServerPort port, String storeLocation )
    {
        this( port.getId(), port.getName(), port.getPort(), port.getProtocol(), storeLocation );
    }

    /**
     * @return the storeLocation
     */
    public String getStoreLocation()
    {
        return storeLocation;
    }

    /**
     * @param storeLocation
     *            the storeLocation to set
     */
    public void setStoreLocation( String storeLocation )
    {
        this.storeLocation = storeLocation;
    }

}

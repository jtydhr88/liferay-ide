package com.liferay.ide.server.core.portal;

import java.beans.PropertyChangeListener;
import java.util.List;

public interface IPortalBundleConfiguration
{
    public List<LiferayServerPort> getServerPorts();
    
    public void addPropertyChangeListener( PropertyChangeListener listener );
    
    public void removePropertyChangeListener( PropertyChangeListener listener );
    
    public void modifyServerPort(String id, int port);
    
    public void addAddtionalPorts( LiferayServerPort port );
}

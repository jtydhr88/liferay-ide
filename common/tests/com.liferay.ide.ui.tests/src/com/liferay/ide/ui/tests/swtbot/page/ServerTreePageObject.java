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

package com.liferay.ide.ui.tests.swtbot.page;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.liferay.ide.ui.tests.ServerAction;

/**
 * @author Li Lu
 */
public class ServerTreePageObject<T extends SWTBot> extends TreeItemPageObject<SWTBot> implements ServerAction
{

    protected AddAndRemoveProjectPageObject<SWTBot> addAndRemovePage = new AddAndRemoveProjectPageObject<SWTBot>(
        bot, ADD_AND_REMOVE, BUTTON_CANCEL, BUTTON_FINISH );

    protected String server_name;

    public ServerTreePageObject( SWTBot bot, String server_name )
    {
        super( bot );
        this.server_name = server_name;
    }

    public void addALL()
    {
        doAction( ADD_AND_REMOVE );
        addAndRemovePage.addAll();
    }

    public boolean checkConsoleHasMessage( String expectedMessage, int timeout )
    {
        long timeoutExpiredMs = System.currentTimeMillis() + timeout;

        while( true )
        {
            bot.sleep( 1000 );

            String content = bot.styledText().getText();
            if( content.contains( expectedMessage ) || content.matches( expectedMessage ) )
            {
                return true;
            }
            if( System.currentTimeMillis() >= timeoutExpiredMs )
            {
                return false;
            }
        }
    }

    public void debugServer()
    {
        doAction( DEBUG );
    }

    public void deleteServer()
    {
        doAction( BUTTON_DELETE );

        DeleteProjectDialogPageObject<SWTBot> deleteDialog =
            new DeleteProjectDialogPageObject<SWTBot>( bot, DELETE_SERVER, BUTTON_CANCEL, BUTTON_OK );
        
        deleteDialog.confirm();
    }

    public void deployProject( String... projectItemNames )
    {
        doAction( ADD_AND_REMOVE );
        addAndRemovePage.add( projectItemNames );
    }

    protected SWTBotTreeItem getServer()
    {
        TreePageObject<SWTBot> tree = new TreePageObject<SWTBot>( bot, 1 );

        String[] servers = tree.getAllItems();

        for( String server : servers )
        {
            if( server.contains( server_name ) || server.equals( server_name ) )
                return tree.getItem( server );
        }

        return null;
    }

    @Override
    protected SWTBotTreeItem getWidget()
    {
        return getServer();
    }

    public void removeALL()
    {
        doAction( ADD_AND_REMOVE );
        addAndRemovePage.removeAll();
    }

    public void removeProject( String... projectItemNames )
    {
        doAction( ADD_AND_REMOVE );
        addAndRemovePage.remove( projectItemNames );
    }

    public void startServer()
    {
        doAction( START );
    }

    public void stopServer()
    {
        doAction( STOP );
        checkConsoleHasMessage( SERVER_STOP_MESSAGE, 10000 );
    }

}

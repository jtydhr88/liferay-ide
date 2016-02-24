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

package com.liferay.ide.server.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.liferay.ide.server.ui.tests.page.NewServerPageObject;
import com.liferay.ide.server.ui.tests.page.NewServerRuntimeEnvPageObject;
import com.liferay.ide.ui.tests.SWTBotBase;
import com.liferay.ide.ui.tests.swtbot.page.ShellPageObject;

/**
 * @author Vicky Wang
 */
public class NewServerRuntimeWizardTests extends SWTBotBase implements ServerRuntimeWizard
{

    NewServerPageObject<SWTWorkbenchBot> newServerPage = new NewServerPageObject<SWTWorkbenchBot>( bot );
    NewServerRuntimeEnvPageObject<SWTWorkbenchBot> setRuntimePage =
        new NewServerRuntimeEnvPageObject<SWTWorkbenchBot>( bot );

    @After
    public void closeWizard()
    {
        ShellPageObject<SWTWorkbenchBot> newServerShell = new ShellPageObject<SWTWorkbenchBot>( bot, TITLE_NEW_SERVER );
        newServerShell.closeIfOpen();
    }

    @Before
    public void openWizard()
    {
        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_SERVER );
    }

    @Test
    public void ServerRuntime() throws Exception
    {

        newServerPage.getServerTypeTree().selectTreeItem( NODE_LIFERAY_INC, NODE_LIFERAY_7X );
        newServerPage.next();

        setRuntimePage.getServerLocation().setText(
            getLiferayServerDir().toString() + "/" + getLiferayPluginServerName() );

        assertEquals( "tomcat", setRuntimePage.getPortalBundleType().getText() );

        setRuntimePage.getServerLocation().setText( getLiferayServerDir().toOSString() );

        assertEquals( "tomcat", setRuntimePage.getPortalBundleType().getText() );

        setRuntimePage.finish();

        String serversStopped = "Liferay 7.x at localhost  [Stopped]";
        String serverStartButton = "Start the server (Ctrl+Alt+R)";

        // Waiting for merging Lilu's server page object commit
        bot.tree( 1 ).getTreeItem( serversStopped ).select();
        bot.toolbarButtonWithTooltip( serverStartButton ).click();

        sleep( 10000 );
        assertTrue( checkServerConsoleMessage( SERVER_STARTUP_MESSAGE, 60000 ) );
        // Future: need to add jboss test
    }
}

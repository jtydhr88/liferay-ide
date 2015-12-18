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

import com.liferay.ide.ui.tests.ServerAction;

/**
 * @author Li Lu
 */
public class AddAndRemoveProjectPageObject<T extends SWTBot> extends DialogPageObject<T> implements ServerAction
{

    public AddAndRemoveProjectPageObject( T bot, String title, String cancelButtonText, String confirmButtonText )
    {
        super( bot, title, cancelButtonText, confirmButtonText );
    }

    public void add( String... projectItemNames )
    {
        for( String projectItemName : projectItemNames )
        {
            TreeItemPageObject<SWTBot> projectTree = new TreeItemPageObject<SWTBot>( bot, projectItemName );

            projectTree.select();

            bot.button( ADD ).click();
            bot.sleep( 200 );
        }

        confirm();
    }

    public void addAll()
    {
        bot.button( ADD_ALL ).click();
        bot.sleep( 200 );
        confirm();
    }

    public String getValidationMessgae()
    {
        return bot.text().getText();
    }

    public void remove( String... projectItemNames )
    {
        for( String projectItemName : projectItemNames )
        {
            TreeItemPageObject<SWTBot> projectTree = new TreeItemPageObject<SWTBot>( bot, projectItemName );
            projectTree.select();

            bot.button( REMOVE ).click();
            bot.sleep( 200 );
        }

        confirm();
    }

    public void removeAll()
    {
        bot.button( REMOVE_ALL ).click();
        bot.sleep( 200 );
        confirm();
    }

}

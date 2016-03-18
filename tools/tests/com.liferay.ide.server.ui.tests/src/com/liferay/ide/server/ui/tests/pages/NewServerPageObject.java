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

package com.liferay.ide.server.ui.tests.pages;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.server.ui.tests.ServerRuntimeWizard;
import com.liferay.ide.ui.tests.swtbot.page.TreePageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

/**
 * @author Vicky Wang
 */
public class NewServerPageObject<T extends SWTBot> extends WizardPageObject<T> implements ServerRuntimeWizard
{

    TreePageObject<SWTBot> serverTypeTree;

    public NewServerPageObject( T bot )
    {
        super( bot, TITLE_NEW_SERVER, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT );

        serverTypeTree = new TreePageObject<SWTBot>( bot );
    }

    public TreePageObject<SWTBot> getServerTypeTree()
    {
        return serverTypeTree;
    }

}

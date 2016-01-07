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

package com.liferay.ide.hook.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.project.ui.tests.ProjectWizard;
import com.liferay.ide.ui.tests.swtbot.page.ButtonPageObject;
import com.liferay.ide.ui.tests.swtbot.page.DialogPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;

/**
 * @author Vicky Wang
 */
public class NewClassPageObject<T extends SWTBot> extends DialogPageObject<T>
    implements HookConfigurationWizard, ProjectWizard
{

    TextPageObject<SWTBot> className;
    TextPageObject<SWTBot> javaPackage;

    ButtonPageObject<SWTBot> create;

    public NewClassPageObject( T bot, String title )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_OK );

        className = new TextPageObject<SWTBot>( bot, LABLE_CLASS_NAME );
        javaPackage = new TextPageObject<SWTBot>( bot, LABLE_JAVA_PACKAGE );
        create = new ButtonPageObject<SWTBot>( bot, BUTTON_CREATE );
    }

    public ButtonPageObject<SWTBot> getCreate()
    {
        return create;
    }

    public void setCreate( ButtonPageObject<SWTBot> create )
    {
        this.create = create;
    }

    public void setJavaPackage( String text )
    {
        javaPackage.setText( text );
    }

    public void setClassName( String text )
    {
        className.setText( text );
    }

}

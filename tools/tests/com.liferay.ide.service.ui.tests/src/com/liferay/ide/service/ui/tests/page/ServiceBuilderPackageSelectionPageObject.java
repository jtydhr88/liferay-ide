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

package com.liferay.ide.service.ui.tests.page;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.service.ui.tests.ServiceBuilderWizard;
import com.liferay.ide.ui.tests.swtbot.page.DialogPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;

/**
 * @author Ying Xu
 */
public class ServiceBuilderPackageSelectionPageObject<T extends SWTBot> extends DialogPageObject<T>
    implements ServiceBuilderWizard
{

    TextPageObject<SWTBot> packageSelectionText;

    public ServiceBuilderPackageSelectionPageObject( T bot, String cancelButtonText, String confirmButtonText )
    {
        this( bot, TEXT_BLANK, cancelButtonText, confirmButtonText );

    }

    public ServiceBuilderPackageSelectionPageObject(
        T bot, String title, String cancelButtonText, String confirmButtonText )
    {
        super( bot, title, cancelButtonText, confirmButtonText );
        packageSelectionText = new TextPageObject<>( bot, LABEL_CHOOSE_PACKAGE );

    }

    public void selectPackage( String packageText )
    {
        packageSelectionText.setText( packageText );
    }

}

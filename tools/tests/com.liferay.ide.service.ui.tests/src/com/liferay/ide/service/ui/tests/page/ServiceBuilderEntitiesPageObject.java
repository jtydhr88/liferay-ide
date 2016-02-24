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
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;

/**
 * @author Ying Xu
 */
public class ServiceBuilderEntitiesPageObject<T extends SWTBot> extends TextPageObject<T>
    implements ServiceBuilderWizard
{

    TextPageObject<SWTBot> entityName;

    public ServiceBuilderEntitiesPageObject( T bot )
    {
        this( bot, TEXT_BLANK );
    }

    public ServiceBuilderEntitiesPageObject( T bot, String title )
    {
        super( bot, title );

        entityName = new TextPageObject<>( bot, LABEL_ENTITY_NAME );
    }

    public void ServiceBuilderEntities( String name )
    {
        entityName.setText( name );
    }
}

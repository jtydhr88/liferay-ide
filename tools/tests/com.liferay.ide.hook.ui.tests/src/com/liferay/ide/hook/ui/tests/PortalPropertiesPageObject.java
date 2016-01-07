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
import com.liferay.ide.ui.tests.swtbot.page.TablePageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

/**
 * @author Vicky Wang
 */
public class PortalPropertiesPageObject<T extends SWTBot> extends WizardPageObject<T>
    implements HookConfigurationWizard, ProjectWizard
{

    ButtonPageObject<SWTBot> browse;
    ButtonPageObject<SWTBot> eventAdd;
    ButtonPageObject<SWTBot> eventEdit;
    ButtonPageObject<SWTBot> eventRemove;

    ButtonPageObject<SWTBot> propertyAdd;
    ButtonPageObject<SWTBot> propertyEdit;
    ButtonPageObject<SWTBot> propertyRemove;

    TextPageObject<SWTBot> portalPropertiesFile;
    TablePageObject<SWTBot> defineActionsOnPortalEvents;
    TablePageObject<SWTBot> specifyPropertiesToOverride;

    DialogPageObject<SWTBot> addEventAction;
    DialogPageObject<SWTBot> addPropertyOverride;
    DialogPageObject<SWTBot> newLiferayHookConfiguration;

    public PortalPropertiesPageObject( T bot, String title, int indexPortalPropertiesValidationMessage )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT, indexPortalPropertiesValidationMessage );
        portalPropertiesFile = new TextPageObject<SWTBot>( bot, LABLE_PORTAL_PROPERTIES_FILE );
        defineActionsOnPortalEvents = new TablePageObject<SWTBot>( bot, LABLE_DEFINE_ACTIONS );
        specifyPropertiesToOverride = new TablePageObject<SWTBot>( bot, LABLE_SPECIFY_PROPERTIES );

        addEventAction = new DialogPageObject<SWTBot>( bot, WINDOW_ADD_EVENT_ACTION, BUTTON_CANCEL, BUTTON_OK );
        addPropertyOverride =
            new DialogPageObject<SWTBot>( bot, WINDOW_ADD_PROPERTY_OVERRIDE, BUTTON_CANCEL, BUTTON_OK );
        newLiferayHookConfiguration =
            new DialogPageObject<SWTBot>( bot, WINDOW_NEW_LIFERAY_HOOK_CONFIGURATION, BUTTON_BACK, BUTTON_NEXT );

        browse = new ButtonPageObject<SWTBot>( bot, BUTTON_BROWSE, 0 );
        eventAdd = new ButtonPageObject<SWTBot>( bot, BUTTON_ADD, 0 );
        eventEdit = new ButtonPageObject<SWTBot>( bot, BUTTON_EDIT, 0 );
        eventRemove = new ButtonPageObject<SWTBot>( bot, BUTTON_REMOVE, 0 );

        propertyAdd = new ButtonPageObject<SWTBot>( bot, BUTTON_ADD, 1 );
        propertyEdit = new ButtonPageObject<SWTBot>( bot, BUTTON_EDIT, 1 );
        propertyRemove = new ButtonPageObject<SWTBot>( bot, BUTTON_REMOVE, 1 );
    }

    public void setPortalPropertiesFile( String text )
    {
        this.portalPropertiesFile.setText( text );
    }

    public ButtonPageObject<SWTBot> getEventAdd()
    {
        return eventAdd;
    }

    public ButtonPageObject<SWTBot> getEventEdit()
    {
        return eventEdit;
    }

    public ButtonPageObject<SWTBot> getEventRemove()
    {
        return eventRemove;
    }

    public ButtonPageObject<SWTBot> getPropertyAdd()
    {
        return propertyAdd;
    }

    public ButtonPageObject<SWTBot> getPropertyEdit()
    {
        return propertyEdit;
    }

    public ButtonPageObject<SWTBot> getPropertyRemove()
    {
        return propertyRemove;
    }

    public ButtonPageObject<SWTBot> getBrowse()
    {
        return browse;
    }

}

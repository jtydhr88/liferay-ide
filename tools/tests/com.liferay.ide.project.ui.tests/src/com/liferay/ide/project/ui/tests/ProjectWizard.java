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

package com.liferay.ide.project.ui.tests;

/**
 * @author Terry Jia
 * @author Ying Xu
 */
public interface ProjectWizard
{

    public final String TEXT_PROJECT_NAME = "Project name:";
    public final String TEXT_SDK_LOCATION = "SDK Location:";
    public final String TEXT_PLUGIN_TYPE = "Plugin type:";
    public final String TEXT_DISPLAY_NAME = "Display name:";

    public final String TOOLTIP_CREATE_LIFERAY_PROJECT = "Create a new Liferay Plugin Project";
    public final String TOOLTIP_MENU_ITEM_NEW_LIFERAY_PROJECT = "New Liferay Plugin Project";

    public final String MENU_PORTLET = "Portlet";
    public final String MENU_SERVICE_BUILDER_PORTLET = "Service Builder Portlet";
    public final String MENU_EXT = "Ext";
    public final String MENU_THEME = "Theme";
    public final String MENU_HOOK = "Hook";
    public final String MENU_LAYOUT_TEMPLATE = "Layout Template";
    public final String MENU_WEB = "Web";
    
    public final int INDEX_VALIDATION_MESSAGE = 2;
    public final int INDEX_VALIDATION_MESSAGE_WEB_SDKVERSION = 1;

}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.liferay.ide.ui.tests.SWTBotBase;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Terry Jia
 * @author Ying Xu
 */
public class ProjectWizardTests extends SWTBotBase implements ProjectWizard
{

    public static boolean added = false;

    private boolean addedProjecs()
    {
        viewUtil.show( "Package Explorer" );

        return treeUtil.hasItems();
    }

    @AfterClass
    public static void cleanAll()
    {
        SWTBotTreeItem[] items = treeUtil.getItems();

        try {
			for( SWTBotTreeItem item : items )
			{
			    item.contextMenu( "Delete" ).click();
			    bot.checkBox().click();
			    buttonUtil.click( BUTTON_OK );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    //@Test
    public void createPortletProject()
    {
        comboBoxUtil.select( 1, MENU_PORTLET );

        buttonUtil.click( BUTTON_NEXT );

        if( !added )
        {
            setSDKLocation();
        }

        buttonUtil.click( BUTTON_FINISH );
    }

    @Test
    public void createServiceBuilderPortletProject()
    {
    	textUtil.setText( TEXT_PROJECT_NAME, "testServiceBuilder" );
    	comboBoxUtil.select( TEXT_PLUGIN_TYPE, MENU_SERVICE_BUILDER_PORTLET );
    	
    	if(!checkBoxUtil.isChecked(CHECKBOX_INCLUDE_SAMPLE_CODE)){
    		checkBoxUtil.click( CHECKBOX_INCLUDE_SAMPLE_CODE );
    	}

        if( !added )
        {
            setSDKLocation();
        }

        buttonUtil.click( BUTTON_FINISH );
        comboBoxUtil.sleep(5000);
        assertTrue(treeUtil.getTreeItem("testServiceBuilder-portlet").isVisible());
        assertTrue((treeUtil.expandNode("testServiceBuilder-portlet").expandNode("docroot").getNode("view.jsp")).isVisible());
        assertTrue((treeUtil.expandNode("testServiceBuilder-portlet").expandNode("docroot").expandNode("css").getNode("main.css")).isVisible());
        assertTrue((treeUtil.expandNode("testServiceBuilder-portlet").expandNode("docroot").expandNode("js").getNode("main.js")).isVisible());
    }
    
    @Test
    public void createServiceBuilderPortletProjectWithoutSampleCode()
    {
    	textUtil.setText( TEXT_PROJECT_NAME, "testServiceBuilderWithoutSampleCode" );
    	comboBoxUtil.select( TEXT_PLUGIN_TYPE, MENU_SERVICE_BUILDER_PORTLET );
    	
    	if(checkBoxUtil.isChecked(CHECKBOX_INCLUDE_SAMPLE_CODE)){
    		checkBoxUtil.click( CHECKBOX_INCLUDE_SAMPLE_CODE );
    	}

        if( !added )
        {
            setSDKLocation();
        }
        
        buttonUtil.click( BUTTON_FINISH );
        assertTrue(treeUtil.getTreeItem("testServiceBuilderWithoutSampleCode-portlet").isVisible());
    }

    @Test
    public void createHookProject()
    {
    	textUtil.setText( TEXT_PROJECT_NAME, "testHook" );
    	comboBoxUtil.select( TEXT_PLUGIN_TYPE, MENU_HOOK );

        if( !added )
        {
            setSDKLocation();
        }

        buttonUtil.click( BUTTON_FINISH );
        assertTrue(treeUtil.getTreeItem("testHook-hook").isVisible());
    }

    @Test
    public void createExtProject()
    {
    	textUtil.setText( TEXT_PROJECT_NAME, "testExt" );
    	comboBoxUtil.select( TEXT_PLUGIN_TYPE, MENU_EXT );

        if( !added )
        {
            setSDKLocation();
        }

        buttonUtil.click( BUTTON_FINISH );
        assertTrue(treeUtil.getTreeItem("testExt-ext").isVisible());
    }

    //@Test
    public void createThemeProject()
    {
        comboBoxUtil.select( 1, MENU_THEME );

        buttonUtil.click( BUTTON_NEXT );

        if( !added )
        {
            setSDKLocation();
        }

        buttonUtil.click( BUTTON_FINISH );
    }

    @Test
    public void createLayoutProject()
    {
    	textUtil.setText( TEXT_PROJECT_NAME, "testLayout" );
    	comboBoxUtil.select( TEXT_PLUGIN_TYPE, MENU_LAYOUT_TEMPLATE );

        if( !added )
        {
            setSDKLocation();
        }

        buttonUtil.click( BUTTON_FINISH );
        assertTrue(treeUtil.getTreeItem("testLayout-layouttpl").isVisible());

    }

    @Test
    public void createWebProject()
    {
    	textUtil.setText( TEXT_PROJECT_NAME, "testWeb" );
    	comboBoxUtil.select( TEXT_PLUGIN_TYPE, MENU_WEB );

        comboBoxUtil.sleep(1000);

        if( !added )
        {
            setSDKLocation();
            comboBoxUtil.sleep();
            assertEquals(" The selected Plugins SDK does not support creating new web type plugins.  Please configure version 7.0.0 or greater.", textUtil.getText(INDEX_VALIDATION_MESSAGE_WEB_SDKVERSION));
        }else{
        	assertEquals(" The selected Plugins SDK does not support creating new web type plugins.  Please configure version 7.0.0 or greater.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        }

        assertEquals( false, buttonUtil.isEnabled( BUTTON_FINISH ) );

        shellUtil.close();
    }

    @Before
    public void openWizard()
    {
        added = addedProjecs();

        toolbarUtil.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_PROJECT );

        assertEquals("Please enter a project name.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, ".." );
        toolbarUtil.sleep(1000);
        assertEquals(" '..' is an invalid name on this platform.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, " " );
        toolbarUtil.sleep(1000);
        assertEquals(" Project name must be specified", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "/" );
        toolbarUtil.sleep(1000);
        assertEquals(" / is an invalid character in resource name '/'.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "/ 1" );
        toolbarUtil.sleep(1000);
        assertEquals(" / is an invalid character in resource name '/ 1'.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "/ aaa" );
        toolbarUtil.sleep(1000);
        assertEquals(" / is an invalid character in resource name '/ aaa'.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "-" );
        toolbarUtil.sleep(1000);
        assertEquals(" The project name is invalid.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "_" );
        toolbarUtil.sleep(1000);
        assertEquals(" The project name is invalid.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "$^%*(($&" );
        toolbarUtil.sleep(1000);
        assertEquals(" * is an invalid character in resource name '$^%*(($&'.", textUtil.getText(INDEX_VALIDATION_MESSAGE));
        textUtil.setText( TEXT_PROJECT_NAME, "test" );
        toolbarUtil.sleep(1000);
        assertEquals("Create a new project configured as a Liferay plugin", textUtil.getText(INDEX_VALIDATION_MESSAGE));
    }

    private void setSDKLocation()
    {
        buttonUtil.click( BUTTON_NEXT );
        textUtil.setText( TEXT_SDK_LOCATION, "D:\\work\\liferay-plugins-sdk\\liferay-plugins-sdk-6.2-ee-sp10" );
    }

}

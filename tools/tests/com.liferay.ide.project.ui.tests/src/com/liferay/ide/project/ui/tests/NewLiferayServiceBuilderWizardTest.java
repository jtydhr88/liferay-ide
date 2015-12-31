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

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;
import static org.junit.Assert.assertEquals;
import com.liferay.ide.ui.tests.SWTBotBase;
import com.liferay.ide.ui.tests.swtbot.page.DialogPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextEditorPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TreeItemPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TreePageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ying Xu
 */
public class NewLiferayServiceBuilderWizardTest extends SWTBotBase implements ServiceBuilderWizard
{

    public static boolean added = false;

    private boolean addedProjects()
    {
        viewBot.show( VIEW_PACKAGE_EXPLORER );

        return treeBot.hasItems();
    }

    @After
    public void timeout()
    {
        sleep( 5000 );
    }

    @AfterClass
    public static void deleteProject()
    {
        try
        {
            TreePageObject<SWTWorkbenchBot> tree = new TreePageObject<SWTWorkbenchBot>( bot );
            String[] projects = tree.getAllItems();

            for( String project : projects )
            {
                ProjectTreePageObject<SWTWorkbenchBot> projectItem =
                    new ProjectTreePageObject<SWTWorkbenchBot>( bot, project );
                
                if( projectItem.getText().equals( getLiferayPluginsSdkName() ) )
                    continue;

                projectItem.deleteProject();
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Test
    public void createServiceBuilderWizardWithoutPortletProject()
    {
        DialogPageObject<SWTWorkbenchBot> page1 = new DialogPageObject<SWTWorkbenchBot>( bot, "", "No", "Yes" );
        page1.confirm();
        sleep( 3000 );
        WizardPageObject<SWTWorkbenchBot> page2 =
            new WizardPageObject<SWTWorkbenchBot>( bot, "", BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT );
        page2.cancel();
        page1.cancel();
        sleep( 3000 );
        NewLiferayServiceBuilderWizardPageObject<SWTWorkbenchBot> page3 =
            new NewLiferayServiceBuilderWizardPageObject<SWTWorkbenchBot>(
                bot, "", "", BUTTON_CANCEL, INDEX_VALIDATION_MESSAGE );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, page3.getValidationMessage() );
        page3.NewServiceBuilder( "packagePath", "namespace" );
        assertEquals( TEXT_ENTER_PROJECT_NAME_SERVICEBUILDER, page3.getValidationMessage() );
        page3.cancel();
    }

    @Test
    public void createServiceBuilderWizard()
    {
        DialogPageObject<SWTWorkbenchBot> page1 = new DialogPageObject<SWTWorkbenchBot>( bot, "", "No", "Yes" );
        page1.confirm();
        sleep( 3000 );
        CreateProjectWizardPageObject<SWTWorkbenchBot> page2 =
            new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, "" );
        page2.createSDKProject( "sbwizardtest", MENU_PORTLET, true );
        if( added )
        {
            page2.next();
            page2.finish();
        }
        else
        {
            page2.next();
            CreateProjectWizardPageObject<SWTWorkbenchBot> page3 =
                new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, "" );
            page3.next();
            SetSDKLocationPageObject<SWTWorkbenchBot> page4 = new SetSDKLocationPageObject<SWTWorkbenchBot>( bot, "" );
            page4.setSdkLocation( getLiferayPluginsSdkDir().toString() );
            page4.finish();
        }
        sleep( 12000 );
        NewLiferayServiceBuilderWizardPageObject<SWTWorkbenchBot> page5 =
            new NewLiferayServiceBuilderWizardPageObject<SWTWorkbenchBot>(
                bot, "", BUTTON_FINISH, BUTTON_CANCEL, INDEX_VALIDATION_MESSAGE );
        page5.NewServiceBuilder( "packagePath", "namespace" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, page5.getValidationMessage() );
        page5.confirm();
        sleep( 3000 );
        TreeItemPageObject<SWTWorkbenchBot> servcieXml =
            new TreeItemPageObject<SWTWorkbenchBot>( bot, "sbwizardtest-portlet", "docroot", "WEB-INF", "service.xml" );
        servcieXml.isVisible();
        servcieXml.doubleClick();
        TextEditorPageObject textEditorPage = new TextEditorPageObject( bot, "service.xml" );
        assertContains( "Foo", textEditorPage.getText() );

        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_NEW_LIFERAY_SERVICE_BUILDER );
        page5.NewServiceBuilder( "packagePath1", "namespace1" );
        assertEquals( TEXT_HAS_SERVICE_BUILDER_XML_FILE_MESSAGE, page5.getValidationMessage() );
        page5.cancel();
        sleep( 3000 );

        ProjectTreePageObject<SWTWorkbenchBot> project = new ProjectTreePageObject<>( bot, "sbwizardtest-portlet" );
        project.deleteProject();
    }

    @Test
    public void createServiceBuilderWizardWithoutSmapleEntity()
    {
        DialogPageObject<SWTWorkbenchBot> page1 = new DialogPageObject<SWTWorkbenchBot>( bot, "", "No", "Yes" );
        page1.confirm();
        sleep( 3000 );
        CreateProjectWizardPageObject<SWTWorkbenchBot> page2 =
            new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, "" );
        page2.createSDKProject( "sbwizardtestwithoutentity", MENU_PORTLET, true );
        if( added )
        {
            page2.next();
            page2.finish();
        }
        else
        {
            page2.next();
            CreateProjectWizardPageObject<SWTWorkbenchBot> page3 =
                new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, "" );
            page3.next();
            SetSDKLocationPageObject<SWTWorkbenchBot> page4 = new SetSDKLocationPageObject<SWTWorkbenchBot>( bot, "" );
            page4.setSdkLocation( getLiferayPluginsSdkDir().toString() );
            page4.finish();
        }
        sleep( 12000 );
        NewLiferayServiceBuilderWizardPageObject<SWTWorkbenchBot> page5 =
            new NewLiferayServiceBuilderWizardPageObject<SWTWorkbenchBot>(
                bot, "", BUTTON_FINISH, BUTTON_CANCEL, INDEX_VALIDATION_MESSAGE );
        page5.NewServiceBuilder( "packagePath", "namespace", false );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, page5.getValidationMessage() );
        page5.confirm();
        sleep( 3000 );

        ProjectTreePageObject<SWTWorkbenchBot> project =
            new ProjectTreePageObject<>( bot, "sbwizardtestwithoutentity-portlet" );
        project.deleteProject();
    }

    @Before
    public void openWizard()
    {
        added = addedProjects();

        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_NEW_LIFERAY_SERVICE_BUILDER );
    }

}

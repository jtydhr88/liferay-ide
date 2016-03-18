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

package com.liferay.ide.service.ui.tests;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.liferay.ide.project.ui.tests.page.CreateProjectWizardPageObject;
import com.liferay.ide.project.ui.tests.page.ProjectTreePageObject;
import com.liferay.ide.project.ui.tests.page.ProjectWizard;
import com.liferay.ide.project.ui.tests.page.SetSDKLocationPageObject;
import com.liferay.ide.service.ui.tests.page.ServiceBuilderEntitiesPageObject;
import com.liferay.ide.service.ui.tests.page.ServiceBuilderPackageSelectionPageObject;
import com.liferay.ide.service.ui.tests.page.ServiceBuilderWizardPageObject;
import com.liferay.ide.ui.tests.SWTBotBase;
import com.liferay.ide.ui.tests.swtbot.page.CTabItemPageObject;
import com.liferay.ide.ui.tests.swtbot.page.DialogPageObject;
import com.liferay.ide.ui.tests.swtbot.page.EditorPageObject;
import com.liferay.ide.ui.tests.swtbot.page.NewJavaPackagePageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextEditorPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TreeItemPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TreePageObject;
//import com.liferay.ide.ui.tests.swtbot.page.PopUpPageObject;
//import com.liferay.ide.ui.tests.swtbot.page.TablePageObject;

/**
 * @author Ying Xu
 */
public class NewLiferayServiceBuilderWizardTest extends SWTBotBase implements ServiceBuilderWizard, ProjectWizard
{

    public static boolean added = false;

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

    private boolean addedProjects()
    {
        viewBot.show( VIEW_PACKAGE_EXPLORER );

        return treeBot.hasItems();
    }

    @Test
    public void createServiceBuilderWizard() throws Exception
    {
        DialogPageObject<SWTWorkbenchBot> noProjectDialog =
            new DialogPageObject<SWTWorkbenchBot>( bot, LABEL_SERVICE_BUILDER_TITLE, BUTTON_NO, BUTTON_YES );

        noProjectDialog.confirm();

        CreateProjectWizardPageObject<SWTWorkbenchBot> createProjectWizard =
            new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, TOOLTIP_MENU_ITEM_NEW_LIFERAY_PROJECT );

        createProjectWizard.waitForPageToOpen();
        createProjectWizard.createSDKProject( "sbwizardtest", MENU_PORTLET, true );
        createProjectWizard.next();

        if( added )
        {
            createProjectWizard.finish();
        }
        else
        {
            createProjectWizard.next();

            SetSDKLocationPageObject<SWTWorkbenchBot> setSDKLocation =
                new SetSDKLocationPageObject<SWTWorkbenchBot>( bot );

            setSDKLocation.setSdkLocation( getLiferayPluginsSdkDir().toString() );
            setSDKLocation.finish();
        }

        ServiceBuilderWizardPageObject<SWTWorkbenchBot> newServiceBuilderWizard =
            new ServiceBuilderWizardPageObject<SWTWorkbenchBot>( bot, TOOLTIP_NEW_SERVICE_BUILDER );

        newServiceBuilderWizard.waitForPageToOpen();

        // check initial state
        String author = System.getenv( "USERNAME" );
        assertEquals( author, newServiceBuilderWizard.getAuthorText().getText() );
        assertEquals( "sbwizardtest-portlet", newServiceBuilderWizard.getPluginProjectComboBox().getText() );
        assertEquals( TEXT_SERVICE_FILE_VALUE, newServiceBuilderWizard.getServiceFileText().getText() );
        assertEquals( TEXT_DEFAULT_PACKAGE_PAHT_VALUE, newServiceBuilderWizard.getPackagePathText().getText() );
        assertEquals( TEXT_DEFAULT_NAMESPACE_VALUE, newServiceBuilderWizard.getNamespaceText().getText() );
        assertTrue( newServiceBuilderWizard.getIncludeSampleEntityCheckBox().isChecked() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        newServiceBuilderWizard.NewServiceBuilder( "packagePath", "namespace" );

        // validation test for package path
        newServiceBuilderWizard.getPackagePathText().setText( "_" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "1" );
        assertEquals(
            TEXT_VALIDATION_PACKAGE_PATH_MESSAGE + "'1' is not a valid Java identifier",
            newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "-" );
        assertEquals(
            TEXT_VALIDATION_PACKAGE_PATH_MESSAGE + "'-' is not a valid Java identifier",
            newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "P" );
        assertEquals(
            " Warning: By convention, package names usually start with a lowercase letter",
            newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "a1" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "a_" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "a-" );
        assertEquals(
            TEXT_VALIDATION_PACKAGE_PATH_MESSAGE + "'a-' is not a valid Java identifier",
            newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "packagePath" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );

        // validation test for namespace
        newServiceBuilderWizard.getNamespaceText().setText( "a1" );
        assertEquals( TEXT_VALIDATION_NAMESPACE_MESSAGE, newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getNamespaceText().setText( "a-" );
        assertEquals( TEXT_VALIDATION_NAMESPACE_MESSAGE, newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getNamespaceText().setText( "a_" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getNamespaceText().setText( "namespace" );
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );

        newServiceBuilderWizard.finish();
        sleep( 3000 );

        EditorPageObject checkServiceFileActive = new EditorPageObject( bot, "service.xml" );

        checkServiceFileActive.isActive();

        TextEditorPageObject textEditorPage = new TextEditorPageObject( bot, "service.xml" );

        assertContains( "Foo", textEditorPage.getText() );
        assertContains( "packagePath", textEditorPage.getText() );
        assertContains( "namespace", textEditorPage.getText() );
        assertContains( author, textEditorPage.getText() );

        Keyboard keyPress = KeyboardFactory.getAWTKeyboard();

        // regression test for IDE-1395,check 9 items which start with sb
        // keyPress.pressShortcut( enter );
        // keyPress.pressShortcut( up );
        // keyPress.pressShortcut( alt, slash );

        // PopUpPageObject<SWTWorkbenchBot> popUpPage = new PopUpPageObject<SWTWorkbenchBot>( bot, 4 );

        // popUpPage.activate();

        // TablePageObject<SWTWorkbenchBot> checkContainItem = new TablePageObject<>( bot, 0 );

        // checkContainItem.containsItem( "sb-column - a column for an entity" );
        // checkContainItem.containsItem( "sb-columnp-primary - a primary column for an entity" );
        // checkContainItem.containsItem( "sb-entity - an entity element" );
        // checkContainItem.containsItem( "sb-entity-sample - a simple entity element" );
        // checkContainItem.containsItem( "sb-exceptions - an exceptions element" );
        // checkContainItem.containsItem( "sb-finder - a finder element" );
        // checkContainItem.containsItem( "sb-finder-column - a finder column for a finder" );
        // checkContainItem.containsItem( "sb-order - an order element" );
        // checkContainItem.containsItem( "sb-order-column - an order column for an order" );

        // choose sb-column and check fast input codes
        // checkContainItem.doubleClick( 2, 0 );
        // servcieXml.select();
        // keyPress.pressShortcut( ctrl, S );
        // assertContains(
        // "column name=" + '"' + "name" + '"' + " type=" + '"' + "String" + '"', textEditorPage.getText() );

        // regression test for IDE-726 and IDE-777,check DTD version
        assertContains( SERVICE_BUILDER_DTD_VERSION, textEditorPage.getText() );
        sleep();

        EditorPageObject closeServiceFile = new EditorPageObject( bot, "service.xml" );

        closeServiceFile.close();

        File serviceXMLFile = getLiferayBundlesPath().append( "service.xml" ).toFile();

        if( !serviceXMLFile.exists() )
        {
            try
            {
                serviceXMLFile.createNewFile();
            }
            catch( IOException e )
            {
            }
        }

        openFile( serviceXMLFile.getAbsolutePath() );

        checkServiceFileActive.isActive();

        CTabItemPageObject<SWTWorkbenchBot> switchCTabItem = new CTabItemPageObject<SWTWorkbenchBot>( bot, "Overview" );

        switchCTabItem.click();

        TreeItemPageObject<SWTWorkbenchBot> addEntities =
            new TreeItemPageObject<SWTWorkbenchBot>( bot, 1, "Service Builder", "Entities" );

        addEntities.doAction( "Add Entity" );

        ServiceBuilderEntitiesPageObject<SWTWorkbenchBot> requireAttributeName =
            new ServiceBuilderEntitiesPageObject<SWTWorkbenchBot>( bot );

        requireAttributeName.ServiceBuilderEntities( "entity" );

        switchCTabItem = new CTabItemPageObject<SWTWorkbenchBot>( bot, "Source" );

        switchCTabItem.click();
        keyPress.pressShortcut( ctrl, S );
        assertContains( "6.0.0", textEditorPage.getText() );

        closeServiceFile.close();
        serviceXMLFile.delete();

        toolbarBot.menuClick( TOOLTIP_MENU_NEW, TOOLTIP_MENU_LIFERAY_SERVICE_BUILDER );
        newServiceBuilderWizard.NewServiceBuilder( "packagePath1", "namespace1" );
        assertEquals( TEXT_ALREADY_HAS_SERVICE_BUILDER_XML_FILE_MESSAGE, newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.cancel();
        sleep( 3000 );

        ProjectTreePageObject<SWTWorkbenchBot> project = new ProjectTreePageObject<>( bot, "sbwizardtest-portlet" );

        project.deleteProject();
    }

    @Test
    public void createServiceBuilderWizardWithoutPortletProject()
    {
        DialogPageObject<SWTWorkbenchBot> noProjectDialog =
            new DialogPageObject<SWTWorkbenchBot>( bot, LABEL_SERVICE_BUILDER_TITLE, BUTTON_NO, BUTTON_YES );

        noProjectDialog.confirm();

        CreateProjectWizardPageObject<SWTWorkbenchBot> createProjectWizard =
            new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, TOOLTIP_MENU_ITEM_NEW_LIFERAY_PROJECT );

        createProjectWizard.waitForPageToOpen();
        createProjectWizard.cancel();
        noProjectDialog.cancel();

        ServiceBuilderWizardPageObject<SWTWorkbenchBot> newServiceBuilderWizard =
            new ServiceBuilderWizardPageObject<SWTWorkbenchBot>( bot, TOOLTIP_NEW_SERVICE_BUILDER );

        newServiceBuilderWizard.waitForPageToOpen();

        // check service builder default initial state
        String author = System.getenv( "USERNAME" );
        assertEquals( author, newServiceBuilderWizard.getAuthorText().getText() );
        assertEquals( TEXT_DEFAULT_PLUGIN_PROJECT_VALUE, newServiceBuilderWizard.getPluginProjectComboBox().getText() );
        assertEquals( TEXT_SERVICE_FILE_VALUE, newServiceBuilderWizard.getServiceFileText().getText() );
        assertEquals( TEXT_DEFAULT_PACKAGE_PAHT_VALUE, newServiceBuilderWizard.getPackagePathText().getText() );
        assertEquals( TEXT_DEFAULT_NAMESPACE_VALUE, newServiceBuilderWizard.getNamespaceText().getText() );
        assertTrue( newServiceBuilderWizard.getIncludeSampleEntityCheckBox().isChecked() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_BROWSE ) );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_CANCEL ) );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );

        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        newServiceBuilderWizard.NewServiceBuilder( "packagePath", "namespace" );
        assertEquals( TEXT_ENTER_PROJECT_NAME_MESSAGE, newServiceBuilderWizard.getValidationMessage() );
        newServiceBuilderWizard.cancel();

    }

    @Test
    public void createServiceBuilderWizardWithoutSmapleEntity()
    {
        DialogPageObject<SWTWorkbenchBot> noProjectDialog =
            new DialogPageObject<SWTWorkbenchBot>( bot, LABEL_SERVICE_BUILDER_TITLE, BUTTON_NO, BUTTON_YES );

        noProjectDialog.confirm();

        CreateProjectWizardPageObject<SWTWorkbenchBot> createProjectWizard =
            new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, TOOLTIP_MENU_ITEM_NEW_LIFERAY_PROJECT );

        createProjectWizard.waitForPageToOpen();
        createProjectWizard.createSDKProject( "sbwizardtestwithoutentity", MENU_PORTLET, true );
        createProjectWizard.next();

        if( added )
        {
            createProjectWizard.finish();
        }
        else
        {
            createProjectWizard.next();

            SetSDKLocationPageObject<SWTWorkbenchBot> setSDKLocation =
                new SetSDKLocationPageObject<SWTWorkbenchBot>( bot );

            setSDKLocation.setSdkLocation( getLiferayPluginsSdkDir().toString() );
            setSDKLocation.finish();
        }

        ServiceBuilderWizardPageObject<SWTWorkbenchBot> newServiceBuilderWizard =
            new ServiceBuilderWizardPageObject<SWTWorkbenchBot>( bot, TOOLTIP_NEW_SERVICE_BUILDER );

        newServiceBuilderWizard.waitForPageToOpen();
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_BROWSE ) );

        // package which is not exist test
        newServiceBuilderWizard.browse();

        ServiceBuilderPackageSelectionPageObject<SWTWorkbenchBot> chooseOnePackage =
            new ServiceBuilderPackageSelectionPageObject<SWTWorkbenchBot>( bot, BUTTON_CANCEL, BUTTON_OK );

        chooseOnePackage.selectPackage( "a" );
        assertFalse( chooseOnePackage.isButtonEnabled( BUTTON_OK ) );
        chooseOnePackage.selectPackage( "" );
        assertTrue( chooseOnePackage.isButtonEnabled( BUTTON_OK ) );
        chooseOnePackage.confirm();
        assertEquals( TEXT_PACKAGE_PATH_EMPTY_MESSAGE, newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.getPackagePathText().setText( "path1" );
        assertEquals( TEXT_NAMESPACE_EMPTY_MESSAGE, newServiceBuilderWizard.getValidationMessage() );
        assertFalse( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.cancel();

        // new a java package
        toolbarBot.menuClick( TOOLTIP_MENU_NEW, TOOLTIP_MENU_PACKAGE );

        NewJavaPackagePageObject<SWTWorkbenchBot> newJavaPackage = new NewJavaPackagePageObject<SWTWorkbenchBot>( bot );

        newJavaPackage.setName( "newpackage" );
        assertEquals(
            "sbwizardtestwithoutentity-portlet/docroot/WEB-INF/src", newJavaPackage.getSourceFolderText().getText() );
        newJavaPackage.finish();

        TreeItemPageObject<SWTWorkbenchBot> checkPackageVisible =
            new TreeItemPageObject<>( bot, "sbwizardtestwithoutentity-portlet", "docroot/WEB-INF/src", "newpackage" );

        checkPackageVisible.isVisible();

        menuBot.menuClick( MENU_FILE, MENU_NEW, MENU_SERVICE_BUILDER );
        newServiceBuilderWizard.browse();
        chooseOnePackage.selectPackage( "newpackage" );
        assertTrue( chooseOnePackage.isButtonEnabled( BUTTON_OK ) );
        chooseOnePackage.confirm();
        newServiceBuilderWizard.getNamespaceText().setText( "namespace" );
        newServiceBuilderWizard.getAuthorText().setText( "liferay-v" );
        newServiceBuilderWizard.getIncludeSampleEntityCheckBox().deselect();
        assertEquals( TEXT_NEW_SERVICE_BUILDER_XML_FILE, newServiceBuilderWizard.getValidationMessage() );
        assertTrue( newServiceBuilderWizard.isButtonEnabled( BUTTON_FINISH ) );
        newServiceBuilderWizard.finish();
        sleep( 3000 );

        EditorPageObject checkServiceFileActive = new EditorPageObject( bot, "service.xml" );

        checkServiceFileActive.isActive();

        TextEditorPageObject textEditorPage = new TextEditorPageObject( bot, "service.xml" );

        String author = System.getenv( "USERNAME" );
        assertContains( "newpackage", textEditorPage.getText() );
        assertContains( "namespace", textEditorPage.getText() );
        assertContains( author + "-v", textEditorPage.getText() );

        ProjectTreePageObject<SWTWorkbenchBot> project =
            new ProjectTreePageObject<>( bot, "sbwizardtestwithoutentity-portlet" );

        project.deleteProject();
    }

    @Before
    public void openWizard()
    {
        added = addedProjects();

        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_SERVICE_BUILDER );
    }

    @After
    public void timeout()
    {
        sleep( 5000 );
    }

}

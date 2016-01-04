
package com.liferay.ide.hook.ui.tests;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.liferay.ide.project.ui.tests.CreateProjectWizardPageObject;
import com.liferay.ide.project.ui.tests.SetSDKLocationPageObject;
import com.liferay.ide.ui.tests.SWTBotBase;
import com.liferay.ide.ui.tests.TextBot;
import com.liferay.ide.ui.tests.swtbot.page.EditorPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextEditorPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TreeItemPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TreePageObject;

/**
 * @author Vicky Wang
 */
public class HookConfigurationWizardTests extends SWTBotBase implements HookConfigurationWizard
{

    public static boolean added = false;
    
    String projectHookName = "hook-configuration-wizard";
    HookTypesToCreatePageObject<SWTWorkbenchBot> newHookTypesPage =
                    new HookTypesToCreatePageObject<SWTWorkbenchBot>( bot, "New Liferay Hook Configuration" );

    @After
    public void waitForCreate()
    {
        sleep( 5000 );
    }

    @AfterClass
    public static void cleanAll()
    {
        SWTBotTreeItem[] items = treeBot.getItems();

        try
        {
            for( SWTBotTreeItem item : items )
            {
                if( !item.getText().equals( getLiferayPluginsSdkName() ) )
                {
                    item.contextMenu( BUTTON_DELETE ).click();

                    checkBoxBot.click();

                    buttonBot.click( BUTTON_OK );

                    if( buttonBot.isEnabled( "Continue" ) )
                    {
                        buttonBot.click( "Continue" );
                    }

                }
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
    public void hookConfigurationCustomJSPs()
    {
        String defaultMessage = "Create customs JSP folder and select JSPs to override.";
        String errorMessage = " Custom JSPs folder not configured.";

        CreateCustomJSPsPageObject<SWTWorkbenchBot> customJSPpage =
            new CreateCustomJSPsPageObject<SWTWorkbenchBot>( bot, "", INDEX_CUSTOM_JSPS_VALIDATION_MESSAGE );
        
        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_HOOK_CONFIGURATION );

        checkBoxBot.click( LABLE_CUSTOM_JSPS );
        newHookTypesPage.next();
        sleep( 1000 );

        // Custom JSPs page
        assertEquals( defaultMessage, customJSPpage.getValidationMessage() );
        assertEquals( "hook-configuration-wizard-hook", customJSPpage.selectedProject.getText() );
        assertEquals( "docroot", customJSPpage.webRootFolder.getText() );
        assertEquals( "/META-INF/custom_jsps", customJSPpage.customJSPfolder.getText() );
        customJSPpage.customJSPfolder.setText( "" );
        sleep( 500 );
        assertEquals( errorMessage, customJSPpage.getValidationMessage() );
        buttonBot.click( BUTTON_BROWSE );
        sleep( 500 );
        TreeItemPageObject<SWTBot> treeItem = new TreeItemPageObject<>( bot, "hook-configuration-wizard-hook" );
        treeItem.expand();
        treeItem.select( "docroot" );
        buttonBot.click( BUTTON_OK );
        customJSPpage.customJSPfolder.setText( "/META-INF/custom_jsps" );
        // JSP files to override
        buttonBot.click( BUTTON_ADD_FROM_LIFERAY );
        TreeItemPageObject<SWTBot> jspItem =
            new TreeItemPageObject<SWTBot>( bot, "html", "common", "themes", "body_bottom.jsp" );
        jspItem.select();
        buttonBot.click( BUTTON_OK );
        sleep( 500 );
        buttonBot.click( BUTTON_ADD );
        customJSPpage.jspFilePath.setText( "test.jsp" );
        buttonBot.click( BUTTON_OK );
        customJSPpage.jspFilesToOverride.click( 1, 0 );
        buttonBot.click( BUTTON_EDIT );
        customJSPpage.jspFilePath.setText( "hooktest.jsp" );
        buttonBot.click( BUTTON_OK );
        customJSPpage.jspFilesToOverride.click( 1, 0 );
        buttonBot.click( BUTTON_REMOVE );

        buttonBot.click( BUTTON_FINISH );
        sleep( 1000 );
        treeBot.doubleClick( "body_bottom.jsp", projectHookName + "-hook", "docroot", "META-INF", "custom_jsps", "html",
            "common", "themes" );
    }
    
    @Test
    public void hookConfigurationPortalProperties()
    {
        String defaultMessage = "Specify which portal properties to override.";
        String errorMessage = " portal.properties file not configured.";

        PortalPropertiesPageObject<SWTWorkbenchBot> portalPropertiesPage =
            new PortalPropertiesPageObject<SWTWorkbenchBot>( bot, "", INDEX_PORTAL_PROPERTIES_VALIDATION_MESSAGE );
        
        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_HOOK_CONFIGURATION );

        checkBoxBot.click( LABLE_PORTAL_PROPERTIES );
        newHookTypesPage.next();
        sleep( 1000 );

        assertEquals( defaultMessage, portalPropertiesPage.getValidationMessage() );
        assertEquals(
            "/hook-configuration-wizard-hook/docroot/WEB-INF/src/portal.properties",
            portalPropertiesPage.portalPropertiesFile.getText() );
        portalPropertiesPage.portalPropertiesFile.setText( "" );
        sleep( 500 );
        assertEquals( errorMessage, portalPropertiesPage.getValidationMessage() );
        buttonBot.click( BUTTON_BROWSE );
        sleep( 500 );

        TreeItemPageObject<SWTBot> treeItem =
            new TreeItemPageObject<>( bot, "hook-configuration-wizard-hook", "docroot", "WEB-INF", "src" );
        treeItem.select();
        buttonBot.click( BUTTON_OK );

        // Define actions to be executed on portal events
        buttonBot.click( BUTTON_ADD );
        buttonBot.click( BUTTON_NEW );
        sleep( 500 );
        portalPropertiesPage.className.setText( "test" );
        portalPropertiesPage.javaPackage.setText( "hook" );
        buttonBot.click( BUTTON_CREATE );
        sleep( 500 );
        buttonBot.click( BUTTON_SELECT );
        TreePageObject<SWTBot> tree=new TreePageObject<>( bot );
        tree.select( "application.startup.events" );
        buttonBot.click( BUTTON_OK );
        sleep( 500 );
        portalPropertiesPage.addEventAction.setFocus();
        buttonBot.click( BUTTON_OK );
        sleep( 500 );

        buttonBot.click( BUTTON_ADD );
        buttonBot.click( BUTTON_SELECT, 0 );
        tree.select( "application.startup.events" );
        buttonBot.click( BUTTON_OK );
        sleep( 500 );
        portalPropertiesPage.addEventAction.setFocus();
        buttonBot.click( BUTTON_SELECT, 1 );
        sleep( 500 );

        TextBot text = new TextBot( bot );
        text.setText( LABLE_SELECT_AN_EVENT_ACTION, "ObjectAction" );

        buttonBot.click( BUTTON_OK, 0 );
        sleep( 500 );
        buttonBot.click( BUTTON_OK );
        sleep( 1000 );

        portalPropertiesPage.defineActionsOnPortalEvents.click( 1, 1 );
        buttonBot.click( BUTTON_REMOVE );
        portalPropertiesPage.defineActionsOnPortalEvents.click( 0, 1 );
        buttonBot.click( BUTTON_EDIT );
        portalPropertiesPage.eventActionclass.setText( "test_hook" );
        buttonBot.click( BUTTON_OK );

        // Specify properties to override
        buttonBot.click( BUTTON_ADD, 1 );
        buttonBot.click( BUTTON_SELECT );
        tree.select( "admin.default.group.names" );
        text.setText( LABLE_SELECT_A_PROPERTY, "admin.default.group.names" );
        buttonBot.click( BUTTON_OK );
        sleep( 1000 );
        portalPropertiesPage.addPropertyOverride.setFocus();
        portalPropertiesPage.value.setText( "1" );
        buttonBot.click( BUTTON_OK );

        buttonBot.click( BUTTON_ADD, 1 );
        portalPropertiesPage.property.setText( "test" );
        portalPropertiesPage.value.setText( "2" );
        buttonBot.click( BUTTON_OK );
        sleep( 500 );

        portalPropertiesPage.newLiferayHookConfiguration.setFocus();
        portalPropertiesPage.specifyPropertiesToOverride.click( 1, 1 );
        buttonBot.click( BUTTON_EDIT, 1 );
        portalPropertiesPage.property.setText( "test_hook" );
        portalPropertiesPage.value.setText( "3" );
        buttonBot.click( BUTTON_OK );
        portalPropertiesPage.newLiferayHookConfiguration.setFocus();
        portalPropertiesPage.specifyPropertiesToOverride.click( 1, 1 );
        buttonBot.click( BUTTON_REMOVE, 1 );
        buttonBot.click( BUTTON_FINISH );
        sleep( 1000 );

        // check files exist in the project
        treeBot.doubleClick( "portal.properties", projectHookName + "-hook", "docroot/WEB-INF/src" );
        EditorPageObject editorPage = new EditorPageObject( bot, "portal.properties" );
        assertTrue( editorPage.isActive() );
        TextEditorPageObject textEditorPage = new TextEditorPageObject( bot, "portal.properties" );
        assertContains( "application.startup.events=test_hook", textEditorPage.getText() );
        assertContains( "admin.default.group.names=1", textEditorPage.getText() );
        sleep( 500 );
        treeBot.doubleClick( "test.java", projectHookName + "-hook", "docroot/WEB-INF/src", "hook" );
        EditorPageObject editorPagejava = new EditorPageObject( bot, "test.java" );
        assertTrue( editorPagejava.isActive() );
        TextEditorPageObject textEditorPagejava = new TextEditorPageObject( bot, "test.java" );
        assertContains( "SimpleAction", textEditorPagejava.getText() );

    }

    @Test
    public void hookConfigurationLanguageProperties()
    {
        String defaultMessage = "Create new Language properties files.";
        String errorMessage = " Content folder not configured.";

        LanguagePropertiesPageObject<SWTWorkbenchBot> languagePropertiesPage =
            new LanguagePropertiesPageObject<SWTWorkbenchBot>( bot, "", INDEX_LANGUAGE_PROPERTIES_VALIDATION_MESSAGE );

        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_HOOK_CONFIGURATION );

        checkBoxBot.click( LABLE_LANGUAGE_PROPERTIES );
        newHookTypesPage.next();
        sleep( 1000 );

        assertEquals( defaultMessage, languagePropertiesPage.getValidationMessage() );
        assertEquals(
            "/hook-configuration-wizard-hook/docroot/WEB-INF/src/content",
            languagePropertiesPage.contentFolder.getText() );
        languagePropertiesPage.contentFolder.setText( "" );
        sleep( 500 );
        assertEquals( errorMessage, languagePropertiesPage.getValidationMessage() );
        buttonBot.click( BUTTON_BROWSE );
        sleep( 500 );
        TreeItemPageObject<SWTBot> treeItem =
            new TreeItemPageObject<SWTBot>( bot, "hook-configuration-wizard-hook", "docroot", "WEB-INF", "src" );
        treeItem.select();
        buttonBot.click( BUTTON_OK );
        sleep( 500 );

        // Language property files
        buttonBot.click( BUTTON_ADD );
        languagePropertiesPage.languagePropertyFile.setText( "test.properties" );
        buttonBot.click( BUTTON_OK );
        buttonBot.click( BUTTON_ADD );
        languagePropertiesPage.languagePropertyFile.setText( "test-hook" );
        buttonBot.click( BUTTON_OK );
        sleep( 500 );
        languagePropertiesPage.languagePropertyFiles.click( 1, 0 );
        buttonBot.click( BUTTON_EDIT );
        languagePropertiesPage.languagePropertyFile.setText( "hook" );
        buttonBot.click( BUTTON_OK );
        sleep( 500 );
        languagePropertiesPage.languagePropertyFiles.click( 1, 0 );
        buttonBot.click( BUTTON_REMOVE );
        buttonBot.click( BUTTON_FINISH );

        // check language properties file exist in the project
        treeBot.doubleClick( "test.properties", projectHookName + "-hook", "docroot/WEB-INF/src", "content" );

    }

    @Test
    public void hookConfigurationServices()
    {
        String defaultMessage = "Specify which Liferay services to extend.";
        String errorMessage = " Need to specify at least one Service to override.";

        ServicesPageObject<SWTWorkbenchBot> servicesPage =
            new ServicesPageObject<SWTWorkbenchBot>( bot, "", INDEX_SERVICES_MESSAGE );
        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_HOOK_CONFIGURATION );

        checkBoxBot.click( LABLE_SERVICES );
        newHookTypesPage.next();
        sleep( 1000 );

        assertEquals( defaultMessage, servicesPage.getValidationMessage() );
        buttonBot.click( BUTTON_ADD );
        sleep( 500 );
        buttonBot.click( BUTTON_NEW );
        servicesPage.addService.setFocus();
        buttonBot.click( BUTTON_OK );
        sleep( 500 );
        servicesPage.addServiceWrapper.setFocus();
        buttonBot.click( BUTTON_SELECT, 1 );

        servicesPage.addService.setFocus();

        buttonBot.click( BUTTON_OK );
        servicesPage.addServiceWrapper.setFocus();
        servicesPage.serviceType.setText( "test" );
        servicesPage.implClass.setText( "test" );
        servicesPage.addServiceWrapper.setFocus();
        buttonBot.click( BUTTON_OK );
        servicesPage.definePortalServices.click( 0, 1 );
        buttonBot.click( BUTTON_REMOVE );
        sleep( 500 );
        assertEquals( errorMessage, servicesPage.getValidationMessage() );

        buttonBot.click( BUTTON_ADD );
        buttonBot.click( BUTTON_SELECT, 0 );
        sleep( 500 );

        TextBot text = new TextBot( bot );
        text.setText( LABLE_CHOOSE_SUPERCLASS, "AccountService" );
        buttonBot.click( BUTTON_OK );
        servicesPage.addServiceWrapper.setFocus();
        buttonBot.click( BUTTON_NEW );
        servicesPage.javaPackage.setText( "hookservice" );
        buttonBot.click( BUTTON_CREATE );
        servicesPage.addServiceWrapper.setFocus();
        buttonBot.click( BUTTON_OK );
        servicesPage.definePortalServices.click( 0, 1 );
        buttonBot.click( BUTTON_EDIT );
        servicesPage.implClass.setText( "hookservice.ExtAccountService" );
        buttonBot.click( BUTTON_OK );
        buttonBot.click( BUTTON_FINISH );
        sleep( 1000 );

        // check file exist in the project
        treeBot.doubleClick( "ExtAccountService.java", projectHookName + "-hook", "docroot/WEB-INF/src",
            "hookservice" );
        EditorPageObject editorPagejava = new EditorPageObject( bot, "ExtAccountService.java" );
        assertTrue( editorPagejava.isActive() );
        TextEditorPageObject textEditorPagejava = new TextEditorPageObject( bot, "ExtAccountService.java" );
        assertContains( "AccountServiceWrapper", textEditorPagejava.getText() );

    }

    @Test
    public void hookConfigurationAllHookTypes()
    {
        CreateCustomJSPsPageObject<SWTWorkbenchBot> customJSPpage =
            new CreateCustomJSPsPageObject<SWTWorkbenchBot>( bot, "", INDEX_CUSTOM_JSPS_VALIDATION_MESSAGE );
        PortalPropertiesPageObject<SWTWorkbenchBot> portalPropertiesPage =
            new PortalPropertiesPageObject<SWTWorkbenchBot>( bot, "", INDEX_PORTAL_PROPERTIES_VALIDATION_MESSAGE );
        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_HOOK_CONFIGURATION );
        checkBoxBot.click( LABLE_CUSTOM_JSPS );
        checkBoxBot.click( LABLE_PORTAL_PROPERTIES );
        checkBoxBot.click( LABLE_SERVICES );
        checkBoxBot.click( LABLE_LANGUAGE_PROPERTIES );
        newHookTypesPage.next();
        sleep( 1000 );
        
        //Custom JSPs
        buttonBot.click( BUTTON_ADD );
        customJSPpage.jspFilePath.setText( "CustomJsps.jsp" );
        buttonBot.click( BUTTON_OK );
        buttonBot.click( BUTTON_NEXT );
        sleep( 500 );
        
        //Portal Properties
        buttonBot.click( BUTTON_ADD );
        portalPropertiesPage.event.setText( "portalProperties" );
        portalPropertiesPage.eventActionclass.setText( "portalPropertiesClass" );
        buttonBot.click( BUTTON_OK );
        buttonBot.click( BUTTON_NEXT );
        sleep( 500 );
        
        //Service
        ServicesPageObject<SWTWorkbenchBot> servicesPage =
                        new ServicesPageObject<SWTWorkbenchBot>( bot, "", INDEX_SERVICES_MESSAGE );
        buttonBot.click( BUTTON_ADD );
        servicesPage.serviceType.setText( "com.liferay.portal.service.AddressService" );
        servicesPage.implClass.setText( "com.liferay.portal.service.AddressServiceWrapper" );
        buttonBot.click( BUTTON_OK );
        buttonBot.click( BUTTON_NEXT );
        sleep( 500 );
        
        //Language Properties
        LanguagePropertiesPageObject<SWTWorkbenchBot> languagePropertiesPage =
            new LanguagePropertiesPageObject<SWTWorkbenchBot>( bot, "", INDEX_LANGUAGE_PROPERTIES_VALIDATION_MESSAGE );
        buttonBot.click( BUTTON_ADD );
        languagePropertiesPage.languagePropertyFile.setText( "languageTest.properties" );
        buttonBot.click( BUTTON_OK );
        buttonBot.click( BUTTON_FINISH );
        sleep( 1000 );
        
        // check files
        treeBot.doubleClick( "CustomJsps.jsp", projectHookName + "-hook", "docroot", "META-INF", "custom_jsps" );
        treeBot.doubleClick( "portal.properties", projectHookName + "-hook", "docroot/WEB-INF/src" );
        TextEditorPageObject textEditorPage = new TextEditorPageObject( bot, "portal.properties" );
        assertContains( "portalProperties=portalPropertiesClass", textEditorPage.getText() );
        treeBot.doubleClick( "languageTest.properties", projectHookName + "-hook", "docroot/WEB-INF/src", "content" );
        
    }

    private SetSDKLocationPageObject<SWTWorkbenchBot> getSetSDKLoactionPage()
    {
        SetSDKLocationPageObject<SWTWorkbenchBot> page = new SetSDKLocationPageObject<SWTWorkbenchBot>( bot, "" );
        page.setSdkLocation( getLiferayPluginsSdkDir().toString() );

        return page;
    }

    @Before
    public void openWizardCreateProject()
    {
        added = addedProjects();

        if( added )
        {
            return;
        }

        toolbarBot.menuClick( TOOLTIP_CREATE_LIFERAY_PROJECT, TOOLTIP_MENU_ITEM_NEW_LIFERAY_PROJECT );

        CreateProjectWizardPageObject<SWTWorkbenchBot> page1 =
            new CreateProjectWizardPageObject<SWTWorkbenchBot>( bot, "" );

        String projectHookName = "hook-configuration-wizard";

        page1.createSDKProject( projectHookName, MENU_HOOK );

        if( added )
        {
            page1.finish();
        }
        else
        {
            page1.next();

            SetSDKLocationPageObject<SWTWorkbenchBot> page2 = getSetSDKLoactionPage();

            page2.finish();
        }

        sleep( 10000 );
    }
}

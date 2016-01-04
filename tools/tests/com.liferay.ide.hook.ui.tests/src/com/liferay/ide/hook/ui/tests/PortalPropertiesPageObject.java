
package com.liferay.ide.hook.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.project.ui.tests.ProjectWizard;
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

    TextPageObject<SWTBot> portalPropertiesFile;
    TablePageObject<SWTBot> defineActionsOnPortalEvents;
    TablePageObject<SWTBot> specifyPropertiesToOverride;
    TextPageObject<SWTBot> className;
    TextPageObject<SWTBot> javaPackage;
    TextPageObject<SWTBot> eventActionclass;
    TextPageObject<SWTBot> value;
    TextPageObject<SWTBot> property;
    TextPageObject<SWTBot> event;

    DialogPageObject<SWTBot> addEventAction;
    DialogPageObject<SWTBot> addPropertyOverride;
    DialogPageObject<SWTBot> newLiferayHookConfiguration;

    public PortalPropertiesPageObject( T bot, String title, int indexPortalPropertiesValidationMessage )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT, 
            indexPortalPropertiesValidationMessage );
        portalPropertiesFile = new TextPageObject<SWTBot>( bot, LABLE_PORTAL_PROPERTIES_FILE );
        defineActionsOnPortalEvents = new TablePageObject<SWTBot>( bot, LABLE_DEFINE_ACTIONS );
        specifyPropertiesToOverride = new TablePageObject<SWTBot>( bot, LABLE_SPECIFY_PROPERTIES );
        className = new TextPageObject<SWTBot>( bot, LABLE_CLASS_NAME );
        javaPackage = new TextPageObject<SWTBot>( bot, LABLE_JAVA_PACKAGE );
        eventActionclass = new TextPageObject<SWTBot>( bot, LABLE_CLASS );
        value = new TextPageObject<SWTBot>( bot, LABLE_VALUE );
        property = new TextPageObject<SWTBot>( bot, LABLE_PROPERTY );
        event = new TextPageObject<SWTBot>( bot, LABLE_EVENT );

        addEventAction = new DialogPageObject<SWTBot>( bot, WINDOW_ADD_EVENT_ACTION, BUTTON_CANCEL, BUTTON_OK );
        addPropertyOverride =
            new DialogPageObject<SWTBot>( bot, WINDOW_ADD_PROPERTY_OVERRIDE, BUTTON_CANCEL, BUTTON_OK );
        newLiferayHookConfiguration =
            new DialogPageObject<SWTBot>( bot, WINDOW_NEW_LIFERAY_HOOK_CONFIGURATION, BUTTON_BACK, BUTTON_NEXT );

    }
}

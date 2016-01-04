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
public class ServicesPageObject<T extends SWTBot>extends WizardPageObject<T> 
implements HookConfigurationWizard, ProjectWizard
{
    TextPageObject<SWTBot> serviceType;
    TextPageObject<SWTBot> implClass;
    TextPageObject<SWTBot> className;
    TextPageObject<SWTBot> javaPackage;
    TablePageObject<SWTBot> definePortalServices;
    
    DialogPageObject<SWTBot> addService;
    DialogPageObject<SWTBot> addServiceWrapper;
    
    public ServicesPageObject( T bot, String title, int indexPortalPropertiesValidationMessage )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT, 
            indexPortalPropertiesValidationMessage );
        serviceType = new TextPageObject<SWTBot>( bot, LABLE_SERVICE_TYPE ); 
        implClass = new TextPageObject<SWTBot>( bot, LABLE_IMPL_CLASS );
        className = new TextPageObject<SWTBot>( bot, LABLE_CLASS_NAME );
        javaPackage = new TextPageObject<SWTBot>( bot, LABLE_JAVA_PACKAGE );
        addService = new DialogPageObject<SWTBot>( bot, WINDOW_ADD_SERVICE, BUTTON_CANCEL, BUTTON_OK );
        addServiceWrapper = new DialogPageObject<SWTBot>( bot, WINDOW_ADD_SERVICE_WRAPPER, BUTTON_CANCEL, BUTTON_OK );
        definePortalServices = new TablePageObject<SWTBot>( bot, LABLE_DEFINE_PORTAL_SERVICES );
        
    }
}

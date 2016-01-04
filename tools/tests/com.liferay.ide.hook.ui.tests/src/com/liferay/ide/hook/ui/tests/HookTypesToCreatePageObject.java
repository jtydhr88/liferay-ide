package com.liferay.ide.hook.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.project.ui.tests.ProjectWizard;
import com.liferay.ide.ui.tests.swtbot.page.CheckBoxPageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

/**
 * @author Vicky Wang
 */
public class HookTypesToCreatePageObject<T extends SWTBot>extends WizardPageObject<T> 
    implements HookConfigurationWizard, ProjectWizard
{
    CheckBoxPageObject<SWTBot> customJSPs;
    CheckBoxPageObject<SWTBot> portalProperties;
    CheckBoxPageObject<SWTBot> services;
    CheckBoxPageObject<SWTBot> languageProperties;
    
    public HookTypesToCreatePageObject( T bot, String title)
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT );
        customJSPs = new CheckBoxPageObject<SWTBot>( bot, LABLE_CUSTOM_JSPS );
        portalProperties = new CheckBoxPageObject<SWTBot>( bot, LABLE_PORTAL_PROPERTIES );
        services = new CheckBoxPageObject<SWTBot>( bot, LABLE_SERVICES );
        languageProperties = new CheckBoxPageObject<SWTBot>( bot, LABLE_LANGUAGE_PROPERTIES );
        
    }

}

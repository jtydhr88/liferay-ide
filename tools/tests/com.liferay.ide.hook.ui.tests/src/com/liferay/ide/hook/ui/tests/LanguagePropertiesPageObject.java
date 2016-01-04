package com.liferay.ide.hook.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.project.ui.tests.ProjectWizard;
import com.liferay.ide.ui.tests.swtbot.page.TablePageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

/**
 * @author Vicky Wang
 */
public class LanguagePropertiesPageObject<T extends SWTBot>extends WizardPageObject<T> 
implements HookConfigurationWizard, ProjectWizard
{
    TextPageObject<SWTBot> contentFolder;
    TablePageObject<SWTBot> languagePropertyFiles;
    TextPageObject<SWTBot> languagePropertyFile;
    
    public LanguagePropertiesPageObject( T bot, String title, int indexLanguagePropertiesValidationMessage )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, 
            BUTTON_NEXT, indexLanguagePropertiesValidationMessage );
        contentFolder = new TextPageObject<SWTBot>( bot, LABLE_CONTENT_FOLDER ); 
        languagePropertyFiles = new TablePageObject<SWTBot>( bot, LABLE_LANGUAGE_PROPERTY_FILES );
        languagePropertyFile = new TextPageObject<SWTBot>( bot, LABLE_LANGUAGE_PROPERTY_FILE );
    }
}

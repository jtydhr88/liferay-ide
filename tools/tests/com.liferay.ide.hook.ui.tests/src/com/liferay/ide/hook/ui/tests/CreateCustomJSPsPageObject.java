package com.liferay.ide.hook.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.project.ui.tests.ProjectWizard;
import com.liferay.ide.ui.tests.swtbot.page.CheckBoxPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TablePageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

/**
 * @author Vicky Wang
 */
public class CreateCustomJSPsPageObject<T extends SWTBot>extends WizardPageObject<T> 
    implements HookConfigurationWizard, ProjectWizard
{
    TextPageObject<SWTBot> selectedProject;
    TextPageObject<SWTBot> webRootFolder;
    TextPageObject<SWTBot> customJSPfolder;
    TextPageObject<SWTBot> jspFilePath;
    
    TablePageObject<SWTBot> jspFilesToOverride;
    CheckBoxPageObject<SWTBot> disableJSPsyntaxValidation;

    public CreateCustomJSPsPageObject( T bot, String title, int indexCustomJSPsValidationMessage )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BACK, BUTTON_NEXT, indexCustomJSPsValidationMessage );
        selectedProject = new TextPageObject<SWTBot>( bot, LABLE_SELECTED_PROJECT );
        webRootFolder = new TextPageObject<SWTBot>( bot, LABLE_WEB_ROOT_FOLDER );
        customJSPfolder = new TextPageObject<SWTBot>( bot, LABLE_CUSTOM_JSP_FOLDER );
        jspFilesToOverride = new TablePageObject<SWTBot>( bot, LABLE_JSP_FILES_TO_OVERRIDE );
        jspFilePath = new TextPageObject<SWTBot>( bot, LABLE_JSP_FILE_PATH );
        disableJSPsyntaxValidation = new CheckBoxPageObject<SWTBot>( bot, LABLE_DISABLE_JSP_SYNTAX_VALIDATION );
        
    }

}


package com.liferay.ide.project.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;

import com.liferay.ide.ui.tests.swtbot.page.CheckBoxPageObject;
import com.liferay.ide.ui.tests.swtbot.page.ComboBoxPageObject;
import com.liferay.ide.ui.tests.swtbot.page.DialogPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;

/**
 * @author Ying Xu
 */
public class NewLiferayServiceBuilderWizardPageObject<T extends SWTBot> extends DialogPageObject<T>
    implements ServiceBuilderWizard
{

    private int validationMessageIndex = -1;
    TextPageObject<SWTBot> packagePathText;
    TextPageObject<SWTBot> namespaceText;
    TextPageObject<SWTBot> authorText;
    CheckBoxPageObject<SWTBot> includeSampleEntityCheckBox;
    ComboBoxPageObject<SWTBot> pluginProjectComboBox;

    public NewLiferayServiceBuilderWizardPageObject(
        T bot, String title, String cancelButtonText, String confirmButtonText )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH );
        packagePathText = new TextPageObject<>( bot, LABEL_PROJECT_NAME );
        namespaceText = new TextPageObject<>( bot, LABEL_NAMESPACE );
        authorText = new TextPageObject<>( bot, LABEL_AUTHOR );
        includeSampleEntityCheckBox = new CheckBoxPageObject<>( bot, LABEL_INCLUDE_SAMPLE_ENTITY );
        pluginProjectComboBox = new ComboBoxPageObject<>( bot, LABEL_PLUGIN_PROJECT );
    }

    public NewLiferayServiceBuilderWizardPageObject(
        T bot, String title, String cancelButtonText, String confirmButtonText, int validationMessageIndex )
    {
        super( bot, title, BUTTON_CANCEL, BUTTON_FINISH );
        packagePathText = new TextPageObject<>( bot, LABEL_PROJECT_NAME );
        namespaceText = new TextPageObject<>( bot, LABEL_NAMESPACE );
        authorText = new TextPageObject<>( bot, LABEL_AUTHOR );
        includeSampleEntityCheckBox = new CheckBoxPageObject<>( bot, LABEL_INCLUDE_SAMPLE_ENTITY );
        pluginProjectComboBox = new ComboBoxPageObject<>( bot, LABEL_PLUGIN_PROJECT );
        this.validationMessageIndex = validationMessageIndex;
    }

    public void NewServiceBuilder( String packagePath, String namespace )
    {
        NewServiceBuilder( packagePath, namespace ,true);
    }

    public void NewServiceBuilder( String packagePath, String namespace, String pluginProject )
    {
        NewServiceBuilder( packagePath, namespace, pluginProject );
    }

    public void NewServiceBuilder( String packagePath, String namespace, String pluginProject, String author )
    {
        NewServiceBuilder( packagePath, namespace, pluginProject, author );
    }

    public void NewServiceBuilder(
        String packagePath, String namespace, String pluginProject, String author, boolean includeSampleEntity )
    {
        NewServiceBuilder( packagePath, namespace, author, pluginProject, true );
    }

    public void NewServiceBuilder( String packagePath, String namespace, boolean includeSampleEntity )
    {
        packagePathText.setText( packagePath );
        namespaceText.setText( namespace );

        if( includeSampleEntity )
        {
            includeSampleEntityCheckBox.select();
        }
        else
        {
            includeSampleEntityCheckBox.deselect();
        }
    }

    public String getValidationMessage()
    {
        if( validationMessageIndex < 0 )
        {
            log.error( "Validation Message Index error" );

            return null;
        }

        return bot.text( validationMessageIndex ).getText();
    }
}

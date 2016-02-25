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

package com.liferay.ide.service.ui.tests.page;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

import com.liferay.ide.service.ui.tests.ServiceBuilderWizard;
import com.liferay.ide.ui.tests.swtbot.page.CheckBoxPageObject;
import com.liferay.ide.ui.tests.swtbot.page.ComboBoxPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

/**
 * @author Ying Xu
 */
public class ServiceBuilderWizardPageObject<T extends SWTBot> extends WizardPageObject<T>
    implements ServiceBuilderWizard
{

    TextPageObject<SWTBot> author;
    TextPageObject<SWTBot> serviceFile;
    TextPageObject<SWTBot> namespace;
    TextPageObject<SWTBot> packagePath;

    ComboBoxPageObject<SWTBot> pluginProjectComboBox;
    CheckBoxPageObject<SWTBot> includeSampleEntityCheckBox;

    private final String browseButtonText;
    private int validationMessageIndex = -1;

    public ServiceBuilderWizardPageObject( T bot )
    {
        this( bot, TEXT_BLANK, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BROWSE, INDEX_VALIDATION_MESSAGE );
    }

    public ServiceBuilderWizardPageObject( T bot, String title )
    {
        this( bot, title, BUTTON_CANCEL, BUTTON_FINISH, BUTTON_BROWSE, INDEX_VALIDATION_MESSAGE );
    }

    public ServiceBuilderWizardPageObject(
        T bot, String title, String cancelButtonText, String finishButtonText, String browseButtonText )
    {
        super( bot, title, cancelButtonText, finishButtonText, browseButtonText, TEXT_BLANK );
        this.browseButtonText = browseButtonText;
        packagePath = new TextPageObject<>( bot, LABEL_PACKAGE_PATH );
        namespace = new TextPageObject<>( bot, LABEL_NAMESPACE );
        author = new TextPageObject<>( bot, LABEL_AUTHOR );
        serviceFile = new TextPageObject<>( bot, LABEL_SERVICE_FILE );
        includeSampleEntityCheckBox = new CheckBoxPageObject<>( bot, CHECKBOX_INCLUDE_SAMPLE_ENTITY );
        pluginProjectComboBox = new ComboBoxPageObject<>( bot, COMBOBOX_PLUGIN_PROJECT );

    }

    public ServiceBuilderWizardPageObject(
        T bot, String title, String cancelButtonText, String finishButtonText, String browseButtonText,
        int validationMessageIndex )
    {
        this( bot, title, cancelButtonText, finishButtonText, browseButtonText );

        this.validationMessageIndex = validationMessageIndex;
    }

    public void browse()
    {
        clickClosingButton( browseButton() );
    }

    protected SWTBotButton browseButton()
    {
        return bot.button( browseButtonText );
    }

    public TextPageObject<SWTBot> getAuthorText()
    {
        return author;
    }

    public CheckBoxPageObject<SWTBot> getIncludeSampleEntityCheckBox()
    {
        return includeSampleEntityCheckBox;
    }

    public TextPageObject<SWTBot> getNamespaceText()
    {
        return namespace;
    }

    public TextPageObject<SWTBot> getPackagePathText()
    {
        return packagePath;
    }

    public ComboBoxPageObject<SWTBot> getPluginProjectComboBox()
    {
        return pluginProjectComboBox;
    }

    public TextPageObject<SWTBot> getServiceFileText()
    {
        return serviceFile;
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

    public void NewServiceBuilder( String packagePathText, String namespaceText )
    {
        NewServiceBuilder( packagePathText, namespaceText, true );
    }

    public void NewServiceBuilder( String packagePathText, String namespaceText, boolean includeSampleEntity )
    {
        packagePath.setText( packagePathText );
        namespace.setText( namespaceText );

        if( includeSampleEntity )
        {
            includeSampleEntityCheckBox.select();
        }
        else
        {
            includeSampleEntityCheckBox.deselect();
        }
    }

}

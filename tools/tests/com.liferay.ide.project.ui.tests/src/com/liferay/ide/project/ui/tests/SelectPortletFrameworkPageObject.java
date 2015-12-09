
package com.liferay.ide.project.ui.tests;

import com.liferay.ide.ui.tests.swtbot.page.LabelPageObject;
import com.liferay.ide.ui.tests.swtbot.page.RadioPageObject;
import com.liferay.ide.ui.tests.swtbot.page.TextPageObject;
import com.liferay.ide.ui.tests.swtbot.page.WizardPageObject;

import org.eclipse.swtbot.swt.finder.SWTBot;

public class SelectPortletFrameworkPageObject<T extends SWTBot> extends WizardPageObject<T> implements ProjectWizard
{

    RadioPageObject<SWTBot> portletFrameworkRadio;
    RadioPageObject<SWTBot> liferayMVCRadio;
    RadioPageObject<SWTBot> jsfRadio;
    RadioPageObject<SWTBot> springMVCRadio;
    RadioPageObject<SWTBot> vaadinRadio;
    LabelPageObject portletNamelabel;
    LabelPageObject displayNamelabel;
    TextPageObject<SWTBot> portletNameText;
    TextPageObject<SWTBot> displayNameText;

    public SelectPortletFrameworkPageObject( T bot, String title )
    {
        super( bot, title, BUTTON_BACK, BUTTON_NEXT, BUTTON_FINISH, BUTTON_CANCEL );

        liferayMVCRadio = new RadioPageObject<SWTBot>( bot, TEXT_LIFERAY_MVC_FRAMEWORK );
        jsfRadio = new RadioPageObject<SWTBot>( bot, TEXT_JSF_FRAMEWORK );
        springMVCRadio = new RadioPageObject<SWTBot>( bot, TEXT_SPRING_MVC_FRAMEWORK );
        vaadinRadio = new RadioPageObject<SWTBot>( bot, TEXT_VAADIN_FRAMEWORK );
        portletNamelabel = new LabelPageObject( bot, LABEL_PORTLET_NAME );
        displayNamelabel = new LabelPageObject( bot, LABEL_DISPLAY_NAME );
        portletNameText = new TextPageObject<SWTBot>( bot, LABEL_PORTLET_NAME );
        displayNameText = new TextPageObject<SWTBot>( bot, LABEL_DISPLAY_NAME );
    }

    public SelectPortletFrameworkPageObject( T bot, String title, String radio )
    {
        this( bot, title );

        selectFramework( radio );
    }

    public void selectFramework( String radio )
    {
        if( radio.equals( liferayMVCRadio.getLabel() ) )
        {
            liferayMVCRadio.click();
        }
        else if( radio.equals( jsfRadio.getLabel() ) )
        {
            jsfRadio.click();
        }
        else if( radio.equals( springMVCRadio.getLabel() ) )
        {
            springMVCRadio.click();
        }
        else if( radio.equals( vaadinRadio.getLabel() ) )
        {
            vaadinRadio.click();
        }
    }

}

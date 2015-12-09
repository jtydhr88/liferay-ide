
package com.liferay.ide.ui.tests.swtbot.page;

import com.liferay.ide.ui.tests.swtbot.condition.WidgetEnabledCondition;

import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

public class RadioPageObject<T extends SWTBot> extends AbstractWidgetPageObject<SWTBot>
{

    public RadioPageObject( SWTBot bot, String label )
    {
        super( bot, label );
    }

    public void click()
    {
        AbstractSWTBot<? extends Widget> widget = getWidget();

        if( widget instanceof SWTBotRadio )
        {
            SWTBotRadio radio = (SWTBotRadio) widget;
            bot.waitUntil( new WidgetEnabledCondition( radio, true ) );
            radio.click();
        }
    }

    @Override
    protected AbstractSWTBot<?> getWidget()
    {
        return bot.radio( label );
    }

    public boolean isSelected()
    {
        AbstractSWTBot<? extends Widget> widget = getWidget();

        SWTBotRadio radio = (SWTBotRadio) widget;

        return radio.isSelected();
    }

}

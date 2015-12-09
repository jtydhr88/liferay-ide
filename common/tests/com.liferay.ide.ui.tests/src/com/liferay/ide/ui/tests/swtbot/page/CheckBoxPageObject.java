
package com.liferay.ide.ui.tests.swtbot.page;

import com.liferay.ide.ui.tests.swtbot.condition.WidgetEnabledCondition;

import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

public class CheckBoxPageObject<T extends SWTBot> extends AbstractWidgetPageObject<SWTBot>
{

    public CheckBoxPageObject( SWTBot bot, String label )
    {
        super( bot, label );
    }

    public void deselect()
    {
        AbstractSWTBot<? extends Widget> widget = getWidget();

        if( widget instanceof SWTBotCheckBox )
        {
            SWTBotCheckBox checkBox = (SWTBotCheckBox) widget;
            bot.waitUntil( new WidgetEnabledCondition( checkBox, true ) );
            checkBox.deselect();;
        }
    }
    
    @Override
    protected AbstractSWTBot<?> getWidget()
    {
        return bot.checkBox( label );
    }

    public boolean isChecked()
    {
        AbstractSWTBot<? extends Widget> widget = getWidget();

        SWTBotCheckBox checkBox = (SWTBotCheckBox) widget;

        return checkBox.isChecked();
    }

    public void select()
    {
        AbstractSWTBot<? extends Widget> widget = getWidget();

        if( widget instanceof SWTBotCheckBox )
        {
            SWTBotCheckBox checkBox = (SWTBotCheckBox) widget;
            bot.waitUntil( new WidgetEnabledCondition( checkBox, true ) );
            checkBox.select();
        }
    }

}

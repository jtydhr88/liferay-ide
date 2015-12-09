
package com.liferay.ide.ui.tests.swtbot.page;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

public class LabelPageObject extends AbstractWidgetPageObject<SWTBot>
{

    public LabelPageObject( SWTBot bot, String label )
    {
        super( bot, label );
    }

    @Override
    protected AbstractSWTBot<?> getWidget()
    {
        return bot.label( label );
    }

    public boolean isVisible( String group, int index )
    {
        return bot.labelInGroup( group, index ).isVisible();
    }

}

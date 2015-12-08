package com.liferay.ide.ui.tests.swtbot.page;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;


public interface ITreePageObject
{
    public void select( String... node );
    public void expand();
    public void expandAll(SWTBotTreeItem... node);
    public void collapse();
    public void doAction(String... action);
    public void doubleClick();
}

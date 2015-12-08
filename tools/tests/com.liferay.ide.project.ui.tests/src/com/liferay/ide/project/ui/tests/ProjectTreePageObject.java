
package com.liferay.ide.project.ui.tests;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.liferay.ide.ui.tests.swtbot.page.impl.TreePageObject;

public class ProjectTreePageObject<T extends SWTBot> extends TreePageObject<T> implements ProjectBuildAction
{

    public ProjectTreePageObject( T bot )
    {
        super( bot );
    }

    public void deleteAllProject()
    {
        SWTBotTreeItem[] items = bot.tree().getAllItems();

        for( SWTBotTreeItem item : items )
        {
            if( !item.getText().equals( "sdk" ) )
            {
                select( item.getText() );
                deleteProject();

            }
        }
    }

    public void deleteProject()
    {
        doAction( BUTTON_DELETE );

        DeleteProjectDialogPageObject<SWTBot> deleteDialog = new DeleteProjectDialogPageObject<SWTBot>( bot );

        deleteDialog.confirmDeleteFromDisk();
    }
}

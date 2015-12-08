
package com.liferay.ide.ui.tests.swtbot.page.impl;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.TableCollection;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.liferay.ide.ui.tests.swtbot.page.ITreePageObject;

public class TreePageObject<T extends SWTBot> extends AbstractPageObject<SWTBot> implements ITreePageObject
{

    protected SWTBotTreeItem selectedNode;

    protected TableCollection selection;

    public TreePageObject( SWTBot bot )
    {
        super( bot );
    }

    @Override
    public void collapse()
    {
        selectedNode.collapse();
    }

    @Override
    public void doAction( String... action )
    {
        SWTBotMenu goalMenu = selectedNode.contextMenu( action[0] ).click();

        for( int i = 1; i < action.length; i++ )

            goalMenu = goalMenu.menu( action[i] ).click();
    }

    @Override
    public void doubleClick()
    {
        selectedNode.doubleClick();
    }

    @Override
    public void expand()
    {
        selectedNode.expand();
    }

    @Override
    public void expandAll( SWTBotTreeItem... node )
    {
        selectedNode.expand();

        SWTBotTreeItem[] subItems = selectedNode.getItems();

        if( subItems != null )
        {

            for( SWTBotTreeItem subsubItem : subItems )
            {
                if( subsubItem.getText().contains( "JRE" ) )
                    continue;
                selectedNode = subsubItem.expand();

                expandAll( subsubItem );
            }
        }
    }

    public SWTBotTreeItem getSelectedNode()
    {
        return selectedNode;
    }

    public String[] getSelection()
    {
        selection = bot.tree().selection();

        String[] elements = new String[selection.rowCount()];

        for( int i = 0; i < selection.rowCount(); i++ )
        {
            elements[i] = selection.get( i, 0 );
        }

        return elements;
    }

    public boolean isSelected( String nodeName )
    {
        for( String node : getSelection() )
        {
            if( node.equals( nodeName ) )
                return true;

        }
        return false;
    }

    @Override
    public void select( String... nodes )
    {
        if( nodes == null )
            return;

        selectedNode = bot.tree().getTreeItem( nodes[0] );

        for( int i = 1; i < nodes.length; i++ )
        {
            selectedNode.expand();
            selectedNode = selectedNode.getNode( nodes[i] );
        }
        selectedNode.select();
        bot.sleep( 1000 );
    }

/*    @Override
    public void selectMulty( String... nodes )
    {
        bot.tree().pressShortcut( Keystrokes.CTRL );
        select( nodes );
    }*/
}

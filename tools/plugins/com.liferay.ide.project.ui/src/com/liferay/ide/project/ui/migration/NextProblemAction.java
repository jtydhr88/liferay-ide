package com.liferay.ide.project.ui.migration;

import com.liferay.ide.project.ui.ProjectUI;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.navigator.CommonViewer;

public class NextProblemAction extends SelectionProviderAction implements IAction
{
    private IStructuredSelection _selection;
    private final CommonViewer _viewer;
    private final MigrationViewTreeUtil _treeUtil;

    public NextProblemAction( CommonViewer viewer, MigrationViewTreeUtil treeUtil )
    {
        super( viewer, "Next Problem" );

        setImageDescriptor( ProjectUI.imageDescriptorFromPlugin( ProjectUI.PLUGIN_ID, "icons/e16/next.gif" ) );
        setDisabledImageDescriptor( ProjectUI.imageDescriptorFromPlugin(
            ProjectUI.PLUGIN_ID, "icons/e16/next_dis.gif" ) );
        setToolTipText( "Next" );
        setEnabled( true );

        _viewer = viewer;
        _treeUtil = treeUtil;
    }

    public void selectionChanged( IStructuredSelection selection )
    {
        final Object element = selection.getFirstElement();

        if( ( element instanceof IFile || element instanceof MPTree ) )
        {
            setEnabled( true );

            _selection = selection;
        }
        else
        {
            setEnabled( false );

            _selection = null;
        }
    }

    @Override
    public void run()
    {
        _viewer.expandAll();

        if( _selection != null )
        {
            final Object element = _selection.getFirstElement();

            if( element instanceof IFile )
            {
                final IFile file = (IFile) element;

                StructuredSelection structuredSelection =
                    new StructuredSelection( _treeUtil.getNextResource( file ) );

                _viewer.setSelection( structuredSelection, true );
            }
            else if( element instanceof MPTree )
            {

                StructuredSelection structuredSelection = new StructuredSelection( _treeUtil.getFirsttResource() );

                _viewer.setSelection( structuredSelection, true );
            }
        }
        else
        {
            StructuredSelection structuredSelection = new StructuredSelection( _treeUtil.getFirsttResource() );

            _viewer.setSelection( structuredSelection, true );
        }
    }
}
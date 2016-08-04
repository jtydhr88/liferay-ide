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

package com.liferay.ide.project.ui.upgrade.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.sapphire.ui.Presentation;

import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.IWebProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.project.ui.dialog.LiferayLayouttplProjectSelectionDialog;
import com.liferay.ide.ui.util.UIUtil;

/**
 * @author Joye Luo
 */
public class LayouttplUpgradeActionHandler extends BaseActionHandler
{

    @Override
    protected Object run( Presentation context )
    {
        LiferayLayouttplProjectSelectionDialog dialog =
            new LiferayLayouttplProjectSelectionDialog( UIUtil.getActiveShell() );

        if( dialog.open() == Window.OK )
        {
            final Object[] selectedProjects = dialog.getResult();

            if( selectedProjects != null )
            {
                for( Object project : selectedProjects )
                {
                    if( project instanceof IJavaProject )
                    {
                        IJavaProject p = (IJavaProject) project;

                        try
                        {
                            upgradeLayouttpl( p.getProject() );
                        }
                        catch( Exception e )
                        {
                            ProjectUI.logError( e );
                        }
                    }
                }
            }
        }

        return null;
    }

    private void upgradeLayouttpl( IProject project ) throws Exception
    {
        ILiferayProject liferayProject = LiferayCore.create( project );

        if( liferayProject != null && liferayProject instanceof IWebProject )
        {
            IWebProject webProject = (IWebProject) liferayProject;
            IFolder layouttplFolder = webProject.getDefaultDocrootFolder();

            for( IResource resource : layouttplFolder.members() )
            {
                if( resource instanceof IFile && resource.getName().endsWith( ".tpl" ) )
                {
                    IFile layoutFile = (IFile) resource;

                    String content = FileUtil.readContents( layoutFile.getContents() );

                    if( content != null && !content.equals( "" ) )
                    {
                        if( content.contains( "row-fluid" ) )
                        {
                            content = content.replaceAll( "row-fluid", "row" );
                        }

                        if( content.contains( "span" ) )
                        {
                            content = content.replaceAll( "span", "col-md-" );
                        }

                        FileUtil.writeFile( layoutFile.getLocation().toFile(), content, project.getName() );
                    }
                }
            }
        }
    }

}

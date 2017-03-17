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

package com.liferay.ide.gradle.action;

import com.liferay.ide.core.util.CoreUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Lovett Li
 * @author Terry Jia
 */
public class BuildServiceTaskAction extends GradleTaskAction
{

    @Override
    protected String getGradleTask()
    {
        return "buildService";
    }

    protected void afterTask()
    {
        List<IFolder> folders = CoreUtil.getSourceFolders( JavaCore.create( _project ) );

        if( folders.size() == 0 )
        {
            Set<IProject> projects = new HashSet<IProject>();
            projects.add( _project );

            CorePlugin.gradleWorkspaceManager().getCompositeBuild( projects ).synchronize(
                NewProjectHandler.IMPORT_AND_MERGE );
        }
    }

}

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
package com.liferay.ide.project.ui.migration;

import com.liferay.ide.core.util.CoreUtil;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;


/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
public class MigrationDecorator extends BaseLabelProvider implements ILightweightLabelDecorator
{

    @Override
    public void decorate( Object element, IDecoration decoration )
    {
        if( element instanceof MPNode )
        {
            final MPNode node = (MPNode) element;

            final IResource member = CoreUtil.getWorkspaceRoot().findMember( node.incrementalPath );

            if( member != null && member.exists() )
            {
                element = member;
            }
        }

        final List<TaskProblem> problems = new ArrayList<>();

        if( element instanceof IResource )
        {
            final IResource resource = (IResource) element;

            problems.addAll( MigrationUtil.getTaskProblemsFromResource( resource ) );
        }
        else if( element instanceof MPTree )
        {
            problems.addAll( MigrationUtil.getAllTaskProblems() );
        }

        if( problems != null && problems.size() > 0 )
        {
            final String suffix = String.format(
                " [ %d %s problem%s]",
                problems.size(),
                ( element instanceof MPTree ? "total" : ""),
                ( problems.size() > 1 ? "s" : "") );
            decoration.addSuffix( suffix );
        }
    }

}

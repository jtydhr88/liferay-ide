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

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.wst.server.ui.ServerUIUtil;

import com.liferay.ide.project.core.upgrade.CodeUpgradeOp;
import com.liferay.ide.ui.util.UIUtil;

/**
 * @author Joye Luo
 */
public class CodeUpgradeNewLiferayRuntimeAction extends SapphireActionHandler
{

    @Override
    protected Object run( Presentation context )
    {
        ServerUIUtil.showNewServerWizard( UIUtil.getActiveShell(), "liferay.bundle", null, "com.liferay." );

        CodeUpgradeOp op = context.part().getModelElement().nearest( CodeUpgradeOp.class );

        op.property( CodeUpgradeOp.PROP_LIFERAY_SERVER_NAME ).refresh();

        return null;
    }

}


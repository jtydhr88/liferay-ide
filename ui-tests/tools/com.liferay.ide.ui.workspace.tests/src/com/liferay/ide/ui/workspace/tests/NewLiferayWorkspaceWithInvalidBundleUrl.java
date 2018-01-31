/**
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
 */

package com.liferay.ide.ui.workspace.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.support.project.ProjectSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Yongqiang Wang
 * @author Simon Jiang
 */

public class NewLiferayWorkspaceWithInvalidBundleUrl extends SwtbotBase
{
	
    @Test
    public void createLiferayWorkspaceWithDownloadBundleInvalidBundleUrl() {
        String workspaceName = project.getName("test-liferay-workspace-gradle");

        wizardAction.openNewLiferayWorkspaceWizard();

        wizardAction.newLiferayWorkspace.prepareGradle(workspaceName);

        wizardAction.newLiferayWorkspace.selectDownloadLiferayBundle();

        Assert.assertNull(wizardAction.getValidationMsg());

        String bundleHttpsErrorUrl = "https://";

        wizardAction.newLiferayWorkspace.setBundleUrl(bundleHttpsErrorUrl);
        
        Assert.assertNull(wizardAction.getValidationMsg());
        
        String bundleHttpErrorUrl = "http://";

        wizardAction.newLiferayWorkspace.setBundleUrl(bundleHttpErrorUrl);

        Assert.assertNull(wizardAction.getValidationMsg());

        String bundleFtpErrorUrl = "ftp://";

        wizardAction.newLiferayWorkspace.setBundleUrl(bundleFtpErrorUrl);

        Assert.assertNull(wizardAction.getValidationMsg());

        wizardAction.cancel();
    }

	@Rule
	public ProjectSupport project = new ProjectSupport(bot);

}

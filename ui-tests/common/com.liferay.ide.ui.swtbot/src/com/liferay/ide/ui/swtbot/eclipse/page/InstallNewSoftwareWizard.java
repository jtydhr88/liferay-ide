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

package com.liferay.ide.ui.swtbot.eclipse.page;

import com.liferay.ide.ui.swtbot.page.Button;
import com.liferay.ide.ui.swtbot.page.CheckBox;
import com.liferay.ide.ui.swtbot.page.ComboBox;
import com.liferay.ide.ui.swtbot.page.Radio;
import com.liferay.ide.ui.swtbot.page.Wizard;

import org.eclipse.swtbot.swt.finder.SWTBot;

/**
 * @author Lily Li
 */
public class InstallNewSoftwareWizard extends Wizard {

	public InstallNewSoftwareWizard(SWTBot bot) {
		super(bot);
	}

	public Radio acceptTermsOfLicenseAgreement() {
		return new Radio(getShell().bot(), I_ACCEPT_THE_TERMS_OF_THE_LICENSE_AGREEMENTS);
	}

	public CheckBox contactAllUpdateSites() {
		return new CheckBox(
			getShell().bot(), CONTACT_ALL_UPDATE_SITES_DURING_SITES_DURING_INSTALL_TO_FIND_REQUIRED_SOFTWARE);
	}

	public ComboBox getSiteUrl() {
		return new ComboBox(getShell().bot(), WORK_WITH);
	}

	public Button selectAllBtn() {
		return new Button(getShell().bot(), SELECT_ALL);
	}

}
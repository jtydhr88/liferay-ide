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

package com.liferay.ide.upgrade.plan.ui.internal;

import com.liferay.ide.upgrade.plan.core.NewUpgradePlanOp;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizard;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 * @author Simon Jiang
 */
public class NewUpgradePlanWizard extends SapphireWizard<NewUpgradePlanOp> {

	public NewUpgradePlanWizard() {
		super(NewUpgradePlanOp.TYPE, DefinitionLoader.sdef(NewUpgradePlanWizard.class).wizard());
	}

	@Override
	public IWizardPage[] getPages() {
		final IWizardPage[] wizardPages = super.getPages();

		if (!_firstErrorMessageRemoved && (wizardPages != null)) {
			SapphireWizardPage wizardPage = (SapphireWizardPage)wizardPages[0];

			wizardPage.setMessage(
				"Create and start a new Upgrade Plan. Any existing plan will be replaced.", SapphireWizardPage.NONE);

			_firstErrorMessageRemoved = true;
		}

		return wizardPages;
	}

	@Override
	protected void performPostFinish() {
		super.performPostFinish();

		IConfigurationElement element = new DelegateConfigurationElement(null) {

			@Override
			public String getAttribute(String name) {
				if ("finalPerspective".equals(name)) {
					return UpgradePlannerPerspectiveFactory.ID;
				}

				return super.getAttribute(name);
			}

		};

		BasicNewProjectResourceWizard.updatePerspective(element);
	}

	private boolean _firstErrorMessageRemoved = false;

}
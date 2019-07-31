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

package com.liferay.ide.project.core.samples.internal;

import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.modules.BladeCLIException;
import com.liferay.ide.project.core.samples.NewSampleOp;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;

/**
 * @author Terry Jia
 */
public class CategoryPossibleValuesService extends PossibleValuesService implements SapphireContentAccessor {

	@Override
	public void dispose() {
		if (_op() != null) {
			SapphireUtil.detachListener(_op().property(NewSampleOp.PROP_BUILD_TYPE), _listener);
		}

		super.dispose();
	}

	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected void compute(Set<String> values) {
		String liferayVersion = get(_op().getLiferayVersion());

		String buildType = get(_op().getBuildType());

		try {
			String[] lines = BladeCLI.execute("samples -lc -b " + buildType + " -v " + liferayVersion);

			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
			}

			List<String> list = Arrays.asList(lines);

			values.addAll(list.subList(1, list.size()));
		}
		catch (BladeCLIException bclie) {
		}
	}

	@Override
	protected void initPossibleValuesService() {
		super.initPossibleValuesService();

		_listener = new FilteredListener<PropertyContentEvent>() {

			@Override
			protected void handleTypedEvent(PropertyContentEvent event) {
				refresh();
			}

		};

		SapphireUtil.attachListener(_op().property(NewSampleOp.PROP_BUILD_TYPE), _listener);
	}

	private NewSampleOp _op() {
		return context(NewSampleOp.class);
	}

	private FilteredListener<PropertyContentEvent> _listener;

}
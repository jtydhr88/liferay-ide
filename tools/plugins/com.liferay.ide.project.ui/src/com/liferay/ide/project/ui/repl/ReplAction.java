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

package com.liferay.ide.project.ui.repl;

import org.eclipse.jface.action.Action;

/**
 * @author Gregory Amerson
 */
public abstract class ReplAction extends Action implements ReplStateChangedListener {

	public ReplAction(LiferayReplEditor liferayReplEditor) {
		setEditor(liferayReplEditor);
	}

	@Override
	public void replStateChanged(LiferayReplEditor liferayReplEditor) {
		setEnabled((liferayReplEditor != null) && liferayReplEditor.isVMConnected());
	}

	public void setEditor(LiferayReplEditor editor) {
		if (_liferayReplEditor != null) {
			_liferayReplEditor.removeReplStateChangedListener(this);
		}

		_liferayReplEditor = editor;

		if (_liferayReplEditor != null) {
			if (_liferayReplEditor.getFile() == null) {
				setEnabled(false);

				return;
			}

			_liferayReplEditor.addReplStateChangedListener(this);
		}

		replStateChanged(_liferayReplEditor);
	}

	protected LiferayReplEditor getEditor() {
		return _liferayReplEditor;
	}

	private LiferayReplEditor _liferayReplEditor;

}
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * @author Gregory Amerson
 */
public interface ReplLauncher {

	public void cleanup(IDebugTarget debugTarget);

	public IDebugTarget getDebugTarget(IFile file);

	public IBreakpoint getMagicBreakpoint(IDebugTarget debugTarget) throws DebugException;

	public IVMInstall getVMInstall(IFile file) throws CoreException;

	public String getWorkingDirectoryAttribute(IFile file) throws CoreException;

	public ILaunch launch(IFile file);

}
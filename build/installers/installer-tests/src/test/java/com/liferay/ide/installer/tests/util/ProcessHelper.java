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

package com.liferay.ide.installer.tests.util;

import java.io.IOException;

/**
 * @author Ashley Yuan
 * @author Terry Jia
 */
public class ProcessHelper {

	public static boolean checkProcessWin(String processName) throws IOException {
		return checkProcess("wmic process get name", processName);
	}

	public static boolean checkProcess(String cmd, String processName) throws IOException {
		if (CommandHelper.execWithResult(cmd).contains(processName)) {
			return true;
		}

		return false;
	}

	public static boolean waitProcessWin(String processName) throws InterruptedException, IOException {
		long timeout = System.currentTimeMillis() + 120 * 1000;

		boolean finished = false;

		while (true) {
			if (System.currentTimeMillis() > timeout) {
				break;
			}

			Thread.sleep(1000);

			if (!checkProcessWin(processName)) {
				finished = true;

				break;
			}
		}

		Thread.sleep(1000);

		return finished;
	}

}

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

package com.liferay.ide.project.ui.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;

import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

/**
 * @author Terry Jia
 */
public class ProcessOverClientSocketStreamConnectionProvider extends ProcessStreamConnectionProvider {

	public ProcessOverClientSocketStreamConnectionProvider(List<String> commands, int port) {
		super(commands);

		_port = port;
	}

	public ProcessOverClientSocketStreamConnectionProvider(List<String> commands, String workingDir, int port) {
		super(commands, workingDir);

		_port = port;
	}

	@Override
	public InputStream getInputStream() {
		return _inputStream;
	}

	@Override
	public OutputStream getOutputStream() {
		return _outputStream;
	}

	@Override
	public void start() throws IOException {
		super.start();

		if (_socket != null) {
			return;
		}

		_socket = new Socket("localhost", _port);

		_inputStream = _socket.getInputStream();
		_outputStream = _socket.getOutputStream();
	}

	@Override
	public void stop() {
		if (_socket != null) {
			try {
				_socket.close();
			}
			catch (IOException ioe) {
			}

			_socket = null;
		}

		super.stop();
	}

	private InputStream _inputStream;
	private OutputStream _outputStream;
	private int _port;
	private Socket _socket;

}
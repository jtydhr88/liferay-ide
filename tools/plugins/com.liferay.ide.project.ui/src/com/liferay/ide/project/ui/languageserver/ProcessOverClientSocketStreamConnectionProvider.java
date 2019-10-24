package com.liferay.ide.project.ui.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class ProcessOverClientSocketStreamConnectionProvider extends ProcessStreamConnectionProvider {

	private int port;
	private Socket _socket;
	private InputStream _inputStream;
	private OutputStream _outputStream;

	public ProcessOverClientSocketStreamConnectionProvider(List<String> commands, int port) {
		super(commands);
		this.port = port;
	}

	public ProcessOverClientSocketStreamConnectionProvider(List<String> commands, String workingDir, int port) {
		super(commands, workingDir);
		this.port = port;
	}

	@Override
	public void start() throws IOException {
		super.start();

		if (_socket != null) {
			return;
		}

		_socket = new Socket("localhost", port);

		_inputStream = _socket.getInputStream();
		_outputStream = _socket.getOutputStream();
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
	public void stop() {
		if (_socket != null) {
			try {
				_socket.close();
			}
			catch (IOException e) {
			}
			
			_socket = null;
		}

		super.stop();
	}
}

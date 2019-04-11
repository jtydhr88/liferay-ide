package com.liferay.repl.session;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class ReplSession {

	private boolean _sessionRunning = true;

	@Activate
	public void sessionStarted() {
		Thread thread = new Thread(
			() -> {
				while (_sessionRunning) {
					try {
						System.out.println("Calling eval " + System.currentTimeMillis());
						eval();
					}
					catch (Throwable t) {
					}
				}
			});
		thread.setName("ReplSession");
		thread.start();
	}

	public void eval() {
		noop();
		System.out.println("noop finished " + System.currentTimeMillis());
	}

	public void noop() {
		try {
			Thread.sleep(100);
		} catch(InterruptedException e) {
			_sessionRunning = false;
		}
	}

	@Deactivate
	public void sessionStopped() {
		_sessionRunning = false;
	}

}
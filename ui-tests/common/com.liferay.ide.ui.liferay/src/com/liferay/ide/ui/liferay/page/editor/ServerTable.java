
package com.liferay.ide.ui.liferay.page.editor;

import com.liferay.ide.ui.swtbot.page.Table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;

public class ServerTable extends Table {

	public ServerTable(SWTBot bot) {
		super(bot);
	}

	public ServerTable(SWTBot bot, int index) {
		super(bot, index);
	}

	public void setText(String oldValue, String newValue) {
		bot.text(oldValue).setText(newValue);
	}

	public String[] getServerPortsInfo() {
		List<String> portValues = new ArrayList<String>();
		int rowCount = getWidget().rowCount();

		for (int i = 0; i < rowCount; i++) {
			portValues.add(getWidget().cell(i, 1));
		}
		return portValues.toArray(new String[portValues.size()]);
	}
}

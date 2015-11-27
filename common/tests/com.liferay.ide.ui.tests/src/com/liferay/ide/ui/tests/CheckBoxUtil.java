package com.liferay.ide.ui.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

/**
 * @author Ying Xu
 */

public class CheckBoxUtil extends SWTBotUtil{

	public CheckBoxUtil(SWTWorkbenchBot bot) {
		super(bot);
		
	}
	 public void click( String checkBoxLabel )
	    {
	        bot.checkBox( checkBoxLabel ).click();

	        sleep();
	    }
	 
	 public boolean isChecked( String checkBoxLabel )
	    {

	        return bot.checkBox( checkBoxLabel ).isChecked();
	    }
	 
}

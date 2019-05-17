package com.liferay.ide.upgrade.problems.test.apichanges72;

import java.io.File;

import com.liferay.ide.upgrade.problems.test.apichanges.APITestBase;

public class MovedTermsOfUseContentProviderTest extends APITestBase {

	@Override
	public int getExpectedNumber() {
		return 3;
	}

	@Override
	public String getComponentName() {
		return "com.liferay.ide.upgrade.problems.core.internal.liferay72.MovedTermsOfUseContentProvider";
	}

	@Override
	public File getTestFile() {
		return new File("jsptests/portal/terms_of_use.jsp");
	}

}

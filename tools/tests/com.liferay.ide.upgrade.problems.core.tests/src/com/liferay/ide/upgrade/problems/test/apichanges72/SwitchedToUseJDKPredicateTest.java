package com.liferay.ide.upgrade.problems.test.apichanges72;

import java.io.File;

import com.liferay.ide.upgrade.problems.test.apichanges.APITestBase;

public class SwitchedToUseJDKPredicateTest extends APITestBase {

	@Override
	public int getExpectedNumber() {
		return 5;
	}

	@Override
	public String getComponentName() {
		return "com.liferay.ide.upgrade.problems.core.internal.liferay72.SwitchedToUseJDKPredicate";
	}

	@Override
	public File getTestFile() {
		return new File("javatests/DDMFormValuesFilterImpl.java");
	}

}

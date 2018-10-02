package com.liferay.ide.installer.tests.extensions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * @author Gregory Amerson
 */
public class TempFolderExtension implements AfterTestExecutionCallback, TestInstancePostProcessor {

	private final Collection<TempFolder> tempFolders;

	public TempFolderExtension() {
		tempFolders = new ArrayList<>();
	}

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
		Class<? extends Object> testClass = testInstance.getClass();

		Stream<Field> stream = Arrays.stream(testClass.getDeclaredFields());

		stream.filter(
			field -> field.getType() == TempFolder.class
		).peek(
			field -> field.setAccessible(true)
		).forEach(
			field -> injectTemporaryFolder(testInstance, field)
		);
	}

	private void injectTemporaryFolder(Object instance, Field field) {
		try {
			field.set(instance, createTempFolder());
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private TempFolder createTempFolder() throws IOException {
		TempFolder tempFolder = new TempFolder();

		tempFolders.add(tempFolder);

		return tempFolder;
	}

	@Override
	public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
		tempFolders.forEach(TempFolder::delete);
	}

}

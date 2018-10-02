package com.liferay.ide.installer.tests.extensions;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Gregory Amerson
 */
public class TempFolder {
	private Path tempDirectory;

	public TempFolder() throws IOException {
		tempDirectory = Files.createTempDirectory("junit5-temp-folder");
	}

	public Path newFile(String name) throws IOException {
		Path newPath = tempDirectory.resolve(name);

		Files.createFile(newPath);

		return newPath;
	}

	void delete() {
		try {
			Files.walkFileTree(tempDirectory, new DeleteAllVisitor());
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private static class DeleteAllVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
			Files.delete(path);

			return CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
			Files.delete(path);

			return CONTINUE;
		}
	}

	public Path getRoot() {
		return tempDirectory;
	}

	public Path newDirectory(String name) {
		Path newFolder = tempDirectory.resolve(name);

		try {
			Files.createDirectories(newFolder);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return newFolder;
	}

}
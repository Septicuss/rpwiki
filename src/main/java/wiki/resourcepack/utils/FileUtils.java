package wiki.resourcepack.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

	public static void clearDirectory(Path directory) {
		try {
			Files.walk(directory).sorted((a, b) -> b.compareTo(a)) // reverse order for files before directories
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createFolder(File file) {
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static void create(File file) {

		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String readFile(File file) {
		try {
			return new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<File> filesInDirectory(File directory) {

		try {
			Path path = directory.toPath();
			return Files.walk(path).filter(Files::isRegularFile).map(filePath -> filePath.toFile())
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}

	public static void copyFilesFrom(File sourceDirectory, File destinationDirectory) {
		if (!sourceDirectory.exists() || !destinationDirectory.exists())
			return;

		try {
			Files.walk(sourceDirectory.toPath()).filter(Files::isRegularFile).forEach(sourceFile -> {
				try {

					Path relativePath = sourceDirectory.toPath().relativize(sourceFile);
					Path destinationPath = destinationDirectory.toPath().resolve(relativePath);
					Files.createDirectories(destinationPath.getParent());

					try (InputStream inputStream = Files.newInputStream(sourceFile)) {
						Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void unzip(File zipFile, File outputFolder) {
		try {
			byte[] buffer = new byte[1024];

			try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {

				if (!outputFolder.exists()) {
					outputFolder.mkdirs();
				}

				ZipEntry zipEntry = zipInputStream.getNextEntry();

				while (zipEntry != null) {
					String filePath = outputFolder.getAbsolutePath() + File.separator + zipEntry.getName();

					if (filePath == null || filePath.isBlank())
						continue;

					File newFile = new File(filePath);

					new File(newFile.getParent()).mkdirs();

					if (!zipEntry.isDirectory()) {
						newFile.createNewFile();
						try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
							int length;
							while ((length = zipInputStream.read(buffer)) > 0) {
								fileOutputStream.write(buffer, 0, length);
							}
						}
					} else {
						newFile.mkdirs();
					}

					zipEntry = zipInputStream.getNextEntry();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

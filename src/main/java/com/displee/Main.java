package com.displee;

import com.displee.ui.notify.ProgressNotification;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A class serving as the entry point of this application.
 * @author Displee
 */
public class Main extends Application {

	public static Stage stage;

	public static void main(String[] args) {
		LauncherImpl.launchApplication(Main.class, MainPreLoader.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		final Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
		primaryStage.setTitle("RS3 Model viewer by Displee");
		primaryStage.setScene(new Scene(root, 1200, 800));
		primaryStage.show();
	}

	@Override
	public void init() throws Exception {
		notifyPreloader(new ProgressNotification(0, "Loading native libraries..."));
		ensureNatives();
		notifyPreloader(new ProgressNotification(100, "Loaded native libraries."));
	}

	/**
	 * Ensure that the native libraries are loaded.
	 * @throws Exception
	 */
	private void ensureNatives() throws Exception {
		final String os;
		final String currentOS = System.getProperty("os.name").toLowerCase();
		if (currentOS.contains("win")) {
			os = "windows";
		} else if (currentOS.contains("mac")) {
			os = "macosx";
		} else if (currentOS.contains("nix") || currentOS.contains("nux") || currentOS.contains("aix")) {
			os = "linux";
		} else if (currentOS.contains("sunos")) {
			os = "solaris";
		} else {
			throw new RuntimeException("Unsupported operating system: " + currentOS + ". No native libraries found!");
		}
		final String nativesPath = "/native/" + os + "/";
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		if (jarFile.isFile()) {
			extractNatives(jarFile);
			addLibraryPath("." + nativesPath);
		} else {
			addLibraryPath(getClass().getResource(nativesPath).toURI().getPath());
		}
	}

	/**
	 * Add a library path to the current loaded libraries.
	 * @param pathToAdd The path to add.
	 * @throws Exception Thrown when the path couldn't be added.
	 */
	private static void addLibraryPath(String pathToAdd) throws Exception {
		Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);
		String[] paths = (String[]) usrPathsField.get(null);
		for (String path : paths) {
			if (path.equals(pathToAdd)) {
				return;
			}
		}
		String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}

	/**
	 * Extract the native libraries to their relative path.
	 * @param jarFile The jar file to extract from.
	 * @throws IOException Thrown when a file could not be extracted.
	 */
	private void extractNatives(File jarFile) throws IOException {
		final JarFile jar = new JarFile(jarFile);
		final Enumeration<JarEntry> tempEntries = jar.entries();
		int totalFiles = 0;
		while(tempEntries.hasMoreElements()) {
			final JarEntry entry = tempEntries.nextElement();
			if (!entry.getName().startsWith("native") || entry.isDirectory()) {
				continue;
			}
			totalFiles++;
		}
		int created = 0;
		final Enumeration<JarEntry> entries = jar.entries();
		while(entries.hasMoreElements()) {
			final JarEntry entry = entries.nextElement();
			if (!entry.getName().startsWith("native")) {
				continue;
			}
			final File file = new File(entry.getName());
			if (entry.isDirectory()) {
				if (!file.mkdirs()) {
					return;
				}
				continue;
			}
			if (!file.createNewFile()) {
				return;
			}
			final InputStream is = jar.getInputStream(entry);
			final FileOutputStream fos = new FileOutputStream(file);
			while (is.available() > 0) {
				fos.write(is.read());
			}
			fos.close();
			is.close();
			created++;
			notifyPreloader(new ProgressNotification(created / (double) totalFiles, "Created " + file.getName() + "..."));
		}
		jar.close();
	}

}

package de.pfannekuchen.forgenogradle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import de.pfannekuchen.forgenogradle.Utils.Os;
import de.pfannekuchen.forgenogradle.exceptions.ConnectionException;
import de.pfannekuchen.forgenogradle.gson.json.Library;
import de.pfannekuchen.forgenogradle.gson.json.NativesDownload;
import de.pfannekuchen.forgenogradle.gson.json.Rule;
import de.pfannekuchen.forgenogradle.gson.json.VersionJson;
import de.pfannekuchen.forgenogradle.gson.jsonassets.Asset;
import de.pfannekuchen.forgenogradle.gson.jsonassets.AssetsJson;
import de.pfannekuchen.forgenogradle.gson.jsonforge.ForgeVersionJson;

/**
 * Downloads all dependencies from a given json file url
 * @author Pancake
 */
public class GameDownloader {
	
	/**
	 * Downloads the dependencies into the folder
	 * @param out Output Folder for dependencies
	 * @param in Json to go off
	 * @param forgeversions Json to go off
	 * @param assets Json to go off
	 */
	public static void downloadDeps(VersionJson in, ForgeVersionJson forgeversions, AssetsJson assets, File natives, File libs, File assetsdir) {
		System.out.println(String.format("[GameDownloader] Downloading Dependencies for Minecraft version %s", in.id));
		// Detect operating system for native libraries
		Os os = Utils.getOs();
		System.out.println(String.format("[GameDownloader] Detected operating system: %s", os.name()));
		// Sort out dependencies based on the operating system
		List<Library> dependencies = sortOutDependencies(in.libraries, os);
		System.out.println(String.format("[GameDownloader] Fetched %d dependencies", dependencies.size()));
		/* Try to download all game libraries */
		try {
			for (Library library : dependencies) {
				// Download the artifact
				if (library.downloads.artifact != null) {
					Files.copy(new URL(library.downloads.artifact.url).openStream(), new File(libs, library.downloads.artifact.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println(String.format("[GameDownloader] Downloading %s...", library.downloads.artifact.path.replaceAll("/", "\\.")));
				}
				// Download the natives for the artifact
				if (library.downloads.classifiers != null) {
					NativesDownload nativesWin32 = library.downloads.classifiers.nativesWindows32; 	
					NativesDownload nativesWin64 = library.downloads.classifiers.nativesWindows64;
					NativesDownload nativesWin = library.downloads.classifiers.nativesWindows;
					NativesDownload nativesLinux = library.downloads.classifiers.nativesLinux;
					NativesDownload nativesOsx = library.downloads.classifiers.nativesOsx;
					switch (os) {
						case WIN64:
							if (nativesWin64 != null) {
								Files.copy(new URL(nativesWin64.url).openStream(), new File(natives, nativesWin64.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								Utils.unzipFileAndDelete(natives, nativesWin64.path.replaceAll("/", "\\."), "natives");
							}
							if (nativesWin != null) {
								Files.copy(new URL(nativesWin.url).openStream(), new File(natives, nativesWin.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								Utils.unzipFileAndDelete(natives, nativesWin.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case WIN32:
							if (nativesWin32 != null) {
								Files.copy(new URL(nativesWin32.url).openStream(), new File(natives, nativesWin32.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								Utils.unzipFileAndDelete(natives, nativesWin32.path.replaceAll("/", "\\."), "natives");
							}
							if (nativesWin != null) {
								Files.copy(new URL(nativesWin.url).openStream(), new File(natives, nativesWin.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								Utils.unzipFileAndDelete(natives, nativesWin.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case LINUX:
							if (nativesLinux != null) {
								Files.copy(new URL(nativesLinux.url).openStream(), new File(natives, nativesLinux.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								Utils.unzipFileAndDelete(natives, nativesLinux.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case OSX:
							if (nativesOsx != null) {
								Files.copy(new URL(nativesOsx.url).openStream(), new File(natives, nativesOsx.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								Utils.unzipFileAndDelete(natives, nativesOsx.path.replaceAll("/", "\\."), "natives");
							}
							break;
					}
				}
			}
		} catch (Exception e) {
			// rethrow exceptions
			throw new ConnectionException("Error downloading dependencies", e);
		}
		// Fetch dependencies for Forge
		System.out.println(String.format("[GameDownloader] Downloading Dependencies for Forge version %s", forgeversions.id));
		List<de.pfannekuchen.forgenogradle.gson.jsonforge.Library> dependencies2 = forgeversions.libraries;
		System.out.println(String.format("[GameDownloader] Fetched %d forge dependencies", dependencies2.size()));
		/* Download Forge Dependencies */
		try {
			for (de.pfannekuchen.forgenogradle.gson.jsonforge.Library library : dependencies2) {
				// Download the artifact
				if (!library.downloads.artifact.url.isEmpty()) {
					Files.copy(new URL(library.downloads.artifact.url).openStream(), new File(libs, library.downloads.artifact.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println(String.format("[GameDownloader] Downloading %s...", library.downloads.artifact.path.replaceAll("/", "\\.")));
				}
			}
		} catch (Exception e) {
			// rethrow exceptions
			throw new ConnectionException("Error downloading forge dependencies", e);
		}
		/* Download the Game Assets */
		try {
			System.out.println(String.format("[GameDownloader] Downloading Assets..."));
			ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // create an executor with the system cores
			for (Entry<String, Asset> library : assets.objects.entrySet()) {
				e.execute(() -> {
					try {
						// Find URL and outFile
						final URL url = new URL("https://resources.download.minecraft.net/" + library.getValue().hash.substring(0, 2) + "/" + library.getValue().hash);
						final File outFile = new File(new File(assetsdir, "objects"), library.getValue().hash.substring(0, 2) + "/" + library.getValue().hash);
						outFile.getParentFile().mkdirs();
						// Download the file
						System.out.println(String.format("[GameDownloader] Downloading %s...", outFile.getName()));
						Files.copy(url.openStream(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
			}
			// Wait for tasks to finish
			e.shutdown();
			while (!e.awaitTermination(200L, TimeUnit.MILLISECONDS)) {}
			// Download the indexes json
			File indexes = new File(assetsdir, "indexes");
			indexes.mkdirs();
			Files.copy(new ByteArrayInputStream(new Gson().toJson(assets).getBytes(StandardCharsets.UTF_8)), new File(indexes, in.assetIndex.id + ".json").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new ConnectionException("Error downloading assets", e);
		}
	}

	/**
	 * Removes dependencies from list following mojangs given rules
	 * @param in In List
	 * @param os Operating System
	 * @return Sorted List
	 */
	private static List<Library> sortOutDependencies(List<Library> in, Os os) {
		// remove unwanted dependencies based on mojangs rule system
		DEPENDENCYLOOP: for (Library library : new ArrayList<>(in)) {
			if (library.rules != null) {
				// check rules
				boolean shouldBeAllowedByDefault = false;
				for (Rule rule : library.rules) {
					// allow or deny rule
					boolean action = "allow".equals(rule.action);
					if (rule.os == null) {
						shouldBeAllowedByDefault = action;
						continue;
					}
					// depends on OS
					if ("windows".equals(rule.os.name) && (os == Os.WIN32 || os == Os.WIN64)) {
						if (!action) in.remove(library);
						continue DEPENDENCYLOOP;
					} else if ("osx".equals(rule.os.name) && os == Os.OSX) {
						if (!action) in.remove(library);
						continue DEPENDENCYLOOP;
					} else if ("linux".equals(rule.os.name) && os == Os.LINUX) {
						if (!action) in.remove(library);
						continue DEPENDENCYLOOP;
					}
				}
				// remove if failed
				if (!shouldBeAllowedByDefault) in.remove(library);
			}	
		}
		return in;
	}
	
}

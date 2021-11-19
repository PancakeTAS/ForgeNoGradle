package de.pfannekuchen.launcher;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.pfannekuchen.launcher.Utils.Os;
import de.pfannekuchen.launcher.exceptions.ConnectionException;
import de.pfannekuchen.launcher.exceptions.ExtractionException;
import de.pfannekuchen.launcher.json.Library;
import de.pfannekuchen.launcher.json.NativesDownload;
import de.pfannekuchen.launcher.json.Rule;
import de.pfannekuchen.launcher.json.VersionJson;
import de.pfannekuchen.launcher.jsonforge.ForgeVersionJson;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Downloads all dependencies from a given json file url
 * @author Pancake
 */
public class JsonDownloader {
	
	/**
	 * Downloads the dependencies into the folder
	 * @param out Output Folder for dependencies
	 * @param in Json to go off
	 * @param forgeversions Json to go off
	 */
	public static void downloadDeps(File out, VersionJson in, ForgeVersionJson forgeversions) {
		int time = (int) System.currentTimeMillis();
		out.mkdirs();
		File natives = new File(out, "natives");
		File libs = new File(out, "libraries");
		natives.mkdir();
		libs.mkdir();
		System.out.println(String.format("[JsonDownloader] Downloading Dependencies for Minecraft version %s", in.id));
		Os os = Utils.getOs();
		System.out.println(String.format("[JsonDownloader] Detected operating system: %s", os.name()));
		List<Library> dependencies = sortOutDependencies(in.libraries, os);
		System.out.println(String.format("[JsonDownloader] Fetching %d dependencies", dependencies.size()));
		try {
			for (Library library : dependencies) {
				if (library.downloads.artifact != null) {
					Files.copy(new URL(library.downloads.artifact.url).openStream(), new File(libs, library.downloads.artifact.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println(String.format("[JsonDownloader] Downloading %s...", library.downloads.artifact.path.replaceAll("/", "\\.")));
				}
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
								unzipFileAndDelete(natives, nativesWin64.path.replaceAll("/", "\\."), "natives");
							}
							if (nativesWin != null) {
								Files.copy(new URL(nativesWin.url).openStream(), new File(natives, nativesWin.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								unzipFileAndDelete(natives, nativesWin.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case WIN32:
							if (nativesWin32 != null) {
								Files.copy(new URL(nativesWin32.url).openStream(), new File(natives, nativesWin32.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								unzipFileAndDelete(natives, nativesWin32.path.replaceAll("/", "\\."), "natives");
							}
							if (nativesWin != null) {
								Files.copy(new URL(nativesWin.url).openStream(), new File(natives, nativesWin.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								unzipFileAndDelete(natives, nativesWin.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case LINUX:
							if (nativesLinux != null) {
								Files.copy(new URL(nativesLinux.url).openStream(), new File(natives, nativesLinux.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								unzipFileAndDelete(natives, nativesLinux.path.replaceAll("/", "\\."), "natives");
							}
							break;
						case OSX:
							if (nativesOsx != null) {
								Files.copy(new URL(nativesOsx.url).openStream(), new File(natives, nativesOsx.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
								unzipFileAndDelete(natives, nativesOsx.path.replaceAll("/", "\\."), "natives");
							}
							break;
					}
				}
			}
		} catch (Exception e) {
			Utils.deleteDirectory(out);
			throw new ConnectionException("Error downloading dependencies", e);
		}
		System.out.println(String.format("[JsonDownloader] Downloading Dependencies for Forge version %s", forgeversions.id));
		List<de.pfannekuchen.launcher.jsonforge.Library> dependencies2 = forgeversions.libraries;
		System.out.println(String.format("[JsonDownloader] Fetching %d forge dependencies", dependencies2.size()));
		try {
			for (de.pfannekuchen.launcher.jsonforge.Library library : dependencies2) {
				if (!library.downloads.artifact.url.isEmpty()) {
					Files.copy(new URL(library.downloads.artifact.url).openStream(), new File(libs, library.downloads.artifact.path.replaceAll("/", "\\.")).toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println(String.format("[JsonDownloader] Downloading %s...", library.downloads.artifact.path.replaceAll("/", "\\.")));
				}
			}
		} catch (Exception e) {
			Utils.deleteDirectory(out);
			throw new ConnectionException("Error downloading forge dependencies", e);
		}
		try {
			System.out.println(String.format("[JsonDownloader] Downloading Client..."));
			Files.copy(new URL(in.downloads.client.url).openStream(), new File(out, "client.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			Utils.deleteDirectory(out);
			throw new ConnectionException("Error downloading client", e);
		}
		System.out.println(String.format(Locale.ENGLISH, "[JsonDownloader] All files successfully downloaded. Took %.2f seconds", (((int) System.currentTimeMillis()) - time) / 1000.0f));
	}

	private static void unzipFileAndDelete(File zipDir, String zipFile, String job) {
		ZipFile zip = new ZipFile(new File(zipDir, zipFile));
		System.out.println(String.format("[JsonDownloader] Extracting %s: %s", job, zipFile));
		try {
			for (FileHeader fileHeader : zip.getFileHeaders()) {
				if (fileHeader.getFileName().contains("META-INF")) continue;
				zip.extractFile(fileHeader, zipDir.getAbsolutePath());
				System.out.println(String.format("[JsonDownloader]     %s", fileHeader.getFileName()));
			}
			new File(zipDir, zipFile).delete();
		} catch (ZipException e) {
			throw new ExtractionException("Error extracting " + job + ": " + zipFile, e);
		}
	}

	private static List<Library> sortOutDependencies(List<Library> in, Os os) {
		// remove unwanted dependencies based on mojangs rule system
		DEPENDENCYLOOP: for (Library library : new ArrayList<>(in)) {
			if (library.rules != null) {
				// check rules
				boolean shouldBeAllowedByDefault = false;
				for (Rule rule : library.rules) {
					boolean action = "allow".equals(rule.action);
					if (rule.os == null) {
						shouldBeAllowedByDefault = action;
						continue;
					}
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
				if (!shouldBeAllowedByDefault) in.remove(library);
			}	
		}
		return in;
	}
	
}

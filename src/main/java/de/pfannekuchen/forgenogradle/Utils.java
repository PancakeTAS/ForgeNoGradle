package de.pfannekuchen.forgenogradle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

import de.pfannekuchen.forgenogradle.exceptions.ConnectionException;
import de.pfannekuchen.forgenogradle.exceptions.ExtractionException;
import de.pfannekuchen.forgenogradle.exceptions.FilesystemException;
import de.pfannekuchen.forgenogradle.exceptions.OperatingSystemException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Utils for the Project
 * @author Pancake
 */
public class Utils {

	/**
	 * Operating System list out of the most used ones
	 * @author Pancake
	 */
	public static enum Os {
		WIN32, WIN64, OSX, LINUX
	}
	
	/**
	 * Obtains the operating system type
	 * @return Returns the operating system type
	 */
	public static Os getOs() {
		// get java property
		String stringOs = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		// parse operating system type
		if (stringOs.contains("mac") || stringOs.contains("darwin")) return Os.OSX;
		else if (stringOs.contains("win")) return new File("C:\\Windows\\SysWOW64").exists() ? Os.WIN64 : Os.WIN32;
		else if (stringOs.contains("nux")) return Os.LINUX;
		// throw error if none found
		throw new OperatingSystemException("Unsupported Operating System: " + stringOs);
	}
	
	/**
	 * Opens an input stream with a user agent
	 * @param url URL
	 * @return URL Stream
	 * @throws IOException urgh
	 */
	public static InputStream userAgentDownload(URL url) throws IOException {
		URLConnection c = url.openConnection();
		c.setRequestProperty("User-Agent", "Mozilla/5.0 (aka; mixin download; java downloader)");
		return c.getInputStream();
	}
	
	/**
	 * Recursively deletes a directory
	 * @param out
	 */
	public static void deleteDirectory(File out) {
		try {
			// Walk all files and delete them.
			Files.walkFileTree(out.toPath(), new FileVisitor<Path>() {
				@Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException { return FileVisitResult.CONTINUE; }
				@Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException { return FileVisitResult.CONTINUE; }

				/**
				 * Visits all files and deletes the content
				 */
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					file.toFile().delete();
					return FileVisitResult.CONTINUE;
				}
				
				/**
				 * Visits all directories after the files within have been deleted
				 */
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					dir.toFile().delete();
					return FileVisitResult.CONTINUE;
				}
			});
			out.delete();
		} catch (IOException e) {
			// catch io exceptions and rethrow them properly
			throw new FilesystemException("Couldn't delete folder: " + out.getAbsolutePath(), e);
		}
	}
	
	/**
	 * Reads a URL Request as String
	 * @param url URL to read form
	 * @return URL Response
	 */
	public static String readAllBytesAsStringFromURL(URL url) {
		// Connect closable
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
	    	// Use a buffer to fully read a stream
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } catch (IOException e) {
	    	// catch io exceptions and rethrow them properly
			throw new ConnectionException("Failed downloading: " + url.toString(), e);
		}
	}

	/**
	 * Unzips a file without copying META-INF
	 * @param zipDir Zip Dir
	 * @param zipFile Zip File
	 * @param job Logging purposes
	 */
	public static void unzipFileAndDelete(File zipDir, String zipFile, String job) {
		ZipFile zip = new ZipFile(new File(zipDir, zipFile));
		System.out.println(String.format("[GameDownloader] Extracting %s: %s", job, zipFile));
		try {
			// Extract File Header Check
			for (FileHeader fileHeader : zip.getFileHeaders()) {
				if (fileHeader.getFileName().contains("META-INF")) continue;
				zip.extractFile(fileHeader, zipDir.getAbsolutePath());
				System.out.println(String.format("[GameDownloader]     %s", fileHeader.getFileName()));
			}
			// Delete the ZIP
			new File(zipDir, zipFile).delete();
		} catch (ZipException e) {
			// rethrow exceptions
			throw new ExtractionException("Error extracting " + job + ": " + zipFile, e);
		}
	}
	
}

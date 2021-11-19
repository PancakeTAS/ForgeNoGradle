package de.pfannekuchen.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

import de.pfannekuchen.launcher.exceptions.ConnectionException;
import de.pfannekuchen.launcher.exceptions.FilesystemException;
import de.pfannekuchen.launcher.exceptions.OperatingSystemException;

public class Utils {

	enum Os {
		WIN32, WIN64, OSX, LINUX
	}
	
	public static Os getOs() {
		String stringOs = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if (stringOs.contains("mac") || stringOs.contains("darwin")) return Os.OSX;
		else if (stringOs.contains("win")) return new File("C:\\Windows\\SysWOW64").exists() ? Os.WIN64 : Os.WIN32;
		else if (stringOs.contains("nux")) return Os.LINUX;
		throw new OperatingSystemException("Unsupported Operating System: " + stringOs);
	}
	
	public static void deleteDirectory(File out) {
		try {
			Files.walkFileTree(out.toPath(), new FileVisitor<Path>() {
				@Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException { return FileVisitResult.CONTINUE; }
				@Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException { return FileVisitResult.CONTINUE; }

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					file.toFile().delete();
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					dir.toFile().delete();
					return FileVisitResult.CONTINUE;
				}
			});
			out.delete();
		} catch (IOException e) {
			throw new FilesystemException("Couldn't delete folder: " + out.getAbsolutePath(), e);
		}
	}
	
	public static String readAllBytesAsStringFromURL(URL url) {
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } catch (IOException e) {
			throw new ConnectionException("Failed downloading: " + url.toString(), e);
		}
	}
	
}

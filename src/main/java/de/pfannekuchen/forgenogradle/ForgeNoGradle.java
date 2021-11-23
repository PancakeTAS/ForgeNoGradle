package de.pfannekuchen.forgenogradle;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.pfannekuchen.forgenogradle.exceptions.ConnectionException;
import de.pfannekuchen.forgenogradle.exceptions.FilesystemException;
import de.pfannekuchen.forgenogradle.exceptions.ReflectionException;
import de.pfannekuchen.forgenogradle.game.Pong;
import de.pfannekuchen.forgenogradle.gson.json.VersionJson;
import de.pfannekuchen.forgenogradle.gson.jsonassets.AssetsJson;
import de.pfannekuchen.forgenogradle.gson.jsonforge.ForgeVersionJson;

/**
 * Main ForgeNoGradle Class that manages the order of the progrma
 * @author Pancake
 */
public class ForgeNoGradle {

	/**
	 * Main Project Folder
	 */
	private static final File PROJECT_DIR = new File(new File("project").getAbsolutePath()); // "project" only temporary, this has to be replaced with "." before releasing
	
	/**
	 * Minecraft Client run directory
	 */
	private static final File RUN_DIR = new File(PROJECT_DIR, "run");
	
	/**
	 * Minecrat Server run directory
	 */
	private static final File RUN_SERVER_DIR = new File(PROJECT_DIR, "run-server");
	
	/**
	 * Forge No Gradle build directory
	 */
	private static final File FNG_LIB_DIR = new File(PROJECT_DIR, "build");
	
	/**
	 * Eclipse temporary class output dir
	 */
	private static final File BIN_DIR = new File(FNG_LIB_DIR, "bin");
	
	/**
	 * Eclipse Source Folder
	 */
	private static final File SRC_DIR = new File(PROJECT_DIR, "src");
	
	/**
	 * Eclipse Resource Folder
	 */
	private static final File RSC_DIR = new File(PROJECT_DIR, "rsc");
	
	/**
	 * Minecraft Forge libraries directory
	 */
	private static final File LIBRARIES_DIR = new File(FNG_LIB_DIR, "libraries");
	
	/**
	 * Minecraft Natives directory
	 */
	private static final File NATIVES_DIR = new File(FNG_LIB_DIR, "natives");
	
	/**
	 * Minecraft Assets directory
	 */
	private static final File ASSETS_DIR = new File(FNG_LIB_DIR, "assets");
	
	/**
	 * Version to download
	 */
	private static final String VERSION = "1.12.2";
	
	/**
	 * The Forge Client/Server Jar
	 */
	private static final File MCFORGE = new File(FNG_LIB_DIR, "mc-forge-" + VERSION + ".jar");
	
	/**
	 * The Forge Client/Server Source Jar
	 */
	private static final File MCFORGE_SRC = new File(FNG_LIB_DIR, "mc-forge-" + VERSION + "-src.jar");
	
	/**
	 * The Mixin Jar
	 */
	private static final File MIXIN = new File(FNG_LIB_DIR, "mixin-8.2-" + VERSION + ".jar");
	
	/**
	 * The Mixin Source Jar
	 */
	private static final File MIXIN_SRC = new File(FNG_LIB_DIR, "mixin-8.2-" + VERSION + "-src.jar");
	
	/**
	 * The Mixin Javadoc Jar
	 */
	private static final File MIXIN_JD = new File(FNG_LIB_DIR, "mixin-8.2-" + VERSION + "-jd.jar");
	
	/**
	 * The Mixin annotation processor
	 */
	private static final File MIXIN_P = new File(FNG_LIB_DIR, "mixin-8.2-" + VERSION + "-p.jar");
	
	/**
	 * Url for the version json
	 */
	private static final String VERSION_URL = "https://launchermeta.mojang.com/v1/packages/f07e0f1228f79b9b04313fc5640cd952474ba6f5/" + VERSION + ".json";
	
	/**
	 * Url for the forge dependency json
	 */
	private static final String FORGE_URL = "https://data.mgnet.work/forge/" + VERSION + ".json";
	
	/**
	 * Base URL for all Mixin downloads
	 */
	private static final String MIXIN_BASE_URL = "https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.8.2/mixin-0.8.2";
	
	/**
	 * Whether dependencies should be skipped
	 */
	public static boolean skipLibs;
	
	/**
	 * Main Class for managing the order of execution
	 * @param args Main
	 * @throws InterruptedException In case someone exites early
	 */
	public static void main(String[] args) throws InterruptedException {
		// Delete Folder Structore
		deleteFolderStructure();
		
		// Run Pong
		Pong.runPong();
		
		System.out.println("========== Starting to prepare ForgeNoGradle workspace");
		long time = System.currentTimeMillis();
		
		// Create Folder Structure
		createFolderStructure();
		
		System.out.println("=========== 1/4 finished. Took " + String.format("%.2f", ((int) (System.currentTimeMillis() - time)) / 1000.0f) + " seconds...");
		
		// Download the game assets
		downloadGameAssets();
		
		System.out.println("=========== 2/4 finished. Took " + String.format("%.2f", ((int) (System.currentTimeMillis() - time)) / 1000.0f) + " seconds...");
		
		// Decompile the game
		decompileForge();
		
		System.out.println("=========== 3/4 finished. Took " + String.format("%.2f", ((int) (System.currentTimeMillis() - time)) / 1000.0f) + " seconds...");
		
		// Create Eclipse Files
		createEclipseFiles();
		
		System.out.println("=========== 4/4 finished. Took " + String.format("%.2f", ((int) (System.currentTimeMillis() - time)) / 1000.0f) + " seconds...");
		System.out.println("Exiting...");

		// Exit
		System.exit(0);
		
	}
	
	/**
	 * Creates the folder structure
	 */
	private static void deleteFolderStructure() {
		System.out.println("[ForgeNoGradle] Deleting Project File Structure...");
		
		// Delete folders and files that require an update
		if (BIN_DIR.exists()) Utils.deleteDirectory(BIN_DIR);
		if (LIBRARIES_DIR.exists() && ASSETS_DIR.exists() && NATIVES_DIR.exists()) skipLibs = true; 
		final File[] delete = new File[] {
			new File(PROJECT_DIR, ".project"),
			new File(PROJECT_DIR, ".classpath"),
			new File(PROJECT_DIR, ".factorypath"),
			new File(PROJECT_DIR, PROJECT_DIR.getName() + "-" + VERSION + ".launch"),
			new File(PROJECT_DIR, PROJECT_DIR.getName() + "-" + VERSION + "-export.launch"),
			new File(PROJECT_DIR, PROJECT_DIR.getName() + "-" + VERSION + "-server.launch"),
			new File(PROJECT_DIR, ".gitignore"),
			new File(PROJECT_DIR, ".settings"),
			new File(PROJECT_DIR, ".apt_generated"),
			new File(PROJECT_DIR, ".apt_generated_tests"),
			new File(PROJECT_DIR, "run"),
			new File(PROJECT_DIR, "run-server"),
			new File(PROJECT_DIR, "build/mcp")
			
		};
		for (File f : delete) {
			if (!f.exists()) continue;
			if (f.isDirectory()) Utils.deleteDirectory(f);
			else f.delete();
		}
		// Delete Jars in build/
		for (File f2 : FNG_LIB_DIR.listFiles()) {
			if (!f2.exists()) continue;
			if (f2.getName().toLowerCase().endsWith(".jar")) f2.delete();
		}
		
		System.out.println("[ForgeNoGradle] Finished deleting file structure");
	}
	
	/**
	 * Creates the folder structure
	 */
	private static void createFolderStructure() {
		System.out.println("[ForgeNoGradle] Create Project Folder Structure...");
		
		// Create all required folders
		RUN_DIR.mkdir();
		RUN_SERVER_DIR.mkdir();
		if (!FNG_LIB_DIR.exists()) FNG_LIB_DIR.mkdir();
		if (!NATIVES_DIR.exists()) NATIVES_DIR.mkdir();
		if (!ASSETS_DIR.exists()) ASSETS_DIR.mkdir();
		if (!SRC_DIR.exists()) SRC_DIR.mkdir();
		if (!RSC_DIR.exists()) RSC_DIR.mkdir();
		BIN_DIR.mkdir();
		
		System.out.println("[ForgeNoGradle] Finished creating folder structure");
	}
	
	/**
	 * Downloads the game assets
	 */
	private static void downloadGameAssets() {
		// Download nessecary JSON Files for the game assets
		final Gson gson = new Gson();
		System.out.println("[ForgeNoGradle] Downloading Game Assets and Libraries");
		
		VersionJson versions;
		try {
			// Download 3 JSONs from the servers containing all dependencies and assets
			versions = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL(VERSION_URL)), VersionJson.class);
			ForgeVersionJson forgeversions = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL(FORGE_URL)), ForgeVersionJson.class);
			AssetsJson assets = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL(versions.assetIndex.url)), AssetsJson.class);
			// Download using these JSON files
			if (!skipLibs) GameDownloader.downloadDeps(versions, forgeversions, assets, NATIVES_DIR, LIBRARIES_DIR, ASSETS_DIR);
			else System.out.println("[ForgeNoGradle] Skipped downloading Game Assets and Libraries");
			Files.copy(ForgeNoGradle.class.getResourceAsStream("/forgeapi.lib"), new File(LIBRARIES_DIR, "forgeapi.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (JsonSyntaxException | IOException e) {
	    	// catch exceptions and rethrow them properly
			throw new ConnectionException("Unable to download files", e);
		}
		
		System.out.println("[ForgeNoGradle] Downloading the Forge Client+Server Jar");
		
		try {
			Files.copy(new URL("https://data.mgnet.work/forge/mc-forge-" + versions.id + ".jar").openStream(), MCFORGE.toPath());
		} catch (IOException e) {
	    	// catch io exceptions and rethrow them properly
			throw new ConnectionException("Failed downloading: https://data.mgnet.work/forge/mc-forge-" + versions.id + ".jar", e);
		}
		
		System.out.println("[ForgeNoGradle] Downloading the Forge MCP Version");
		
		try {
			File mcpDir = new File(FNG_LIB_DIR, "mcp");
			mcpDir.mkdir();
			File mcp = new File(mcpDir, "mcp.zip");
			Files.copy(new URL("https://data.mgnet.work/forge/" + versions.id + "-mcp.zip").openStream(), mcp.toPath());
			// extract
			Utils.unzipFileAndDelete(mcpDir, "mcp.zip", "MCP");
			mcp.delete();
		} catch (IOException e) {
	    	// catch io exceptions and rethrow them properly
			throw new ConnectionException("Failed downloading: https://data.mgnet.work/forge/mc-forge-" + versions.id + ".jar", e);
		}
		
		System.out.println("[ForgeNoGradle] Downloading Mixin");
		
		try {
			Files.copy(Utils.userAgentDownload(new URL(MIXIN_BASE_URL + ".jar")), MIXIN.toPath());
			Files.copy(Utils.userAgentDownload(new URL(MIXIN_BASE_URL + "-sources.jar")), MIXIN_SRC.toPath());
			Files.copy(Utils.userAgentDownload(new URL(MIXIN_BASE_URL + "-javadoc.jar")), MIXIN_JD.toPath());
			Files.copy(Utils.userAgentDownload(new URL(MIXIN_BASE_URL + "-processor.jar")), MIXIN_P.toPath());
		} catch (IOException e) {
	    	// catch io exceptions and rethrow them properly
			throw new ConnectionException("Failed downloading mixin", e);
		}
		
		System.out.println("[ForgeNoGradle] Finished downloading the Game Assets");
	}
	
	/**
	 * Decompiles the forge game jar
	 */
	private static void decompileForge() {
		try {
			System.out.println("[ForgeNoGradle] Preparing decompiler settings...");
			
			// Prepare mandatory settings
			Map<String, Object> mapOptions = new HashMap<String, Object>();
			mapOptions.put(IFernflowerPreferences.DECOMPILE_INNER, "1");
			mapOptions.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
			mapOptions.put(IFernflowerPreferences.ASCII_STRING_CHARACTERS, "1");
			mapOptions.put(IFernflowerPreferences.THREADS, Runtime.getRuntime().availableProcessors() + ""); // Multithreaded to system cores
			mapOptions.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
			mapOptions.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
			mapOptions.put(IFernflowerPreferences.REMOVE_BRIDGE, "1");
			mapOptions.put(IFernflowerPreferences.LITERALS_AS_IS, "0");
			mapOptions.put(IFernflowerPreferences.UNIT_TEST_MODE, "0");
			mapOptions.put(IFernflowerPreferences.MAX_PROCESSING_METHOD, "0");
			
			System.out.println("[ForgeNoGradle] Creating decompiler...");
			
			// ConsoleDecompiler is private ._.. We use reflection to create an instance
			Constructor<?> c = Class.forName("org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler").getDeclaredConstructors()[0];
			c.setAccessible(true);
			PrintStreamLogger logger = new PrintStreamLogger(System.out);
			File temp_source = new File(FNG_LIB_DIR, "src");
			Object consoledecompiler = c.newInstance(temp_source, mapOptions, logger);
			// Create the Decompiler Wrapper
			BaseDecompiler decompiler = new BaseDecompiler((IBytecodeProvider) consoledecompiler, (IResultSaver) consoledecompiler, mapOptions, logger);
			decompiler.addSource(MCFORGE);
			for (File library : LIBRARIES_DIR.listFiles()) {
				decompiler.addLibrary(library);
			}
			
			System.out.println("[ForgeNoGradle] Decompiling...");
			
			decompiler.decompileContext();
			
			System.out.println("[ForgeNoGradle] Moving source jar...");
			
			new File(temp_source, "mc-forge-" + VERSION + ".jar").renameTo(MCFORGE_SRC);
			temp_source.delete();
			
			System.out.println("[ForgeNoGradle] Finished decompiling the game");
		} catch (SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ReflectionException("Unable to create decompiler", e);
		}
	}
	
	/**
	 * Creates all eclipse project files
	 */
	private static void createEclipseFiles() {
		try {

			System.out.println("[ForgeNoGradle] Preparing .project...");
			
			// Prepare Project File
			Files.write(new File(PROJECT_DIR, ".project").toPath(), Eclipse.PROJECT.replaceFirst("%NAME%", PROJECT_DIR.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

			System.out.println("[ForgeNoGradle] Preparing .settings/...");
			
			// Prepare settings file
			new File(PROJECT_DIR, ".settings").mkdir();
			Files.write(new File(PROJECT_DIR, ".settings/org.eclipse.jdt.core.prefs").toPath(), Eclipse.CORE_PREFS.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			Files.write(new File(PROJECT_DIR, ".settings/org.eclipse.jdt.apt.core.prefs").toPath(), Eclipse.APT_CORE_PREFS.replaceFirst("%SRG%", new File(FNG_LIB_DIR, "mcp/mcp-srg.srg").getAbsolutePath().replaceAll(Pattern.quote(":"), "\\:").replaceAll(Pattern.quote("\\"), "/")).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			
			System.out.println("[ForgeNoGradle] Preparing .gitignore...");
			
			// Prepare .gitignore
			Files.write(new File(PROJECT_DIR, ".gitignore").toPath(), Eclipse.GITIGNORE.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			
			System.out.println("[ForgeNoGradle] Preparing .factorypath...");
			
			// Prepare .gitignore
			Files.write(new File(PROJECT_DIR, ".factorypath").toPath(), Eclipse.FACTOY_PATH.replaceFirst("%PROCESSOR%", MIXIN_P.getAbsolutePath().replace('\\', '/')).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			
			System.out.println("[ForgeNoGradle] Preparing *.launch...");
			
			// Prepare launch files
			Files.write(new File(PROJECT_DIR, PROJECT_DIR.getName() + "-" + VERSION + ".launch").toPath(), Eclipse.RUN.replaceFirst("%MIXIN%", MIXIN.getName()).replaceAll("%PROJECT%",PROJECT_DIR.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			Files.write(new File(PROJECT_DIR, PROJECT_DIR.getName() + "-" + VERSION + "-server.launch").toPath(), Eclipse.RUN.replaceFirst("%MIXIN%", MIXIN.getName()).replaceFirst("GradleStart", "GradleStartServer").replaceAll(Pattern.quote("/run"), "/run-server").replaceAll("%PROJECT%", PROJECT_DIR.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			Files.write(new File(PROJECT_DIR, PROJECT_DIR.getName() + "-" + VERSION + "-export.launch").toPath(), Eclipse.EXPORT.replaceAll("%PROJECT%", PROJECT_DIR.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			
			System.out.println("[ForgeNoGradle] Preparing .classpath...");
			
			// Prepare Classpath File
			String partclasspath = "";
			for (File lib : LIBRARIES_DIR.listFiles()) {
				// Add natives for lwjgl, jinput and text2speech
				if (lib.getName().toLowerCase().contains("lwjgl-") || lib.getName().toLowerCase().contains("jinput") || lib.getName().toLowerCase().contains("text2speech"))
					partclasspath += '\t' + Eclipse.LIBRARY_NATIVE.replaceAll("%PATH%", PROJECT_DIR.toURI().relativize(lib.toURI()).getPath()).replaceAll("%NATIVES%", PROJECT_DIR.toURI().relativize(NATIVES_DIR.toURI()).getPath());
				else
					partclasspath += Eclipse.LIBRARY.replaceFirst("%PATH%", PROJECT_DIR.toURI().relativize(lib.toURI()).getPath()) + '\n';
			}
			partclasspath += Eclipse.LIBRARY_SOURCE.replaceFirst("%PATH%", PROJECT_DIR.toURI().relativize(MCFORGE.toURI()).getPath()).replaceFirst("%SOURCE%", PROJECT_DIR.toURI().relativize(MCFORGE_SRC.toURI()).getPath()) + '\n';
			partclasspath += Eclipse.LIBRARY_FULL.replaceFirst("%PATH%", PROJECT_DIR.toURI().relativize(MIXIN.toURI()).getPath()).replaceFirst("%SOURCE%", PROJECT_DIR.toURI().relativize(MIXIN_SRC.toURI()).getPath()).replaceFirst("%JAVADOC%", PROJECT_DIR.toURI().relativize(MIXIN_JD.toURI()).getPath()) + '\n';
			Files.write(new File(PROJECT_DIR, ".classpath").toPath(), Eclipse.CLASSPATH.replaceFirst("%INSERT%", partclasspath.substring(0, partclasspath.length() - 1)).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

			System.out.println("[ForgeNoGradle] Finished creating Eclipse Files");
		} catch (IOException e) {
			throw new FilesystemException("Unable to create eclipse projec files", e);
		}
	}
	
}

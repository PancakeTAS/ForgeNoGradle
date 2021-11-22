package de.pfannekuchen.forgenogradleapi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.md_5.specialsource.Jar;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JarProvider;
import net.md_5.specialsource.provider.JointProvider;

/**
 * This Class contains everything needed to Export a Jar File runnable from forge
 * @author Pancake
 */
public class ExportMain {

	/**
	 * Exported Jar File
	 */
	private static final File JAR_FILE = new File("export.jar");
	
	/**
	 * Exported Reobfuscated Jar File
	 */
	private static final File JAR_FILE_REOBF = new File("export-reobf.jar");
	
	/**
	 * Class Files
	 */
	private static final File BIN_DIR = new File("bin");
	
	/**
	 * Mixin Files
	 */
	private static final File MIXIN_FILE = new File("mixin-8.2-1.12.2.jar");
	
	/**
	 * Mappings for reobfuscation
	 */
	private static final File MAPPINGS_FILE = new File("mcp/mcp-srg.srg");
	
	/**
	 * Folder with all libraries
	 */
	private static final File LIBS_DIR = new File("libraries");
	
	/**
	 * MC Forge library
	 */
	private static final File MCFORGE = new File("mc-forge-1.12.2.jar");
	
	/**
	 * Exports a Jar File
	 * @param args
	 */
	@SuppressWarnings("deprecation") // For some reason URL is deprecated
	public static void main(String[] args) throws Exception {
		/* Create the jar */
		// Create a new Zip File based off the mixin file
		Files.copy(MIXIN_FILE.toPath(), JAR_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
		final ZipFile out = new ZipFile(JAR_FILE);
		// Add Files to Jar
		for (File bin : BIN_DIR.listFiles()) {
			if (bin.isDirectory()) out.addFolder(bin);
			else out.addFile(bin);
		}
		// Create Manifest for Jar
		ZipParameters parameters = new ZipParameters();
		parameters.setOverrideExistingFilesInZip(true);
		parameters.setFileNameInZip("META-INF/MANIFEST.MF");
		ZipFile mixinFile = new ZipFile(MIXIN_FILE);
		FileHeader header = mixinFile.getFileHeader("META-INF/MANIFEST.MF");
		InputStream b = mixinFile.getInputStream(header);
		out.addStream(new SequenceInputStream(new ByteArrayInputStream("FMLCorePluginContainsFMLMod: true\r\nMixinConfigs: mixin.json\r\nTweakClass: org.spongepowered.asm.launch.MixinTweaker\r\nForceLoadAsMod: true\r\n".getBytes(StandardCharsets.UTF_8)), b), parameters);
		out.close();
		mixinFile.close();
		
		/* Obfuscate the Jar */
		// Prepare Remapper
		JarMapping mapping = new JarMapping();
		mapping.loadMappings(Files.newBufferedReader(MAPPINGS_FILE.toPath()), null, null, false);
		JarRemapper remapper = new JarRemapper(null, mapping);
		// Prepare Classpath
		List<URL> url = new ArrayList<>();
		url.add(MCFORGE.toURL());
		for (File lib : LIBS_DIR.listFiles())  url.add(lib.toURL());
		// Obfuscate
		URLClassLoader classLoader = null;
		try (Jar input = Jar.init(JAR_FILE)) {
			// Enable Inheritance Providers
			JointProvider inheritanceProviders = new JointProvider();
			inheritanceProviders.add(new JarProvider(input));
			inheritanceProviders.add(new ClassLoaderProvider(classLoader = new URLClassLoader(url.toArray(new URL[url.size()]))));
			mapping.setFallbackInheritanceProvider(inheritanceProviders);

			// remap jar
			remapper.remapJar(input, JAR_FILE_REOBF);
		} finally {
			// Close Class Loader again
			if (classLoader != null) classLoader.close();
		}
	}
	
}

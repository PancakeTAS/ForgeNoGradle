package forge;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import de.pfannekuchen.launcher.JsonDownloader;
import de.pfannekuchen.launcher.Utils;
import de.pfannekuchen.launcher.json.VersionJson;
import de.pfannekuchen.launcher.jsonassets.AssetsJson;
import de.pfannekuchen.launcher.jsonforge.ForgeVersionJson;

public class Forge {

	private static final Gson gson = new Gson();
	private static final String PROJECT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<projectDescription>\r\n"
			+ "	<name>%NAME%</name>\r\n"
			+ "	<comment></comment>\r\n"
			+ "	<projects>\r\n"
			+ "	</projects>\r\n"
			+ "	<buildSpec>\r\n"
			+ "		<buildCommand>\r\n"
			+ "			<name>org.eclipse.jdt.core.javabuilder</name>\r\n"
			+ "			<arguments>\r\n"
			+ "			</arguments>\r\n"
			+ "		</buildCommand>\r\n"
			+ "	</buildSpec>\r\n"
			+ "	<natures>\r\n"
			+ "		<nature>org.eclipse.jdt.core.javanature</nature>\r\n"
			+ "	</natures>\r\n"
			+ "</projectDescription>";
	
	public static final String CLASSPATH = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<classpath>\r\n"
			+ "	<classpathentry kind=\"src\" path=\"src\"/>\r\n"
			+ "	<classpathentry kind=\"src\" path=\"rsc\"/>\r\n"
			+ "	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8\">\r\n"
			+ "		<attributes>\r\n"
			+ "			<attribute name=\"module\" value=\"false\"/>\r\n"
			+ "		</attributes>\r\n"
			+ "	</classpathentry>\r\n"
			+ "	<classpathentry kind=\"output\" path=\".bin\"/>\r\n"
			+ "%INSERT%\r\n"
			+ "</classpath>";
	
	public static final String GITIGNORE = "# Eclipse Files\r\n"
			+ "			libraries/\r\n"
			+ "			.settings/\r\n"
			+ "			.bin/\r\n"
			+ "			.run/\r\n"
			+ "			.runserver/\r\n"
			+ "			.classpath\r\n"
			+ "			.project\r\n"
			+ "			\r\n"
			+ "			# Minecraft Files\r\n"
			+ "			run/\r\n"
			+ "			\r\n"
			+ "			# Export Stuff\r\n"
			+ "			export/";
	
	public static final String CORE_PREFS = "eclipse.preferences.version=1\r\n"
			+ "org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled\r\n"
			+ "org.eclipse.jdt.core.compiler.codegen.methodParameters=do not generate\r\n"
			+ "org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.8\r\n"
			+ "org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve\r\n"
			+ "org.eclipse.jdt.core.compiler.compliance=1.8\r\n"
			+ "org.eclipse.jdt.core.compiler.debug.lineNumber=generate\r\n"
			+ "org.eclipse.jdt.core.compiler.debug.localVariable=generate\r\n"
			+ "org.eclipse.jdt.core.compiler.debug.sourceFile=generate\r\n"
			+ "org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\r\n"
			+ "org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures=disabled\r\n"
			+ "org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\r\n"
			+ "org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures=warning\r\n"
			+ "org.eclipse.jdt.core.compiler.release=disabled\r\n"
			+ "org.eclipse.jdt.core.compiler.source=1.8";
	
	public static final String LIBRARY_NATIVE = "<classpathentry kind=\"lib\" path=\"%PATH%\">\r\n"
			+ "		<attributes>\r\n"
			+ "			<attribute name=\"org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY\" value=\"%NATIVES%\"/>\r\n"
			+ "		</attributes>\r\n"
			+ "	</classpathentry>";
	
	public static final String RUN = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
			+ "<launchConfiguration type=\"org.eclipse.jdt.launching.localJavaApplication\">\r\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"GradleStart\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MODULE_NAME\" value=\"%PROJECT%\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"%PROJECT%\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.WORKING_DIRECTORY\" value=\"${workspace_loc:%PROJECT%}/.run\"/>\r\n"
			+ "</launchConfiguration>";
	
	public static final String LIBRARY = "\t<classpathentry kind=\"lib\" path=\"%PATH%\"/>";
	
	public static void main(String[] args) throws Exception {
		Pong.runPong();
		final File out = new File("project");
		if (out.exists()) Utils.deleteDirectory(out);
		// Obtain versions.json and download dependencies
		VersionJson versions = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL("https://launchermeta.mojang.com/v1/packages/f07e0f1228f79b9b04313fc5640cd952474ba6f5/1.12.2.json")), VersionJson.class);
		ForgeVersionJson forgeversions = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL("https://data.mgnet.work/forge/" + versions.id + ".json")), ForgeVersionJson.class);
		AssetsJson assets = gson.fromJson(Utils.readAllBytesAsStringFromURL(new URL(versions.assetIndex.url)), AssetsJson.class);
		final File libraries = new File(out, "libraries");
		JsonDownloader.downloadDeps(libraries, versions, forgeversions, assets);
		Files.copy(new URL("https://data.mgnet.work/forge/mc-forge-" + versions.id + ".jar").openStream(), new File(libraries, "mc-forge-" + versions.id + ".jar").toPath());
		// Prepare Project File
		Files.write(new File(out, ".project").toPath(), PROJECT.replaceFirst("%NAME%", out.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		// Prepare Classpath File
		String partclasspath = "";
		for (File lib : new File(libraries, "libraries").listFiles()) {
			if (lib.getName().toLowerCase().contains("lwjgl-") || lib.getName().toLowerCase().contains("jinput") || lib.getName().toLowerCase().contains("text2speech"))
				partclasspath += '\t' + LIBRARY_NATIVE.replaceAll("%PATH%", out.toURI().relativize(lib.toURI()).getPath()).replaceAll("%NATIVES%", out.toURI().relativize(new File(out, "libraries/natives").toURI()).getPath());
			else
				partclasspath += LIBRARY.replaceFirst("%PATH%", out.toURI().relativize(lib.toURI()).getPath()) + '\n';
		}
		partclasspath += LIBRARY.replaceFirst("%PATH%", out.toURI().relativize(new File(libraries, "mc-forge-" + versions.id + ".jar").toURI()).getPath()) + '\n';
		Files.write(new File(out, ".classpath").toPath(), CLASSPATH.replaceFirst("%INSERT%", partclasspath.substring(0, partclasspath.length() - 1)).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		// Prepare settings file
		new File(out, ".settings").mkdir();
		Files.write(new File(out, ".settings/org.eclipse.jdt.core.prefs").toPath(), CORE_PREFS.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		// Prepare .gitignore
		Files.write(new File(out, ".gitignore").toPath(), GITIGNORE.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		// Prepare launch files
		Files.write(new File(out, out.getName() + "-" + versions.id + ".launch").toPath(), RUN.replaceAll("%PROJECT%", out.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		Files.write(new File(out, out.getName() + "-" + versions.id + "-server.launch").toPath(), RUN.replaceFirst("GradleStart", "GradleStartServer").replaceAll(Pattern.quote("/.run"), "/.runserver").replaceAll("%PROJECT%", out.getName()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		// Create source directories
		new File(out, "src").mkdir();
		new File(out, "rsc").mkdir();
		// Finally create a build and run directory
		new File(out, ".bin").mkdir();
		new File(out, ".run").mkdir();
		new File(out, ".runserver").mkdir();
		System.exit(0);
	}
	
}

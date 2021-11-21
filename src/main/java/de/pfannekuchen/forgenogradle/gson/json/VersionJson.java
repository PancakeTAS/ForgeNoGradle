
package de.pfannekuchen.forgenogradle.gson.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionJson {

	@SerializedName("assetIndex")
	@Expose
	public AssetIndex assetIndex;
	@SerializedName("assets")
	@Expose
	public String assets;
	@SerializedName("downloads")
	@Expose
	public Downloads downloads;
	@SerializedName("id")
	@Expose
	public String id;
	@SerializedName("javaVersion")
	@Expose
	public JavaVersion javaVersion;
	@SerializedName("libraries")
	@Expose
	public List<Library> libraries = null;
	@SerializedName("mainClass")
	@Expose
	public String mainClass;
	@SerializedName("minecraftArguments")
	@Expose
	public String minecraftArguments;
	@SerializedName("type")
	@Expose
	public String type;

}

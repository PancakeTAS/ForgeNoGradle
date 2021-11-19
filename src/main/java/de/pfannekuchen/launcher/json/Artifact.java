
package de.pfannekuchen.launcher.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Artifact {

	@SerializedName("path")
	@Expose
	public String path;
	@SerializedName("url")
	@Expose
	public String url;

}

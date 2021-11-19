
package de.pfannekuchen.launcher.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DependencyDownloads {

	@SerializedName("artifact")
	@Expose
	public Artifact artifact;
	@SerializedName("classifiers")
	@Expose
	public Classifiers classifiers;

}

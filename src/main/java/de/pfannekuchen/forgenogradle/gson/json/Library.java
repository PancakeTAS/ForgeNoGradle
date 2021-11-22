
package de.pfannekuchen.forgenogradle.gson.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Library {

	@SerializedName("downloads")
	@Expose
	public DependencyDownloads downloads;
	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("rules")
	@Expose
	public List<Rule> rules = null;
	@SerializedName("natives")
	@Expose
	public Natives natives;

}

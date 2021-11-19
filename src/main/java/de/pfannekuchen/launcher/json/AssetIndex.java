
package de.pfannekuchen.launcher.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AssetIndex {

	@SerializedName("id")
	@Expose
	public String id;
	@SerializedName("url")
	@Expose
	public String url;

}

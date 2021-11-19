
package de.pfannekuchen.launcher.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Natives {

	@SerializedName("linux")
	@Expose
	public String linux;
	@SerializedName("osx")
	@Expose
	public String osx;
	@SerializedName("windows")
	@Expose
	public String windows;

}

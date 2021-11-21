
package de.pfannekuchen.forgenogradle.gson.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Classifiers {

	@SerializedName("natives-linux")
	@Expose
	public NativesDownload nativesLinux;
	@SerializedName("natives-osx")
	@Expose
	public NativesDownload nativesOsx;
	@SerializedName("natives-windows")
	@Expose
	public NativesDownload nativesWindows;
	@SerializedName("natives-windows-32")
	@Expose
	public NativesDownload nativesWindows32;
	@SerializedName("natives-windows-64")
	@Expose
	public NativesDownload nativesWindows64;

}


package de.pfannekuchen.forgenogradle.gson.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JavaVersion {

	@SerializedName("majorVersion")
	@Expose
	public Long majorVersion;

}

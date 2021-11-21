
package de.pfannekuchen.forgenogradle.gson.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Client {

	@SerializedName("url")
	@Expose
	public String url;

}

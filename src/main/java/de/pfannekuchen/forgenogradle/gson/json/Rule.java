
package de.pfannekuchen.forgenogradle.gson.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rule {

	@SerializedName("action")
	@Expose
	public String action;
	@SerializedName("os")
	@Expose
	public Os os;

}

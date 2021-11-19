
package de.pfannekuchen.launcher.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Downloads {

	@SerializedName("client")
	@Expose
	public Client client;

}

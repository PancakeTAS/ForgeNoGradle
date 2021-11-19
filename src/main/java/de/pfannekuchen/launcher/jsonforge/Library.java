
package de.pfannekuchen.launcher.jsonforge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Library {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("downloads")
    @Expose
    public Downloads downloads;

}

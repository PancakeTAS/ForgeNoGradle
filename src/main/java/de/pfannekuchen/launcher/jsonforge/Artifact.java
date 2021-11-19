
package de.pfannekuchen.launcher.jsonforge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Artifact {

    @SerializedName("path")
    @Expose
    public String path;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("sha1")
    @Expose
    public String sha1;
    @SerializedName("size")
    @Expose
    public Long size;

}


package de.pfannekuchen.forgenogradle.gson.jsonforge;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForgeVersionJson {

    @SerializedName("_comment_")
    @Expose
    public List<String> comment = null;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("time")
    @Expose
    public String time;
    @SerializedName("releaseTime")
    @Expose
    public String releaseTime;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("mainClass")
    @Expose
    public String mainClass;
    @SerializedName("inheritsFrom")
    @Expose
    public String inheritsFrom;
    @SerializedName("logging")
    @Expose
    public Logging logging;
    @SerializedName("minecraftArguments")
    @Expose
    public String minecraftArguments;
    @SerializedName("libraries")
    @Expose
    public List<Library> libraries = null;

}

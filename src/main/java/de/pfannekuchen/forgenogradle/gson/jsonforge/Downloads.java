
package de.pfannekuchen.forgenogradle.gson.jsonforge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Downloads {

    @SerializedName("artifact")
    @Expose
    public Artifact artifact;

}

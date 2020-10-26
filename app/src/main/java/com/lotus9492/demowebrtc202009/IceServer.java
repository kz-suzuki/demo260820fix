package com.lotus9492.demowebrtc202009;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IceServer {

    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("credential")
    @Expose
    public String credential;

    @Override
    public String toString() {
        return "IceServer{" +
                "urls='" + url + '\'' +
                ", username='" + username + '\'' +
                ", credential='" + credential + '\'' +
                '}';
    }
}

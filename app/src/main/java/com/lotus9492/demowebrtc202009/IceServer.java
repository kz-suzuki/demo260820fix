package com.lotus9492.demowebrtc202009;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IceServer {

    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("urls")
    @Expose
    public List<String> urls = null;
    @SerializedName("credential")
    @Expose
    public String credential;


    @Override
    public String toString() {
        return "IceServer{" +
                "urls='" + urls + '\'' +
                ", username='" + username + '\'' +
                ", credential='" + credential + '\'' +
                '}';
//                "username='" + username + '\'' +
//                ", urls='" + urls + '\'' +
//                ", credential='" + credential + '\'' +
//                '}';
    }
}

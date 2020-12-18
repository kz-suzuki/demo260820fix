package com.lotus9492.demowebrtc202009;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TurnServerPojo {

    @SerializedName("v")
    @Expose
    public V v;
    @SerializedName("s")
    @Expose
    public String s;

    public class V {

        @SerializedName("iceServers")
        @Expose
        public IceServer iceServer;

    }

}






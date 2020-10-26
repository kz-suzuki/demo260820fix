package com.lotus9492.demowebrtc202009;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface TurnServer {

    @PUT("/_turn/MyFirstApp")
    Call<TurnServerPojo> getIceCandidates(@Header("Authorization") String authkey);

}

package com.lotus9492.demowebrtc202009;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface TurnServer {
    @Headers({
            "Content-Type: application/json"
    })
    @PUT("/_turn/MyFirstApp")
    Call<TurnServerPojo> getIceCandidates(@Header("Authorization") String authkey,
                                          @Body RequestBody body);

}

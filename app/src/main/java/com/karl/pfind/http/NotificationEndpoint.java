package com.karl.pfind.http;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotificationEndpoint {

    @Headers({"Accept: application/json"})
    @POST("send-notification")
    Call<String> triggerLostPet(@Query("token") String token, @Query("topic") String topic, @Body Note dataModal);


}

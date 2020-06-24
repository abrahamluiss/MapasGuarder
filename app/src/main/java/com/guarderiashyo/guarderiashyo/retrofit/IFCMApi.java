package com.guarderiashyo.guarderiashyo.retrofit;

import com.guarderiashyo.guarderiashyo.models.FCMBody;
import com.guarderiashyo.guarderiashyo.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAzjAaGig:APA91bHXNu3mmDXEBTgU_8XvFP0qdHpNYh8cMBwO0Vslm2jnvZxkUucSjT920OYZkxQD5Uu2XbqmqPTKgmvrXHylMMgI--Dgk5hb1tOQ1DmufuWn0GHwIuNGN8uHr_ZOGj5M9fA1taNe"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}

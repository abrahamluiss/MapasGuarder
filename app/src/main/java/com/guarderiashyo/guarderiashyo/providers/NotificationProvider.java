package com.guarderiashyo.guarderiashyo.providers;



import com.guarderiashyo.guarderiashyo.models.FCMBody;
import com.guarderiashyo.guarderiashyo.models.FCMResponse;
import com.guarderiashyo.guarderiashyo.retrofit.IFCMApi;
import com.guarderiashyo.guarderiashyo.retrofit.RetrofitClient;

import retrofit2.Call;
public class NotificationProvider {


    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}

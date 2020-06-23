package com.guarderiashyo.guarderiashyo.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleApi {
    @GET
    Call<String> getDirections(@Url String url);
}

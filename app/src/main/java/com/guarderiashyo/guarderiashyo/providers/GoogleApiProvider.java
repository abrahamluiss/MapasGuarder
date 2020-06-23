package com.guarderiashyo.guarderiashyo.providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.retrofit.IGoogleApi;
import com.guarderiashyo.guarderiashyo.retrofit.RetrofitClient;

import java.util.Date;

import retrofit2.Call;



public class GoogleApiProvider {

    private Context context;
    public GoogleApiProvider(Context context){
        this.context = context;

    }
    public Call<String> getDirections(LatLng originLatLng, LatLng destinoLatLng){
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                + "destination=" + destinoLatLng.latitude + "," + destinoLatLng.longitude + "&"
                + "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&"
                + "traffic_model=best_guess&"
                + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitClient.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}

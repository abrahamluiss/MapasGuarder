package com.guarderiashyo.guarderiashyo.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {

    private DatabaseReference mDatabase;
    private GeoFire mGeofire;

    public GeofireProvider (String reference) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(reference);
        mGeofire = new GeoFire(mDatabase);
    }

    public void saveLocation(String idGuarderia, LatLng latLng) {
        mGeofire.setLocation(idGuarderia, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void removeLocation(String idGuarderia) {
        mGeofire.removeLocation(idGuarderia);
    }


    public GeoQuery getActiveGuarderias(LatLng latLng, double radius) {
        GeoQuery geoQuery = mGeofire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference getGuarderiaLocation(String idGuarderia) {
        return mDatabase.child(idGuarderia).child("l");
    }

    public DatabaseReference isGuarderiaWorking(String idGuarderia) {
        return FirebaseDatabase.getInstance().getReference().child("guarderias_working").child(idGuarderia);
    }

}

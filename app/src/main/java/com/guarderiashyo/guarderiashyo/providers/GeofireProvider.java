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

    public GeofireProvider () {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Guarderia_activa");
        mGeofire = new GeoFire(mDatabase);
    }

    public void saveLocation(String idGuarderia, LatLng latLng) {
        mGeofire.setLocation(idGuarderia, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void removeLocation(String idGuarderia) {
        mGeofire.removeLocation(idGuarderia);
    }

/*
    public GeoQuery getActiveDrivers(LatLng latLng, double radius) {
        GeoQuery geoQuery = mGeofire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference getDriverLocation(String idDriver) {
        return mDatabase.child(idDriver).child("l");
    }

    public DatabaseReference isDriverWorking(String idDriver) {
        return FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }
*/
}

package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinoLat;
    private double mExtraDestinoLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinoLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        MyToolbar.show(this, "Tus datos", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinoLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinoLng = getIntent().getDoubleExtra("destination_lng", 0);

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinoLatLng = new LatLng(mExtraDestinoLat, mExtraDestinoLng);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_roja)));
        mMap.addMarker(new MarkerOptions().position(mDestinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_azul)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatLng)
                .zoom(14f)
                .build()
        ));

    }
}

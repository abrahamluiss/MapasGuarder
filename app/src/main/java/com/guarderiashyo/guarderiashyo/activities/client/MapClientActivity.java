package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.InicioActivity;
import com.guarderiashyo.guarderiashyo.activities.guarderia.MapGuarderiaActivity;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {
    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;//actua como bandera para solicitar los permisos de ubicacion
    private final static int SETTINGS_REQUEST_CODE = 2;//actua como bandera para solicitar los permisos de ubicacion

    private Marker mMarker;

    private GeofireProvider mGeofireProvider;
    private LatLng mActualLatLng;

    private List<Marker> mGuarderiaMarcadores = new ArrayList<>();
    private boolean mIsFirstTime = true;



    LocationCallback mLocationCallback = new LocationCallback(){//si se mueve lo registra
        @Override
        public void onLocationResult(LocationResult locationResult){
            for(Location location: locationResult.getLocations()){
                if(getApplicationContext() != null){
                    //obtener la localizacion del cliente en tiempo real
                    if(mMarker != null){
                        mMarker.remove();//elimnar marca si ya esta
                    }
                    mActualLatLng = new LatLng(location.getLatitude(), location.getLongitude());


                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            ).title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                    );
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    if(mIsFirstTime){//solo se ejecutara una vez
                        mIsFirstTime = false;
                        getActiveGuarderias();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);
        MyToolbar.show(this, "Cliente", false);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);


        mGeofireProvider = new GeofireProvider();
        mAuthProvider = new AuthProvider();


        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);//iniciar o detener la ubicacion de user


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_logout){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getActiveGuarderias() {
        mGeofireProvider.getActiveGuarderias(mActualLatLng).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE SE CONECTEN EN LA APLICACION

                for (Marker marker: mGuarderiaMarcadores) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng guarderiaLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(guarderiaLatLng).title("Guarderia disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.madre)));
                marker.setTag(key);
                mGuarderiaMarcadores.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker: mGuarderiaMarcadores) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mGuarderiaMarcadores.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // ACTUALIZAR LA POSICION DE CADA CONDUCTOR
                for (Marker marker: mGuarderiaMarcadores) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocaction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(gpsActivar()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                    }else{
                        mostrarDialogNoGps();
                    }
                }
                else{
                    checkLocationPermissions();
                }
            }
            else{
                checkLocationPermissions();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGS_REQUEST_CODE && gpsActivar()){
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        }else{
            mostrarDialogNoGps();
        }
    }

    private void mostrarDialogNoGps(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Porfavor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        startActivityForResult( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean  gpsActivar(){
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActive = true;
        }
        return isActive;
    }

    private void startLocaction(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(gpsActivar()){
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }else{
                    mostrarDialogNoGps();
                }


            }
            else{
                checkLocationPermissions();
            }
        } else {
            if(gpsActivar()){
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            }
            else{
                mostrarDialogNoGps();
            }
        }
    }

    private  void checkLocationPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    void logout(){
        mAuthProvider.logout();
        Intent i = new Intent(MapClientActivity.this, InicioActivity.class);
        startActivity(i);
        finish();

    }
}

package com.guarderiashyo.guarderiashyo.activities.guarderia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ActivityInfoCompat;

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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.InicioActivity;
import com.guarderiashyo.guarderiashyo.activities.client.MapClientActivity;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;
import com.guarderiashyo.guarderiashyo.providers.TokenProviders;

public class MapGuarderiaActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button btnCerrarSesion;
    AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private TokenProviders mTokenProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;//actua como bandera para solicitar los permisos de ubicacion
    private final static int SETTINGS_REQUEST_CODE = 2;//actua como bandera para solicitar los permisos de ubicacion

    private Marker mMarker;

    private  Button mBtnConectar;

    private  boolean mIsConnect = false;

    private LatLng mActualLatLng;
    ValueEventListener mListener;

    LocationCallback mLocationCallback = new LocationCallback(){//si se mueve lo registra
        @Override
        public void onLocationResult(LocationResult locationResult){
            for(Location location: locationResult.getLocations()){
                if(getApplicationContext() != null){

                    mActualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if(mMarker != null){
                        mMarker.remove();//elimnar marca si ya esta
                    }

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                    ).title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.madre))
                    );
                    //obtener la localizacion del cliente en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    actualizarLocalizacion();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_guarderia);
        MyToolbar.show(this, "Guarderia", false);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);


        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_guarderias");
        mTokenProvider = new TokenProviders();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);//iniciar o detener la ubicacion de user

        mBtnConectar = findViewById(R.id.btnConectar);
        mBtnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsConnect){
                    desconectar();
                }else{
                    startLocaction();
                }
            }
        });

        generateToken();
        isGuarderiWorking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener != null){
            mGeofireProvider.isGuarderiaWorking(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    void isGuarderiWorking(){
        mListener = mGeofireProvider.isGuarderiaWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    desconectar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void actualizarLocalizacion(){
        if(mAuthProvider.existSession() && mActualLatLng != null){
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mActualLatLng);

        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);



        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);//tiempo de actualizacion
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        //startLocaction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(gpsActivar()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        //mMap.setMyLocationEnabled(true); //punto azul

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
            //mMap.setMyLocationEnabled(true); //punto azul

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

    private void desconectar(){

        if(mFusedLocation != null){
            mBtnConectar.setText("Conectarse");
            mIsConnect = false;
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if(mAuthProvider.existSession()){
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }

        }else{
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }
    private void startLocaction(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(gpsActivar()){
                    mBtnConectar.setText("Deconectarse");
                    mIsConnect = true;
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    //mMap.setMyLocationEnabled(true); //punto azul

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
                //mMap.setMyLocationEnabled(true); //punto azul


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
                                ActivityCompat.requestPermissions(MapGuarderiaActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapGuarderiaActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    void logout(){
        //desconectar(); //cuando cierra sesion elimina la ubic de la bd
        mAuthProvider.logout();
        Intent i = new Intent(MapGuarderiaActivity.this, InicioActivity.class);
        startActivity(i);
        finish();

    }
    void generateToken(){

        mTokenProvider.create(mAuthProvider.getId());
    }
}

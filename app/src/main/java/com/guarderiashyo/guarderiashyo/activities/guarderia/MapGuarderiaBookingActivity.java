package com.guarderiashyo.guarderiashyo.activities.guarderia;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.Utils.DecodePoints;
import com.guarderiashyo.guarderiashyo.activities.client.DetailRequestActivity;
import com.guarderiashyo.guarderiashyo.activities.client.RequestGuarderiaActivity;
import com.guarderiashyo.guarderiashyo.models.ClientBooking;
import com.guarderiashyo.guarderiashyo.models.FCMBody;
import com.guarderiashyo.guarderiashyo.models.FCMResponse;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientBookingProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientProvider;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;
import com.guarderiashyo.guarderiashyo.providers.GoogleApiProvider;
import com.guarderiashyo.guarderiashyo.providers.NotificationProvider;
import com.guarderiashyo.guarderiashyo.providers.TokenProviders;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapGuarderiaBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;

    NotificationProvider mNotificationProvider;
    TokenProviders mTokenProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;
    private LatLng mCurrentLatLng;

    TextView txtViewClientBooking, txtViewClientEmail, txtViewOriginBooking, txtViewDestinationBooking;
    String mExtraClientId;

    private LatLng mOriginLatLng;
    private LatLng mDestinoLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private boolean mIsFirstTime = true;
    private boolean mIsCloseToClient = false;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.madre))
                    );
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    updateLocation();

                    if(mIsFirstTime){//solo se ejecutara una vez
                        mIsFirstTime = false;
                        getClientBooking();
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_guarderia_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("guarderias_working");
        mTokenProvider = new TokenProviders();
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mNotificationProvider = new NotificationProvider();

        //mButtonStartBooking.setEnabled(false);//inicia con estado enable.false


        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        txtViewClientBooking = findViewById(R.id.txtViewClientBooking);
        txtViewClientEmail = findViewById(R.id.txtViewEmailClientBooking);
        txtViewOriginBooking = findViewById(R.id.txtViewOriginClientBooking);
        txtViewDestinationBooking = findViewById(R.id.txtViewDestinationClientBooking);
        mButtonStartBooking = findViewById(R.id.btnStartBooking);
        mButtonFinishBooking = findViewById(R.id.btnFinishBooking);

        mExtraClientId = getIntent().getStringExtra("idClient");
        mGoogleApiProvider = new GoogleApiProvider(MapGuarderiaBookingActivity.this);

        getClient();//obtenemos la inf del cliente
        //getClientBooking();//informacion de la solicitud del place


        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsCloseToClient){
                    startBooking();
                } else{
                    startBooking();
                    //Toast.makeText(MapGuarderiaBookingActivity.this, "Debe estar dcerca a la posición", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBooking();
            }
        });
    }

    private void finishBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId, "finish");
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId);
        sendNotification("Ida finalizada");
        if(mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
        mGeofireProvider.removeLocation(mAuthProvider.getId());

        Intent i = new Intent(MapGuarderiaBookingActivity.this, CalificationClientActivity.class);
        i.putExtra("idClient", mExtraClientId);
        startActivity(i);
        finish();
    }
    private void startBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId, "start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Cliente").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_azul)));

        drawRoute(mOriginLatLng);
        sendNotification("Ida iniciada");


    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng guarderLatLng){
        double distance = 0;
        Location clientLocation = new Location("");
        Location guarderLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        guarderLocation.setLatitude(guarderLatLng.latitude);
        guarderLocation.setLongitude(guarderLatLng.longitude);
        distance = clientLocation.distanceTo(guarderLocation);
        return distance;
    }

    void getClientBooking(){
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String destino = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    double destinoLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinoLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinoLatLng = new LatLng(destinoLat, destinoLng);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Cliente").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_roja)));


                    txtViewOriginBooking.setText("Ubicación: "+origin);
                    txtViewDestinationBooking.setText("Destino: "+destino);
                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);//color
                    mPolylineOptions.width(10f);//grosor
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distancia = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaText = distancia.getString("text");
                    String duracionText = duration.getString("text");


                } catch (Exception e){
                    Log.d("Error", "Error encontrado"+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
    void getClient(){
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String email = dataSnapshot.child("email").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    txtViewClientBooking.setText(name);
                    txtViewClientEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
            if(!mIsCloseToClient){
                if(mOriginLatLng != null && mCurrentLatLng != null){
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng); //metros
                    if(distance <= 1000000000){//distancia q debe estar cerca
                        //mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Esta cerca a la posición del cliente", Toast.LENGTH_SHORT).show();
                    }
                }

            }

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                    else {
                        showAlertDialogNOGPS();
                    }
                }
                else {
                    checkLocationPermissions();
                }
            }
            else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived())  {
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void disconnect() {

        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {

                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapGuarderiaBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapGuarderiaBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status) {
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//contiene la inf del nodo del token
                if(dataSnapshot.exists()){
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title","Estado de tu ida");
                    map.put("body","El estado de tu ida es: "+status);
                    map.put("idClient", mAuthProvider.getId());
                    FCMBody fcmBody = new FCMBody(token, "high","4500s", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() != 1){
                                    Toast.makeText(MapGuarderiaBookingActivity.this, "No se envio la notificacion", Toast.LENGTH_SHORT).show();

                                }
                            }
                            else{
                                Toast.makeText(MapGuarderiaBookingActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());

                        }
                    });
                }else{
                    Toast.makeText(MapGuarderiaBookingActivity.this, "No se pudo enviar la notificacion porque la guarderia no tiene un token de sesión", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

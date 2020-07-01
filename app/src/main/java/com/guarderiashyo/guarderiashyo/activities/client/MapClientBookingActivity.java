package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.Utils.DecodePoints;
import com.guarderiashyo.guarderiashyo.activities.guarderia.MapGuarderiaBookingActivity;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientBookingProvider;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;
import com.guarderiashyo.guarderiashyo.providers.GoogleApiProvider;
import com.guarderiashyo.guarderiashyo.providers.GuarderiaProvider;
import com.guarderiashyo.guarderiashyo.providers.TokenProviders;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;

    private GeofireProvider mGeofireProvider;
    private TokenProviders mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private GuarderiaProvider mGuarderiaProvider;


    private Marker mMarkerGuarder;

    private boolean mIsFirstTime = true;

    private PlacesClient mPlaces;

    private String mOrigin;
    private LatLng mOriginLatLng;

    private String mDestination;
    private LatLng mDestinationLatLng;
    private LatLng mGuarderLatLng;

    TextView txtViewClientBooking, txtViewClientEmailBooking, txtViewOriginClientBooking, txtViewDestinationClientBooking;
    TextView mtxtViewStatusBooking;
    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private ValueEventListener mListener;
    private String mIdGuarder;
    private ValueEventListener mListenerStatus;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("guarderias_working");
        mTokenProvider = new TokenProviders();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);
        mGuarderiaProvider = new GuarderiaProvider();


        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        //mPlaces = Places.createClient(this);

        txtViewClientBooking = findViewById(R.id.textViewGuarderBooking);
        txtViewClientEmailBooking = findViewById(R.id.textViewEmailGuarderBooking);
        txtViewOriginClientBooking = findViewById(R.id.txtViewOriginGuarderBooking);
        txtViewDestinationClientBooking = findViewById(R.id.textViewDestinationGuarderBooking);
        mtxtViewStatusBooking = findViewById(R.id.textViewStatusBooking);

        getStatus();

        getClientBooking();

    }
    void getStatus(){
        mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status = dataSnapshot.getValue().toString();
                    if(status.equals("accept")){
                        mtxtViewStatusBooking.setText("Estado: Aceptado");

                    }
                    if(status.equals("start")){
                        mtxtViewStatusBooking.setText("Estado: A espera");
                        startBooking();
                    } else if(status.equals("finish")) {
                        mtxtViewStatusBooking.setText("Estado: Finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void startBooking(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Cliente").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_roja)));
        drawRoute(mOriginLatLng);//destination aqui cambie

    }
    void finishBooking(){
        Intent i = new Intent(MapClientBookingActivity.this, CalificationGuarderiaActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener!=null){
            mGeofireProvider.getGuarderiaLocation(mIdGuarder).removeEventListener(mListener);//despúes q se cierre no siga escuchando la posicion del conductor
        }
        if(mListenerStatus != null){
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListenerStatus);
        }
    }

    void getClientBooking(){
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String destino = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idGuarder = dataSnapshot.child("idGuarderia").getValue().toString();
                    mIdGuarder = idGuarder;
                    double destinoLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinoLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinoLat, destinoLng);
                    txtViewOriginClientBooking.setText("Ubicación: "+origin);
                    txtViewDestinationClientBooking.setText("Destino: "+destino);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Cliente").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_roja)));

                    getGuarderia(idGuarder);
                    getGuarderiaLocation(idGuarder);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void getGuarderia(String idGuarder){
        mGuarderiaProvider.getGuarderia(idGuarder).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    //String servicio = dataSnapshot.child("servicio").getValue().toString();
                    txtViewClientBooking.setText(name);
                    txtViewClientEmailBooking.setText(email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void getGuarderiaLocation(String idGuarder){
        mListener = mGeofireProvider.getGuarderiaLocation(idGuarder).addValueEventListener(new ValueEventListener() {//actualiza la posicion de la guardera
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    mGuarderLatLng = new LatLng(lat, lng);
                    if(mMarkerGuarder != null){
                        mMarkerGuarder.remove();
                    }
                    mMarkerGuarder = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title("La guarderia")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.madre)));
                    if(mIsFirstTime){
                        mIsFirstTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mGuarderLatLng)
                                        .zoom(14f)
                                        .build()
                        ));
                        drawRoute(mOriginLatLng);//aqui para el origen
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirections(mGuarderLatLng, latLng).enqueue(new Callback<String>() {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMyLocationEnabled(true);

    }
}

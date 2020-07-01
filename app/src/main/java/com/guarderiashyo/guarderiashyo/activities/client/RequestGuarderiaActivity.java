package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.Utils.DecodePoints;
import com.guarderiashyo.guarderiashyo.activities.guarderia.MapGuarderiaBookingActivity;
import com.guarderiashyo.guarderiashyo.models.ClientBooking;
import com.guarderiashyo.guarderiashyo.models.FCMBody;
import com.guarderiashyo.guarderiashyo.models.FCMResponse;
import com.guarderiashyo.guarderiashyo.models.Token;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientBookingProvider;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;
import com.guarderiashyo.guarderiashyo.providers.GoogleApiProvider;
import com.guarderiashyo.guarderiashyo.providers.NotificationProvider;
import com.guarderiashyo.guarderiashyo.providers.TokenProviders;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class RequestGuarderiaActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTxtViewLookingFor;
    private Button mBtnCancelRequest;

    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;


    double mRadius = 0.1;
    boolean mGuarderiaFound = false;
    String mIdGuarderiaFound = "";
    LatLng mGuarderiaFoundLatLng;

    String mExtraOrigin, mExtraDestination;
    NotificationProvider mNotificationProvider;
    TokenProviders mtokenProvider;
    ClientBookingProvider mClientBookingProvider;
    AuthProvider mAuthProvider;
    GoogleApiProvider mGoogleApiProvider;
    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_guarderia);
        mAnimation = findViewById(R.id.animation);
        mTxtViewLookingFor = findViewById(R.id.txtViewLookingFor);
        mBtnCancelRequest = findViewById(R.id.cancelRequest);

        mAnimation.playAnimation();

        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng",0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng",0);
        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);


        mGeofireProvider = new GeofireProvider("active_guarderias");
        mtokenProvider = new TokenProviders();
        mNotificationProvider = new NotificationProvider();

        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestGuarderiaActivity.this);
        getClosesGuarderias();
    }

    private void getClosesGuarderias(){

        mGeofireProvider.getActiveGuarderias(mOriginLatLng,mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!mGuarderiaFound){
                    mGuarderiaFound = true;
                    mIdGuarderiaFound = key;
                    mGuarderiaFoundLatLng = new LatLng(location.latitude, location.longitude);
                    mTxtViewLookingFor.setText("Guarderia Disponible\n Esperando respuesta");

                    createClientBooking();
                    Log.d("Guarderia", "ID: "+mIdGuarderiaFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //Ingresa cuando termina la busqueda de la guarderia en un radio de 0.1km
                if(!mGuarderiaFound){
                    mRadius = mRadius + 0.2f;//se incrementa el radio si no se encuentra
                    //No encontro ninguna guarderia
                    if(mRadius > 5){
                        mTxtViewLookingFor.setText("No se encontro una guarderia disponible");
                        Toast.makeText(RequestGuarderiaActivity.this, "No se encontro una guarderia disponible", Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        getClosesGuarderias();//vuelve a ser la busqueda con un radio mas grande
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    void createClientBooking(){

        mGoogleApiProvider.getDirections(mOriginLatLng, mGuarderiaFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distancia = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaText = distancia.getString("text");
                    String duracionText = duration.getString("text");

                    sendNotification(duracionText, distanciaText);


                } catch (Exception e){
                    Log.d("Error", "Error encontrado"+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void sendNotification(final String time, final String km) {
        mtokenProvider.getToken(mIdGuarderiaFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//contiene la inf del nodo del token
                if(dataSnapshot.exists()){
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title","SOLICITUD DE SERVICIO A "+ time + "DE TU POSICIÓN");
                    map.put("body","Un cliente esta solicitando el servicio a una distancia de "+km);
                    map.put("idClient", mAuthProvider.getId());
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s",map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1){
                                    ClientBooking clientBooking = new ClientBooking(
                                            mAuthProvider.getId(),
                                            mIdGuarderiaFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng

                                    );
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            checkStatusClientBooking();
                                            //Toast.makeText(RequestGuarderiaActivity.this, "La peticion se creo correctamente", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Toast.makeText(RequestGuarderiaActivity.this, "Notificación enviada", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(RequestGuarderiaActivity.this, "No se envio la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(RequestGuarderiaActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());

                        }
                    });
                }else{
                    Toast.makeText(RequestGuarderiaActivity.this, "No se pudo enviar la notificacion porque la guarderia no tiene un token de sesión", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void checkStatusClientBooking() {
        mListener = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("accept")) {
                        Intent intent = new Intent(RequestGuarderiaActivity.this, MapClientBookingActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (status.equals("cancel")) {
                        Toast.makeText(RequestGuarderiaActivity.this, "La guarderia no acepto", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RequestGuarderiaActivity.this, MapClientActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
}

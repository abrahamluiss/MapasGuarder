package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.models.FCMBody;
import com.guarderiashyo.guarderiashyo.models.FCMResponse;
import com.guarderiashyo.guarderiashyo.models.Token;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;
import com.guarderiashyo.guarderiashyo.providers.NotificationProvider;
import com.guarderiashyo.guarderiashyo.providers.TokenProviders;

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
    private LatLng mOriginLatLng;

    double mRadius = 0.1;
    boolean mGuarderiaFound = false;
    String mIdGuarderiaFound = "";
    LatLng mGuarderiaFoundLatLng;

    NotificationProvider mNotificationProvider;
    TokenProviders mtokenProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_guarderia);
        mAnimation = findViewById(R.id.animation);
        mTxtViewLookingFor = findViewById(R.id.txtViewLookingFor);
        mBtnCancelRequest = findViewById(R.id.cancelRequest);

        mAnimation.playAnimation();

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng",0);
        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);

        mGeofireProvider = new GeofireProvider();

        mNotificationProvider = new NotificationProvider();
        mtokenProvider = new TokenProviders();
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

                    sendNotification();
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

    private void sendNotification() {
        mtokenProvider.getToken(mIdGuarderiaFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//contiene la inf del nodo del token
                if(dataSnapshot.exists()){
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title","SOLICITUD DE SERVICIO");
                    map.put("body","Un cliente esta solicitando el servicio");
                    FCMBody fcmBody = new FCMBody(token, "high", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1){
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
}

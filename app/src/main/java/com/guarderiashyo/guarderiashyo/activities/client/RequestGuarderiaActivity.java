package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.providers.GeofireProvider;

import java.security.Key;

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
}

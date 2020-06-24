package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.Utils.DecodePoints;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;
import com.guarderiashyo.guarderiashyo.providers.GoogleApiProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinoLat;
    private double mExtraDestinoLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinoLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private TextView txtViewOrigin, txtViewDestino, txtViewTime, txtViewDistancia;
    private String mExtraOrigin, mExtraDestino;

    private Button mBtnRequest;

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
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestino = getIntent().getStringExtra("destino");


        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinoLatLng = new LatLng(mExtraDestinoLat, mExtraDestinoLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);

        txtViewOrigin = findViewById(R.id.txtViewOrigin);
        txtViewDestino = findViewById(R.id.txtViewDestino);
        txtViewTime = findViewById(R.id.txtViewTime);
        txtViewDistancia = findViewById(R.id.txtViewDistancia);
        mBtnRequest = findViewById(R.id.btnRequestNow);

        txtViewOrigin.setText(mExtraOrigin);
        txtViewDestino.setText(mExtraDestino);

        mBtnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestGuarderia();
            }
        });
    }

    private void goToRequestGuarderia() {
        Intent i = new Intent(DetailRequestActivity.this, RequestGuarderiaActivity.class);
        i.putExtra("origin_lat", mOriginLatLng.latitude);
        i.putExtra("origin_lng", mOriginLatLng.longitude);
        i.putExtra("origin", mExtraOrigin);
        i.putExtra("destination", mExtraDestino);
        i.putExtra("destination_lat", mDestinoLatLng.latitude);
        i.putExtra("destination_lng", mDestinoLatLng.longitude);
        startActivity(i);
        finish();

    }

    private void drawRoute(){
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinoLatLng).enqueue(new Callback<String>() {
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
                    txtViewTime.setText(duracionText);
                    txtViewDistancia.setText(distanciaText);

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


        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_roja)));
        mMap.addMarker(new MarkerOptions().position(mDestinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.bandera_azul)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatLng)
                .zoom(14f)
                .build()
        ));

        drawRoute();
    }
}

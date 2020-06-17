package com.guarderiashyo.guarderiashyo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.client.MapClientActivity;
import com.guarderiashyo.guarderiashyo.activities.guarderia.MapGuarderiaActivity;

public class InicioActivity extends AppCompatActivity {
    Button tengoGuarderia, buscoGuarderia;
    SharedPreferences mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        final SharedPreferences.Editor editor = mPref.edit();


        tengoGuarderia = findViewById(R.id.btnTengoGuard);
        buscoGuarderia = findViewById(R.id.btnBuscoGuard);

        buscoGuarderia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user", "client");
                editor.apply();
                irASeleccionarAuth();
            }
        });
        tengoGuarderia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user", "guarderia");
                editor.apply();
                irASeleccionarAuth();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String user = mPref.getString("user", "");
            if(user.equals("client")){
                Intent i = new Intent(InicioActivity.this, MapClientActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando se registre ya no podra ir al atras
                startActivity(i);
            }else{
                Intent i = new Intent(InicioActivity.this, MapGuarderiaActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando se registre ya no podra ir al atras
                startActivity(i);
            }
        }
    }

    private void irASeleccionarAuth() {
        Intent i = new Intent(InicioActivity.this, SelectOptionAuthActivity.class);
        startActivity(i);
    }

}

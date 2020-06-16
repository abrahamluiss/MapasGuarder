package com.guarderiashyo.guarderiashyo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    private void irASeleccionarAuth() {
        Intent i = new Intent(InicioActivity.this, SelectOptionAuthActivity.class);
        startActivity(i);
    }

}

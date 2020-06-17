package com.guarderiashyo.guarderiashyo.activities.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.InicioActivity;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;

public class MapClientActivity extends AppCompatActivity {
    Button btnCerrarSesion;
    AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        mAuthProvider = new AuthProvider();
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthProvider.logout();
                Intent i = new Intent(MapClientActivity.this, InicioActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}

package com.guarderiashyo.guarderiashyo.activities;

import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.client.RegisterActivity;
import com.guarderiashyo.guarderiashyo.activities.guarderia.RegisterGuardeActivity;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;

public class SelectOptionAuthActivity extends AppCompatActivity {


    Button mBotonIrALogin, mBotonIrARegistro;
    SharedPreferences mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        MyToolbar.show(this,"Seleccione una opcion", true);

        mBotonIrALogin = findViewById(R.id.btnIrLogin);
        mBotonIrALogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irALogin();
            }
        });
        mBotonIrARegistro = findViewById(R.id.btnIrRegistro);
        mBotonIrARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irARegistro();
            }
        });

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
    }

    private void irALogin() {
        Intent i = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void irARegistro() {
        String tupoUsuario = mPref.getString("user", "");
        if(tupoUsuario.equals("client"))
        {
            Intent i = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
            startActivity(i);
        }else{
            Intent i = new Intent(SelectOptionAuthActivity.this, RegisterGuardeActivity.class);
            startActivity(i);
        }

    }

}

package com.guarderiashyo.guarderiashyo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.guarderiashyo.guarderiashyo.includes.MyToolbar;

public class SelectOptionAuthActivity extends AppCompatActivity {


    Button mBotonIrALogin, mBotonIrARegistro;
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
    }

    private void irALogin() {
        Intent i = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void irARegistro() {
        Intent i = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
        startActivity(i);
    }

}

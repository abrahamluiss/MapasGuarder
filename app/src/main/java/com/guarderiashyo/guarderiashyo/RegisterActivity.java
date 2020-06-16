package com.guarderiashyo.guarderiashyo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    SharedPreferences mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        String seleccionarUser = mPref.getString("user", "");
        Toast.makeText(this, "El valor es: "+seleccionarUser, Toast.LENGTH_SHORT).show();

    }
}

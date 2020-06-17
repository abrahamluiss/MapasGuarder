package com.guarderiashyo.guarderiashyo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.client.MapClientActivity;
import com.guarderiashyo.guarderiashyo.activities.client.RegisterActivity;
import com.guarderiashyo.guarderiashyo.activities.guarderia.MapGuarderiaActivity;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences mPref;
    EditText mTxtInputEmail, mTxtInputPassword;
    Button mBotonLogin;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyToolbar.show(this,"Login de usuario", true);

        mTxtInputEmail = findViewById(R.id.txtInputEmail);
        mTxtInputPassword = findViewById(R.id.txtInputPassword);
        mBotonLogin = findViewById(R.id.btnLogin);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDialog = new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momento").build();

        mBotonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String email = mTxtInputEmail.getText().toString();
        String password = mTxtInputPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty()){ //si el email y el pass no esta vacio
            if(password.length() >= 6){//si el pass tiene al menos 6 caracteres
                mDialog.show();
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String user = mPref.getString("user", "");
                            if(user.equals("client")){
                                Intent i = new Intent(LoginActivity.this, MapClientActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando se registre ya no podra ir al atras
                                startActivity(i);
                            }else{
                                Intent i = new Intent(LoginActivity.this, MapGuarderiaActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando se registre ya no podra ir al atras
                                startActivity(i);
                            }
                            //Toast.makeText(LoginActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "La contraseña es incorrecta", Toast.LENGTH_SHORT).show();
                        }
                        mDialog.dismiss();
                    }
                });
            }
            else{
                Toast.makeText(this, "La contraseña debe tener mas de 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "La contraseña y el email son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }
}

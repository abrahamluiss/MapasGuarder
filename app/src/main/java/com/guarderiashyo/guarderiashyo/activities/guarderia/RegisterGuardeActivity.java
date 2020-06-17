package com.guarderiashyo.guarderiashyo.activities.guarderia;

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
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.client.RegisterActivity;
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;
import com.guarderiashyo.guarderiashyo.models.Client;
import com.guarderiashyo.guarderiashyo.models.Guarderia;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientProvider;
import com.guarderiashyo.guarderiashyo.providers.GuarderiaProvider;

public class RegisterGuardeActivity extends AppCompatActivity {



    AuthProvider mAuthProvider;
    GuarderiaProvider mGuarderiaProvider;

    AlertDialog mDialog;
    //Views
    Button mBtnRegister;
    EditText txtInputNombre, txtInputEmail, txtInputPass, txtInputRuc, txtInputTrabajadores;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_guarder);
        MyToolbar.show(this,"Registro de guarderia", true);

        mAuthProvider = new AuthProvider();
        mGuarderiaProvider = new GuarderiaProvider();






        mDialog = new SpotsDialog.Builder().setContext(RegisterGuardeActivity.this).setMessage("Espere un momento").build();


        //Toast.makeText(this, "El valor es: "+seleccionarUser, Toast.LENGTH_SHORT).show();
        mBtnRegister = findViewById(R.id.btnRegister);
        txtInputNombre = findViewById(R.id.txtInputNombre);
        txtInputEmail = findViewById(R.id.txtInputEmail);
        txtInputPass = findViewById(R.id.txtInputPassword);
        txtInputRuc = findViewById(R.id.txtInputRuc);
        txtInputTrabajadores = findViewById(R.id.txtInputTrabajadores);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegister();

            }
        });

    }

    void clickRegister() {
        final String name = txtInputNombre.getText().toString();
        final String email = txtInputEmail.getText().toString();
        final String ruc = txtInputRuc.getText().toString();
        final String trabajadores = txtInputTrabajadores.getText().toString();
        final String password = txtInputPass.getText().toString();
        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !ruc.isEmpty() && !trabajadores.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
                register(name,email, password, ruc, trabajadores);

            }else{
                Toast.makeText(this, "min 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }
    void register(final String name, final String email,final String password, final String ruc, final String trabajadores ){
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Guarderia guarderia =  new Guarderia(id, name, email, ruc, trabajadores);
                    create(guarderia);
                }else{
                    Toast.makeText(RegisterGuardeActivity.this, "No se pudo registrar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Guarderia guarderia){
        mGuarderiaProvider.create(guarderia).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    //Toast.makeText(RegisterGuardeActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(RegisterGuardeActivity.this, MapGuarderiaActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//cuando se registre ya no podra ir al atras
                    startActivity(i);

                }
                else{
                    Toast.makeText(RegisterGuardeActivity.this, "No se pudo crear", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

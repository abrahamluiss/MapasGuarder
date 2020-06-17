package com.guarderiashyo.guarderiashyo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
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
import com.guarderiashyo.guarderiashyo.includes.MyToolbar;
import com.guarderiashyo.guarderiashyo.models.User;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences mPref;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    AlertDialog mDialog;
    //Views
    Button mBtnRegister;
    EditText txtInputNombre, txtInputEmail, txtInputPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        MyToolbar.show(this,"Registro", true);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();//referencia al nodo principal de firebase


        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();


        //Toast.makeText(this, "El valor es: "+seleccionarUser, Toast.LENGTH_SHORT).show();
        mBtnRegister = findViewById(R.id.btnRegister);
        txtInputNombre = findViewById(R.id.txtInputNombre);
        txtInputEmail = findViewById(R.id.txtInputEmail);
        txtInputPass = findViewById(R.id.txtInputPassword);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

    }

    void registerUser() {
        final String name = txtInputNombre.getText().toString();
        final String email = txtInputEmail.getText().toString();
        String password = txtInputPass.getText().toString();
        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mDialog.hide();
                        if(task.isSuccessful()){
                            String id = mAuth.getCurrentUser().getUid();
                            saveUser(id,name, email);
                        }else{
                            Toast.makeText(RegisterActivity.this, "No se pudo registrar", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Toast.makeText(this, "min 6 carac", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void saveUser(String id,String name, String email){
        String seleccionarUser = mPref.getString("user", "");
        User user = new User();
        user.setEmail(email);
        user.setName(name);

        if(seleccionarUser.equals("guarderia")){

            mDatabase.child("Users").child("guarderias").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Fallor registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(seleccionarUser.equals("client")){
            mDatabase.child("Users").child("clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Fallor registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}

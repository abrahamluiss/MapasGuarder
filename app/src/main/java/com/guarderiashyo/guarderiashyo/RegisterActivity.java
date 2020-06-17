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
import com.guarderiashyo.guarderiashyo.models.Client;
import com.guarderiashyo.guarderiashyo.models.User;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;
import com.guarderiashyo.guarderiashyo.providers.ClientProvider;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences mPref;
    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;

    AlertDialog mDialog;
    //Views
    Button mBtnRegister;
    EditText txtInputNombre, txtInputEmail, txtInputPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        MyToolbar.show(this,"Registro", true);

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();




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
                clickRegister();

            }
        });

    }

    void clickRegister() {
        final String name = txtInputNombre.getText().toString();
        final String email = txtInputEmail.getText().toString();
        final String password = txtInputPass.getText().toString();
        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
                register(name,email, password);

            }else{
                Toast.makeText(this, "min 6 carac", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }
    void register(final String name, final String email,final String password){
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Client client =  new Client(id, name, email);
                    create(client);
                }else{
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Client client){
        mClientProvider.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(RegisterActivity.this, "No se pudo crear", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
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
    */

}

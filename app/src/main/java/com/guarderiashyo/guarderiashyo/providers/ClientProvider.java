package com.guarderiashyo.guarderiashyo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guarderiashyo.guarderiashyo.models.Client;

import java.util.HashMap;
import java.util.Map;

public class ClientProvider {
    DatabaseReference mDatabase;
    public ClientProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("clients");//referencia al nodo principal de firebase

    }
    public Task<Void> create(Client client){
        Map<String, Object> map = new HashMap<>();
        map.put("name", client.getName());
        map.put("email", client.getEmail());

        return  mDatabase.child(client.getId()).setValue(map);
    }
    public DatabaseReference getClient(String idClient){
        return  mDatabase.child(idClient);
    }
}

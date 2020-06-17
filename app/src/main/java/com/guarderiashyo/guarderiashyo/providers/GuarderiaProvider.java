package com.guarderiashyo.guarderiashyo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guarderiashyo.guarderiashyo.models.Guarderia;

import java.util.HashMap;
import java.util.Map;


public class GuarderiaProvider {
    DatabaseReference mDatabase;
    public GuarderiaProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("guarderias");//referencia al nodo principal de firebase

    }
    public Task<Void> create(Guarderia guarderia){
        Map<String, Object> map = new HashMap<>();
        map.put("name", guarderia.getName());
        map.put("email", guarderia.getEmail());
        map.put("ruc", guarderia.getRuc());
        map.put("trabajadores", guarderia.getTrabajadores());

        return  mDatabase.child(guarderia.getId()).setValue(map);
    }
}

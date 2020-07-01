package com.guarderiashyo.guarderiashyo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guarderiashyo.guarderiashyo.models.ClientBooking;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {
    private DatabaseReference mDatabase;

    public ClientBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create(ClientBooking clientBooking) {
        return mDatabase.child(clientBooking.getIdClient()).setValue(clientBooking);
    }

    public Task<Void> updateStatus(String idClientBooking, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }
    public Task<Void> updateIdHistoryBooking(String idClientBooking) {
        String idPush = mDatabase.push().getKey();//genera un identificador unico en la bd
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    public DatabaseReference getStatus(String idClientBooking) {
        return mDatabase.child(idClientBooking).child("status");
    }
    public DatabaseReference getClientBooking(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }
}


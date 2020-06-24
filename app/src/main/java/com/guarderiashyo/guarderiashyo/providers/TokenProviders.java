package com.guarderiashyo.guarderiashyo.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.guarderiashyo.guarderiashyo.models.Token;

public class TokenProviders {

    DatabaseReference mDatabase;

    public TokenProviders() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(final String idUser) {
        if (idUser == null) return;
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token token = new Token(instanceIdResult.getToken());
                mDatabase.child(idUser).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUser) {
        return mDatabase.child(idUser);
    }


}

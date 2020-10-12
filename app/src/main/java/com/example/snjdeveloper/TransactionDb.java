package com.example.snjdeveloper;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Objects;

public class TransactionDb {
    public FirebaseApp loadFirebase(Context context, String... dbName) {
        String db;
        if (dbName.length == 0)
            db = "billrec-transactions";
        else db = dbName[0];
        String name = String.valueOf(System.currentTimeMillis());
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(db)
                .setApplicationId("1:495792582566:android:b13403452166ec7f3a6967")
                .setApiKey("AIzaSyBSSWEGRjUmb5_nT3nVUFnB_bIdvW_O0mI")
                .setDatabaseUrl("https://" + db + ".firebaseio.com/")
                .setStorageBucket(db+".appspot.com")
                .build();// Initialize with secondary app
        FirebaseApp.initializeApp(Objects.requireNonNull(context) /* Context */, options, name);
        return FirebaseApp.getInstance(name);
    }

    public DatabaseReference getReference(Context context) {
        return FirebaseDatabase.
                getInstance(loadFirebase(context))
                .getReference()
                .child("Transactions")
                ;
    }

    public DatabaseReference getDatabaseReference(Context context) {
        return FirebaseDatabase.
                getInstance(loadFirebase(context))
                .getReference()
                ;
    }
    public FirebaseStorage getStorageReference(Context context) {
        return FirebaseStorage.getInstance(loadFirebase(context))
                ;
    }
    public DatabaseReference getDatabaseReference(Context context, String name) {
        return FirebaseDatabase.
                getInstance(loadFirebase2(context, name))
                .getReference()
                ;
    }
    public FirebaseStorage getStorageReference(Context context, String name) {
        return FirebaseStorage.getInstance(loadFirebase2(context, name))
                ;
    }
    public FirebaseApp loadFirebase2(Context context, String... dbName) {
        String db;
        if (dbName.length == 0)
            db = "billrec-transactions";
        else db = dbName[0];
        String name = String.valueOf(System.currentTimeMillis());
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(db)
                .setApplicationId("1:138409351874:android:29a8910af25978b3ffb95b")
                .setApiKey("AIzaSyCEhQilGGQThhfgzzT5iPJtUJ6CZTcPceM")
                .setDatabaseUrl("https://" + db + ".firebaseio.com/")
                .setStorageBucket(db+".appspot.com")
                .build();// Initialize with secondary app
        FirebaseApp.initializeApp(Objects.requireNonNull(context) /* Context */, options, name);
        return FirebaseApp.getInstance(name);
    }
}

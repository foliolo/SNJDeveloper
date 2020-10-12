package com.example.snjdeveloper.storage;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.snjdeveloper.R;
import com.example.snjdeveloper.TransactionDb;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class StorageFileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_images);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Storage");
        findViewById(R.id.progressbar).setVisibility(View.GONE);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon()
                .setColorFilter(ContextCompat.getColor(this,R.color.black), PorterDuff.Mode.SRC_ATOP);
       loadAllDb();
    }

    private void loadAllDb() {
        String[] dbName=new String[]{"Billrec","Billrec-transactions"};
        ArrayAdapter<String> adapter1=new ArrayAdapter<>(this,
                R.layout.textview_layout,dbName);
        ListView recyclerView=findViewById(R.id.listview);
        recyclerView.setAdapter(adapter1);
        recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("CLicked","Item "+dbName[position]);
                initiateFragment(dbName[position]);
            }
        });
    }

    private void initiateFragment(String dbName) {
        FirebaseStorage storage;
        if (dbName.contains("trans"))
            storage=new TransactionDb().getStorageReference(this);
        else storage=FirebaseStorage.getInstance();
        StorageReference storageReference=storage.getReference();
        Fragment fragmentClass =new StorageFileFragment(storageReference,"");
        FragmentTransaction fragmentTransaction2;
        fragmentTransaction2= getSupportFragmentManager().beginTransaction();
        try{ fragmentTransaction2.replace(R.id.frame, Objects.requireNonNull(fragmentClass));
            fragmentTransaction2.commit();
        }catch (Exception ignored){}

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

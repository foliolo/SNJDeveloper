package com.example.snjdeveloper;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snjdeveloper.extra.Utility;
import com.example.snjdeveloper.storage.ImageAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class ServerImagesManagement extends AppCompatActivity implements AdvanceViewHolder.ItemClickListener {
    private RecyclerView recyclerView;
    private int reqCode;
    private String folderName;
    ArrayList<String>images;
    private ImageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_images);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon()
                .setColorFilter(ContextCompat.getColor(this,R.color.black), PorterDuff.Mode.SRC_ATOP);
        recyclerView=findViewById(R.id.recyclerview);
        images=new ArrayList<>();
        receive();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void receive() {
        Intent intent=getIntent();
        reqCode= intent.getIntExtra("RequestCode",-1);
        folderName=intent.getStringExtra("FOLDER_NAME");
        String dbName = intent.getStringExtra("DB_NAME");
        StorageReference storageReference;
        setTitle(folderName.substring(0,1).toUpperCase()+folderName.substring(1).toLowerCase());
        if (reqCode==-1){
            if (dbName==null)//default db
            storageReference= FirebaseStorage.getInstance().getReference(folderName);
            else //transaction db
            storageReference= new TransactionDb()
                    .getStorageReference(this)
                    .getReference().child(folderName);
            loadIconsToSelect(storageReference);
        }
    }

    private void loadIconsToSelect(StorageReference storageReference) {
        configureRecyclerview();
        storageReference.listAll().addOnSuccessListener(listResult -> {
            int size=listResult.getItems().size();
            final int[] count = {0};
            for (StorageReference st:listResult.getItems()){
                st.getDownloadUrl().addOnSuccessListener(uri -> {
                    findViewById(R.id.progressbar).setVisibility(View.GONE);
                    count[0]++;
                    Log.e("Url","->"+uri);
                    images.add(uri.toString());
                    if (count[0]==size)
                        adapter.notifyDataSetChanged();

                });
            }
        });
    }

    private void configureRecyclerview() {
        adapter=new ImageAdapter(this,images,R.layout.image);
        int column;
        if (folderName.toLowerCase().contains("app"))
            column=1;
        else column=Utility.calculateNoOfColumns(this,150);
        recyclerView.setLayoutManager(new GridLayoutManager(this, column));
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.e("ServerImages","item clicked pos "+position);
        Intent intent=new Intent();
        intent.putExtra("DATA",images.get(position));
//        if (reqCode instanceof Integer)
            intent.putExtra("EXTRA",(int)getIntent().getIntExtra("DATA",-1));
        setResult(reqCode,intent);
        onBackPressed();
        Log.e("ServerImages","Sending data back \nextra->"+"\nIntent->"+intent.toUri(0));
    }



}

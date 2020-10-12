package com.example.snjdeveloper.extra;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class FileSelector extends AppCompatActivity {
    String type;
    Object extra;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        String from=intent.getStringExtra("FROM");
        HashMap<String,Object>map;
       try {
           if (from!=null&&from.equals("AppUpdate")){
                extra=intent.getIntExtra("DATA",-1);
           }
           type=intent.getStringExtra("TYPE");
           Log.e("AppUpdate","Received\nExtra->"+extra);
           showImageChooser();
       }catch (Exception e){e.printStackTrace();}
    }

    private void showImageChooser() {
        Log.e("Type","->"+type);
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent ;
            if (getIntent().getIntExtra("RequestCode",101)==101)
            {
//                intent= new Intent(Intent.ACTION_PICK, Uri.parse(type));
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType(type);
                intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }
            else {Intent chooseIntent;
                chooseIntent =new Intent(Intent.ACTION_OPEN_DOCUMENT);
                chooseIntent.setType(type);
                chooseIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                chooseIntent.addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent = Intent.createChooser(chooseIntent, "Choose a file");
            }
            startActivityForResult(intent, 102);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE},102);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("FileSelector","Inside activity result");
        if (requestCode == 102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri link = data.getData();
            Intent intent=new Intent();
            intent.putExtra("DATA",link.toString());
            if (extra instanceof Integer)
            intent.putExtra("EXTRA",(int)extra);
            setResult(getIntent().getIntExtra("RequestCode",101),intent);
            Log.e("FileSelector","Sending data back \nextra->"+extra+"\nData->"+link+"\nIntent->"+intent.toUri(0));
        }
        onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            showImageChooser();
        }
    }

}

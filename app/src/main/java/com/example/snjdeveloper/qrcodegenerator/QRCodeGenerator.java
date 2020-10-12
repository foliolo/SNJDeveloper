package com.example.snjdeveloper.qrcodegenerator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;

public class QRCodeGenerator {

    private static final String TAG = "QRGenerator";

    public QRCodeGenerator(String data, String id, Activity activity){
       generateQR(data,id,activity);
   }
    private void generateQR(String data, String id, Activity activity) {

        Bitmap bitmap = QRCodeHelper
                .newInstance(activity)
                .setContent(data)
                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
                .setMargin(2)
                .getQRCOde();
        getImageData(bitmap,id);


    }
       


    private void getImageData(Bitmap bmp, String id) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        bmp.recycle();
        byte[] byteArray = bao.toByteArray();
        String imageB64 = Base64.encodeToString(byteArray, Base64.URL_SAFE);
        Base64.decode(imageB64, Base64.URL_SAFE);

        //  store & retrieve this string which is URL safe(can be used to store in FBDB) to firebase
        Log.d(TAG,"Uploading started for storage");
        uploadImage(byteArray,id);
    }
    private void uploadImage(byte[] byteArray, String id){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("files/"+id);
     storageRef.putBytes(byteArray).addOnFailureListener(exception -> {
         // Handle unsuccessful uploads
         Log.d(TAG,"Image uploaded fail");
     }).addOnSuccessListener(taskSnapshot -> {
         Log.d(TAG,"Image uploaded");


     });

    }

}

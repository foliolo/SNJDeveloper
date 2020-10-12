package com.example.snjdeveloper.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.snjdeveloper.AllCustomerActivity;
import com.example.snjdeveloper.AppUpdateActivity;
import com.example.snjdeveloper.ChangeLoginCredentialsActivity;
import com.example.snjdeveloper.LoginActivity;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.StatsActivity;
import com.example.snjdeveloper.StatsBottleActivity;
import com.example.snjdeveloper.messages.ProblemReports;
import com.example.snjdeveloper.notification.NotificationActivity;
import com.example.snjdeveloper.qrcode.LiveBarcodeScanningActivity;
import com.example.snjdeveloper.search.SearchActivity;
import com.example.snjdeveloper.settings.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class HomeAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);
        checkNotification();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void checkNotification()
        {
            FirebaseDatabase.getInstance().getReference("Developers/Notification").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        String html= (String) dataSnapshot.child("HTML").getValue();
                        if (html!=null)
                        showNotificationDialog(html,(int)((long)dataSnapshot.child("WIDTH").getValue()),
                                (int)((long)dataSnapshot.child("HEIGHT").getValue()),
                        (int)((long)dataSnapshot.child("HTML_WIDTH").getValue()),
                                (int)((long)dataSnapshot.child("HTML_HEIGHT").getValue()),
                                (String)dataSnapshot.child("ACTION").getValue());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    @SuppressLint("SetJavaScriptEnabled")
    private  void showNotificationDialog(String html, int width, int height,int h_w,int h_h,String action){

        View dialogView = View.inflate( HomeAdmin.this, R.layout.activity_notification, null);
        final Dialog dialog = new Dialog(HomeAdmin.this,R.style.Dialog1);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width =dpToPx(width);//WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height =dpToPx(height);// WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        WebView webView=dialogView.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getLayoutParams().height=dpToPx(h_h);
        webView.getLayoutParams().width=dpToPx(h_w);
        String encode= Base64.encodeToString(html.getBytes(),Base64.NO_PADDING);
        webView.loadData(encode,"text/html","base64");
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(dialogInterface -> {
            if(action!=null)
            {   String[] allActions=action.split(",");
                for (String act :
                        allActions)
                    checkAction(dialog, act);

            }
            else checkAction(dialog,"");
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.dismiss());
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {

                dialog.dismiss();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    private void checkAction(Dialog dialog,String action) {

        switch (action) {
            case "ACTION_EXIT":
                finishAffinity();
                break;
            case "ACTION_OPEN_ORDERS":
                orderOnClick(new View(this));
                break;
            case "ACTION_OPEN_TRANSACTIONS":
                transactionOnClick(new View(this));
                break;
            case "ACTION_OPEN_BOTTLES":
                BottleOnClick(new View(this));
                break;
            case "ACTION_OPEN_NEW":
                newOnClick(new View(this));
                break;
            case "ACTION_OPEN_ADMIN_INFO":
                userinfoOnClicked(new View(this));
                break;
            case "ACTION_OPEN_CUSTOMERS":
                customersOnClicked(new View(this));
                break;
            case "ACTION_OPEN_SETTING":
                settingsOnClick(new View(this));
                break;
            case "ACTION_DELETE_NOTIFICATION":
                FirebaseDatabase.getInstance()
                        .getReference("Developers/Notification").setValue(null);
                break;

            case "ACTION_LOGIN":
                clearPreferences("CRED");
                clearPreferences("RECENT");
                clearPreferences("RECENT_SEARCH");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("admin");

                FirebaseDatabase.getInstance()
                        .getReference("Developers/Notification").setValue(null);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                break;
            case "ACTION_CHANGE_PASSWORD":
                Intent intent1 = new Intent(this, ChangeLoginCredentialsActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent1.putExtra("CHANGE", "Password");
                startActivity(intent1);
                break;
            case "ACTION_CHANGE_EMAIL":
                Intent intent2 = new Intent(this, ChangeLoginCredentialsActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent2.putExtra("CHANGE", "Email");
                startActivity(intent2);
                break;
            case "ACTION_CLOSE":
            default:
                dialog.dismiss();
        }
    }

    void clearPreferences(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
    }

    public int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public void scanQROnClicked(View view) {
        checkCameraPermission();
//        startActivity(new Intent(this, LiveBarcodeScanningActivity.class));
    }

    public void userinfoOnClicked(View view) {
        startActivity(new Intent(this,com.example.snjdeveloper. AdminInfoActivity.class));

    }

    public void customersOnClicked(View view) {
        Intent intent=new Intent(this, AllCustomerActivity.class);
        intent.putExtra("CLASS_NAME","Customers");
        startActivity(intent);
    }
    public void transactionOnClick(View view) {
        Intent intent=new Intent(this, StatsActivity.class);
        intent.putExtra("CLASS_NAME","Transactions");
        startActivity(intent);
    }

    public void orderOnClick(View view) {
        Intent intent=new Intent(this, StatsActivity.class);
        intent.putExtra("CLASS_NAME","Orders");
        startActivity(intent);
    }

    public void newOnClick(View view) {
        checkCameraPermissionforNew();
    }


    public void settingsOnClick(View view) {
       startActivity(new Intent(this, SettingsActivity.class));
    }


    private void showAlertDialog(boolean isQR) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Camera Permission");
        alert.setMessage("App need to access your camera. Please provide the camera access permission");
        alert.setPositiveButton("Access Camera", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if(isQR)
                checkCameraPermission();
            else checkCameraPermissionforNew();
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            //finish();
        });
    AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, LiveBarcodeScanningActivity.class));
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},102);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (requestCode==102)
                checkCameraPermission();
            else checkCameraPermissionforNew();
        }else{
            if (requestCode==102)
                showAlertDialog(true);
            else showAlertDialog(false);
            //showAlertDialog();
        }
    }

    private void checkCameraPermissionforNew() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, NewRecordActivity.class));
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}
            ,103);
        }
    }

    public void BottleOnClick(View view) {
        Intent intent=new Intent(this, StatsBottleActivity.class);
        intent.putExtra("CLASS_NAME","Bottles");
        startActivity(intent);

    }

    public void notificationOnClick(View view) {
        startActivity(new Intent(this, NotificationActivity.class));
    }

    public void searchOnClick(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("FROM","search");
        startActivity(intent);
    }
    
    public void appOnClick(View view) {
        startActivity(new Intent(this, AppUpdateActivity.class));
    }

    public void ProblemOnClick(View view) {
        startActivity(new Intent(this, ProblemReports.class));
    }


}

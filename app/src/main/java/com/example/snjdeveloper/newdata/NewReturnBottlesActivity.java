package com.example.snjdeveloper.newdata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.snjdeveloper.LoadRecentUsersFragment;
import com.example.snjdeveloper.R;
import com.example.snjdeveloper.admin.HomeAdmin;
import com.example.snjdeveloper.qrcodegenerator.QRCodeGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public  class NewReturnBottlesActivity extends AppCompatActivity {
    private String mobile;
    private String uid;
    private AppCompatEditText amount_received;
    private String mode="";
    private Button saveBt;
    private boolean isDataLoaded=false;
    private long bottle;
    private TextView userNameText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottle_return);
        TextView textView=findViewById(R.id.title);
        textView.setText("Return Bottle");
        amount_received=findViewById(R.id.quantity_return);
        userNameText = findViewById(R.id.username);

        loadData();
        loadName();
        loadListner();
    }

    private void loadName() {
        try {
            Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/" +
                    "files%2F" + uid + "_pr?alt=media&token=" + "479a3ebb-6ef0-4ef8-b111-3e30fa2efb")
                    .apply(RequestOptions.circleCropTransform()).listener(new RequestListener<Drawable>() {

                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    userNameText.setVisibility(View.GONE);
                    return false;
                }
            }).into((ImageView) findViewById(R.id.userimage));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void loadListner() {
        saveBt=findViewById(R.id.saveBt);
        saveBt.setOnClickListener(view -> {
            if(isDataLoaded) {
                if (isNetworkAvailable(this)) {
                    String returnbottle= Objects.requireNonNull(amount_received.getText()).toString().trim();
                    if (mobile == null)
                        showAlertDialog(false, "Mobile not found for this user.");
                    else if (amount_received.getText().toString().isEmpty() ||
                            returnbottle.equals("0") || returnbottle.replace("0", "").trim().isEmpty())
                        showAlertDialog(false, "Total bottle returned is empty");
                    else if (returnbottle.trim().startsWith("0"))
                        showAlertDialog(false, "Do not include extra zero in bottle quantity");
                    else if (bottle < Long.parseLong(returnbottle))
                        showAlertDialog(false, "Quantity of returned bottles should not be greater than original pending bottles");
                    else
                        showAlertDialog(true, "Save this returned bottles for the mobile number " + mobile + ".\n" +
                                "Before saving please make sure bottle quantity received is carefully entered as once saved cannot be modified later.");
                } else
                    showAlertDialog(false, "No internet connectivity available.\nPlease connect to a stable network and try again.");
            }else showAlertDialog(false, "Data is not loaded from the server.\nPlease wait to load the data or connect to a more stable internet connection.");
        });

    }
    private void showAlertDialog(boolean forSave, String msg) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Return bottle ");
        alert.setMessage(msg);
        if (!forSave){
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();

            });
        }else{
            alert.setPositiveButton("Save", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                saveData(null,uid,bottle,Long.parseLong(Objects.requireNonNull(amount_received.getText()).toString().trim()),false);
            });
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });
        }



        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
    public static boolean isNetworkAvailable(Context con){
        try{
            ConnectivityManager cm=(ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=cm.getActiveNetworkInfo();
            if (networkInfo!=null&& networkInfo.isConnected()){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public void saveData(String note,String uid,long bottle,long returnbottle ,boolean check) {
            if(!check) {
                findViewById(R.id.progressBt).setVisibility(View.VISIBLE);
                saveBt.setEnabled(false);
                saveBt.setText("");
            }
            HashMap<String, Object> data = new HashMap<>();
            long time = System.currentTimeMillis();
            data.put("TIME", time);
            String id = String.valueOf(time);
            if (note != null)
                data.put("NOTE", note);
            data.put("UID", uid);
            data.put("KEY", uid + id);
            data.put("QUANTITY", returnbottle);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Bottles").document(id)
                    .set(data)
                    .addOnSuccessListener(documentReference -> {
                        FirebaseDatabase.getInstance().getReference("Customers/" + uid).
                                child("WATER_BOTTLE_PENDING").setValue(bottle - returnbottle);
                        new QRCodeGenerator("ReturnBottle,"+id,id+"_re",NewReturnBottlesActivity.this);
                        if(!check) {
                            Intent intent = new Intent(getApplicationContext(), HomeAdmin.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if(!check)
                        showAlertDialog(false, "Failed to save the changes. Check your internet connectivity");
                    });
        }

    private void loadData() {
        TextView textView=findViewById(R.id.mobilenumber);
        mobile=getIntent().getStringExtra("MOBILE");
        textView.setText(mobile);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        textView=findViewById(R.id.date);
        textView.setText(dateFormat.format(calendar.getTime()));
        uid=getIntent().getStringExtra("UID");
        FirebaseDatabase.getInstance().getReference("Customers/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isDataLoaded=true;
                try {
                    String name = (String) dataSnapshot.child("NAME").getValue();
                    if (name != null) {
                        if (!name.isEmpty()) {
                            String[] d = name.split(" ");
                            if (d.length > 1)
                                name = d[0].substring(0, 1) + d[1].substring(0, 1);
                            else {
                                if (name.length() >= 2)
                                    name = name.substring(0, 2);
                                else
                                    name = name.substring(0, 1);
                            }
                        }
                        userNameText.setText(name.toUpperCase());
                        new LoadRecentUsersFragment().addNewRecent(mobile, uid, name.toUpperCase(), NewReturnBottlesActivity.this);
                    }
                }catch (Exception e){e.printStackTrace();}


                try {
                    bottle=(long)dataSnapshot.child("WATER_BOTTLE_PENDING").getValue();
                }catch (Exception e){bottle=0;}
                loadBottle(findViewById(R.id.bottleProgress),findViewById(R.id.bottle),bottle);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void loadBottle(ProgressBar bottleProgress,TextView textView,long bottle) {
       bottleProgress.setVisibility(View.INVISIBLE);
        String w=String.valueOf(bottle);

        if(bottle>0)
        {   w="Pending "+w;
            textView.setTextColor(Color.parseColor("#D50000"));
            //getResources().getColor(R.color.red));
        }
        else
            textView.setTextColor(Color.parseColor("#ff99cc00"));//getResources().getColor(R.color.green_light));

        textView.setText(String.format(Locale.UK," %s", w));
    }


    public void backOnClicked(View view) {
        finish();
    }
}

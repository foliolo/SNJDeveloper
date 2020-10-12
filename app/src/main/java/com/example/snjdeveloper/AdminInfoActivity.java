package com.example.snjdeveloper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
public class AdminInfoActivity extends AppCompatActivity {
    char btAction;
    RelativeLayout dataLinear;
    NestedScrollView scrollView;
    Button button;
    HashMap<String,String> orginalData;
    private TextView textView;
    TextInputEditText editText;
    TextInputLayout editTextLayout;
    private ProgressBar progress_data;
    private boolean editWindowVisible=false;
    private TextView loginEmailText;
    private Uri link;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        loadFragment();
        loginEmailText = findViewById(R.id.login_email);
        button = findViewById(R.id.action_button);
        scrollView = findViewById(R.id.scroll_view);
        dataLinear = findViewById(R.id.edit_linear);
        editText = findViewById(R.id.edit_data);
        editTextLayout = findViewById(R.id.one);
        orginalData = new HashMap<>();
        textView = findViewById(R.id.title);
        textView.setText(R.string.admin_info);
        progress_data = findViewById(R.id.progress_data);
        try {
            extractDetail();
            new CustomScreen(R.string.custom_admin_info_screen, this, findViewById(R.id.recyclerview));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadFragment() {
        Fragment fragmentClass =new ImageSliderFragment();
        FragmentTransaction fragmentTransaction2;
        fragmentTransaction2= getSupportFragmentManager().beginTransaction();
        try{ fragmentTransaction2.replace(R.id.frame, Objects.requireNonNull(fragmentClass));
            fragmentTransaction2.commit();
        }catch (Exception ignored){}

    }
    @Override
    protected void onStart() {
        super.onStart();
        loginEmailText.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
    }

    public void changeMobileOnClick(View view) {
        button.setText(String.format("Change %s", getString(R.string.mobile_number)));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        editWindowVisible = true;
        textView.setText(getString(R.string.mobile_number));
        editTextLayout.setHint(getString(R.string.mobile_number));
        btAction = 'm';
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_DATETIME);
    }

    private void showMsg(String msg, int color, boolean isIndefinite) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.scroll_view), msg, Snackbar.LENGTH_LONG);
        if (isIndefinite)
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, color));
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }

    public void nameOnClick(View view) {
        button.setText(String.format("Change %s", getString(R.string.username_shop_name)));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        editWindowVisible = true;
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        textView.setText(getString(R.string.username_shop_name));
        editTextLayout.setHint(getString(R.string.username_shop_name));
        btAction = 'n';
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

    }
    public void backOnClicked(View view) {
        if (editWindowVisible)
            cancelEditWindowOnClick(view);
        else
        finish();
    }

    @Override
    public void onBackPressed() {

        if (editWindowVisible)
            cancelEditWindowOnClick(new View(this));
        else
            super.onBackPressed();
    }

    public void addressOnClick(View view) {
        editWindowVisible = true;
        button.setText(String.format("Change %s", getString(R.string.address)));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        textView.setText(getString(R.string.address));
        editTextLayout.setHint(getString(R.string.address));
        btAction = 'a';
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }
    public void changePublicEmailOnClick(View view) {
        editWindowVisible = true;
        button.setText(String.format("Change %s", getString(R.string.public_email)));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        textView.setText(getString(R.string.public_email));
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editTextLayout.setHint(getString(R.string.public_email));
        btAction = 'p';
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

    }
    public void enterQRName() {
        editWindowVisible = true;
        button.setText("Save QR Code");
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        textView.setText(R.string.qr_name);
        editTextLayout.setHint("QR Name");
        btAction = 'q';
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }
    public void changeLoginEmailOnClick(View view) {
        Intent intent=new Intent(this,ChangeLoginCredentialsActivity.class);
        intent.putExtra("CHANGE","Email");
        startActivity(intent);

    }

    public void changePasswordOnClick(View view) {
        Intent intent=new Intent(this,ChangeLoginCredentialsActivity.class);
        intent.putExtra("CHANGE","Password");
        startActivity(intent);
    }
    public void actionButtonOnClick(View view) {
        if (Objects.requireNonNull(editText.getText()).toString().trim().isEmpty()) {
            String msg = button.getText().toString().toLowerCase().replace("change", "").trim();
            showMsg("Enter " + msg, R.color.red, false);
            return;
        }
        String data = Objects.requireNonNull(editText.getText()).toString().trim();
        if (btAction == 'm') {
            data = data.replace("+91", "");
            if (data.isEmpty()) {
                showMsg("Enter mobile number", R.color.red, false);

            }
            if (data.length() != 10) {
                showMsg("Mobile Number is invalid", R.color.red, false);
                return;
            } else data = "+91" + data;

        } else if (btAction == 'p') {
            if (!Patterns.EMAIL_ADDRESS.matcher(data).matches()) {
                showMsg("Email address invalid", R.color.red, false);
                return;
            }

        }
        progress_data.setVisibility(View.VISIBLE);
        button.setText("");
        switch (btAction) {
            case 'm':
                save(data, "MOBILE_NUMBER", editText, findViewById(R.id.mobilenumber));
                break;
            case 'n':
                save(data, "NAME", editText, findViewById(R.id.username));
                break;
            case 'a':
                save(data,"SHOP_ADDRESS", editText, findViewById(R.id.shop_address));
                break;
            case 'p':
                save(data,"PUBLIC_EMAIL", editText, findViewById(R.id.public_email));
                break;
            case 'q':
                save(data,"QR", editText, null);
                break;
        }
    }
    private void save(String data, String type, TextInputEditText editText, TextView textid) {
        if(type.equalsIgnoreCase("qr")){
              uploadImageToFirebaseStorage(data);
        }else {

            FirebaseDatabase.getInstance().getReference("Admin/personal_details").child(type).setValue(data).
                    addOnSuccessListener(aVoid -> {
                        {
                            if (type.equals("PUBLIC_EMAIL"))
                                storePreference(getSharedPreferences("CRED", MODE_PRIVATE), data, type);
                            textid.setText(data);
                            editText.setText(data);
                            progress_data.setVisibility(View.GONE);
                            button.setText(type + " Saved");
                            editText.setText("");
                            dataLinear.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            editWindowVisible = false;
                            editText.setEnabled(false);
                            TextView textView = findViewById(R.id.title);
                            textView.setText(R.string.user_info);
                            String msg = type.replace("_", " ");
                            showMsg(msg.substring(0, 1)
                                            .toUpperCase() + "" + msg.substring(1).toLowerCase() + " saved"
                                    , R.color.colorAccent, false);
                        }
                    }).addOnFailureListener(e -> {
                editText.setText(orginalData.get(type));
                Toast.makeText(this, "Error: Unable to process request", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                dataLinear.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                editText.setEnabled(false);
            });
        }

    }
    private void extractDetail() {
        FirebaseDatabase.getInstance().getReference().child("Admin/personal_details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                TextView  textView;

                String data;

                  data= (String) dataSnapshot.child("NAME").getValue();
                //if(data!=null&& !data.isEmpty() && !Objects.equals(orginalData.get("Name"), data)) {
                    orginalData.put("NAME",data);
                    textView=findViewById(R.id.username);
                textView.setText(data);
                //}
                data = (String) dataSnapshot.child("MOBILE_NUMBER").getValue();
                //if(data!=null&& !data.isEmpty() &&!Objects.equals(orginalData.get("MOBILE_NUMBER"), data)) {
                orginalData.put("MOBILE_NUMBER", data);
                textView = findViewById(R.id.mobilenumber);
                textView.setText(data);
                //}
                data = (String) dataSnapshot.child("SHOP_ADDRESS").getValue();
                //if(data!=null&& !data.isEmpty() &&!Objects.equals(orginalData.get("SHOP_ADDRESS"), data)) {
                orginalData.put("SHOP_ADDRESS", data);
                textView = findViewById(R.id.shop_address);
                textView.setText(data);
                //}

                data = (String) dataSnapshot.child("PUBLIC_EMAIL").getValue();
                SharedPreferences preferences = getSharedPreferences("CRED", MODE_PRIVATE);
                if (!preferences.getString("PUBLIC_EMAIL", "").equals(data))
                    storePreference(preferences, data, "PUBLIC_EMAIL");
                //if(data!=null&& !data.isEmpty() &&!Objects.equals(orginalData.get("PUBLIC_EMAIL"), data)) {
                orginalData.put("PUBLIC_EMAIL", data);
                textView = findViewById(R.id.public_email);
                textView.setText(data);
                //}
                data = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
                //if(data!=null&& !data.isEmpty() &&!Objects.equals(orginalData.get("LOGIN_EMAIL"), data)) {
                orginalData.put("LOGIN_EMAIL", data);
                loginEmailText.setText(data);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void storePreference(SharedPreferences preferences, String data, String name) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, data);
        editor.apply();
    }


    public void cancelEditWindowOnClick(View view) {
        editWindowVisible = false;
        editText.setText("");
        findViewById(R.id.qr_preview).setVisibility(View.GONE);
        dataLinear.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        textView.setText(getString(R.string.admin_info));
        editText.setEnabled(false);
    }

    private void showImageChooser() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 102);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE},102);
        }
    }
    private void uploadImageToFirebaseStorage(String data) {
        String time=String.valueOf(System.currentTimeMillis());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("qr/"+time+"_qr.png");
        storageRef.putFile(link).addOnFailureListener(exception -> {
            Toast.makeText(AdminInfoActivity.this,"Failed to upload QR image",Toast.LENGTH_SHORT).show();
            Log.d("Upload Image","Image uploaded fail");
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d("Upload Image","Image uploaded");
            findViewById(R.id.qr_preview).setVisibility(View.GONE);
            progress_data.setVisibility(View.GONE);
            button.setText(data + " Saved");
            editText.setText("");
            dataLinear.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            editWindowVisible = false;
            editText.setEnabled(false);
            TextView textView = findViewById(R.id.title);
            textView.setText(R.string.user_info);
            FirebaseDatabase.getInstance().getReference("Admin/QR/"+time).child("NAME").setValue(data).
                    addOnSuccessListener(aVoid -> loadFragment());});
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            link = data.getData();
            ImageView imageView=findViewById(R.id.qr_preview);
            Bitmap bitmap;
            try {imageView.setVisibility(View.VISIBLE);
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),link);
                Glide.with(getApplicationContext()).asBitmap().load(bitmap).into(imageView);
                enterQRName();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            showImageChooser();
        }
    }

    public void logoutclick(View view) {
        showAlertDialog();
    }
    private void showAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Logout");
        alert.setMessage("Are you sure you want to logout.");
        alert.setPositiveButton("Logout", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            clearPreferences("CRED");
            clearPreferences("RECENT");
            clearPreferences("RECENT_SEARCH");
            FirebaseMessaging.getInstance().unsubscribeFromTopic("admin");
            FirebaseAuth.getInstance().signOut();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("admin");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    void clearPreferences(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.apply();
    }

    public void addNewQRClicked(View view) {
        Log.e("QR", "Clicked");
        showImageChooser();
    }
}

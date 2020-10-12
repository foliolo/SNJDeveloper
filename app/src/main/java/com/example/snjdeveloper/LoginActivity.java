package com.example.snjdeveloper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snjdeveloper.admin.HomeAdmin;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth mAuth;
    private MaterialButton bt;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private View resetLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email_address);
        editTextPassword = findViewById(R.id.password);
        bt = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressbar);
        resetLayout = findViewById(R.id.reset_layout);
        ((EditText) findViewById(R.id.password)).setOnEditorActionListener
                ((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        if (resetLayout.getVisibility() == View.VISIBLE)
                            resetOnClick(findViewById(R.id.buttonResetLogin));
                        else loginOnClick(bt);
                        return true;
                    }
                    return false;
                });
    }

    public void loginOnClick(View view) {
     checkFields();
    }

    private void checkFields() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Minimum length of password should be 6");
            editTextPassword.requestFocus();
            return;
        }
        bt.setText("");

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SharedPreferences sharedPreferences = getSharedPreferences("CRED", MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("PASSWORD",
                        new Encryption().getEncodedString(password));
                configureFCM();
//                Date today= Calendar.getInstance().getTime();
                edit.putString("LAST_LOGIN", getFormatedTime(System.currentTimeMillis()));
                edit.apply();
                Intent intent = new Intent(LoginActivity.this, HomeAdmin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                progressBar.setVisibility(View.GONE);
                bt.setText("Login");
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    Toast.makeText(getApplicationContext(), "Email address or password is incorrect", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    String getFormatedTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy hh:mm aa", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return dateFormat.format(calendar.getTime());
    }

    private void configureFCM() {
        FirebaseMessaging.getInstance().subscribeToTopic("admin").addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Log.e("FCM", "Subscription  completed");
            else {
                Log.e("FCM", "Subscription Failed\n" + task.getException().getMessage());
                task.getException().printStackTrace();
            }


        });
        FirebaseMessaging.getInstance().subscribeToTopic("developer").addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Log.e("FCM", "Subscription  completed");
            else {
                Log.e("FCM", "Subscription Failed\n" + task.getException().getMessage());
                task.getException().printStackTrace();
            }


        });
        FirebaseDatabase.getInstance().getReference().child("Admin/personal_details")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String data = (String) snapshot.child("PUBLIC_EMAIL").getValue();
                        if (data != null) {
                            SharedPreferences preferences = getSharedPreferences("CRED", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("PUBLIC_EMAIL", data);
                            editor.apply();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void forgotPasswordCLicked(View view) {
        showAlertDialog(true,"Send Mail","Reset your password.\nA mail will be send to your registered email id to reset your password");
    }
    private void showAlertDialog(boolean sendEmail,String header, String msg) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(header);
        alert.setMessage(msg);
        if (!sendEmail){
            alert.setPositiveButton("OK", (dialogInterface, i) -> {

                dialogInterface.dismiss();

            });
        }else{
            if (msg.contains("mail is sent")){
                alert.setPositiveButton("OK", (dialogInterface, i) -> {
                    editTextPassword.setVisibility(View.VISIBLE);
                    findViewById(R.id.forgetPass).setVisibility(View.VISIBLE);
                    bt.setVisibility(View.VISIBLE);
                    if (titleTextView != null)
                        titleTextView.setText(R.string.login);
                    resetLayout.setVisibility(View.GONE);
                    findViewById(R.id.pass_text).setVisibility(View.VISIBLE);
                    dialogInterface.dismiss();
                });
            }else {
                alert.setPositiveButton("Send", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    editTextPassword.setVisibility(View.GONE);
                    findViewById(R.id.forgetPass).setVisibility(View.GONE);
                    bt.setVisibility(View.GONE);
                    resetLayout.setVisibility(View.VISIBLE);
                    findViewById(R.id.pass_text).setVisibility(View.GONE);
                    titleTextView = findViewById(R.id.login_title);
                    titleTextView.setText("Reset");

                });
                alert.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
            }
        }
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    public void resetOnClick(View view) {
        view.setClickable(false);
        String email = editTextEmail.getText().toString();
        MaterialButton bt = (MaterialButton) view;

        ProgressBar progressBar = findViewById(R.id.progressPasswordReset);
        if (email.isEmpty()) {
            view.setClickable(true);
            showAlertDialog(false, "Reset Password", "Please enter the email id to reset your password.");
        } else {
            bt.setText("");
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(aVoid -> {
                view.setClickable(true);
                progressBar.setVisibility(View.GONE);
                bt.setText("Send password reset mail");
                editTextEmail.setText("");
                showAlertDialog(true, "Reset Password"," A mail is sent to your email id "+email+". Please reset your password and try to login again." );
            }).addOnFailureListener(e -> {
                view.setClickable(true);
                progressBar.setVisibility(View.GONE);
                bt.setText("Send password reset mail");
                e.printStackTrace();
                showAlertDialog(false, "Reset Password","Error in sending password reset mail to your email address. \n"+e.getMessage() );
            });
        }
    }
}

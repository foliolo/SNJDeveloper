package com.example.snjdeveloper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Objects;

public class ChangeLoginCredentialsActivity extends AppCompatActivity {
    private String TAG = getClass().getCanonicalName();
    private String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_login_cred);
        String receive = getIntent().getStringExtra("CHANGE");
        try {
            password =
                    new Encryption().getDecodedString(getSharedPreferences("CRED", MODE_PRIVATE)
                            .getString("PASSWORD", "NOT FOUND"));

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (receive.contains("Email")) {
            {
                findViewById(R.id.email_layout).setVisibility(View.VISIBLE);
                changeEmail();
            }
        } else {
            findViewById(R.id.password_layout).setVisibility(View.VISIBLE);
            changePass();
        }
    }

    private void changePass() {
        EditText old_pass = findViewById(R.id.old_password);
        EditText new_pass = findViewById(R.id.new_password);
        MaterialButton bt = findViewById(R.id.buttonLogin);
        bt.setText("Change Password");
        bt.setOnClickListener(view -> {
            String old = old_pass.getText().toString().trim();
            String newp = new_pass.getText().toString().trim();
            if (old.isEmpty()) {

                showAlertDialog(false, "Change Password", "Enter old password");
                return;
            }
            if (newp.isEmpty()) {
                showAlertDialog(false,"Change Password","Enter new password");

                return;
            }
            if(old.equals(newp)){
                showAlertDialog(false,"Change Password","Both Passwords cannot be same");

                return;
            }
            if(!old.equals(password)){
                showAlertDialog(false,"Change Password","Old password is incorrect");

                return;
            }
            if (password.length() < 6) {
                showAlertDialog(false,"Change Password","Minimum length of password should be 6");

                new_pass.requestFocus();
                return;
            }
            bt.setText("");
            bt.setClickable(false);
            authForNewPass(old, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), newp, findViewById(R.id.progressbar));
        });


    }

    private void authForNewPass(String old_pass, String email, String new_pass, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        MaterialButton bt = findViewById(R.id.buttonLogin);

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, old_pass);

// Prompt the user to re-provide their sign-in credentials
        Objects.requireNonNull(user).reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(new_pass).addOnCompleteListener(task1 -> {
                            progressBar.setVisibility(View.GONE);
                            if (task1.isSuccessful()) {
                                savePassword(new_pass);
                                showAlertDialog(true, "Change Password", "Password updated");
                                bt.setText("Sending back");
                            } else {
                                bt.setText("Change Password");
                                bt.setClickable(true);
                                showAlertDialog(false, "Change Password", "Error password not updated");

                            }
                        });
                    } else {
                        showAlertDialog(false, "Change Password", "An error occurred during authorization");

                    }
                });


    }

    private void savePassword(String new_pass) {
        SharedPreferences sharedPreferences = getSharedPreferences("CRED", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("PASSWORD",
                new Encryption().getEncodedString(new_pass));
        edit.apply();
    }

    private void changeEmail() {
        try {
            EditText pass = findViewById(R.id.password);
            EditText email = findViewById(R.id.email_address);
       // String password = getSharedPreferences("CRED", MODE_PRIVATE).getString("PASSWORD", "NOT FOUND");
            MaterialButton bt = findViewById(R.id.buttonLogin);
            bt.setText("Change Email");
            bt.setOnClickListener(view -> {
                if (pass.getVisibility() == View.VISIBLE) {
                    if (pass.getText().toString().trim().equals(password)) {
                        findViewById(R.id.password_email).setVisibility(View.VISIBLE);
                        email.setVisibility(View.VISIBLE);
                        email.requestFocus();
                        pass.setVisibility(View.GONE);
                        findViewById(R.id.pass_text).setVisibility(View.GONE);
                    } else
                        showAlertDialog(false, "Change Email", "Password is incorrect");

                } else {
                    bt.setText("Change Email");
                    String e = email.getText().toString().trim();
                    if (e.isEmpty()) {
                        showAlertDialog(false, "Change Email", "Enter email id");
                        return;
                    }
                    String oldEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    if ((oldEmail != null && oldEmail.equals(e))) {
                        showAlertDialog(false, "Change Email", "Enter new email id");
                        email.requestFocus();
                        return;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                        showAlertDialog(false, "Change Email", "Invalid email id");
                        email.requestFocus();
                        return;
                    }
                    bt.setText("");
                    bt.setClickable(false);
                    authForNewEmail(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                            e, password, findViewById(R.id.progressbar));

                }
        });
    }catch(Exception e){
        showAlertDialog(true,"Change Email","An error occurred");

    }
    }

    private void authForNewEmail(String oldemail, String newemail, String pass, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(oldemail, pass); // Current Login Credentials \\
        // Prompt the user to re-provide their sign-in credentials
        Objects.requireNonNull(user).reauthenticate(credential)
                .addOnSuccessListener(task -> {
                    Log.d(TAG, "User re-authenticated.");
                    //Now change your email address \\
                    //----------------Code for Changing Email Address----------\\
                    FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                    Objects.requireNonNull(user1).updateEmail(newemail)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    MaterialButton bt = findViewById(R.id.buttonLogin);
                                    bt.setText("Sending back");
                                    progressBar.setVisibility(View.GONE);
                                    showAlertDialog(true, "Change Email", "Email address is updated");
                                }
                            }).addOnFailureListener(e -> {
                        MaterialButton bt = findViewById(R.id.buttonLogin);
                        bt.setText("Change Email");
                        bt.setClickable(true);
                        showAlertDialog(false,"Change Email","Failed to change the email.Check your password and new email again");

                            });
                    //----------------------------------------------------------\\
                });
    }

    private void showAlertDialog(boolean success,String header, String msg) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(header);
        alert.setMessage(msg);
        if (!success){
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();

            });
        }else {
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                finish();
            });
            if (!msg.contains("updated"))
                alert.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
            else alert.setOnDismissListener(dialog -> {
                dialog.dismiss();
                finish();
            });
        }



        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

}

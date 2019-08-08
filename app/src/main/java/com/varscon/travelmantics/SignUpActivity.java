package com.varscon.travelmantics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEmailInput, mNameInput, mPasswordInput, mPasswordConfirmInput;
    private Button mRegisterBtn;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN=1;
    private TextView mLogintext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);


        mLogintext = findViewById(R.id.login_text);
        mRegisterBtn = findViewById(R.id.register_button);
        mEmailInput = findViewById(R.id.input_email);
        mNameInput = findViewById(R.id.input_name);
        mPasswordInput = findViewById(R.id.input_password);
        mPasswordConfirmInput = findViewById(R.id.input_password_confirm);


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmailInput.getText().toString();
                String password = mPasswordInput.getText().toString();

                createAccount(email, password);
            }
        });

        mLogintext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignActivity.class));
            }
        });

    }

    private boolean validateForm() {
        boolean valid = true;

        String name = mNameInput.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameInput.setError("Required.");
            valid = false;
        } else {
            mNameInput.setError(null);
        }


        String email = mEmailInput.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailInput.setError("Required.");
            valid = false;
        } else {
            mEmailInput.setError(null);
        }

        String password = mPasswordInput.getText().toString();
        String password_confirm = mPasswordConfirmInput.getText().toString();
        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(password_confirm) && password.equals(password_confirm)) {
            mPasswordInput.setError("Required.");
            mPasswordConfirmInput.setError("Must be same as password");
            valid = false;
        } else {
            mPasswordInput.setError(null);
            mPasswordConfirmInput.setError(null);
        }

        return valid;
    }

    private void createAccount(String email, String password) {
        Log.d("SignUpActivity", "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgress("Flow is Creating Your Account");

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignUpActivity", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            hideProgress();
                            sendEmailVerification();
                            goHome();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignUpActivity", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            hideProgress();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void updateUI(FirebaseUser user) {
        DatabaseReference addUser = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        addUser.child("displayName").setValue(user.getDisplayName());
        addUser.child("photoURL").setValue(user.getPhotoUrl());
    }

    private void goHome() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showProgress(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.dismiss();
    }

    private void sendEmailVerification() {

        // Send verification email
        // [START send_email_verification]
        showProgress("Sending Verification Email");
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]

                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("SignUpActivity", "sendEmailVerification", task.getException());
                            Toast.makeText(SignUpActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                        hideProgress();
                    }
                });
        // [END send_email_verification]
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("SignUpActivity", "Google sign in failed", e);
                // [START_EXCLUDE]
//                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.d("SignUpActivity", "firebaseAuthWithGoogle:" + account.getId());
        // [START_EXCLUDE silent]
        showProgress("Signing Up");
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignUpActivity", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                            goHome();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignUpActivity", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.annonce_main_coordinator), task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgress();
                        // [END_EXCLUDE]
                    }
                });
    }

}

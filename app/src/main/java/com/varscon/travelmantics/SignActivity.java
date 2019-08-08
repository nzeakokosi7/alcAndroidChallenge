package com.varscon.travelmantics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignActivity extends AppCompatActivity {

    private EditText mEmailInput, mPasswordInput;
    private FirebaseAuth mAuth;
    private TextView mLoginBtn, mSignUp, mPasswordRecovery;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private Button mGoogleSignIn;
    private static final int RC_SIGN_IN=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignIn = findViewById(R.id.google_button);
        mLoginBtn =findViewById(R.id.sign_in_text_button);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        mEmailInput = findViewById(R.id.input_email);
        mPasswordInput = findViewById(R.id.input_password);
        mSignUp = findViewById(R.id.sign_up_text);
//        mPasswordRecovery = findViewById(R.id.password_recovery);

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignActivity.this, SignUpActivity.class));
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmailInput.getText().toString();
                String password = mPasswordInput.getText().toString();

                signIn(email, password);
            }
        });

        mGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    public void signIn(String email, String password) {
        Log.d("SignInActivity", "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgress("Flow is signing you in");

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignInActivity", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                            goHome();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignInActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }


//                        }
                        hideProgress();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }



    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailInput.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailInput.setError("Required.");
            valid = false;
        } else {
            mEmailInput.setError(null);
        }

        String password = mPasswordInput.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordInput.setError("Required.");
            valid = false;
        } else {
            mPasswordInput.setError(null);
        }

        return valid;
    }

    private void showProgress(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.dismiss();
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

    private void goHome() {
        Intent intent = new Intent(SignActivity.this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}


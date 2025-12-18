package com.agriberriesmx.agriberries;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private EditText etvUser, etvPassword;
    private ProgressBar progressBar;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvUser = findViewById(R.id.etvUser);
        etvPassword = findViewById(R.id.etvPassword);
        progressBar = findViewById(R.id.progressBar);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Connect to FirebaseAuth and verify if the user is already logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Reload the user to get fresh data
            user.reload().addOnSuccessListener(unused -> {
                // Couldn't verify user status, go to home activity
                startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                finish();
            }).addOnFailureListener(e -> {
                if (e instanceof FirebaseNetworkException) {
                    // Successfully login inform user
                    startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                    finish();
                } else {
                    // The user doesn't exist or was blocked
                    if (e.toString().contains("disabled")) Toast.makeText(SignInActivity.this, getResources().getString(R.string.userBlocked), Toast.LENGTH_SHORT).show();
                    else Toast.makeText(SignInActivity.this, getResources().getString(R.string.userNotExist), Toast.LENGTH_SHORT).show();
                    auth.signOut();
                    progressBar.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // Change views visibilities
            progressBar.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
        }

        //Add listener
        btnSignIn.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        // Change visibility
        btnSignIn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Get user and password to verify that they are not empty
        String username = etvUser.getText().toString().trim();
        String password = etvPassword.getText().toString().trim();

        if (!username.isEmpty() && !password.isEmpty()) {
            // Connect to FirebaseAuthentication to verify credentials
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String email = username + getResources().getString(R.string.domain);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // Go to home activity
                        startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Change visibility
                        progressBar.setVisibility(View.GONE);
                        btnSignIn.setVisibility(View.VISIBLE);

                        if (e instanceof FirebaseAuthInvalidCredentialsException)
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.invalid_credentials), Toast.LENGTH_SHORT).show();
                        else if (e instanceof FirebaseAuthInvalidUserException)
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.error_user_does_not_exist), Toast.LENGTH_SHORT).show();
                        else if (e instanceof FirebaseNetworkException)
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.try_again_later), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();

            // Change visibility
            progressBar.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
        }
    }

}
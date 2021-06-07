package com.adriann.opscmaps2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextFullName, editTextEmail, editTextPassword, editTextConfPass;
    private Button regButton;
    private TextView bannerReg, userSignedIn;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        bannerReg = findViewById(R.id.bannerReg);
        bannerReg.setOnClickListener(this);
        //variable for a user that's already signed in
        userSignedIn = findViewById(R.id.Log);
        userSignedIn.setOnClickListener(this);

        regButton = findViewById(R.id.regBtn);
        regButton.setOnClickListener(this);

        editTextFullName = findViewById(R.id.FullName);
        editTextEmail = findViewById(R.id.emailReg);
        editTextPassword = findViewById(R.id.passwordReg);
        editTextConfPass = findViewById(R.id.confPassword);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bannerReg:
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
                break;
            case R.id.Log:
                Log();
            case R.id.regBtn:
                registerUser();
        }
    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String conPass = editTextConfPass.getText().toString().trim();

        //validating fields for the register class
        if (fullName.isEmpty()) {
            editTextFullName.setError("Full Name is required");
            editTextFullName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter provide valid email");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Please enter correct password");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Please enter 6 characters or more");
            editTextPassword.requestFocus();
            return;
        }
        if (!password.equals(conPass)) {
            editTextConfPass.setError("Passwords do not match");
            editTextConfPass.requestFocus();
            return;
        }
        if (conPass.length() < 6) {//why? I'm not sure. Hold on a second. Nevermind it's working now
            editTextConfPass.setError("Please enter 6 characters or more");
            editTextConfPass.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(fullName, email, password);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser verify = FirebaseAuth.getInstance().getCurrentUser();
                                        if (verify.isEmailVerified()) {
                                            startActivity(new Intent(getApplicationContext(), Profile.class));
                                            finish();
                                        } else {
                                            verify.sendEmailVerification();
                                        }
                                    } else {
                                        Toast.makeText(Register.this, "Failed to create user. Try again", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(Register.this, "Failed to create user. Try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //This method sends the user to the login page
    private void Log() {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}

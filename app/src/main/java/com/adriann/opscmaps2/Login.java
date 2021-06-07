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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener{

    private TextView register,forgotPassword;
    private EditText editTextEmailLog, editTextPasswordLog;
    private Button loginBtn;

    private FirebaseAuth mAuth;
    //progressBar name
    private ProgressBar peanutLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        register = findViewById(R.id.RegLog);
        register.setOnClickListener(this);

        loginBtn = findViewById(R.id.logBtn);
        loginBtn.setOnClickListener(this);

        editTextEmailLog = findViewById(R.id.emailLog);
        editTextPasswordLog = findViewById(R.id.passwordLog);

        mAuth = FirebaseAuth.getInstance();

        forgotPassword = findViewById(R.id.txtForgotPass);
        forgotPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.RegLog:
                startActivity(new Intent(getApplicationContext(),Register.class));
                finish();
                break;
            case R.id.logBtn:
                userSignin();
                finish();
                break;

            case R.id.txtForgotPass:
                startActivity(new Intent(getApplicationContext(),Password_Reset.class));
                finish();
                break;

        }
    }

    private void userSignin() {
        String emailLog = editTextEmailLog.getText().toString().trim();
        String passwordLog = editTextPasswordLog.getText().toString().trim();

        if(emailLog.isEmpty()) {
            editTextEmailLog.setError("Email is required");
            editTextEmailLog.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailLog).matches()){
            editTextEmailLog.setError("Please enter a valid email");
            editTextEmailLog.requestFocus();
            return;
        }
        if(passwordLog.isEmpty()){
            editTextPasswordLog.setError("Password is required");
            editTextPasswordLog.requestFocus();
            return;
        }
        if(passwordLog.length() < 6 ){
            editTextPasswordLog.setError("Min password length is 6 characters");
            editTextPasswordLog.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(emailLog, passwordLog).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //redirect to user profile
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                }else{
                    Toast.makeText(Login.this, "Failed to login. Please check your credentials", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Profile.class));
            finish();
        }
    }
}
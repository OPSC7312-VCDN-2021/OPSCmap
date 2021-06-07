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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Password_Reset extends AppCompatActivity {

    private EditText emailReset;
    private Button resetButton, loginButtonPass;

    FirebaseAuth authReset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_password_reset);

        emailReset = findViewById(R.id.emailReset);
        resetButton = findViewById(R.id.resetPassBtn);
        loginButtonPass = findViewById(R.id.loginPasswordReset);

        authReset = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        loginButtonPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
    }

    private void resetPassword() {
        String email = emailReset.getText().toString().trim();

        if(email.isEmpty()){
            emailReset.setError("Email is required");
            emailReset.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailReset.setError("Please provide a valid email");
            emailReset.requestFocus();
            return;
        }

        authReset.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete( Task<Void> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }else{
                    Toast.makeText(Password_Reset.this, "Something wrong happened", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
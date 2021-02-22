package com.example.ass15.User_Interface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ass15.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private TextView register, ForgotPassword;
     EditText UserEmailAddress, UserPassword;
     Button LoginButton;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton = findViewById(R.id.LoginButton);
        UserEmailAddress= findViewById(R.id.UserEmailAddress);
        UserPassword= findViewById(R.id.UserPassword);
        progressBar = findViewById(R.id.progressBar);

       firebaseAuth = FirebaseAuth.getInstance();

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Extract and validate
                if(UserEmailAddress.getText().toString().isEmpty()){
                    UserEmailAddress.setError("Email required");
                    return;
                }
                if(UserPassword.getText().toString().isEmpty()){
                    UserPassword.setError("Password required");
                    return;
                }
                showDialog();

                //Login User
                firebaseAuth.signInWithEmailAndPassword(UserEmailAddress.getText().toString(), UserPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                        hideDialog();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                        hideDialog();

                    }
                });

            }
        });



        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);
        ForgotPassword = (TextView) findViewById(R.id.UserForgotPassword);
        ForgotPassword.setOnClickListener(this);
    }

    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }




    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.register:
                startActivity(new Intent(this, Register.class));
                break;

            case R.id.UserForgotPassword:
                startActivity(new Intent(this, ForgotPassword.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

    }
}
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

public class Register extends AppCompatActivity {
    private TextView Logo;
    private EditText UserFullname,UserPhoneNumber,UserAge,UserFirstLineOfAddress,UserEmail,UserPasswordReg;
    private ProgressBar progressBar;
    private Button RegisterUser;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        Logo = (TextView)findViewById(R.id.Logo);
        RegisterUser = (Button) findViewById(R.id.RegisterUser);
        UserFullname = (EditText)findViewById(R.id.UserFullname);
        UserPhoneNumber = (EditText)findViewById(R.id.UserPhoneNumber);
        UserAge = (EditText)findViewById(R.id.UserAge);
        UserFirstLineOfAddress = (EditText)findViewById(R.id.UserFirstLineOfAddress);
        UserEmail = (EditText)findViewById(R.id.UserEmail);
        UserPasswordReg = (EditText)findViewById(R.id.UserPasswordReg);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();


        Logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });


        RegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email, Password, FullName, PhoneNr, Age, FirstLineOfAddress;
                Email = String.valueOf(UserEmail.getText());
                Password = String.valueOf(UserPasswordReg.getText());
                FullName = String.valueOf(UserFullname.getText());
                PhoneNr = String.valueOf(UserPhoneNumber.getText());
                Age = String.valueOf(UserAge.getText());
                FirstLineOfAddress = String.valueOf(UserFirstLineOfAddress.getText());
                if(FullName.isEmpty()){
                    UserFullname.setError("Full Name is required");
                    return;
                }
                if(Email.isEmpty()){
                    UserEmail.setError("Email is required");
                    return;
                }
                if(Password.isEmpty()){
                    UserPasswordReg.setError("Password is required");
                    return;
                }
                if(PhoneNr.isEmpty()){
                    UserPhoneNumber.setError("Phone Nr  is required");
                    return;
                }
                if(Age.isEmpty()){
                    UserAge.setError("Age is required");
                    return;
                }
                if(FirstLineOfAddress.isEmpty()){
                    UserFirstLineOfAddress.setError("Address is required");
                    return;
                }
              // Add password check

                Toast.makeText(Register.this,"Data Valid",Toast.LENGTH_SHORT).show();


                fAuth.createUserWithEmailAndPassword(Email,Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Redirect the user to new activity
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }
}
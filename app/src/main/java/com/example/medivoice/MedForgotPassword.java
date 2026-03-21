package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MedForgotPassword extends AppCompatActivity {

    EditText edtEmail;
    ImageView imgBack;
    Button btnResetPassword, btnBack;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        mAuth = FirebaseAuth.getInstance();
        imgBack = findViewById(R.id.imgBack);

        imgBack.setOnClickListener(v -> {
            startActivity(new Intent(MedForgotPassword.this, MedCaregiverLogin.class));
            finish();
        });

        btnResetPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Email is required");
                return;
            }

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MedForgotPassword.this,
                            "Password reset link sent to your email",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MedForgotPassword.this,
                            "Failed: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}

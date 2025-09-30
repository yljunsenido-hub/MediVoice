package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GuardianLogin extends AppCompatActivity {

    Button btnLogin, btnRegister;
    EditText emailPass2, password2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        emailPass2 = findViewById(R.id.emailPass2);
        password2 = findViewById(R.id.password2);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = emailPass2.getText().toString().trim();
            String pass = password2.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailPass2.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(pass)) {
                password2.setError("Password is required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkIfConnected(user.getUid());
                            }
                            Toast.makeText(GuardianLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GuardianLogin.this, VoiceRecord.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(GuardianLogin.this,
                                    "Wrong credentials",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(GuardianLogin.this, GuardianRegister.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkIfConnected(String guardianId) {
        DatabaseReference guardianRef = FirebaseDatabase.getInstance()
                .getReference("Guardians")
                .child(guardianId)
                .child("users");  // <-- where linked users are stored

        guardianRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Intent intent = new Intent(this, GuardianHomePage.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, EnterCodePage.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to check connection",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
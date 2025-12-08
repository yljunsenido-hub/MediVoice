package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedNurseLogin extends AppCompatActivity {

        Button btnLogin, btnRegister;
        ImageButton backButton;
        EditText emailPass2, password2;
        FirebaseAuth mAuth;
        DatabaseReference nurseRef;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_med_nurse_login);

            // FIXED: ImageButton instead of Button
            btnLogin = findViewById(R.id.btnLogin);
            btnRegister = findViewById(R.id.btnRegister);
            backButton = findViewById(R.id.btnBack);
            emailPass2 = findViewById(R.id.emailPass2);
            password2 = findViewById(R.id.password2);


            mAuth = FirebaseAuth.getInstance();
        nurseRef = FirebaseDatabase.getInstance().getReference("Nurse");

        // Back button: go back to role selection / WelcomePage
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MedNurseLogin.this, MedWelcome.class);
            startActivity(intent);
            finish();
        });

        // Login button
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
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) {
                                Toast.makeText(MedNurseLogin.this,
                                        "Error: user not found",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = firebaseUser.getUid();

                            // Check if this UID exists under "Nurse" and has role "nurse"
                            nurseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        if ("nurse".equalsIgnoreCase(role)) {
                                            Toast.makeText(MedNurseLogin.this,
                                                    "Login successful",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(MedNurseLogin.this, MedNurseHomepage.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // role mismatch
                                            mAuth.signOut();
                                            Toast.makeText(MedNurseLogin.this,
                                                    "This account is not a nurse account.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Not found under Nurse node
                                        mAuth.signOut();
                                        Toast.makeText(MedNurseLogin.this,
                                                "This account is not registered as Nurse.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    mAuth.signOut();
                                    Toast.makeText(MedNurseLogin.this,
                                            "Database error: " + error.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(MedNurseLogin.this,
                                    "Wrong credentials",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Go to nurse register screen
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MedNurseLogin.this, MedNurseRegister.class);
            startActivity(intent);
            finish();
        });
    }
}

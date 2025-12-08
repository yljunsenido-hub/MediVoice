package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageButton;

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

public class MedCaregiverLogin extends AppCompatActivity {

    Button btnLogin, btnRegister, backButton;
    EditText emailPass2, password2;
    FirebaseAuth mAuth;

    ImageView imgBack;
    DatabaseReference caregiverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_caregiver_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        imgBack = findViewById(R.id.imgBack);
        emailPass2 = findViewById(R.id.emailPass2);
        password2 = findViewById(R.id.password2);

        mAuth = FirebaseAuth.getInstance();
        caregiverRef = FirebaseDatabase.getInstance().getReference("Caregiver");

        // Back button (go back to role selection / WelcomePage)
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(MedCaregiverLogin.this, MedWelcome.class);
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
                                Toast.makeText(MedCaregiverLogin.this,
                                        "Error: user not found",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = firebaseUser.getUid();

                            // Check if this UID exists under "Caregiver" and has role "caregiver"
                            caregiverRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        if ("caregiver".equalsIgnoreCase(role)) {
                                            Toast.makeText(MedCaregiverLogin.this,
                                                    "Login successful",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(MedCaregiverLogin.this, MedCaregiverHomepage.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            mAuth.signOut();
                                            Toast.makeText(MedCaregiverLogin.this,
                                                    "This account is not a caregiver account.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        mAuth.signOut();
                                        Toast.makeText(MedCaregiverLogin.this,
                                                "This account is not registered as Caregiver.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    mAuth.signOut();
                                    Toast.makeText(MedCaregiverLogin.this,
                                            "Database error: " + error.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(MedCaregiverLogin.this,
                                    "Wrong credentials",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Go to caregiver register screen
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MedCaregiverLogin.this, MedCaregiverRegister.class);
            startActivity(intent);
            finish();
        });
    }
}

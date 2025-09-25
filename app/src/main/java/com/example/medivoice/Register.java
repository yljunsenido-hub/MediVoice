package com.example.medivoice;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    EditText firstName, middleName, lastName, address, contactNumber, emailPass, password;
    Button btnRegister, btnBack;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstName = findViewById(R.id.firstName);
        middleName = findViewById(R.id.middleName);
        lastName = findViewById(R.id.lastName);
        address = findViewById(R.id.address);
        contactNumber = findViewById(R.id.contactNumber);
        emailPass = findViewById(R.id.emailPass);
        password = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        btnRegister.setOnClickListener(v -> {
            String fName = firstName.getText().toString().trim();
            String mName = middleName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String add = address.getText().toString().trim();
            String contact = contactNumber.getText().toString().trim();
            String email = emailPass.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (TextUtils.isEmpty(fName)) {
                firstName.setError("First Name is required");
                return;
            }
            if (TextUtils.isEmpty(mName)) {
                middleName.setError("Middle Name is required");
                return;
            }
            if (TextUtils.isEmpty(lName)) {
                lastName.setError("Last Name is required");
                return;
            }
            if (TextUtils.isEmpty(contact)) {
                contactNumber.setError("Contact Number is required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailPass.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(pass) || pass.length() < 6) {
                password.setError("Password must be at least 6 characters");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserData(user, fName, mName, lName, add, contact, email);

                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(Register.this,
                                                        "Verification email sent. Please check your inbox.",
                                                        Toast.LENGTH_LONG).show();

                                                mAuth.signOut(); // sign out until email is verified
                                                startActivity(new Intent(Register.this, Login.class));
                                                finish();
                                            } else {
                                                Toast.makeText(Register.this,
                                                        "Failed to send verification: " +
                                                                verifyTask.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Register.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveUserData(FirebaseUser user, String fName, String mName,
                              String lName, String add, String contact, String email) {
        String userId = user.getUid();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", fName);
        userMap.put("middleName", mName);
        userMap.put("lastName", lName);
        userMap.put("address", add);
        userMap.put("contactNumber", contact);
        userMap.put("email", email);

        usersRef.child(userId).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Register.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this, "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}

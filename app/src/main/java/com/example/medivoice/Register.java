package com.example.medivoice;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    EditText firstName, middleName, lastName, address, contactNumber, emailPass, password;
    Button btnRegister, btnBack;
    FirebaseAuth mAuth;

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

            //if (TextUtils.isEmpty(add)) {
            //    address.setError("Address is required");
            //    return;
            //}

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
                            String uid = mAuth.getCurrentUser().getUid();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("First Name", fName);
                            userMap.put("Middle Name", mName);
                            userMap.put("Last Name", lName);
                            userMap.put("Address", add);
                            userMap.put("Contact Number", contact);
                            userMap.put("Email", email);

                            DatabaseReference db = FirebaseDatabase.getInstance().getReference();

                            // Save user details under Users/{uid}
                            db.child("Users").child(uid).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            // Also save to Usernames/{name} = uid
                                            db.child("Usernames").child(fName).setValue(uid);

                                            Toast.makeText(Register.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                            Log.d("Register", "User data + secondary index saved successfully");
                                        } else {
                                            Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                            Log.e("Register", "Error saving user data", dbTask.getException());
                                        }
                                    });
                        } else {
                            Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        //btnBack.setOnClickListener(v -> {
        //    Intent intent = new Intent(Register.this, Login.class);
        //    startActivity(intent);
        //    finish(); // Optional: closes the current activity
        //});
    }
}

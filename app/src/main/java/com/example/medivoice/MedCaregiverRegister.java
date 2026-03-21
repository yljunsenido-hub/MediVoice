package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.HashMap;

public class MedCaregiverRegister extends AppCompatActivity {

    EditText employeeNumber, firstName, lastName, age, contactNumber, shift, emailPass, password;
    Button btnRegister;
    ImageView btnBack;
    DatabaseReference requestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_caregiver_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        employeeNumber = findViewById(R.id.employeeNumber);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        age = findViewById(R.id.age);
        contactNumber = findViewById(R.id.contactNumber);
        shift = findViewById(R.id.shift);
        emailPass = findViewById(R.id.emailPass);
        password = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        requestRef = FirebaseDatabase.getInstance()
                .getReference("Employee_Request")
                .child("caregiver");

        btnRegister.setOnClickListener(v -> submitRequest());

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(
                    MedCaregiverRegister.this,
                    MedCaregiverLogin.class));
            finish();
        });
    }

    private void submitRequest() {

        String empNo = employeeNumber.getText().toString().trim();
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String age = this.age.getText().toString().trim();
        String contact = contactNumber.getText().toString().trim();
        String shift = this.shift.getText().toString().trim();
        String email = emailPass.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (TextUtils.isEmpty(empNo)) {
            employeeNumber.setError("Employee Number required");
            return;
        }

        if (TextUtils.isEmpty(fName)) {
            firstName.setError("First Name required");
            return;
        }

        if (TextUtils.isEmpty(lName)) {
            lastName.setError("Last Name required");
            return;
        }
        if (TextUtils.isEmpty(age)) {
            this.age.setError("Age required");
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            contactNumber.setError("Contact required");
            return;
        }
        if (TextUtils.isEmpty(shift)) {
            this.shift.setError("Shift required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailPass.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        String requestId = requestRef.push().getKey();

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("employeeNumber", empNo);
        requestMap.put("firstName", fName);
        requestMap.put("lastName", lName);
        requestMap.put("age", age);
        requestMap.put("contactNumber", contact);
        requestMap.put("shift", shift);
        requestMap.put("email", email);
        requestMap.put("password", pass);
        requestMap.put("status", "pending");
        requestMap.put("role", "caregiver");

        requestRef.child(requestId)
                .setValue(requestMap)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            MedCaregiverRegister.this,
                            "Request submitted. Wait for admin approval.",
                            Toast.LENGTH_LONG).show();

                    startActivity(new Intent(
                            MedCaregiverRegister.this,
                            MedCaregiverLogin.class));

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                MedCaregiverRegister.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
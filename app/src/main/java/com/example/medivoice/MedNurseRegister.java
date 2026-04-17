package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MedNurseRegister extends AppCompatActivity {

    EditText employeeNumber, firstName, lastName, age, contactNumber, shift, emailPass, password;
    Button btnRegister;
    ImageView btnBack;
    CheckBox checkboxTerms;

    DatabaseReference requestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_nurse_register);

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
        checkboxTerms = findViewById(R.id.checkboxTerms);

        requestRef = FirebaseDatabase.getInstance()
                .getReference("Employee_Request")
                .child("nurse");

        btnRegister.setOnClickListener(v -> submitRequest());

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(
                    MedNurseRegister.this,
                    MedNurseLogin.class));
            finish();
        });

        checkboxTerms.setOnClickListener(v -> {
            if (checkboxTerms.isChecked()) {
                showTermsDialog();
            }
        });
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage(
                        "By clicking \"I Agree\" and registering for the MediVoice Mobile Application (the \"Service\"), you agree to be bound by these Terms and Conditions.\n\n" +

                                "1. Eligibility and Registration\n" +
                                "- Only for caregivers and nurses of One Cainta Sunset Retreat.\n" +
                                "- Information must be true and complete.\n\n" +

                                "2. Purpose\n" +
                                "- Reduce medication risk\n" +
                                "- Provide alerts and notifications\n" +
                                "- Improve workflow\n\n" +

                                "3. Data Privacy\n" +
                                "- Your data will be protected\n" +
                                "- Not shared for marketing\n\n" +

                                "4. Responsibilities\n" +
                                "- Keep password secure\n\n" +

                                "5. Termination\n" +
                                "- Account may be terminated if terms are violated\n\n" +

                                "6. Governing Law\n" +
                                "- Republic of the Philippines"
                )
                .setPositiveButton("I Agree", (dialog, which) -> {
                    checkboxTerms.setChecked(true);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    checkboxTerms.setChecked(false);
                })
                .setCancelable(false)
                .show();
    }

    private void submitRequest() {

        if (!checkboxTerms.isChecked()) {
            Toast.makeText(this, "You must agree to Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        String empNo = employeeNumber.getText().toString().trim();
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String ageStr = age.getText().toString().trim();
        String contact = contactNumber.getText().toString().trim();
        String shiftStr = shift.getText().toString().trim();
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

        if (TextUtils.isEmpty(ageStr)) {
            age.setError("Age required");
            return;
        }

        if (TextUtils.isEmpty(contact)) {
            contactNumber.setError("Contact required");
            return;
        }

        if (TextUtils.isEmpty(shiftStr)) {
            shift.setError("Shift required");
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
        requestMap.put("age", ageStr);
        requestMap.put("contactNumber", contact);
        requestMap.put("shift", shiftStr);
        requestMap.put("email", email);
        requestMap.put("password", pass);
        requestMap.put("status", "pending");
        requestMap.put("role", "nurse");

        requestRef.child(requestId)
                .setValue(requestMap)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            MedNurseRegister.this,
                            "Request submitted. Wait for admin approval.",
                            Toast.LENGTH_LONG).show();

                    startActivity(new Intent(
                            MedNurseRegister.this,
                            MedNurseLogin.class));

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                MedNurseRegister.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
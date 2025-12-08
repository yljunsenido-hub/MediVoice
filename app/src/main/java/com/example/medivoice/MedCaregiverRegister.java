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

    EditText firstName, lastName, age, shift, contactNumber, emailPass, password;
    Button btnRegister;
    ImageView btnBack;
    FirebaseAuth mAuth;
    DatabaseReference usersRef; // this will now point to "Caregiver"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_caregiver_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstName = findViewById(R.id.firstName);
        age = findViewById(R.id.age);
        lastName = findViewById(R.id.lastName);
        contactNumber = findViewById(R.id.contactNumber);
        emailPass = findViewById(R.id.emailPass);
        password = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference("Caregiver");

        btnRegister.setOnClickListener(v -> {
            String fName = firstName.getText().toString().trim();
            String agee = age.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String shiftt = shift.getText().toString().trim();
            String contact = contactNumber.getText().toString().trim();
            String email = emailPass.getText().toString().trim();
            String pass = password.getText().toString().trim();

            // Validations
            if (TextUtils.isEmpty(fName)) {
                firstName.setError("First Name is required");
                return;
            }
            if (TextUtils.isEmpty(agee)) {
                age.setError("Age is required");
                return;
            }
            if (TextUtils.isEmpty(lName)) {
                lastName.setError("Last Name is required");
                return;
            }
            if (TextUtils.isEmpty(shiftt)) {
                shift.setError("Shift is required");
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
                                // Save caregiver data
                                saveUserData(user, fName, agee, lName, shiftt, contact, email);

                                Toast.makeText(MedCaregiverRegister.this,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(MedCaregiverRegister.this, MedCaregiverLogin.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(MedCaregiverRegister.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(MedCaregiverRegister.this, MedCaregiverLogin.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveUserData(FirebaseUser user,
                              String fName,
                              String age,
                              String lName,
                              String shift,
                              String contact,
                              String email) {

        String userId = user.getUid();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", fName);
        userMap.put("lastName", lName);
        userMap.put("age", age);
        userMap.put("shift", shift);
        userMap.put("contactNumber", contact);
        userMap.put("email", email);
        userMap.put("role", "caregiver");

        usersRef.child(userId).setValue(userMap)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(MedCaregiverRegister.this,
                                "User data saved successfully",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(MedCaregiverRegister.this,
                                "Failed to save user data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}

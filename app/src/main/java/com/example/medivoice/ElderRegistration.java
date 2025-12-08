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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ElderRegistration extends AppCompatActivity {

    EditText etName, etAge, etBirthday, etGender, etCivilStatus, etHomeAddress;
    EditText etPrimaryContactPerson, etPrimaryRelationship, etPrimaryContactNumber;
    EditText etSecondaryContactPerson, etSecondaryRelationship, etSecondaryContactNumber;
    EditText etAllergies, etCognitiveStatus, etDisabilities;

    Button btnListOfElderly, btnSubmitRegister;

    BottomNavigationView bottomNav;

    FirebaseAuth mAuth;
    DatabaseReference eldersRef;
    DatabaseReference nursesRef; // to get nurse info (name)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_elder_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        eldersRef = FirebaseDatabase.getInstance().getReference("Elders");
        nursesRef = FirebaseDatabase.getInstance().getReference("Nurse"); // make sure this matches your nurse node

        // Top buttons
        btnListOfElderly = findViewById(R.id.btnListOfElderly);

        // Inputs - Basic Personal Info
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etBirthday = findViewById(R.id.etBirthday);
        etGender = findViewById(R.id.etGender);
        etCivilStatus = findViewById(R.id.etCivilStatus);
        etHomeAddress = findViewById(R.id.etHomeAddress);

        // Family & Emergency Contact
        etPrimaryContactPerson = findViewById(R.id.etPrimaryContactPerson);
        etPrimaryRelationship = findViewById(R.id.etPrimaryRelationship);
        etPrimaryContactNumber = findViewById(R.id.etPrimaryContactNumber);
        etSecondaryContactPerson = findViewById(R.id.etSecondaryContactPerson);
        etSecondaryRelationship = findViewById(R.id.etSecondaryRelationship);
        etSecondaryContactNumber = findViewById(R.id.etSecondaryContactNumber);

        // Medical & Health Info
        etAllergies = findViewById(R.id.etAllergies);
        etCognitiveStatus = findViewById(R.id.etCognitiveStatus);
        etDisabilities = findViewById(R.id.etDisabilities);

        // Bottom submit button
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);

        // Bottom REGISTER button
        btnSubmitRegister.setOnClickListener(v -> saveElder());

        // LIST OF ELDERLY button (open list activity – you can create it)
        btnListOfElderly.setOnClickListener(v -> {
            Intent intent = new Intent(ElderRegistration.this, ElderList.class);
            startActivity(intent);
        });

        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_medLog) {
                return true; // Already home
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(ElderRegistration.this, MedNurseHomepage.class));
                return true;
            } else if (id == R.id.nav_elderlyStatLog) {
                startActivity(new Intent(ElderRegistration.this, ElderList.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(ElderRegistration.this, NurseProfile.class));
                return true;
            }
            return false;
        });
    }

    private void saveElder() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String civilStatus = etCivilStatus.getText().toString().trim();
        String homeAddress = etHomeAddress.getText().toString().trim();

        String primaryContactPerson = etPrimaryContactPerson.getText().toString().trim();
        String primaryRelationship = etPrimaryRelationship.getText().toString().trim();
        String primaryContactNumber = etPrimaryContactNumber.getText().toString().trim();

        String secondaryContactPerson = etSecondaryContactPerson.getText().toString().trim();
        String secondaryRelationship = etSecondaryRelationship.getText().toString().trim();
        String secondaryContactNumber = etSecondaryContactNumber.getText().toString().trim();

        String allergies = etAllergies.getText().toString().trim();
        String cognitiveStatus = etCognitiveStatus.getText().toString().trim();
        String disabilities = etDisabilities.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            return;
        }
        if (TextUtils.isEmpty(primaryContactPerson)) {
            etPrimaryContactPerson.setError("Primary contact is required");
            return;
        }
        if (TextUtils.isEmpty(primaryContactNumber)) {
            etPrimaryContactNumber.setError("Primary contact number is required");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in as a nurse", Toast.LENGTH_SHORT).show();
            return;
        }

        String nurseId = currentUser.getUid();

        long timestampMillis = System.currentTimeMillis();
        String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(timestampMillis));

        HashMap<String, Object> elderMap = new HashMap<>();
        elderMap.put("name", name);
        elderMap.put("age", age);
        elderMap.put("birthday", birthday);
        elderMap.put("gender", gender);
        elderMap.put("civilStatus", civilStatus);
        elderMap.put("homeAddress", homeAddress);

        elderMap.put("primaryContactPerson", primaryContactPerson);
        elderMap.put("primaryRelationship", primaryRelationship);
        elderMap.put("primaryContactNumber", primaryContactNumber);

        elderMap.put("secondaryContactPerson", secondaryContactPerson);
        elderMap.put("secondaryRelationship", secondaryRelationship);
        elderMap.put("secondaryContactNumber", secondaryContactNumber);

        elderMap.put("allergies", allergies);
        elderMap.put("cognitiveStatus", cognitiveStatus);
        elderMap.put("disabilities", disabilities);

        // Nurse information & timestamp
        elderMap.put("nurseId", nurseId);
        elderMap.put("nurseName", ""); // will fill after fetching nurse’s name
        elderMap.put("timestampMillis", timestampMillis);
        elderMap.put("timestampFormatted", formattedTime);

        String elderId = eldersRef.push().getKey();
        if (elderId == null) {
            Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // First fetch nurse name then save elder
        nursesRef.child(nurseId).get().addOnSuccessListener(snapshot -> {
            String nurseFirstName = snapshot.child("firstName").getValue(String.class);
            String nurseLastName = snapshot.child("lastName").getValue(String.class);

            String nurseName = "";
            if (nurseFirstName != null) nurseName += nurseFirstName;
            if (nurseLastName != null) nurseName += (nurseName.isEmpty() ? "" : " ") + nurseLastName;

            elderMap.put("nurseName", nurseName);

            eldersRef.child(elderId).setValue(elderMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(ElderRegistration.this,
                                "Elder registered successfully",
                                Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ElderRegistration.this,
                                    "Failed to save: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e ->
                Toast.makeText(ElderRegistration.this,
                        "Failed to get nurse info: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etName.setText("");
        etAge.setText("");
        etBirthday.setText("");
        etGender.setText("");
        etCivilStatus.setText("");
        etHomeAddress.setText("");

        etPrimaryContactPerson.setText("");
        etPrimaryRelationship.setText("");
        etPrimaryContactNumber.setText("");
        etSecondaryContactPerson.setText("");
        etSecondaryRelationship.setText("");
        etSecondaryContactNumber.setText("");

        etAllergies.setText("");
        etCognitiveStatus.setText("");
        etDisabilities.setText("");
    }
}

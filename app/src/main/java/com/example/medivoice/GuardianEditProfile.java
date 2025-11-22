package com.example.medivoice;

import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GuardianEditProfile extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText middleNameEditText;
    private EditText lastNameEditText;
    private EditText addressEditText;
    private EditText contactNumberEditText;
    private EditText emailEditText;
    private Button updateProfile;
    private Button backButton;

    private DatabaseReference guardianRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstNameEditText = findViewById(R.id.firstName);
        middleNameEditText = findViewById(R.id.middleName);
        lastNameEditText = findViewById(R.id.lastName);
        addressEditText = findViewById(R.id.address);
        contactNumberEditText = findViewById(R.id.contactNumber);
        emailEditText = findViewById(R.id.emailPass);
        updateProfile = findViewById(R.id.updateProfile);
        backButton = findViewById(R.id.backButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No logged in user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // Directly point to Guardians/{uid}
        guardianRef = db.getReference("Guardians").child(uid);

        loadGuardianData();

        updateProfile.setOnClickListener(v -> saveGuardianData());

        backButton.setOnClickListener(v -> finish());
    }

    private void loadGuardianData() {
        guardianRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String middleName = snapshot.child("middleName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String contactNumber = snapshot.child("contactNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (firstName != null) firstNameEditText.setText(firstName);
                    if (middleName != null) middleNameEditText.setText(middleName);
                    if (lastName != null) lastNameEditText.setText(lastName);
                    if (address != null) addressEditText.setText(address);
                    if (contactNumber != null) contactNumberEditText.setText(contactNumber);
                    if (email != null) emailEditText.setText(email);
                } else {
                    Toast.makeText(GuardianEditProfile.this,
                            "Guardian data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GuardianEditProfile.this,
                        "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveGuardianData() {
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contactNumber = contactNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        guardianRef.child("firstName").setValue(firstName);
        guardianRef.child("middleName").setValue(middleName);
        guardianRef.child("lastName").setValue(lastName);
        guardianRef.child("address").setValue(address);
        guardianRef.child("contactNumber").setValue(contactNumber);
        guardianRef.child("email").setValue(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(GuardianEditProfile.this,
                                "Guardian profile updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GuardianEditProfile.this,
                                "Failed to update guardian profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

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

public class EditProfile extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText middleNameEditText;
    private EditText lastNameEditText;
    private EditText addressEditText;
    private EditText contactNumberEditText;
    private EditText emailEditText;
    private Button updateProfile;

    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

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

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No logged in user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        loadUserData();

        updateProfile.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Toast.makeText(EditProfile.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contactNumber = contactNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        userRef.child("firstName").setValue(firstName);
        userRef.child("middleName").setValue(middleName);
        userRef.child("lastName").setValue(lastName);
        userRef.child("address").setValue(address);
        userRef.child("contactNumber").setValue(contactNumber);
        userRef.child("email").setValue(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

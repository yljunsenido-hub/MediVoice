package com.example.medivoice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class EmergencyContact extends AppCompatActivity {

    private Button backButton, addMoreContact, saveContact;
    private EditText sosPerson, sosContact;
    private TextView personLabel;
    private FirebaseAuth auth;
    private DatabaseReference contactsRef;
    private int personCount = 1; // to update "Person 1", "Person 2", etc.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_contact);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.backButton);
        addMoreContact = findViewById(R.id.addMoreContact);
        saveContact = findViewById(R.id.saveContact);
        sosPerson = findViewById(R.id.sosPerson);
        sosContact = findViewById(R.id.sosContact);
        personLabel = findViewById(R.id.person);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reference: Users/<uid>/EmergencyContacts
        contactsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid())
                .child("EmergencyContacts");

        backButton.setOnClickListener(v -> finish());

        saveContact.setOnClickListener(v -> saveContactToFirebase());

        addMoreContact.setOnClickListener(v -> {
            // Clear inputs and increment person number (just UI)
            sosPerson.setText("");
            sosContact.setText("");
            personCount++;
            personLabel.setText("Person " + personCount);
        });
    }

    private void saveContactToFirebase() {
        String name = sosPerson.getText().toString().trim();
        String contact = sosContact.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contact.isEmpty()) {
            Toast.makeText(this, "Please enter a contact number!", Toast.LENGTH_SHORT).show();
            return;
        }

        String contactId = contactsRef.push().getKey();
        if (contactId == null) {
            Toast.makeText(this, "Failed to generate contact ID", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("contact", contact);

        contactsRef.child(contactId).setValue(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
                    // You can either clear or keep the fields here; I'll clear them:
                    sosPerson.setText("");
                    sosContact.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

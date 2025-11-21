package com.example.medivoice;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private FirebaseAuth auth;
    private DatabaseReference contactsRef;

    private LinearLayout personContainer;
    private int personCount = 0; // start from 0, will increment when adding blocks

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
        personContainer = findViewById(R.id.personContainer);

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

        // First person block
        addPersonBlock();

        addMoreContact.setOnClickListener(v -> addPersonBlock());

        saveContact.setOnClickListener(v -> saveAllContacts());
    }

    private void addPersonBlock() {
        personCount++;

        // Container for this person
        LinearLayout personBlock = new LinearLayout(this);
        personBlock.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams personParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        personParams.setMargins(0, dpToPx(10), 0, dpToPx(10));
        personBlock.setLayoutParams(personParams);

        // "Person X" TextView
        TextView personLabel = new TextView(this);
        personLabel.setText("Person " + personCount);
        personLabel.setTextSize(25);
        personLabel.setTextColor(getResources().getColor(R.color.white));
        personLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(dpToPx(45), dpToPx(10), 0, 0);
        personLabel.setLayoutParams(labelParams);

        // Name container
        LinearLayout nameLayout = new LinearLayout(this);
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                dpToPx(320),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.gravity = Gravity.CENTER_HORIZONTAL;
        nameParams.setMargins(0, dpToPx(10), 0, dpToPx(10));
        nameLayout.setLayoutParams(nameParams);
        nameLayout.setPadding(dpToPx(13), dpToPx(13), dpToPx(13), dpToPx(13));
        nameLayout.setBackground(getResources().getDrawable(R.drawable.input_bg));

        EditText nameEditText = new EditText(this);
        nameEditText.setHint("Full Name");
        nameEditText.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        nameEditText.setTextColor(android.graphics.Color.BLACK);
        nameEditText.setTextSize(16);
        nameEditText.setPadding(dpToPx(10), 0, 0, 0);
        nameEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        nameLayout.addView(nameEditText);

        // Contact container
        LinearLayout contactLayout = new LinearLayout(this);
        contactLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams contactParams = new LinearLayout.LayoutParams(
                dpToPx(320),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contactParams.gravity = Gravity.CENTER_HORIZONTAL;
        contactParams.setMargins(0, 0, 0, dpToPx(10));
        contactLayout.setLayoutParams(contactParams);
        contactLayout.setPadding(dpToPx(13), dpToPx(13), dpToPx(13), dpToPx(13));
        contactLayout.setBackground(getResources().getDrawable(R.drawable.input_bg));

        EditText contactEditText = new EditText(this);
        contactEditText.setHint("Contact Number");
        contactEditText.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        contactEditText.setTextColor(android.graphics.Color.BLACK);
        contactEditText.setTextSize(16);
        contactEditText.setPadding(dpToPx(10), 0, 0, 0);
        contactEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        contactLayout.addView(contactEditText);

        // Tag the views so we can find them later when saving
        personBlock.setTag("personBlock");
        nameEditText.setTag("nameEditText");
        contactEditText.setTag("contactEditText");

        // Add to person block
        personBlock.addView(personLabel);
        personBlock.addView(nameLayout);
        personBlock.addView(contactLayout);

        // Finally add to main container
        personContainer.addView(personBlock);
    }

    private void saveAllContacts() {
        int childCount = personContainer.getChildCount();
        boolean hasAtLeastOne = false;

        for (int i = 0; i < childCount; i++) {
            View block = personContainer.getChildAt(i);
            if (!(block instanceof LinearLayout)) continue;

            LinearLayout personBlock = (LinearLayout) block;

            // Find name & contact edittexts inside this block
            EditText nameEditText = null;
            EditText contactEditText = null;

            // Loop children to find tagged views
            for (int j = 0; j < personBlock.getChildCount(); j++) {
                View inner = personBlock.getChildAt(j);

                if (inner instanceof LinearLayout) {
                    LinearLayout container = (LinearLayout) inner;
                    for (int k = 0; k < container.getChildCount(); k++) {
                        View field = container.getChildAt(k);
                        if (field instanceof EditText) {
                            Object tag = field.getTag();
                            if (tag != null && tag.equals("nameEditText")) {
                                nameEditText = (EditText) field;
                            } else if (tag != null && tag.equals("contactEditText")) {
                                contactEditText = (EditText) field;
                            }
                        }
                    }
                }
            }

            if (nameEditText == null || contactEditText == null) continue;

            String name = nameEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();

            if (name.isEmpty() && contact.isEmpty()) {
                // Skip completely empty blocks
                continue;
            }

            if (name.isEmpty()) {
                Toast.makeText(this, "One of the contacts has no name.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (contact.isEmpty()) {
                Toast.makeText(this, "One of the contacts has no number.", Toast.LENGTH_SHORT).show();
                return;
            }

            hasAtLeastOne = true;

            String contactId = contactsRef.push().getKey();
            if (contactId == null) {
                Toast.makeText(this, "Failed to generate contact ID", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("contact", contact);

            contactsRef.child(contactId).setValue(data);
        }

        if (!hasAtLeastOne) {
            Toast.makeText(this, "Please fill at least one contact.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "All emergency contacts saved!", Toast.LENGTH_SHORT).show();
        finish(); // go back after saving
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

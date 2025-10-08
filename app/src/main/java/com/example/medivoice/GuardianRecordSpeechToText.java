package com.example.medivoice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class GuardianRecordSpeechToText extends AppCompatActivity {

    Button createButton;
    LinearLayout recordsContainer;
    FirebaseAuth mAuth;
    DatabaseReference guardianRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_record_speech_to_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createButton = findViewById(R.id.createButton);
        recordsContainer = findViewById(R.id.recordsContainer);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser guardian = mAuth.getCurrentUser();

        if (guardian != null) {
            String guardianId = guardian.getUid();
            guardianRef = FirebaseDatabase.getInstance().getReference("Guardians").child(guardianId).child("users");
            usersRef = FirebaseDatabase.getInstance().getReference("Users");

            loadConnectedUsersRecords();
        }
    }

    private void loadConnectedUsersRecords() {
        guardianRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                recordsContainer.removeAllViews();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        loadSpeechRecordsForUser(userId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void loadSpeechRecordsForUser(String userId) {
        DatabaseReference speechRef = usersRef.child(userId).child("SpeechToText");

        speechRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    SpeechRecord record = recordSnapshot.getValue(SpeechRecord.class);
                    if (record != null) {
                        addRecordCard(record);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void addRecordCard(SpeechRecord record) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_speech_record, recordsContainer, false);

        TextView nameView = cardView.findViewById(R.id.nameView);
        TextView dateView = cardView.findViewById(R.id.dateView);
        TextView textView = cardView.findViewById(R.id.textView);
        Button deleteButton = cardView.findViewById(R.id.deleteButton);

        // Guardian should not delete — hide the button
        deleteButton.setVisibility(View.GONE);

        nameView.setText(record.name);
        dateView.setText("Date: " + record.date);
        textView.setText("Text: " + record.text);
        textView.setVisibility(View.GONE);

        cardView.setOnClickListener(v -> {
            if (textView.getVisibility() == View.GONE) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        });

        recordsContainer.addView(cardView);
    }
}

package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecordSpeechToText extends AppCompatActivity {

    Button createButton;
    LinearLayout recordsContainer;
    DatabaseReference userSpeechRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_speech_to_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createButton = findViewById(R.id.createButton);
        recordsContainer = findViewById(R.id.recordsContainer);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userSpeechRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("SpeechToText");

            loadSpeechRecords();
        }

        createButton.setOnClickListener(v ->
                startActivity(new Intent(this, TextToSpeech.class))
        );
    }

    private void loadSpeechRecords() {
        userSpeechRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                recordsContainer.removeAllViews();

                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    SpeechRecord record = recordSnapshot.getValue(SpeechRecord.class);
                    if (record != null) {
                        String recordKey = recordSnapshot.getKey();
                        addRecordCard(record, recordKey);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void addRecordCard(SpeechRecord record, String recordKey) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_speech_record, recordsContainer, false);

        Button deleteButton = cardView.findViewById(R.id.deleteButton);
        TextView nameView = cardView.findViewById(R.id.nameView);
        TextView dateView = cardView.findViewById(R.id.dateView);
        TextView textView = cardView.findViewById(R.id.textView);

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

        deleteButton.setOnClickListener(v -> {
            if (recordKey != null) {
                userSpeechRef.child(recordKey).removeValue()
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });

        recordsContainer.addView(cardView);
    }
}

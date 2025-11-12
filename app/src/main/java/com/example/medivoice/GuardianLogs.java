package com.example.medivoice;

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
import com.google.firebase.database.*;

public class GuardianLogs extends AppCompatActivity {

    LinearLayout recordsContainer;
    FirebaseAuth mAuth;
    DatabaseReference guardianRef;
    Button btnVoice, btnScanner, btnPrescription;
    String currentType = "SpeechToText"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_logs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recordsContainer = findViewById(R.id.recordsContainer);
        btnVoice = findViewById(R.id.btnVoice);
        btnScanner = findViewById(R.id.btnScanner);
        btnPrescription = findViewById(R.id.btnText);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentGuardian = mAuth.getCurrentUser();
        if (currentGuardian == null) {
            Toast.makeText(this, "Guardian not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        guardianRef = FirebaseDatabase.getInstance().getReference("Guardians")
                .child(currentGuardian.getUid());

        btnVoice.setOnClickListener(v -> {
            currentType = "SpeechToText";
            fetchConnectedUsersAndLogs();
        });
        btnScanner.setOnClickListener(v -> {
            currentType = "ImageToText";
            fetchConnectedUsersAndLogs();
        });
        btnPrescription.setOnClickListener(v -> {
            currentType = "PrescriptionText";
            fetchConnectedUsersAndLogs();
        });

        // load default
        fetchConnectedUsersAndLogs();
    }

    private void fetchConnectedUsersAndLogs() {
        guardianRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                recordsContainer.removeAllViews();
                if (!snapshot.exists()) {
                    Toast.makeText(GuardianLogs.this, "No connected patients found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Boolean isConnected = userSnapshot.getValue(Boolean.class);
                    if (isConnected != null && isConnected) {
                        String userId = userSnapshot.getKey();
                        fetchPatientLogs(userId, currentType);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GuardianLogs.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPatientLogs(String userId, String type) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child(type);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    if (type.equals("PrescriptionText")) {
                        PrescriptionRecord record = recordSnapshot.getValue(PrescriptionRecord.class);
                        if (record != null) addPrescriptionCard(record, userId);
                    } else {
                        SpeechRecord record = recordSnapshot.getValue(SpeechRecord.class);
                        if (record != null) addSpeechOrImageCard(record, userId, type);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void addSpeechOrImageCard(SpeechRecord record, String patientName, String type) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_speech_record, recordsContainer, false);
        TextView nameView = card.findViewById(R.id.nameView);
        TextView dateView = card.findViewById(R.id.dateView);
        TextView textView = card.findViewById(R.id.textView);
        Button deleteButton = card.findViewById(R.id.deleteButton);

        if (type.equals("SpeechToText")) {
            // Show the record's name only
            nameView.setText(record.name);
        } else { // ImageToText
            // Show record name with optional prefix
            nameView.setText("Image Result: " + record.name);
        }

        dateView.setText("Date: " + record.date);
        textView.setText(record.text);
        textView.setVisibility(View.GONE);

        card.setOnClickListener(v -> textView.setVisibility(textView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
        deleteButton.setVisibility(View.GONE);

        recordsContainer.addView(card);
    }

    private void addPrescriptionCard(PrescriptionRecord record, String patientName) {
        View card = LayoutInflater.from(this).inflate(R.layout.text_feature, recordsContainer, false);

        TextView nameView = card.findViewById(R.id.nameView);
        TextView startdateView = card.findViewById(R.id.startdateView);
        TextView scheduleView = card.findViewById(R.id.scheduleView);
        TextView durationView = card.findViewById(R.id.durationView);
        TextView dosageView = card.findViewById(R.id.dosageView);
        TextView notesView = card.findViewById(R.id.notesView);
        Button deleteButton = card.findViewById(R.id.deleteButton);

        // Show medication name only
        nameView.setText(record.medicationName);
        startdateView.setText("Start Date: " + record.startDate);
        scheduleView.setText("Schedule: " + record.schedule);
        durationView.setText("Duration: " + record.duration);
        dosageView.setText("Dosage: " + record.dosage);
        notesView.setText("Notes: " + record.notes);

        durationView.setVisibility(View.GONE);
        dosageView.setVisibility(View.GONE);
        notesView.setVisibility(View.GONE);

        card.setOnClickListener(v -> {
            boolean hidden = durationView.getVisibility() == View.GONE;
            durationView.setVisibility(hidden ? View.VISIBLE : View.GONE);
            dosageView.setVisibility(hidden ? View.VISIBLE : View.GONE);
            notesView.setVisibility(hidden ? View.VISIBLE : View.GONE);
        });

        deleteButton.setVisibility(View.GONE);
        recordsContainer.addView(card);
    }
}

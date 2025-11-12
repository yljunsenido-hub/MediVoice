package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class Logs extends AppCompatActivity {

    LinearLayout recordsContainer;
    DatabaseReference userLogsRef;
    FirebaseAuth mAuth;
    BottomNavigationView bottomNavigationView;
    Button btnVoice, btnScanner, btnText; // Text ignored for now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        btnVoice = findViewById(R.id.btnVoice);
        btnScanner = findViewById(R.id.btnScanner);
        btnText = findViewById(R.id.btnText);
        recordsContainer = findViewById(R.id.recordsContainer);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) return;

        // Load SpeechToText (Voice) by default
        loadData("SpeechToText");

        btnVoice.setOnClickListener(v -> loadData("SpeechToText"));
        btnScanner.setOnClickListener(v -> loadData("ImageToText"));
        btnText.setOnClickListener(v -> loadData("PrescriptionText"));

        // for bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_chat) {
                    return true; // Stay on Home
                } else if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                    overridePendingTransition(0, 0);
                    return true;
//                } else if (itemId == R.id.nav_mic) {
//                    startActivity(new Intent(getApplicationContext(), GuardianRecordActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
//                } else if (itemId == R.id.nav_profile) {
//                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
                }

                return false;
            }
        });
    }

    private void loadData(String type) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        userLogsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid())
                .child(type);

        userLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                recordsContainer.removeAllViews();

                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {

                    if (type.equals("PrescriptionText")) {
                        PrescriptionRecord record = recordSnapshot.getValue(PrescriptionRecord.class);
                        if (record != null) addPrescriptionCard(record, recordSnapshot.getKey());
                    } else {
                        SpeechRecord record = recordSnapshot.getValue(SpeechRecord.class);
                        if (record != null) addRecordCard(record, recordSnapshot.getKey());
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
            textView.setVisibility(textView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        deleteButton.setOnClickListener(v -> {
            if (recordKey != null) {
                userLogsRef.child(recordKey).removeValue()
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

    private void addPrescriptionCard(PrescriptionRecord record, String recordKey) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.text_feature, recordsContainer, false);

        TextView nameView = cardView.findViewById(R.id.nameView);
        TextView startdateView = cardView.findViewById(R.id.startdateView);
        TextView scheduleView = cardView.findViewById(R.id.scheduleView);
        TextView durationView = cardView.findViewById(R.id.durationView);
        TextView dosageView = cardView.findViewById(R.id.dosageView);
        TextView notesView = cardView.findViewById(R.id.notesView);
        Button deleteButton = cardView.findViewById(R.id.deleteButton);

        nameView.setText("Medicine: " + record.medicationName);
        startdateView.setText("Start Date: " + record.startDate);
        scheduleView.setText("Schedule: " + record.schedule);
        durationView.setText("Duration: " + record.duration);
        dosageView.setText("Dosage: " + record.dosage);
        notesView.setText("Notes: " + record.notes);

        // HIDE EXTRA DETAILS BY DEFAULT
        durationView.setVisibility(View.GONE);
        dosageView.setVisibility(View.GONE);
        notesView.setVisibility(View.GONE);

        // TOGGLE VISIBILITY ON CARD CLICK
        cardView.setOnClickListener(v -> {
            boolean isHidden = durationView.getVisibility() == View.GONE;
            durationView.setVisibility(isHidden ? View.VISIBLE : View.GONE);
            dosageView.setVisibility(isHidden ? View.VISIBLE : View.GONE);
            notesView.setVisibility(isHidden ? View.VISIBLE : View.GONE);
        });

        deleteButton.setOnClickListener(v -> {
            if (recordKey != null) {
                userLogsRef.child(recordKey).removeValue();
            }
        });

        recordsContainer.addView(cardView);
    }

}

package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TextFeaturePage extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference userSpeechRef;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_feature_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText medicationName = findViewById(R.id.medicationName);
        EditText dosage = findViewById(R.id.dosage);
        EditText schedule = findViewById(R.id.schedule);
        EditText duration = findViewById(R.id.duration);
        EditText notes = findViewById(R.id.notes);
        Button saveButton = findViewById(R.id.saveButton);
        CalendarView startDateCalendar = findViewById(R.id.startDate);

        final String[] selectedDate = {""};

        startDateCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String formattedDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                    .format(new GregorianCalendar(year, month, dayOfMonth).getTime());
            selectedDate[0] = formattedDate;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TextFeaturePage.this, HomePage.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            userSpeechRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("PrescriptionText");
        }

        saveButton.setOnClickListener(v -> {
            String medName = medicationName.getText().toString().trim();
            String dosageText = dosage.getText().toString().trim();
            String scheduleText = schedule.getText().toString().trim();
            String durationText = duration.getText().toString().trim();
            String notesText = notes.getText().toString().trim();
            String startDateText = selectedDate[0];

            if (medName.isEmpty()) {
                Toast.makeText(this, "Please enter a medication name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDateText.isEmpty()) {
                Toast.makeText(this, "Please select a start date!", Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = userSpeechRef.push().getKey();

            if (noteId != null) {
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("medicationName", medName);
                data.put("dosage", dosageText);
                data.put("schedule", scheduleText);
                data.put("duration", durationText);
                data.put("startDate", startDateText);
                data.put("notes", notesText);

                String currentTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                data.put("createdAt", currentTime);

                userSpeechRef.child(noteId).setValue(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Prescription record saved successfully!", Toast.LENGTH_SHORT).show();
                            medicationName.setText("");
                            dosage.setText("");
                            schedule.setText("");
                            duration.setText("");
                            notes.setText("");
                            selectedDate[0] = "";
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to save record: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}

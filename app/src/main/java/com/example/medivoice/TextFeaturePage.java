package com.example.medivoice;

import android.app.DatePickerDialog;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TextFeaturePage extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference userSpeechRef;

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
        EditText startDate = findViewById(R.id.startDate);
        EditText notes = findViewById(R.id.notes);
        Button saveButton = findViewById(R.id.saveButton);

        startDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                .format(new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime());
                        startDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
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
            String startDateText = startDate.getText().toString().trim();
            String notesText = notes.getText().toString().trim();

            if (medName.isEmpty()) {
                Toast.makeText(this, "Please enter a medication name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDateText.isEmpty()) {
                Toast.makeText(this, "Please enter a start date!", Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = userSpeechRef.push().getKey();

            if (noteId != null) {
                // Create a map to store all data
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("medicationName", medName);
                data.put("dosage", dosageText);
                data.put("schedule", scheduleText);
                data.put("duration", durationText);
                data.put("startDate", startDateText);
                data.put("notes", notesText);

                // Optionally add timestamp
                String currentTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                data.put("createdAt", currentTime);

                userSpeechRef.child(noteId).setValue(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Prescription record saved successfully!", Toast.LENGTH_SHORT).show();

                            medicationName.setText("");
                            dosage.setText("");
                            schedule.setText("");
                            duration.setText("");
                            startDate.setText("");
                            notes.setText("");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to save record: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
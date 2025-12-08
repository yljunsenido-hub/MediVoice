package com.example.medivoice;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedCaregiverRunningnotes extends AppCompatActivity {

    private LinearLayout runningNotesContainer;
    private DatabaseReference notesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_med_caregiver_runningnotes); // your XML layout

        runningNotesContainer = findViewById(R.id.runningNotesContainer);
        notesRef = FirebaseDatabase.getInstance().getReference("RunningNotes");

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                runningNotesContainer.removeAllViews();
                for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                    String noteText = noteSnapshot.child("text").getValue(String.class);
                    if (noteText != null) {
                        TextView noteView = new TextView(MedCaregiverRunningnotes.this);
                        noteView.setText(noteText);
                        noteView.setTextSize(14);
                        noteView.setPadding(8, 8, 8, 8);
                        noteView.setBackgroundResource(R.drawable.rounded_bg);
                        noteView.setTextColor(getResources().getColor(android.R.color.white));
                        runningNotesContainer.addView(noteView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}

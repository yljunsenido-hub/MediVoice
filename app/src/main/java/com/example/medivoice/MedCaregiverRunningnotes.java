package com.example.medivoice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_med_caregiver_runningnotes);

        runningNotesContainer = findViewById(R.id.runningNotesContainer);
        imgBack = findViewById(R.id.imgBack);

        // Back button
        imgBack.setOnClickListener(v -> finish());

        notesRef = FirebaseDatabase.getInstance().getReference("RunningNotes");

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                runningNotesContainer.removeAllViews();

                for (DataSnapshot noteSnapshot : snapshot.getChildren()) {

                    String noteText = noteSnapshot.child("note").getValue(String.class);
                    String timestamp = noteSnapshot.child("timestamp").getValue(String.class);

                    // Inflate card layout correctly
                    View cardView = LayoutInflater.from(MedCaregiverRunningnotes.this)
                            .inflate(R.layout.item_note, runningNotesContainer, false);

                    TextView txtNote = cardView.findViewById(R.id.txtNoteContent);
                    TextView txtTime = cardView.findViewById(R.id.txtTimestamp);

                    txtNote.setText(noteText != null ? noteText : "No Note");
                    txtTime.setText(timestamp != null ? timestamp : "No Time");

                    runningNotesContainer.addView(cardView);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}

package com.example.medivoice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateNoteActivity extends AppCompatActivity {

    EditText edtNote;
    Button btnSave;
    DatabaseReference notesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        edtNote = findViewById(R.id.edtNote);
        btnSave = findViewById(R.id.btnSaveNote);

        // Firebase reference
        notesRef = FirebaseDatabase.getInstance().getReference("RunningNotes");

        btnSave.setOnClickListener(v -> {
            String note = edtNote.getText().toString().trim();
            if (!note.isEmpty()) {
                // Save note with unique ID and timestamp
                String id = notesRef.push().getKey();
                if (id != null) {
                    notesRef.child(id).child("text").setValue(note);
                    notesRef.child(id).child("timestamp").setValue(System.currentTimeMillis());
                    Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Please type a note.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

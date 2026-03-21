package com.example.medivoice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CGNursesRunningnotes extends Fragment {

    private EditText editRunningNote;
    private Button btnSendGuardian;
    private DatabaseReference notesRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_c_g_nurses_runningnotes, container, false);

        editRunningNote = view.findViewById(R.id.editRunningNote);
        btnSendGuardian = view.findViewById(R.id.btnSendGuardian);

        // Firebase reference (you can organize by caregiver UID if needed)
        notesRef = FirebaseDatabase.getInstance().getReference("RunningNotes");

        btnSendGuardian.setOnClickListener(v -> {
            String note = editRunningNote.getText().toString().trim();
            if (!note.isEmpty()) {
                // Push note to Firebase
                String key = notesRef.push().getKey();
                Map<String, Object> noteData = new HashMap<>();
                noteData.put("text", note);
                noteData.put("timestamp", System.currentTimeMillis());

                if (key != null) {
                    notesRef.child(key).setValue(noteData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Note sent!", Toast.LENGTH_SHORT).show();
                                editRunningNote.setText(""); // Clear input
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to send note.", Toast.LENGTH_SHORT).show()
                            );
                }
            } else {
                Toast.makeText(getContext(), "Please type a note", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}

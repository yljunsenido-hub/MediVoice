package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedNurseRunningNote extends AppCompatActivity {

    RecyclerView rvNotes;
    FloatingActionButton btnCreateNote;

    NoteAdapter adapter;
    List<Note> notes = new ArrayList<>();
    DatabaseReference notesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_nurse_running_note);

        //rvNotes = findViewById(R.id.rvNotes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoteAdapter();
        rvNotes.setAdapter(adapter);

        //btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateNoteActivity.class));
        });

        // Firebase reference
        notesRef = FirebaseDatabase.getInstance().getReference("RunningNotes");

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String text = ds.child("text").getValue(String.class);
                    Long timestamp = ds.child("timestamp").getValue(Long.class);
                    notes.add(new Note(text, timestamp != null ? timestamp : 0L));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // -------------------------
    // RecyclerView Adapter
    // -------------------------
    class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Note n = notes.get(position);
            holder.txtNote.setText(n.text);

            // Format timestamp to date string
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(n.timestamp));
            holder.txtTimestamp.setText(formattedDate);
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNote;
            TextView txtTimestamp;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtNote = itemView.findViewById(R.id.txtNoteContent);
                txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            }
        }
    }

    // -------------------------
    // Note model
    // -------------------------
    class Note {
        String text;
        long timestamp;

        public Note(String text, long timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }
    }
}

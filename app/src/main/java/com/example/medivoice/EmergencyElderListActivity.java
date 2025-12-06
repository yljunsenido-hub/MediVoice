package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencyElderListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<ElderData> elderList = new ArrayList<>();
    DatabaseReference eldersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_elder_list);

        recyclerView = findViewById(R.id.rvElders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eldersRef = FirebaseDatabase.getInstance().getReference("Elders");

        eldersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                elderList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String name = ds.child("name").getValue(String.class);
                    String age = ds.child("age").getValue(String.class);

                    elderList.add(new ElderData(id, name, age));
                }
                recyclerView.setAdapter(new ElderAdapter());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    class ElderData {
        String id, name, age;

        ElderData(String id, String name, String age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

    class ElderAdapter extends RecyclerView.Adapter<ElderAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_elder_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ElderData e = elderList.get(position);
            holder.txtName.setText(e.name);
            holder.txtAge.setText("Age: " + e.age);

            holder.card.setOnClickListener(v -> {
                Intent i = new Intent(EmergencyElderListActivity.this, EmergencyContactsActivity.class);
                i.putExtra("elderId", e.id);
                i.putExtra("elderName", e.name);
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return elderList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtAge;
            CardView card;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.txtElderName);
                txtAge = itemView.findViewById(R.id.txtElderAge);
                card = itemView.findViewById(R.id.cardElder); // FIXED
            }
        }
    }
}

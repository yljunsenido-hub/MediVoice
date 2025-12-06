package com.example.medivoice;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmergencyContactsActivity extends AppCompatActivity {

    TextView txtName;
    LinearLayout contactContainer;
    DatabaseReference eldersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        txtName = findViewById(R.id.txtElderName);
        contactContainer = findViewById(R.id.contactContainer);

        String elderId = getIntent().getStringExtra("elderId");
        String elderName = getIntent().getStringExtra("elderName");

        txtName.setText(elderName);

        eldersRef = FirebaseDatabase.getInstance().getReference("Elders").child(elderId);

        eldersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactContainer.removeAllViews();

                if (!snapshot.exists()) return;

                addContactCard(
                        "Primary Contact",
                        snapshot.child("primaryContactPerson").getValue(String.class),
                        snapshot.child("primaryRelationship").getValue(String.class),
                        snapshot.child("primaryContactNumber").getValue(String.class)
                );

                addContactCard(
                        "Secondary Contact",
                        snapshot.child("secondaryContactPerson").getValue(String.class),
                        snapshot.child("secondaryRelationship").getValue(String.class),
                        snapshot.child("secondaryContactNumber").getValue(String.class)
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addContactCard(String type, String person, String relation, String number) {
        if (person == null || number == null) return;

        // Inflate as CardView, not LinearLayout
        CardView cardView = (CardView) LayoutInflater.from(this)
                .inflate(R.layout.item_contact_card, contactContainer, false);

        TextView txtType = cardView.findViewById(R.id.txtContactType);
        TextView txtPerson = cardView.findViewById(R.id.txtContactPerson);
        TextView txtNumber = cardView.findViewById(R.id.txtContactNumber);
        Button btnCall = cardView.findViewById(R.id.btnCall);

        txtType.setText(type);
        txtPerson.setText(person + " (" + relation + ")");
        txtNumber.setText(number);

        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        });

        contactContainer.addView(cardView);
    }

}

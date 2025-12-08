package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class ElderList extends AppCompatActivity {

    LinearLayout elderContainer;

    ImageView btnBack;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elder_list);

        elderContainer = findViewById(R.id.elderContainer);

        loadElders();

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ElderList.this, ElderRegistration.class);
            startActivity(intent);
            finish();
        });

        bottomNav = findViewById(R.id.bottomNav);
        // Set the highlighted/selected item to profile
        bottomNav.setSelectedItemId(R.id.nav_elderlyStatLog);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_elderlyStatLog) {
                return true; // Already home
            } else if (id == R.id.nav_medLog) {
                startActivity(new Intent(ElderList.this, MedNursePrescription.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(ElderList.this, NurseProfile.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(ElderList.this, MedNurseHomepage.class));
                return true;
            }
            return false;
        });
    }

    private void loadElders() {
        FirebaseDatabase.getInstance().getReference("Elders")
                .get()
                .addOnSuccessListener(snapshot -> {

                    elderContainer.removeAllViews();

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        View card = LayoutInflater.from(this)
                                .inflate(R.layout.elder_item, elderContainer, false);

                        TextView txtBasicName = card.findViewById(R.id.txtBasicName);
                        TextView txtBasicAge = card.findViewById(R.id.txtBasicAge);

                        TextView txtGender = card.findViewById(R.id.txtGender);
                        TextView txtBirthday = card.findViewById(R.id.txtBirthday);
                        TextView txtCivilStatus = card.findViewById(R.id.txtCivilStatus);
                        TextView txtHomeAddress = card.findViewById(R.id.txtHomeAddress);

                        TextView txtPrimaryContactPerson = card.findViewById(R.id.txtPrimaryContactPerson);
                        TextView txtPrimaryRelationship = card.findViewById(R.id.txtPrimaryRelationship);
                        TextView txtPrimaryContactNumber = card.findViewById(R.id.txtPrimaryContactNumber);

                        TextView txtSecondaryContactPerson = card.findViewById(R.id.txtSecondaryContactPerson);
                        TextView txtSecondaryRelationship = card.findViewById(R.id.txtSecondaryRelationship);
                        TextView txtSecondaryContactNumber = card.findViewById(R.id.txtSecondaryContactNumber);

                        TextView txtAllergies = card.findViewById(R.id.txtAllergies);
                        TextView txtCognitiveStatus = card.findViewById(R.id.txtCognitiveStatus);
                        TextView txtDisabilities = card.findViewById(R.id.txtDisabilities);
                        TextView txtNurseName = card.findViewById(R.id.txtNurseName);

                        LinearLayout expandSection = card.findViewById(R.id.expandSection);

                        // GET DATA
                        String name = ds.child("name").getValue(String.class);
                        String age = ds.child("age").getValue(String.class);
                        String gender = ds.child("gender").getValue(String.class);
                        String birthday = ds.child("birthday").getValue(String.class);
                        String civil = ds.child("civilStatus").getValue(String.class);
                        String address = ds.child("homeAddress").getValue(String.class);

                        String pName = ds.child("primaryContactPerson").getValue(String.class);
                        String pRel = ds.child("primaryRelationship").getValue(String.class);
                        String pNum = ds.child("primaryContactNumber").getValue(String.class);

                        String sName = ds.child("secondaryContactPerson").getValue(String.class);
                        String sRel = ds.child("secondaryRelationship").getValue(String.class);
                        String sNum = ds.child("secondaryContactNumber").getValue(String.class);

                        String allergies = ds.child("allergies").getValue(String.class);
                        String cognitive = ds.child("cognitiveStatus").getValue(String.class);
                        String disabilities = ds.child("disabilities").getValue(String.class);

                        String nurseName = ds.child("nurseName").getValue(String.class);

                        // BASIC INFO
                        txtBasicName.setText(name);
                        txtBasicAge.setText("Age: " + age);

                        // FULL INFO
                        txtGender.setText("Gender: " + gender);
                        txtBirthday.setText("Birthday: " + birthday);
                        txtCivilStatus.setText("Civil Status: " + civil);
                        txtHomeAddress.setText("Address: " + address);

                        txtPrimaryContactPerson.setText("Primary Contact: " + pName);
                        txtPrimaryRelationship.setText("Relationship: " + pRel);
                        txtPrimaryContactNumber.setText("Contact No: " + pNum);

                        txtSecondaryContactPerson.setText("Secondary Contact: " + sName);
                        txtSecondaryRelationship.setText("Relationship: " + sRel);
                        txtSecondaryContactNumber.setText("Contact No: " + sNum);

                        txtAllergies.setText("Allergies: " + allergies);
                        txtCognitiveStatus.setText("Cognitive Status: " + cognitive);
                        txtDisabilities.setText("Disabilities: " + disabilities);

                        txtNurseName.setText("Registered by: " + nurseName);

                        // EXPAND / COLLAPSE
                        card.setOnClickListener(v -> {
                            if (expandSection.getVisibility() == View.GONE) {
                                expandSection.setVisibility(View.VISIBLE);
                            } else {
                                expandSection.setVisibility(View.GONE);
                            }
                        });

                        elderContainer.addView(card);
                    }
                });

    }
}

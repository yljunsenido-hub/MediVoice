package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CGProfile extends Fragment {

    private static final String TAG = "CGProfile";

    // UI
    private TextView txtFullName, txtEmail, txtRole, txtContact, txtAge;
    private Button btnLogout;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    // --- DATA MODEL ---
    public static class Caregiver {
        public String age;
        public String contactNumber;
        public String email;
        public String firstName;
        public String lastName;
        public String role;

        public Caregiver() {}
    }
    // -------------------

    public CGProfile() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_c_g_profile, container, false);

        // Initialize UI
        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtRole = view.findViewById(R.id.txtRole);
        txtContact = view.findViewById(R.id.txtContact);
        txtAge = view.findViewById(R.id.txtAge);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return view;
        }

        // 🔥 CHANGE: Caregiver instead of Nurse
        databaseRef = FirebaseDatabase.getInstance()
                .getReference("Caregiver")
                .child(currentUser.getUid());

        loadUserProfile();
        setupLogout();

        return view;
    }

    private void loadUserProfile() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Caregiver caregiver = snapshot.getValue(Caregiver.class);

                    if (caregiver != null) {
                        displayProfileData(caregiver);
                    }

                } else {
                    Log.w(TAG, "No caregiver data found");

                    Toast.makeText(getContext(),
                            "Profile data not found.",
                            Toast.LENGTH_LONG).show();

                    txtEmail.setText("Email: " + currentUser.getEmail());
                    txtFullName.setText("User Data Missing");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile", error.toException());

                Toast.makeText(getContext(),
                        "Failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayProfileData(Caregiver caregiver) {
        String fullName = caregiver.firstName + " " + caregiver.lastName;

        txtFullName.setText(fullName);
        txtEmail.setText("Email: " + caregiver.email);
        txtRole.setText("Role: " + caregiver.role);
        txtContact.setText("Contact: " + caregiver.contactNumber);
        txtAge.setText("Age: " + caregiver.age);
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Toast.makeText(getContext(),
                    "Logged out successfully.",
                    Toast.LENGTH_SHORT).show();

            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MedNurseLogin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
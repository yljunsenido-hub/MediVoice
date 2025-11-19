package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView fullnameText, addressText, contactText, emailText;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind TextViews
        fullnameText = findViewById(R.id.fullnameText);
        addressText = findViewById(R.id.addressText);
        contactText = findViewById(R.id.contactText);
        emailText = findViewById(R.id.emailText);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        loadUserData();

        // for bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    return true; // Stay on Home
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(getApplicationContext(), Logs.class));
                    overridePendingTransition(0, 0);
                    return true;
//                } else if (itemId == R.id.nav_mic) {
//                    startActivity(new Intent(getApplicationContext(), GuardianRecordActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
                } else if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(Profile.this, "User data not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String contactNumber = snapshot.child("contactNumber").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (firstName == null) firstName = "";
                if (lastName == null) lastName = "";
                if (address == null) address = "";
                if (contactNumber == null) contactNumber = "";
                if (email == null) email = "";

                String fullName = firstName + " " + lastName;

                fullnameText.setText("Full Name: " + fullName);
                addressText.setText("Address: " + address);
                contactText.setText("Contact: " + contactNumber);
                emailText.setText("Email: " + email);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Profile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

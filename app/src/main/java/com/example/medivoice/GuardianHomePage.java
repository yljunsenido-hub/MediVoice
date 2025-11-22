package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GuardianHomePage extends AppCompatActivity {

    BottomNavigationView GuardianBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        GuardianBottomNavigationView = findViewById(R.id.guardianBottomNavigation);
        GuardianBottomNavigationView.setSelectedItemId(R.id.nav_home);

        GuardianBottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true; // Stay on Home
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(getApplicationContext(), GuardianLogs.class));
                    overridePendingTransition(0, 0);
                    return true;
//                } else if (itemId == R.id.nav_mic) {
//                    startActivity(new Intent(getApplicationContext(), GuardianRecordActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(getApplicationContext(), GuardianProfile.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }
}
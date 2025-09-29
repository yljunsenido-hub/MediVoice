package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    Button btnLogin, btnRegister;
    EditText emailPass2, password2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        emailPass2 = findViewById(R.id.emailPass2);
        password2 = findViewById(R.id.password2);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = emailPass2.getText().toString().trim();
            String pass = password2.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailPass2.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(pass)) {
                password2.setError("Password is required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this,
                                    "Wrong credentials",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            finish();
        });
    }
}

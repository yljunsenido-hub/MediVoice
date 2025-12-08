package com.example.medivoice;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class MedCareTextToSpeech extends AppCompatActivity implements TextToSpeech.OnInitListener {

    Button btnCamera, btnGallery, btnSpeak;
    TextView textResult;
    EditText recordName;
    ImageView imgBack;
    Uri imageUri;
    private TextToSpeech tts;

    private static final int CAMERA_REQUEST = 101;
    private static final int GALLERY_REQUEST = 102;
    private static final int CAMERA_PERMISSION_CODE = 200; // Use a constant for permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_care_text_to_speech);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize TextToSpeech engine
        tts = new TextToSpeech(this, this);

        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSpeak = findViewById(R.id.btnSpeak); // New Speak button
        recordName = findViewById(R.id.recordName);
        textResult = findViewById(R.id.textResult);
        imgBack = findViewById(R.id.imgBack);

        // Back button logic (Assuming HomePage.class is the target)
        imgBack.setOnClickListener(v -> finish());

        // Speak button functionality
        btnSpeak.setOnClickListener(v -> {
            String text = textResult.getText().toString();
            if (text.trim().isEmpty() || text.equals("Scan a prescription or image to read text aloud.")) {
                Toast.makeText(this, "Please scan text first.", Toast.LENGTH_SHORT).show();
            } else {
                speakOut(text);
            }
        });

        // Camera and Gallery setup
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });
        btnGallery.setOnClickListener(v -> openGallery());
    }

    // --- TextToSpeech Initialization ---
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US); // Set language to US English

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported for Text-to-Speech.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Text-to-Speech initialization failed.", Toast.LENGTH_LONG).show();
        }
    }

    // --- TextToSpeech Speaking Logic ---
    private void speakOut(String text) {
        if (tts != null && !tts.isSpeaking()) {
            // Use QUEUE_FLUSH to interrupt any previous speaking
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "text-id");
        }
    }

    // --- Camera/Gallery and OCR Logic (Modified from original) ---

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            InputImage image = null;

            if (requestCode == GALLERY_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                try {
                    image = InputImage.fromFilePath(this, selectedImage);
                } catch (IOException e) {
                    Toast.makeText(this, "Error loading image from gallery.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                try {
                    image = InputImage.fromFilePath(this, imageUri);
                } catch (IOException e) {
                    Toast.makeText(this, "Error loading image from camera.", Toast.LENGTH_SHORT).show();
                }
            }

            if (image != null) {
                processTextRecognition(image);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void processTextRecognition(InputImage image) {
        textResult.setText("Scanning image..."); // Provide immediate feedback
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder result = new StringBuilder();
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        result.append(block.getText()).append("\n");
                    }
                    String finalResult = result.toString().trim();
                    textResult.setText(finalResult);

                    // ⭐️ NEW: Automatically read the text after a successful scan ⭐️
                    if (!finalResult.isEmpty()) {
                        speakOut(finalResult);
                    } else {
                        speakOut("No readable text found in the image.");
                    }
                })
                .addOnFailureListener(e -> {
                    textResult.setText("Failed to scan text.");
                    Toast.makeText(this, "Failed to scan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- Cleanup TTS engine when activity is destroyed ---
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
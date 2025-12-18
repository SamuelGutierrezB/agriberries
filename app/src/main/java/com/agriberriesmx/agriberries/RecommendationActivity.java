package com.agriberriesmx.agriberries;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.Utils.AgronomistAI;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecommendationActivity extends AppCompatActivity {
    private EditText etvRecommendation;
    private Button btnRecommendation;
    private ProgressBar progressBar;
    private String clientId, unitId, diagnosticId, recommendationFromDocument;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvRecommendation = findViewById(R.id.etvRecommendation);
        btnRecommendation = findViewById(R.id.btnRecommendation);
        progressBar = findViewById(R.id.progressBar);

        // Get data from intent
        if (getIntent().getExtras() != null) {
            clientId = getIntent().getStringExtra("clientId");
            unitId = getIntent().getStringExtra("unitId");
            diagnosticId = getIntent().getStringExtra("diagnosticId");
            recommendationFromDocument = getIntent().getStringExtra("recommendation");
        }

        etvRecommendation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Logic to enable/disable button
                if (!s.toString().trim().isEmpty()) {
                    // Enable button
                    btnRecommendation.setText("Guardar recomendación");
                    btnRecommendation.setOnClickListener(v -> saveRecommendation());
                } else {
                    // Disable button
                    btnRecommendation.setText("Generar recomendación");
                    btnRecommendation.setOnClickListener(v -> generateRecommendation());
                }
            }
        });

        // Set recommendation from document if it exists
        if (recommendationFromDocument != null && !recommendationFromDocument.isEmpty()) {
            // Add listeners and set initial state
            etvRecommendation.setText(recommendationFromDocument);
            btnRecommendation.setText("Guardar recomendación");
            btnRecommendation.setOnClickListener(v -> saveRecommendation());
        } else {
            // Add listeners and set initial state
            btnRecommendation.setText("Generar recomendación");
            btnRecommendation.setOnClickListener(v -> generateRecommendation());
        }
    }

    private void generateRecommendation() {
        // Disable buttons and show progress bar
        setLoadingState(true);
        etvRecommendation.setText(getString(R.string.generatingRecommendation));

        // Call API to generate recommendation
        AgronomistAI.getAgronomistRecommendation(this, clientId, unitId, diagnosticId, new AgronomistAI.RecommendationCallback() {
            @Override
            public void onSuccess(String recommendation) {
                // Successfully generated recommendation, update the UI in the main thread
                runOnUiThread(() -> {
                    setLoadingState(false);
                    etvRecommendation.setText(recommendation);
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                // An error occurred, update the UI in the main thread
                runOnUiThread(() -> {
                    setLoadingState(false);
                    etvRecommendation.setText("Error generating recommendation:\n\n" + e.getMessage());
                });
            }
        });
    }

    private void saveRecommendation() {
        String recommendationText = etvRecommendation.getText().toString();

        if (recommendationText.isEmpty()) {
            Toast.makeText(this, "No hay recomendación para guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        // Get reference to Firestore and save recommendation
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("clients").document(clientId)
                .collection("diagnostics").document(diagnosticId)
                .update("recommendation", recommendationText)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(RecommendationActivity.this, "Recomendación guardada con éxito.", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(RecommendationActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }));
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRecommendation.setVisibility(View.GONE);
            btnRecommendation.setEnabled(false);
            etvRecommendation.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRecommendation.setVisibility(View.VISIBLE);
            btnRecommendation.setEnabled(true);
            etvRecommendation.setEnabled(true);
        }
    }

}

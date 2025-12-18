package com.agriberriesmx.agriberries;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setup();
    }

    private void setup() {
        // Link XML to Java
        TextView tvVersionNotes = findViewById(R.id.tvVersionNotes);
        Button btnSearchUpdate = findViewById(R.id.btnSearchUpdate);

        // Initialize views
        tvVersionNotes.setMovementMethod(new ScrollingMovementMethod());

        // Add listeners
        btnSearchUpdate.setOnClickListener(v -> searchUpdate());
    }

    private void searchUpdate() {
        // Connect to Firebase Remote config
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Define min interval to check
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Get remote config
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Get version value
                String remoteVersion = mFirebaseRemoteConfig.getString("version");
                String localVersion = getResources().getString(R.string.version);

                if (remoteVersion.equals(localVersion)) {
                    // Same version
                    Toast.makeText(this, getResources().getString(R.string.current_version_updated), Toast.LENGTH_LONG).show();
                } else {
                    // New version
                    Toast.makeText(this, getResources().getString(R.string.new_version_available), Toast.LENGTH_LONG).show();

                    // Create an intent to open the Google Play Store
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.agriberriesmx.agriberries"));
                    startActivity(intent);
                }
            } else Toast.makeText(this, getResources().getString(R.string.could_not_get_version), Toast.LENGTH_LONG).show();
        });
    }

}

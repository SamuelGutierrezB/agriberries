package com.agriberriesmx.agriberries;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowPdfActivity extends AppCompatActivity {
    private static final int WRITE_REQUEST_CODE = 234;
    private ActivityResultLauncher<Intent> downloadFileActivityResultLauncher;
    private String path, reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pdf);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        PDFView pdfView = findViewById(R.id.pdfView);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setTitleTextColor(getColor(R.color.white));
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Initialize ActivityResultLauncher
        downloadFileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // The result contains information form URI to create PDF
                        Intent data = result.getData();
                        Uri uri;
                        if (data != null) {
                            uri = data.getData();
                            // Copy the file to the URI
                            try {
                                InputStream in = Files.newInputStream(new java.io.File(path).toPath());
                                OutputStream out = getContentResolver().openOutputStream(uri);
                                byte[] buf = new byte[1024];
                                int len;
                                while ((len = in.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                                }

                                in.close();
                                out.close();
                                Toast.makeText(this, getResources().getString(R.string.pdfDownloadPath) + path, Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, getResources().getString(R.string.errorDownloadingFile) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );

        // Get path reference
        reference = getIntent().getStringExtra("path");

        if (reference != null) {
            // Connect to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference(reference);

            // Get download url
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Show image or pdf depending of type of file
                progressBar.setVisibility(View.GONE);
                pdfView.setVisibility(View.VISIBLE);

                // Http request to get PDF
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(uri.toString())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        // Handle error
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // Load PDF using downloaded bytes
                            final byte[] pdfBytes = Objects.requireNonNull(response.body()).bytes();
                            runOnUiThread(() -> pdfView.fromBytes(pdfBytes).load());
                        }
                    }
                });
            });
        }
    }

    private void downloadFileToCache(boolean share) {
        // Connect to Firebase Storage and get reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child(reference);

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Http request to get file
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(uri.toString()).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Toast.makeText(ShowPdfActivity.this, getResources().getString(R.string.could_not_share), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        // Get current date and obtain week of the year and year (2 digits)
                        Diagnostic diagnostic = getIntent().getParcelableExtra("diagnostic");
                        Calendar calendar = Calendar.getInstance();
                        if (diagnostic != null) calendar.setTime(diagnostic.getCreation());
                        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
                        SimpleDateFormat yearFormat = new SimpleDateFormat("yy", Locale.getDefault());
                        String yearInTwoDigits = yearFormat.format(calendar.getTime());
                        String formattedWeekAndYear = String.format(Locale.getDefault(), "%02d.%s", weekOfYear, yearInTwoDigits);

                        // Create PDF
                        Unit unit = getIntent().getParcelableExtra("unit");
                        String clientName = getIntent().getStringExtra("clientName");
                        String fileName = clientName + "." + unit.getCrop() + "."
                                + unit.getName() + "." + formattedWeekAndYear + ".pdf";

                        // Replace file name
                        java.io.File cacheFile = new java.io.File(getCacheDir(), fileName);
                        try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                             OutputStream outputStream = Files.newOutputStream(cacheFile.toPath())) {

                            byte[] buffer = new byte[2048];
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, length);
                            }
                            runOnUiThread(() -> {
                                if (share) shareFile(cacheFile, fileName);
                                else saveFile(cacheFile, fileName);
                            });
                        } catch (IOException e) {
                            Toast.makeText(ShowPdfActivity.this, getResources().getString(R.string.could_not_share), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }).addOnFailureListener(e -> Toast.makeText(ShowPdfActivity.this, getResources().getString(R.string.could_not_share), Toast.LENGTH_SHORT).show());
    }

    private void saveFile(java.io.File file, String originalFileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, use SAF
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, file.getName());

            // Start activity to create PDF file
            path = file.getPath();
            downloadFileActivityResultLauncher.launch(intent);
        } else {
            // For Android 10 and less
            try {
                // Get file from cache
                java.io.File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                java.io.File newFile = new java.io.File(storageDir, originalFileName);

                // Get bytes to save them into another path
                try (InputStream in = Files.newInputStream(file.toPath()); OutputStream out = Files.newOutputStream(newFile.toPath())) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    Toast.makeText(this, getResources().getString(R.string.pdfDownloadPath) + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, getResources().getString(R.string.errorDownloadingFile), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, getResources().getString(R.string.storagePermissionDenied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareFile(java.io.File file, String originalFileName) {
        // Share file
        Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

        // Create intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, originalFileName);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shareDiagnostic)));
    }

    private void openAppSettings() {
        // Open the app setting
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                downloadFileToCache(false);
            } else {
                // Permission denied
                Toast.makeText(this, getResources().getString(R.string.storagePermissionDenied), Toast.LENGTH_SHORT).show();
                openAppSettings();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.downloadPdf) {
            // Download PDF
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(ShowPdfActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ShowPdfActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
            } else downloadFileToCache(false);
        } else if (itemId == R.id.sharePdf) {
            // Share PDF
            downloadFileToCache(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_pdf, menu);

        return true;
    }

}

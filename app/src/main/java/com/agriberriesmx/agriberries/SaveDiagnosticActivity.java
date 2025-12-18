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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.agriberriesmx.agriberries.POJO.ConsultantActivity;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Item;
import com.agriberriesmx.agriberries.POJO.Statistic;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.Utils.SharedPreferencesManager;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SaveDiagnosticActivity extends AppCompatActivity {
    private static final int WRITE_REQUEST_CODE = 1;
    private static final int LOCATION_REQUEST_CODE = 231;
    private ActivityResultLauncher<Intent> downloadFileActivityResultLauncher;
    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_diagnostic);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        PDFView pdfView = findViewById(R.id.pdfView);
        Button btnFinishDiagnostic = findViewById(R.id.btnFinishDiagnostic);

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
                                InputStream in = Files.newInputStream(new File(path).toPath());
                                OutputStream out = getContentResolver().openOutputStream(uri);
                                byte[] buf = new byte[1024];
                                int len;
                                while ((len = in.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                                }

                                in.close();
                                out.close();
                                Toast.makeText(this, getResources().getString(R.string.pdfDownloaded), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, getResources().getString(R.string.errorDownloadingFile) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle(getResources().getString(R.string.diagnostic));
        toolbar.setTitleTextColor(getColor(R.color.white));
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Get path and diagnostic
        Diagnostic diagnostic = getIntent().getParcelableExtra("diagnostic");
        path = getIntent().getStringExtra("path");

        // Load PDF from path
        File file = new File(path);
        pdfView.fromFile(file).load();

        // Add listeners
        if (!diagnostic.isFinished()) btnFinishDiagnostic.setOnClickListener(v -> saveDiagnostic());
        else btnFinishDiagnostic.setVisibility(View.GONE);
    }

    private void saveDiagnostic() {
        // Get location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // The permission is granted
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    // Get latitude and longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Inflate custom content dialog
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

                    // Link XML to Java
                    TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
                    Button btnPositive = dialogView.findViewById(R.id.btnPositive);
                    Button btnNegative = dialogView.findViewById(R.id.btnNegative);

                    // Initialize views
                    tvMessage.setText(getResources().getString(R.string.ask_for_save_diagnostic));
                    btnPositive.setText(getResources().getString(R.string.confirm));

                    // Create Alert Dialog to ask confirmation
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    // Add listeners
                    btnPositive.setOnClickListener(v -> {
                        // Hide main Alert Dialog
                        dialog.dismiss();

                        // Create an AlertDialog Builder
                        AlertDialog.Builder secondBuilder = new AlertDialog.Builder(this);
                        secondBuilder.setTitle(getResources().getString(R.string.uploading_file));

                        // Create a ProgressBar
                        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(100);

                        // Add ProgressBar to AlertDialog
                        secondBuilder.setView(progressBar);
                        secondBuilder.setCancelable(false);

                        // Show AlertDialog
                        AlertDialog secondDialog = secondBuilder.create();
                        secondDialog.show();

                        // Get important info
                        String client = getIntent().getStringExtra("client");
                        Diagnostic diagnostic = getIntent().getParcelableExtra("diagnostic");

                        // Connect to Firebase Storage
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference().child(client + "/" + diagnostic.getId() + ".pdf");
                        Uri fileUri = Uri.fromFile(new File(path));

                        // Start upload
                        UploadTask uploadTask = storageRef.putFile(fileUri);

                        uploadTask.addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressBar.setProgress((int) progress);
                        }).addOnSuccessListener(taskSnapshot -> {
                            // Connect to SharedPreferences Manager
                            SharedPreferencesManager preferencesManager = SharedPreferencesManager.getInstance(SaveDiagnosticActivity.this);

                            // Connect to Firebase Auth
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser currentUser = auth.getCurrentUser();

                            if (currentUser != null) {
                                // Connect to Firebase Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                WriteBatch batch = db.batch();

                                // Update diagnostic
                                DocumentReference diagnosticReference = db.collection("clients")
                                        .document(client).collection("diagnostics").document(diagnostic.getId());
                                batch.update(diagnosticReference, "finished", true);
                                batch.update(diagnosticReference, "consultant", preferencesManager.getNameFromPreferences());

                                // Get unit and client name
                                Unit unit = getIntent().getParcelableExtra("unit");
                                String clientName = getIntent().getStringExtra("clientName");

                                // Create activity
                                DocumentReference activityReference = db.collection("activities").document();
                                ConsultantActivity activity = new ConsultantActivity();
                                activity.setId(activityReference.getId());
                                activity.setConsultant(currentUser.getUid());
                                activity.setClient(client);
                                activity.setConsultantName(preferencesManager.getNameFromPreferences());
                                activity.setClientName(clientName);
                                activity.setLocation(unit.getName() + ", " + unit.getLocation());
                                activity.setLatitude(latitude);
                                activity.setLongitude(longitude);
                                activity.setStartDate(diagnostic.getCreation());
                                activity.setEndDate(new Date());
                                batch.set(activityReference, activity);

                                // Get items from diagnostic
                                List<Item> items = getIntent().getParcelableArrayListExtra("items");

                                if (items != null) {
                                    // Get all plagues and create map
                                    List<String> allPlagues = getAllPlagues(items);
                                    Map<String, PlagueStats> plagueStatsMap = new HashMap<>();

                                    // Get formatted plagues
                                    for (String plague : allPlagues) {
                                        for (Item item : items) {
                                            List<String> itemPlagues = item.getPlagues();
                                            for (String itemPlague : itemPlagues) {
                                                if (itemPlague.startsWith(plague)) {
                                                    // Extract intensity of the plague
                                                    int intensity = extractIntensity(itemPlague);

                                                    // Update map
                                                    plagueStatsMap.compute(plague, (key, value) -> {
                                                        if (value == null) {
                                                            // If the plague is not in the map, create a new entry
                                                            return new PlagueStats(1, intensity);
                                                        } else {
                                                            // If the plague is in the map, update the quantity and the intensity
                                                            return new PlagueStats(value.getQuantity() + 1, value.getIntensity() + intensity);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }

                                    // Create a list with the formatted results
                                    List<String> result = createFormattedResult(plagueStatsMap);

                                    // Create statistic
                                    DocumentReference statisticReference = db.collection("statistics").document();
                                    Statistic statistic = new Statistic();
                                    statistic.setId(statisticReference.getId());
                                    statistic.setState(unit.getState());
                                    statistic.setCrop(unit.getCrop());
                                    statistic.setManagement(unit.getManagement());
                                    statistic.setSoil(unit.getSoil());
                                    statistic.setPlagues(result);
                                    statistic.setDate(new Date());
                                    batch.set(statisticReference, statistic);
                                }

                                // Commit batch
                                batch.commit();

                                // Inform user
                                secondDialog.dismiss();
                                Toast.makeText(SaveDiagnosticActivity.this, getResources().getString(R.string.file_uploaded), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SaveDiagnosticActivity.this, ShowClientActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(e -> {
                            secondDialog.dismiss();
                            Toast.makeText(SaveDiagnosticActivity.this, getResources().getString(R.string.try_again_later), Toast.LENGTH_SHORT).show();
                        });
                    });
                    btnNegative.setOnClickListener(v -> dialog.dismiss());

                    // Show Alert Dialog
                    dialog.show();
                }
            });
        } else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    private static List<String> getAllPlagues(List<Item> itemList) {
        List<String> allPlagues = new ArrayList<>();
        for (Item item : itemList) {
            List<String> itemPlagues = item.getPlagues();
            for (String itemPlague : itemPlagues) {
                String plagueName = extractPlagueName(itemPlague);
                if (!allPlagues.contains(plagueName)) {
                    allPlagues.add(plagueName);
                }
            }
        }
        return allPlagues;
    }

    private static String extractPlagueName(String plague) {
        int endIndex = plague.indexOf('(');
        if (endIndex != -1) {
            return plague.substring(0, endIndex).trim();
        }
        return plague.trim();
    }

    private static int extractIntensity(String plague) {
        int startIndex = plague.indexOf('(');
        int endIndex = plague.indexOf(')');
        if (startIndex != -1 && endIndex != -1) {
            String intensityStr = plague.substring(startIndex + 1, endIndex).trim();
            try {
                return Integer.parseInt(intensityStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static List<String> createFormattedResult(Map<String, PlagueStats> plagueStatsMap) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, PlagueStats> entry : plagueStatsMap.entrySet()) {
            String plagueName = entry.getKey();
            PlagueStats stats = entry.getValue();
            double averageIntensity = (double) stats.getIntensity() / stats.getQuantity();
            String formattedResult = String.format(Locale.getDefault(), "{name:\"%s\", quantity:%d, intensity:%.1f}", plagueName, stats.getQuantity(), averageIntensity);
            result.add(formattedResult);
        }
        return result;
    }

    private static class PlagueStats {
        private final int quantity;
        private final int intensity;

        public PlagueStats(int quantity, int intensity) {
            this.quantity = quantity;
            this.intensity = intensity;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getIntensity() {
            return intensity;
        }
    }

    private void downloadFile() {
        File pdfFile = new File(path);
        if (!pdfFile.exists()) {
            return;
        }

        // Extract the file name from the last '÷'
        String filePath = pdfFile.getPath();
        String fileName = filePath.substring(filePath.lastIndexOf('÷') + 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, use SAF
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);

            // Start activity to create PDF file
            downloadFileActivityResultLauncher.launch(intent);
        } else {
            // Request permission in case they are not granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
            } else {
                // Write the PDF file into the storage
                writePdfToStorage(pdfFile);
            }
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "resource"})
    private void writePdfToStorage(File pdfFile) {
        try {
            String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File destDir = new File(destinationPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // Extract the file name from the last '÷'
            String filePath = pdfFile.getPath();
            String fileName = filePath.substring(filePath.lastIndexOf('÷') + 1);

            // Create the new file with the extracted name
            File newFile = new File(destDir, fileName);

            if (newFile.exists()) {
                newFile.delete();
            }

            try (FileChannel source = new FileInputStream(pdfFile).getChannel();
                 FileChannel destination = new FileOutputStream(newFile).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }

            Toast.makeText(this, getResources().getString(R.string.pdfDownloadPath) + " " + destinationPath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.errorDownloadingFile) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                File pdfFile = new File(path);
                writePdfToStorage(pdfFile);
            } else {
                // Permission denied
                Toast.makeText(this, getResources().getString(R.string.storagePermissionDenied), Toast.LENGTH_SHORT).show();
                openAppSettings();
            }
        } else if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                saveDiagnostic();
            } else {
                // Permission denied
                Toast.makeText(this, getResources().getString(R.string.locationPermissionDenied), Toast.LENGTH_SHORT).show();
                openAppSettings();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void shareFile() {
        // Extract the file name from the last '÷'
        String fileName = path.substring(path.lastIndexOf('÷') + 1);

        // Create a source file
        File sourceFile = new File(path);

        // Create a temporary file in the cache directory with the extracted name
        File tempFile = new File(getCacheDir(), fileName);

        try {
            // Copy the content of the source file to the temporary file
            copy(sourceFile, tempFile);

            // Create an intent to share the PDF file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");

            // Get the PDF file URI using FileProvider (recommended for SDK 24+)
            Uri pdfUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile);

            // Add the file URI to the intent
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);

            // Grant read permission to the receiving app
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start the activity to share the file
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shareDiagnostic)));
        } catch (IOException e) {
            // Handle the error if the file copy fails
            e.printStackTrace();
        }
    }

    // Method to copy a file
    private void copy(File src, File dst) throws IOException {
        try (InputStream in = Files.newInputStream(src.toPath()); OutputStream out = Files.newOutputStream(dst.toPath())) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.downloadPdf) {
            // Download PDF
            downloadFile();
        } else if (itemId == R.id.sharePdf) {
            // Share PDF
            shareFile();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_pdf, menu);

        return true;
    }

}

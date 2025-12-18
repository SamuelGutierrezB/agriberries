package com.agriberriesmx.agriberries.Admin;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.SpinnerCropAdapter;
import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UpdatePlagueAdmin extends AppCompatActivity {
    private Plague plague;
    private MenuItem deleteRestoreItem;
    private TextAdapter cropAdapter;
    private final List<String> crops = new ArrayList<>();
    private final List<String> plagues = new ArrayList<>();
    private Spinner spinnerCrop;
    private EditText etvName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_plague);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        etvName = findViewById(R.id.etvName);
        RecyclerView rvCrops = findViewById(R.id.rvCrops);
        ImageButton btnAddCrop = findViewById(R.id.btnAddCrop);
        Button btnUpdatePlague = findViewById(R.id.btnUpdatePlague);

        // Get plague
        plague = getIntent().getParcelableExtra("plague");
        crops.addAll(plague.getCrops());

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle("");
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Initialize views
        etvName.setText(Formatting.capitalizeFirstLetter(plague.getName()));

        // Get crops names
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        List<Crop> cropList = agriBerries.getGlobalCropList();
        List<String> cropNameList = new ArrayList<>();
        for (Crop crop : cropList)  if (crop.getDeleted() == null) cropNameList.add(crop.getName());

        // Get plague names
        List<Plague> plagueList = agriBerries.getGlobalPlagueList();
        for (Plague plague : plagueList) plagues.add(plague.getName());

        // Create spinner adapter
        SpinnerCropAdapter spinnerCropAdapter = new SpinnerCropAdapter(this, cropNameList);
        spinnerCrop.setAdapter(spinnerCropAdapter);

        // Initialize views
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cropAdapter = new TextAdapter(crops);
        rvCrops.setLayoutManager(linearLayoutManager);
        rvCrops.setAdapter(cropAdapter);

        // Enable swipe to delete and undo
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                crops.remove(position);
                cropAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvCrops);

        // Add listeners
        btnAddCrop.setOnClickListener(v -> addCrop());
        btnUpdatePlague.setOnClickListener(v -> updatePlague());
    }

    private void addCrop() {
        // Add crop to the list and update adapter
        String crop = spinnerCrop.getSelectedItem().toString();

        if (!crop.isEmpty()) {
            if (!crops.contains(crop)) {
                int position = Collections.binarySearch(crops, crop);

                // Get insert position for alphabetic order
                if (position < 0) position = -(position + 1);

                // Add element
                crops.add(position, crop);
                cropAdapter.notifyItemInserted(position);
            } else Toast.makeText(this, getResources().getString(R.string.cropAlreadyAdded), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.cropNotAdd), Toast.LENGTH_SHORT).show();
    }

    private void showDeletePlagueDialog() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        if (plague.getDeleted() == null) {
            // To delete
            tvMessage.setText(getResources().getString(R.string.askDeletePlague));
            btnPositive.setText(getResources().getString(R.string.delete));
        } else {
            // To restore
            tvMessage.setText(getResources().getString(R.string.askRestorePlague));
            btnPositive.setText(getResources().getString(R.string.restore));
        }

        // Create Alert Dialog to ask confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Add listeners
        btnPositive.setOnClickListener(v -> {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("plagues").document(plague.getId());

            if (plague.getDeleted() == null) {
                // Delete crop
                documentReference.update("deleted", Formatting.addSevenDaysAndFormat(new Date()));
                plague.setDeleted(Formatting.addSevenDaysAndFormat(new Date()));
                deleteRestoreItem.setTitle(getResources().getString(R.string.restore));
                Toast.makeText(UpdatePlagueAdmin.this, getResources().getString(R.string.plagueDeleted), Toast.LENGTH_SHORT).show();
            } else {
                // Restore crop
                documentReference.update("deleted", null);
                plague.setDeleted(null);
                deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
                Toast.makeText(UpdatePlagueAdmin.this, getResources().getString(R.string.plagueRestored), Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Show Alert Dialog
        dialog.show();
    }

    private void updatePlague() {
        // Get attributes to verify that they are not empty
        String name = etvName.getText().toString().trim().toUpperCase();

        if (!name.isEmpty() && !crops.isEmpty()) {
            if (name.equals(plague.getName()) || !plagues.contains(name)) {
                // Connect to Firebase Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("plagues").document(plague.getId());

                // Create plague info
                plague.setName(name);
                plague.setCrops(crops);

                // Save new plague info into database
                documentReference.set(plague);
                Toast.makeText(this, getResources().getString(R.string.plagueUpdated), Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, getResources().getString(R.string.plague_already_created), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.deleteRestore) {
            showDeletePlagueDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        // Change text
        deleteRestoreItem = menu.findItem(R.id.deleteRestore);
        if (plague.getDeleted() == null)
            deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
        else
            deleteRestoreItem.setTitle(getResources().getString(R.string.restore));

        return true;
    }

}

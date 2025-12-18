package com.agriberriesmx.agriberries.Admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.SpinnerCropAdapter;
import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreatePlagueAdmin extends AppCompatActivity {
    private TextAdapter cropAdapter;
    private final List<String> crops = new ArrayList<>();
    private final List<String> plagues = new ArrayList<>();
    private Spinner spinnerCrop;
    private EditText etvName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_create_plague);
        setup();
    }

    private void setup() {
        // Link XML to Java
        spinnerCrop = findViewById(R.id.spinnerCrop);
        etvName = findViewById(R.id.etvName);
        RecyclerView rvCrops = findViewById(R.id.rvCrops);
        ImageButton btnAddCrop = findViewById(R.id.btnAddCrop);
        Button btnCreatePlague = findViewById(R.id.btnCreatePlague);

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
        btnCreatePlague.setOnClickListener(v -> createPlague());
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

    private void createPlague() {
        // Get attributes to verify that they are not empty
        String name = etvName.getText().toString().trim().toUpperCase();

        if (!name.isEmpty() && !crops.isEmpty()) {
            if (!plagues.contains(name)) {
                // Connect to Firebase Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection("plagues").document();

                // Create plague info
                Plague plague = new Plague();
                plague.setId(documentReference.getId());
                plague.setName(name);
                plague.setCrops(crops);
                plague.setDeleted(null);

                // Save new plague info into database
                documentReference.set(plague);
                Toast.makeText(this, getResources().getString(R.string.plagueCreated), Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, getResources().getString(R.string.plague_already_created), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();

    }

}

package com.agriberriesmx.agriberries.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.CropAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Firestore;

import java.util.ArrayList;
import java.util.List;

public class ManageCropsAdmin extends AppCompatActivity {
    private CropAdapter cropAdapter;
    private final List<Crop> cropList = new ArrayList<>();
    private final List<Crop> filteredCropList = new ArrayList<>();
    private SearchView svCrops;
    private LinearLayout linearLayoutNoCrops;
    private RecyclerView rvCrops;
    private String name = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_crops);
        Firestore.deleteDocuments("crops", "deleted");
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get crops and update lists
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        cropList.clear();
        cropList.addAll(agriBerries.getGlobalCropList());
        filteredCropList.clear();
        filteredCropList.addAll(agriBerries.getGlobalCropList());

        // Filter crops and clear focus
        filterCrops();
        svCrops.clearFocus();
    }

    private void setup() {
        // Link XML to Java
        svCrops = findViewById(R.id.svCrops);
        linearLayoutNoCrops = findViewById(R.id.linearLayoutNoCrops);
        rvCrops = findViewById(R.id.rvCrops);
        Button btnCreateCrop = findViewById(R.id.btnCreateCrop);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvCrops.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        cropAdapter = new CropAdapter(filteredCropList);
        rvCrops.setAdapter(cropAdapter);

        // Add listeners
        svCrops.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                name = newText;
                filterCrops();

                return true;
            }
        });
        btnCreateCrop.setOnClickListener(v -> startActivity(new Intent(this, CreateCropAdmin.class)));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCrops() {
        // Filter crops base on the name
        filteredCropList.clear();

        for (Crop crop : cropList) {
            if (crop.getName().toLowerCase().contains(name.toLowerCase()))
                filteredCropList.add(crop);
        }

        // Notify about the changes on the list
        cropAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredCropList.size() == 0) {
            linearLayoutNoCrops.setVisibility(View.VISIBLE);
            rvCrops.setVisibility(View.GONE);
        } else {
            rvCrops.setVisibility(View.VISIBLE);
            linearLayoutNoCrops.setVisibility(View.GONE);
        }
    }

}

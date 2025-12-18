package com.agriberriesmx.agriberries.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.PlagueAdapter;
import com.agriberriesmx.agriberries.Adapter.SpinnerCropAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.AgriBerries;
import com.agriberriesmx.agriberries.Utils.Firestore;

import java.util.ArrayList;
import java.util.List;

public class ManagePlaguesAdmin extends AppCompatActivity {
    private PlagueAdapter plagueAdapter;
    private final List<Plague> plagueList = new ArrayList<>();
    private final List<Plague> filteredPlagueList = new ArrayList<>();
    private SearchView svPlagues;
    private Spinner spinnerCrop;
    private LinearLayout linearLayoutNoPlagues;
    private RecyclerView rvPlagues;
    private String name = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_plagues);
        Firestore.deleteDocuments("plagues", "deleted");
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get plagues and update lists
        AgriBerries agriBerries = (AgriBerries) getApplicationContext();
        plagueList.clear();
        plagueList.addAll(agriBerries.getGlobalPlagueList());
        filteredPlagueList.clear();
        filteredPlagueList.addAll(agriBerries.getGlobalPlagueList());

        // Get crops names
        List<Crop> cropList = agriBerries.getGlobalCropList();
        List<String> cropNameList = new ArrayList<>();
        cropNameList.add(getResources().getString(R.string.all));
        for (Crop crop : cropList)  if (crop.getDeleted() == null) cropNameList.add(crop.getName());

        // Create spinner adapter
        SpinnerCropAdapter spinnerCropAdapter = new SpinnerCropAdapter(this, cropNameList);
        spinnerCrop.setAdapter(spinnerCropAdapter);

        // Filter plagues and clear focus
        filterPlagues();
        svPlagues.clearFocus();
    }

    private void setup() {
        // Link XML to Java
        svPlagues = findViewById(R.id.svPlagues);
        linearLayoutNoPlagues = findViewById(R.id.linearLayoutNoPlagues);
        rvPlagues = findViewById(R.id.rvPlagues);
        spinnerCrop = findViewById(R.id.spinnerCrop);
        Button btnCreatePlague = findViewById(R.id.btnCreatePlague);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvPlagues.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        plagueAdapter = new PlagueAdapter(filteredPlagueList);
        rvPlagues.setAdapter(plagueAdapter);

        // Add listeners
        svPlagues.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                name = newText;
                filterPlagues();

                return true;
            }
        });
        spinnerCrop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterPlagues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnCreatePlague.setOnClickListener(v -> startActivity(new Intent(this, CreatePlagueAdmin.class)));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterPlagues() {
        // Filter plagues base on the name and crop
        String crop = spinnerCrop.getSelectedItem().toString();
        filteredPlagueList.clear();

        for (Plague plague : plagueList) {
            if (plague.getName().toLowerCase().contains(name.toLowerCase())) {
                if (crop.equals(getResources().getString(R.string.all))) filteredPlagueList.add(plague);
                else if (plague.getCrops().contains(crop)) filteredPlagueList.add(plague);
            }
        }

        // Notify about the changes on the list
        plagueAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredPlagueList.size() == 0) {
            linearLayoutNoPlagues.setVisibility(View.VISIBLE);
            rvPlagues.setVisibility(View.GONE);
        } else {
            rvPlagues.setVisibility(View.VISIBLE);
            linearLayoutNoPlagues.setVisibility(View.GONE);
        }
    }

}

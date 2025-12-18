package com.agriberriesmx.agriberries.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.ClientAdapter;
import com.agriberriesmx.agriberries.Adapter.SpinnerStatusAdapter;
import com.agriberriesmx.agriberries.POJO.Client;
import com.agriberriesmx.agriberries.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ManageClientsAdmin extends AppCompatActivity {
    private ListenerRegistration clientListener;
    private ClientAdapter clientAdapter;
    private final List<Client> clientList = new ArrayList<>();
    private final List<Client> filteredClientList = new ArrayList<>();
    private SearchView svClients;
    private LinearLayout linearLayoutNoClients;
    private RecyclerView rvClients;
    private String name = "";
    private int prospects = 0;
    private boolean blocked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_clients);
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get clients info and clear focus
        svClients.clearFocus();
        if (clientListener == null) getClients();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove listener (if exists)
        if (clientListener != null) {
            clientListener.remove();
            clientListener = null;
        }
    }

    private void setup() {
        // Link XML to Java
        svClients = findViewById(R.id.svClients);
        linearLayoutNoClients = findViewById(R.id.linearLayoutNoClients);
        rvClients = findViewById(R.id.rvClients);
        Spinner spinnerStatus = findViewById(R.id.spinnerStatus);
        Spinner spinnerProspects = findViewById(R.id.spinnerProspects);
        Button btnCreateClient = findViewById(R.id.btnCreateClient);

        // Get string array for spinner status
        List<String> values = Arrays.asList(getResources().getStringArray(R.array.actives));
        SpinnerStatusAdapter adapterStatus = new SpinnerStatusAdapter(this, values);

        // Get string array for spinner prospects
        ArrayAdapter<CharSequence> adapterProspects = ArrayAdapter.createFromResource(this,
                R.array.prospects, android.R.layout.simple_spinner_item);
        adapterProspects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvClients.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        clientAdapter = new ClientAdapter(filteredClientList, true);
        rvClients.setAdapter(clientAdapter);

        // Initialize views
        spinnerStatus.setAdapter(adapterStatus);
        spinnerProspects.setAdapter(adapterProspects);

        // Add listeners
        svClients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                name = newText;
                filterClients();

                return true;
            }
        });
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Change blocked status
                blocked = position != 0;
                getClients();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerProspects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Change prospect filter
                prospects = position;
                filterClients();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnCreateClient.setOnClickListener(v -> startActivity(new Intent(this, CreateClientAdmin.class)));
    }

    private void getClients() {
        // Connect to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("clients");

        // Get all clients sorted by name
        clientListener = collectionReference.whereEqualTo("blocked", blocked)
                .orderBy("name")
                .addSnapshotListener((value, error) -> {
                    // Clear all lists
                    filteredClientList.clear();
                    clientList.clear();

                    for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(value).getDocuments()) {
                        Client client = documentSnapshot.toObject(Client.class);
                        clientList.add(client);
                        filteredClientList.add(client);
                    }

                    filterClients();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterClients() {
        // Filter clients base on the name
        filteredClientList.clear();

        for (Client client : clientList) {
            if (client.getName().toLowerCase().contains(name.toLowerCase())) {
                if (prospects == 0) filteredClientList.add(client);
                else if (prospects == 1 && !client.isProspect()) filteredClientList.add(client);
                else if (prospects == 2 && client.isProspect()) filteredClientList.add(client);
            }
        }

        // Notify about the changes on the list
        clientAdapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        // Update visibility of the recycler view and linear layout
        if (filteredClientList.size() == 0) {
            linearLayoutNoClients.setVisibility(View.VISIBLE);
            rvClients.setVisibility(View.GONE);
        } else {
            rvClients.setVisibility(View.VISIBLE);
            linearLayoutNoClients.setVisibility(View.GONE);
        }
    }

}

package com.agriberriesmx.agriberries;

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

import com.agriberriesmx.agriberries.Adapter.ClientAdapter;
import com.agriberriesmx.agriberries.POJO.Client;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientsActivity extends AppCompatActivity {
    private ListenerRegistration clientListener;
    private ClientAdapter clientAdapter;
    private final List<Client> clientList = new ArrayList<>();
    private final List<Client> filteredClientList = new ArrayList<>();
    private SearchView svClients;
    private LinearLayout linearLayoutNoClients;
    private RecyclerView rvClients;
    private String name = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);
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
        Button btnCreateProspect = findViewById(R.id.btnCreateProspect);

        // Set layout manager for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvClients.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        clientAdapter = new ClientAdapter(filteredClientList, false);
        rvClients.setAdapter(clientAdapter);

        // Add listeners
        btnCreateProspect.setOnClickListener(v -> startActivity(new Intent(this, CreateProspectActivity.class)));
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
    }

    private void getClients() {
        // Connect to Firebase Auth and get user's UID
        FirebaseAuth auth =  FirebaseAuth.getInstance();
        String uid = auth.getUid();

        if (uid != null) {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference collectionReference = db.collection("clients");

            // Get all clients sorted by name
            clientListener = collectionReference.whereArrayContains("consultants", uid)
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterClients() {
        // Filter clients base on the name
        filteredClientList.clear();

        for (Client client : clientList) {
            if (client.getName().toLowerCase().contains(name.toLowerCase()))
                filteredClientList.add(client);
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

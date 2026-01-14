package com.agriberriesmx.agriberries;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.DiagnosticAdapter;
import com.agriberriesmx.agriberries.Adapter.UnitAdapter;
import com.agriberriesmx.agriberries.POJO.Client;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ShowClientActivity extends AppCompatActivity {
    private Client client;
    private ListenerRegistration unitListener;
    private ListenerRegistration diagnosticListener;
    private final List<Unit> unitList = new ArrayList<>();
    private final List<Diagnostic> diagnosticList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_client);
        setup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove listeners (if exist)
        if (unitListener != null) {
            unitListener.remove();
            unitListener = null;
        }
        if (diagnosticListener != null) {
            diagnosticListener.remove();
            diagnosticListener = null;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout linearLayoutToggleGeneralInformation = findViewById(R.id.linearLayoutToggleGeneralInformation);
        LinearLayout linearLayoutToggleUnits = findViewById(R.id.linearLayoutToggleUnits);
        LinearLayout linearLayoutToggleDiagnostics = findViewById(R.id.linearLayoutToggleDiagnostics);
        LinearLayout linearLayoutGeneralInformation = findViewById(R.id.linearLayoutGeneralInformation);
        LinearLayout linearLayoutNoUnits = findViewById(R.id.linearLayoutNoUnits);
        LinearLayout linearLayoutNoDiagnostics = findViewById(R.id.linearLayoutNoDiagnostics);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvManager = findViewById(R.id.tvManager);
        TextView tvBusiness = findViewById(R.id.tvBusiness);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvFrequency = findViewById(R.id.tvFrequency);
        TextView tvRegistration = findViewById(R.id.tvRegistration);
        TextView tvStatus = findViewById(R.id.tvStatus);
        ImageView ivGeneralInformation = findViewById(R.id.ivGeneralInformation);
        ImageView ivUnits = findViewById(R.id.ivUnits);
        ImageView ivDiagnostics = findViewById(R.id.ivDiagnostics);
        RecyclerView rvUnits = findViewById(R.id.rvUnits);
        RecyclerView rvDiagnostics = findViewById(R.id.rvDiagnostics);
        Button btnCreateUnit = findViewById(R.id.btnCreateUnit);
        Button btnCreateDiagnostic = findViewById(R.id.btnCreateDiagnostic);

        // Get client
        client = getIntent().getParcelableExtra("client");

        // Initialize toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("");
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Set layout manager for recycler view units
        LinearLayoutManager linearLayoutManagerUnits = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        rvUnits.setLayoutManager(linearLayoutManagerUnits);

        // Set adapter for recycler view units
        UnitAdapter unitAdapter = new UnitAdapter(unitList, client.getId());
        rvUnits.setAdapter(unitAdapter);

        // Set layout manager for recycler view diagnostics
        LinearLayoutManager linearLayoutManagerDiagnostics = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        rvDiagnostics.setLayoutManager(linearLayoutManagerDiagnostics);

        // Set adapter for recycler view diagnostics
        DiagnosticAdapter diagnosticAdapter = new DiagnosticAdapter(diagnosticList, unitList, client.getId(),
                client.getName());
        rvDiagnostics.setAdapter(diagnosticAdapter);

        // Initialize views
        ivGeneralInformation.setBackgroundResource(R.drawable.ic_arrow_right);
        ivUnits.setBackgroundResource(R.drawable.ic_arrow_right);
        ivDiagnostics.setBackgroundResource(R.drawable.ic_arrow_right);

        // Get frequency array and create simple date format
        String[] frequencies = getResources().getStringArray(R.array.frequencies);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize general information views
        setTitle(client.getName());
        tvName.setText(client.getName());
        tvManager.setText(client.getManager());
        tvBusiness.setText(client.getBusiness());
        tvPhone.setText(client.getPhone());
        tvEmail.setText(client.getEmail());
        tvFrequency.setText(frequencies[client.getFrequency()]);
        tvRegistration.setText(simpleDateFormat.format(client.getRegistration()));
        if (client.isBlocked())
            tvStatus.setText(getResources().getString(R.string.blocked));
        else
            tvStatus.setText(getResources().getString(R.string.active));

        // Get document reference of current user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("clients").document(client.getId());

        // Get units
        CollectionReference unitsCollectionReference = documentReference.collection("units");

        unitListener = unitsCollectionReference.whereEqualTo("deleted", null)
                .orderBy("name").addSnapshotListener((value, error) -> {
                    // Get documents
                    if (value != null) {
                        // Convert them to units
                        unitList.clear();
                        unitList.addAll(value.toObjects(Unit.class));

                        // Notify about the changes on the list
                        unitAdapter.notifyDataSetChanged();

                        // Update visibility
                        if (btnCreateUnit.getVisibility() == View.VISIBLE) {
                            if (unitList.size() > 0) {
                                // Show recycler view
                                rvUnits.setVisibility(View.VISIBLE);
                                linearLayoutNoUnits.setVisibility(View.GONE);
                            } else {
                                // Hide recycler view
                                rvUnits.setVisibility(View.GONE);
                                linearLayoutNoUnits.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        // Get diagnostics
        CollectionReference diagnosticsCollectionReference = documentReference.collection("diagnostics");

        diagnosticListener = diagnosticsCollectionReference.orderBy("creation", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    // Get documents
                    if (value != null) {
                        // Convert them to diagnostics
                        diagnosticList.clear();
                        diagnosticList.addAll(value.toObjects(Diagnostic.class));

                        // Notify about the changes on the list
                        diagnosticAdapter.notifyDataSetChanged();

                        // Update visibility
                        if (btnCreateDiagnostic.getVisibility() == View.VISIBLE) {
                            if (diagnosticList.size() > 0) {
                                // Show recycler view
                                linearLayoutNoDiagnostics.setVisibility(View.GONE);
                                rvDiagnostics.setVisibility(View.VISIBLE);
                            } else {
                                // Hide recycler view
                                rvUnits.setVisibility(View.GONE);
                                linearLayoutNoUnits.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        // Add listeners
        linearLayoutToggleGeneralInformation.setOnClickListener(v -> {
            if (linearLayoutGeneralInformation.getVisibility() == View.VISIBLE) {
                // Hide general information
                linearLayoutGeneralInformation.setVisibility(View.GONE);
                ivGeneralInformation.setBackgroundResource(R.drawable.ic_arrow_right);
            } else {
                // Show general information
                linearLayoutGeneralInformation.setVisibility(View.VISIBLE);
                ivGeneralInformation.setBackgroundResource(R.drawable.ic_arrow_drop);
            }
        });
        linearLayoutToggleUnits.setOnClickListener(v -> {
            if (btnCreateUnit.getVisibility() == View.VISIBLE) {
                // Hide units
                linearLayoutNoUnits.setVisibility(View.GONE);
                rvUnits.setVisibility(View.GONE);
                btnCreateUnit.setVisibility(View.GONE);
                ivUnits.setBackgroundResource(R.drawable.ic_arrow_right);
            } else {
                // Show units
                if (unitList.size() == 0)
                    linearLayoutNoUnits.setVisibility(View.VISIBLE);
                else
                    rvUnits.setVisibility(View.VISIBLE);
                btnCreateUnit.setVisibility(View.VISIBLE);
                ivUnits.setBackgroundResource(R.drawable.ic_arrow_drop);
            }
        });
        linearLayoutToggleDiagnostics.setOnClickListener(v -> {
            if (btnCreateDiagnostic.getVisibility() == View.VISIBLE) {
                // Hide units
                linearLayoutNoDiagnostics.setVisibility(View.GONE);
                rvDiagnostics.setVisibility(View.GONE);
                btnCreateDiagnostic.setVisibility(View.GONE);
                ivDiagnostics.setBackgroundResource(R.drawable.ic_arrow_right);
            } else {
                // Show units
                if (diagnosticList.size() == 0)
                    linearLayoutNoDiagnostics.setVisibility(View.VISIBLE);
                else
                    rvDiagnostics.setVisibility(View.VISIBLE);
                btnCreateDiagnostic.setVisibility(View.VISIBLE);
                ivDiagnostics.setBackgroundResource(R.drawable.ic_arrow_drop);
            }
        });
        btnCreateUnit.setOnClickListener(
                v -> startActivity(new Intent(this, CreateUnitActivity.class).putExtra("clientId", client.getId())));
        btnCreateDiagnostic.setOnClickListener(v -> changeActivityDiagnostic());
    }

    private void changeActivityDiagnostic() {
        // Initialize variables
        List<String> unitCropLocationList = new ArrayList<>();
        for (Unit unit : unitList) {
            String cropLocation = unit.getName();
            cropLocation += " - ";
            cropLocation += unit.getCrop();
            unitCropLocationList.add(cropLocation);
        }

        if (unitCropLocationList.size() > 0) {
            // Create alert dialog to select unit
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.selectUnit));

            // Create spinner and adapter
            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    unitCropLocationList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            builder.setView(spinner);

            // Add positive and negative listeners
            builder.setPositiveButton(getResources().getString(R.string.confirm), (dialog, which) -> {
                // Get selected item position of the spinner and unit id
                int position = spinner.getSelectedItemPosition();
                Unit unit = unitList.get(position);

                // Change activity to create diagnostic activity
                Intent intent = new Intent(ShowClientActivity.this, CreateDiagnosticActivity.class);
                intent.putExtra("clientId", client.getId());
                intent.putExtra("clientName", client.getName());
                intent.putExtra("unit", unit);
                startActivity(intent);
            });

            builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

            // Show Alert Dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else
            Toast.makeText(this, getResources().getString(R.string.noCreatedUnits), Toast.LENGTH_SHORT).show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.locate) {
            // Convert unit list to String
            List<Pair<Unit, Integer>> filteredUnits = unitList.stream()
                    .filter(unit -> unit.getLatitude() != -1000 && unit.getLongitude() != -1000)
                    .map(unit -> new Pair<>(unit, unitList.indexOf(unit)))
                    .collect(Collectors.toList());

            if (!filteredUnits.isEmpty()) {
                // Create alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.selectUnit));

                // Create ListView for Alert Dialog
                AtomicInteger selectedPosition = new AtomicInteger(-1);
                ListView listView = new ListView(this);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_list_view,
                        filteredUnits.stream().map(pair -> pair.first.getName() + " - " + pair.first.getLocation())
                                .collect(Collectors.toList())) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = view.findViewById(R.id.tvUnit);

                        if (selectedPosition.get() == position) {
                            textView.setBackgroundColor(ContextCompat.getColor(ShowClientActivity.this, R.color.green));
                        } else {
                            textView.setBackgroundColor(Color.TRANSPARENT);
                        }

                        return view;
                    }
                };

                listView.setAdapter(adapter);
                builder.setView(listView);

                // Save selected unit on the array
                final Unit[] selectedUnit = new Unit[1];
                listView.setSelector(R.drawable.sc_list_item);

                listView.setOnItemClickListener((parent, view1, position, id) -> {
                    selectedPosition.set(position);
                    adapter.notifyDataSetChanged();

                    selectedUnit[0] = filteredUnits.get(position).first;
                });

                // Button to accept and cancel
                builder.setPositiveButton(getResources().getString(R.string.confirm), (dialog, which) -> {
                    if (selectedUnit[0] != null) {
                        // Open Google Maps and trace route from current location
                        String uriStr = "https://www.google.com/maps/dir/?api=1&destination="
                                + selectedUnit[0].getLatitude() + "," + selectedUnit[0].getLongitude()
                                + "&travelmode=driving";
                        Uri gmmIntentUri = Uri.parse(uriStr);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null)
                            startActivity(mapIntent);
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                builder.show();
            } else
                Toast.makeText(this, getResources().getString(R.string.noUnits), Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.call) {
            // Verify the client has information
            if (client != null) {
                // Call the client
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + client.getPhone()));
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(this, getResources().getString(R.string.no_app_for_calls), Toast.LENGTH_SHORT)
                            .show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_client, menu);

        return true;
    }

}

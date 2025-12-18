package com.agriberriesmx.agriberries;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_REQUEST_CODE = 231;
    private GoogleMap map;
    private double latitude, longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setup();
    }

    private void setup() {
        // Link XML to Java
        SearchView svLocation = findViewById(R.id.svLocation);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        FloatingActionButton btnSaveLocation = findViewById(R.id.btnSaveLocation);

        // Initialize views
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Add listeners
        svLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Get location
                String location = svLocation.getQuery().toString().trim();
                List<Address> addressList = null;

                if (location.length() > 0) {
                    // Get geocode location based on the name
                    Geocoder geocoder = new Geocoder(MapActivity.this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Get altitude and longitude
                if (addressList != null && addressList.size() > 0) {
                    // Go to searched location
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.clear();
                    map.addMarker(new MarkerOptions().position(latLng).title(location));
                    if (map.getCameraPosition().zoom < 10) map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    else map.animateCamera(CameraUpdateFactory.newLatLng(latLng));


                    // Save latitude and longitude
                    latitude = address.getLatitude();
                    longitude = address.getLongitude();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        btnSaveLocation.setOnClickListener(v -> {
            // Return to the previous activity
            if (latitude != -1000 && longitude != -1000) {
                Intent intent = new Intent();
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else Toast.makeText(MapActivity.this, getResources().getString(R.string.no_location_selected), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Initialize map
        map = googleMap;

        // Get location, latitude and longitude (if exists)
        String location = getIntent().getStringExtra("location");
        latitude = getIntent().getDoubleExtra("latitude", -1000);
        longitude = getIntent().getDoubleExtra("longitude", -1000);

        // Load previous location (if exists)
        if (latitude != -1000 && longitude != -1000) {
            // Add marker and zoom to location
            LatLng latLng = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(latLng).title(location));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        } else getCurrentLocation();

        // Add map listener
        map.setOnMapClickListener(latLng -> {
            // Add marker
            map.clear();
            map.addMarker(new MarkerOptions().position(latLng));

            // Save latitude and longitude
            latitude = latLng.latitude;
            longitude = latLng.longitude;
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    map.addMarker(new MarkerOptions().position(latLng));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }
            });
        } else ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

}

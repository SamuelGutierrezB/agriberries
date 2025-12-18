package com.agriberriesmx.agriberries.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.R;

public class MenuAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_menu);
        setup();
    }

    private void setup() {
        // Link XML to Java
        TextView tvManageConsultants = findViewById(R.id.tvManageConsultants);
        TextView tvManageKnowledgeCenter = findViewById(R.id.tvManageKnowledgeCenter);
        TextView tvManageClients = findViewById(R.id.tvManageClients);
        TextView tvManageCrops = findViewById(R.id.tvManageCrops);
        TextView tvManageNotifications = findViewById(R.id.tvManageNotifications);
        TextView tvManagePlagues = findViewById(R.id.tvManagePlagues);
        TextView tvManageTreatments = findViewById(R.id.tvManageTreatments);

        // Add listeners
        tvManageConsultants.setOnClickListener(v -> startActivity(new Intent(this, ManageConsultantsAdmin.class)));
        tvManageKnowledgeCenter.setOnClickListener(v -> startActivity(new Intent(this, ManageKnowledgeCenterAdmin.class)));
        tvManageClients.setOnClickListener(v -> startActivity(new Intent(this, ManageClientsAdmin.class)));
        tvManageCrops.setOnClickListener(v -> startActivity(new Intent(this, ManageCropsAdmin.class)));
        tvManageNotifications.setOnClickListener(v -> startActivity(new Intent(this, ManageNotificationsAdmin.class)));
        tvManagePlagues.setOnClickListener(v -> startActivity(new Intent(this, ManagePlaguesAdmin.class)));
        tvManageTreatments.setOnClickListener(v -> startActivity(new Intent(this, ManageTreatmentsAdmin.class)));
    }

}

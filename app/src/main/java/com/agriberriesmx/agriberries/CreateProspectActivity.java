package com.agriberriesmx.agriberries;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.agriberriesmx.agriberries.POJO.Client;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateProspectActivity extends AppCompatActivity {
    private Calendar registration;
    private CountryCodePicker ccp;
    private EditText etvName, etvManager, etvBusiness, etvEmail;
    private Spinner spinnerFrequency;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_prospect);
        setup();
    }

    private void setup() {
        // Link XML to Java
        etvName = findViewById(R.id.etvName);
        etvManager = findViewById(R.id.etvManager);
        etvBusiness = findViewById(R.id.etvBusiness);
        etvEmail = findViewById(R.id.etvEmail);
        ccp = findViewById(R.id.ccp);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        EditText etvPhone = findViewById(R.id.etvPhone);
        TextView tvRegistration = findViewById(R.id.tvRegistration);
        Button btnSignUpClient = findViewById(R.id.btnSignUpClient);

        // Get formatted registration date
        registration =  Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedBirthDate = sdf.format(registration.getTime());

        // Get string array for spinner
        ArrayAdapter<CharSequence> adapterCategories = ArrayAdapter.createFromResource(this, R.array.frequencies, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Link CCP to Edit Text
        ccp.registerCarrierNumberEditText(etvPhone);

        // Initialize views
        tvRegistration.setText(formattedBirthDate);
        spinnerFrequency.setAdapter(adapterCategories);

        // Add listener
        tvRegistration.setOnClickListener(v -> {
            int day = registration.get(Calendar.DAY_OF_MONTH);
            int month = registration.get(Calendar.MONTH);
            int year = registration.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateProspectActivity.this, (view, updatedYear, updatedMonth, updatedDay) -> {
                // Update birth date
                registration.set(Calendar.YEAR, updatedYear);
                registration.set(Calendar.MONTH, updatedMonth);
                registration.set(Calendar.DAY_OF_MONTH, updatedDay);

                // Get formatted birth date and show it
                tvRegistration.setText(sdf.format(registration.getTime()));
            }, year, month, day);

            // Show DatePicker dialog
            datePickerDialog.show();

        });
        btnSignUpClient.setOnClickListener(v -> createClient());
    }

    private void createClient() {
        // Get attributes to verify that they are not empty
        String name = Formatting.capitalizeName(etvName.getText().toString().trim());
        String manager = etvManager.getText().toString().trim();
        String business = etvBusiness.getText().toString().trim();
        String phone = ccp.getFullNumberWithPlus();
        String email = etvEmail.getText().toString().trim();
        int frequency = spinnerFrequency.getSelectedItemPosition();

        if (ccp.isValidFullNumber()) {
            if (!name.isEmpty()) {
                // Connect to Firebase Auth
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser != null) {
                    // Connect to Firebase Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String id = db.collection("clients").document().getId();
                    DocumentReference documentReference = db.collection("clients").document(id);

                    // Create consultants list
                    List<String> consultantList = new ArrayList<>();
                    consultantList.add(currentUser.getUid());

                    // Create client info
                    Client client = new Client();
                    client.setId(id);
                    client.setName(name);
                    client.setManager(manager);
                    client.setBusiness(business);
                    client.setPhone(phone);
                    client.setEmail(email);
                    client.setFrequency(frequency);
                    client.setConsultants(consultantList);
                    client.setRegistration(registration.getTime());
                    client.setBlocked(false);
                    client.setProspect(true);

                    // Save new client info into database
                    documentReference.set(client);
                    Toast.makeText(CreateProspectActivity.this, getResources().getString(R.string.clientCreated), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();

    }

}

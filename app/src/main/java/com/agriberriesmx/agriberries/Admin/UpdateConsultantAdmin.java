package com.agriberriesmx.agriberries.Admin;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.agriberriesmx.agriberries.POJO.Consultant;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UpdateConsultantAdmin extends AppCompatActivity {
    private Consultant consultant;
    private Calendar birth;
    private MenuItem deleteRestoreItem;
    private CountryCodePicker ccp;
    private EditText etvPassword, etvName;
    private Spinner spinnerCategory;
    private SwitchCompat swBlocked;
    private boolean newPassword = false;
    private boolean previousBlocked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_consultant);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout linearLayoutPassword = findViewById(R.id.linearLayoutPassword);
        EditText etvUser = findViewById(R.id.etvUser);
        etvPassword = findViewById(R.id.etvPassword);
        etvName = findViewById(R.id.etvName);
        ccp = findViewById(R.id.ccp);
        EditText etvPhone = findViewById(R.id.etvPhone);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        swBlocked = findViewById(R.id.swBlocked);
        TextView tvBirth = findViewById(R.id.tvBirth);
        ImageButton btnGeneratePassword = findViewById(R.id.btnGeneratePassword);
        Button btnNewPassword = findViewById(R.id.btnNewPassword);
        Button btnUpdateConsultant = findViewById(R.id.btnUpdateConsultant);

        // Get consultant
        boolean superAdmin = getIntent().getBooleanExtra("superAdmin", false);
        consultant = getIntent().getParcelableExtra("consultant");
        previousBlocked = consultant.isBlocked();

        // Get formatted birth date
        birth = Calendar.getInstance();
        birth.setTime(consultant.getBirth());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedBirthDate = sdf.format(birth.getTime());

        // Get string array for spinner
        ArrayAdapter<CharSequence> adapterCategories = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (!superAdmin) {
            // Initialize toolbar
            setSupportActionBar(toolbar);
            setTitle("");
            Drawable icon = toolbar.getOverflowIcon();
            if (icon != null) {
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                toolbar.setOverflowIcon(icon);
            }
        }

        // Link CCP to Edit Text
        ccp.registerCarrierNumberEditText(etvPhone);

        // Initialize views
        spinnerCategory.setAdapter(adapterCategories);
        etvUser.setText(consultant.getUsername());
        etvName.setText(consultant.getName());
        ccp.setFullNumber(consultant.getPhone());
        swBlocked.setChecked(consultant.isBlocked());
        tvBirth.setText(formattedBirthDate);
        spinnerCategory.setSelection(consultant.getCategory());

        // Add listener
        tvBirth.setOnClickListener(v -> {
            int day = birth.get(Calendar.DAY_OF_MONTH);
            int month = birth.get(Calendar.MONTH);
            int year = birth.get(Calendar.YEAR);

            // Create DatePicker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(UpdateConsultantAdmin.this, (view, updatedYear, updatedMonth, updatedDay) -> {
                // Update birth date
                birth.set(Calendar.YEAR, updatedYear);
                birth.set(Calendar.MONTH, updatedMonth);
                birth.set(Calendar.DAY_OF_MONTH, updatedDay);

                // Get formatted birth date and show it
                tvBirth.setText(sdf.format(birth.getTime()));
            }, year, month, day);

            // Show DatePicker dialog
            datePickerDialog.show();

        });
        btnNewPassword.setOnClickListener(v -> {
            // Change visibility and values
            newPassword = true;
            btnNewPassword.setVisibility(View.GONE);
            linearLayoutPassword.setVisibility(View.VISIBLE);
        });
        btnGeneratePassword.setOnClickListener(v -> generatePassword());
        btnUpdateConsultant.setOnClickListener(v -> updateConsultant());
    }

    private void updateConsultant() {
        // Get consultant information to verify that they are not empty
        String password = etvPassword.getText().toString().trim();
        String name = Formatting.capitalizeName(etvName.getText().toString().trim());
        String phone = ccp.getFullNumberWithPlus();
        int category = spinnerCategory.getSelectedItemPosition();
        boolean blocked = swBlocked.isChecked();

        if ((!password.isEmpty() || !newPassword) && !name.isEmpty() && !phone.isEmpty()) {
            if (ccp.isValidFullNumber()) {
                if (newPassword && password.length() < 6)
                    Toast.makeText(this, getResources().getString(R.string.passwordLength), Toast.LENGTH_SHORT).show();
                else {
                    // Connect to Firebase Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = db.collection("consultants").document(consultant.getId());

                    // Update consultant info
                    documentReference.update("name", name, "phone", phone, "category", category);
                    if (previousBlocked != blocked) toggleBlockStatus(consultant.getId(), blocked);
                    if (newPassword) updateUserPassword(consultant.getId(), password);
                    Toast.makeText(this, getResources().getString(R.string.user_updated), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else Toast.makeText(this, getResources().getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void toggleBlockStatus(String uid, boolean blocked) {
        // Connect to Firebase Functions
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        // Create Map with info
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("blocked", blocked);

        // Call function
        mFunctions
                .getHttpsCallable("toggleUserBlockStatus")
                .call(data)
                .addOnCompleteListener(task -> {
                    // Get result
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) task.getResult().getData();

                    if (result != null) {
                        Boolean success = (Boolean) result.get("success");
                        if (success != null && !success)
                            Toast.makeText(UpdateConsultantAdmin.this, "Error: " + result.get("error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserPassword(String uid, String password) {
        // Connect to Firebase Functions
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        // Create Map with information
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("password", password);

        // Call function
        mFunctions
                .getHttpsCallable("updateUserPassword")
                .call(data)
                .addOnCompleteListener(task -> {
                    // Get result
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) task.getResult().getData();

                    if (result != null) {
                        Boolean success = (Boolean) result.get("success");
                        if (success != null && !success)
                            Toast.makeText(UpdateConsultantAdmin.this, "Error: " + result.get("error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generatePassword() {
        // Characters that will be use to generate the password and length of the password
        String characters = getResources().getString(R.string.characters);
        int length = 16;

        // Generate random password
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int)(characters.length() * Math.random());
            sb.append(characters.charAt(index));
        }

        // Show password in EditText
        String password = sb.toString();
        etvPassword.setText(password);
    }

    private void showDeleteConsultantDialog() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        if (consultant.getDeleted() == null) {
            // To delete
            tvMessage.setText(getResources().getString(R.string.askDeleteConsultant));
            btnPositive.setText(getResources().getString(R.string.delete));
        } else {
            // To restore
            tvMessage.setText(getResources().getString(R.string.askRestoreConsultant));
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
            DocumentReference documentReference = db.collection("consultants").document(consultant.getId());

            if (consultant.getDeleted() == null) {
                // Delete consultant
                documentReference.update("deleted", Formatting.addSevenDaysAndFormat(new Date()));
                consultant.setDeleted(Formatting.addSevenDaysAndFormat(new Date()));
                deleteRestoreItem.setTitle(getResources().getString(R.string.restore));
                Toast.makeText(UpdateConsultantAdmin.this, getResources().getString(R.string.userDeleted), Toast.LENGTH_SHORT).show();
            } else {
                // Restore consultant
                documentReference.update("deleted", null);
                consultant.setDeleted(null);
                deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
                Toast.makeText(UpdateConsultantAdmin.this, getResources().getString(R.string.userRestored), Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Show Alert Dialog
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.deleteRestore) {
            showDeleteConsultantDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        // Change text
        deleteRestoreItem = menu.findItem(R.id.deleteRestore);
        if (consultant.getDeleted() == null)
            deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
        else
            deleteRestoreItem.setTitle(getResources().getString(R.string.restore));

        return true;
    }

}

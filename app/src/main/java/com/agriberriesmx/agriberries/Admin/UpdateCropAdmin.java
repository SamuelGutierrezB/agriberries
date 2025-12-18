package com.agriberriesmx.agriberries.Admin;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Adapter.TextAdapter;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Formatting;
import com.agriberriesmx.agriberries.Utils.SwipeToDeleteCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UpdateCropAdmin extends AppCompatActivity {
    private Crop crop;
    private MenuItem deleteRestoreItem;
    private List<String> types;
    private EditText etvName, etvType;
    private TextAdapter textAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_update_crop);
        setup();
    }

    private void setup() {
        // Link XML to Java
        Toolbar toolbar = findViewById(R.id.toolbar);
        etvName = findViewById(R.id.etvName);
        etvType = findViewById(R.id.etvType);
        RecyclerView rvTypes = findViewById(R.id.rvTypes);
        ImageButton btnAddType = findViewById(R.id.btnAddType);
        Button btnUpdateCrop = findViewById(R.id.btnUpdateCrop);

        // Get crop
        crop = getIntent().getParcelableExtra("crop");
        types = crop.getTypes();

        // Initialize views
        etvName.setText(crop.getName());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        textAdapter = new TextAdapter(types);
        rvTypes.setLayoutManager(linearLayoutManager);
        rvTypes.setAdapter(textAdapter);

        // Initialize toolbar
        setSupportActionBar(toolbar);
        setTitle("");
        Drawable icon = toolbar.getOverflowIcon();
        if (icon != null) {
            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(icon);
        }

        // Enable swipe to delete and undo
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                types.remove(position);
                textAdapter.notifyItemRemoved(position);
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvTypes);

        // Add listeners
        btnAddType.setOnClickListener(v -> addType());
        btnUpdateCrop.setOnClickListener(v -> updateCrop());
    }

    private void addType() {
        // Add type to the list and update adapter
        String type = etvType.getText().toString().trim();

        if (!type.isEmpty()) {
            if (!types.contains(type)) {
                int position = Collections.binarySearch(types, type);

                // Get insert position for alphabetic order
                if (position < 0) position = -(position + 1);

                // Add element
                types.add(position, type);
                textAdapter.notifyItemInserted(position);
                etvType.setText("");
            } else Toast.makeText(this, getResources().getString(R.string.type_already_added), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    private void showDeleteCropDialog() {
        // Inflate custom content dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.content_dialog_confirmation, null);

        // Link XML to Java
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPositive = dialogView.findViewById(R.id.btnPositive);
        Button btnNegative = dialogView.findViewById(R.id.btnNegative);

        // Initialize views
        if (crop.getDeleted() == null) {
            // To delete
            tvMessage.setText(getResources().getString(R.string.askDeleteCrop));
            btnPositive.setText(getResources().getString(R.string.delete));
        } else {
            // To restore
            tvMessage.setText(getResources().getString(R.string.askRestoreCrop));
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
            DocumentReference documentReference = db.collection("crops").document(crop.getId());

            if (crop.getDeleted() == null) {
                // Delete crop
                documentReference.update("deleted", Formatting.addSevenDaysAndFormat(new Date()));
                crop.setDeleted(Formatting.addSevenDaysAndFormat(new Date()));
                deleteRestoreItem.setTitle(getResources().getString(R.string.restore));
                Toast.makeText(UpdateCropAdmin.this, getResources().getString(R.string.cropDeleted), Toast.LENGTH_SHORT).show();
            } else {
                // Restore crop
                documentReference.update("deleted", null);
                crop.setDeleted(null);
                deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
                Toast.makeText(UpdateCropAdmin.this, getResources().getString(R.string.cropRestored), Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        // Show Alert Dialog
        dialog.show();
    }

    private void updateCrop() {
        // Get attributes to verify that they are not empty
        String name = etvName.getText().toString().trim();

        if (!name.isEmpty() && types.size() > 0) {
            // Connect to Firebase Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("crops").document(crop.getId());

            // Update crop info
            documentReference.update("name", name, "types", types);
            Toast.makeText(UpdateCropAdmin.this, getResources().getString(R.string.cropUpdated), Toast.LENGTH_SHORT).show();
            finish();
        } else Toast.makeText(this, getResources().getString(R.string.unfilled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.deleteRestore) {
            showDeleteCropDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        // Change text
        deleteRestoreItem = menu.findItem(R.id.deleteRestore);
        if (crop.getDeleted() == null)
            deleteRestoreItem.setTitle(getResources().getString(R.string.delete));
        else
            deleteRestoreItem.setTitle(getResources().getString(R.string.restore));

        return true;
    }

}

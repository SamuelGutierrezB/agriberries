package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.UpdateUnitActivity;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.R;

import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.ViewHolder> {
    private final List<Unit> unitList;
    private final String clientId;

    public UnitAdapter(List<Unit> unitList, String clientId) {
        this.unitList = unitList;
        this.clientId = clientId;
    }

    @NonNull
    @Override
    public UnitAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCropLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            tvCropLocation = itemView.findViewById(R.id.tvCropLocation);
        }

        public void setup(int position) {
            // Get unit info
            Unit unit = unitList.get(position);

            // Initialize views
            String text = unit.getName() + " - " + unit.getCrop();
            tvCropLocation.setText(text);

            // Add listeners
            tvCropLocation.setOnClickListener(v -> changeActivity(itemView.getContext(), unit));
        }

        private void changeActivity(Context context, Unit unit) {
            // Change activity to update unit
            Intent intent = new Intent(context, UpdateUnitActivity.class);
            intent.putExtra("unit", unit);
            intent.putExtra("clientId", clientId);
            context.startActivity(intent);
        }

    }

}

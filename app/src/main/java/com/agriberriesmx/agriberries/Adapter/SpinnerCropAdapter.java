package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.agriberriesmx.agriberries.R;

import java.util.List;

public class SpinnerCropAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> cropList;

    public SpinnerCropAdapter(Context context, List<String> cropList) {
        super(context, R.layout.spinner_crop_item, cropList);
        this.cropList = cropList;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.spinner_crop_item, parent, false);

        // Link XML to Java
        ImageView ivCrop = view.findViewById(R.id.ivCrop);
        TextView tvCrop = view.findViewById(R.id.tvCrop);

        // Get crop text and initialize views
        String crop = cropList.get(position);

        if (crop.equals(context.getResources().getString(R.string.all)))
            ivCrop.setBackgroundResource(R.drawable.ic_all);
        else if (crop.equals(context.getResources().getString(R.string.agave)))
            ivCrop.setBackgroundResource(R.drawable.ic_agave);
        else if (crop.equals(context.getResources().getString(R.string.avocado)))
            ivCrop.setBackgroundResource(R.drawable.ic_avocado);
        else if (crop.equals(context.getResources().getString(R.string.blueberry)))
            ivCrop.setBackgroundResource(R.drawable.ic_blueberry);
        else if (crop.equals(context.getResources().getString(R.string.raspberry)))
            ivCrop.setBackgroundResource(R.drawable.ic_raspberry);
        else if (crop.equals(context.getResources().getString(R.string.strawberry)))
            ivCrop.setBackgroundResource(R.drawable.ic_strawberry);
        else if (crop.equals(context.getResources().getString(R.string.blackberry)))
            ivCrop.setBackgroundResource(R.drawable.ic_blackberry);
        else if (crop.equals(context.getResources().getString(R.string.fig)))
            ivCrop.setBackgroundResource(R.drawable.ic_fig);
        tvCrop.setText(crop);

        return view;
    }
}


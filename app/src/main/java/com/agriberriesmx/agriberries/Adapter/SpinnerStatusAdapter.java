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

public class SpinnerStatusAdapter extends ArrayAdapter<String> {
    private final Context context;
    List<String> statusList;

    public SpinnerStatusAdapter(Context context, List<String> statusList) {
        super(context, R.layout.spinner_status_item, statusList);
        this.context = context;
        this.statusList = statusList;
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
        View view = inflater.inflate(R.layout.spinner_status_item, parent, false);

        ImageView ivStatus = view.findViewById(R.id.ivStatus);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        String status = statusList.get(position);

        switch (position) {
            case 0:
                // Active
                ivStatus.setBackgroundColor(context.getColor(R.color.green));
                break;
            case 1:
                // Deletion / Inactive / Blocked
                ivStatus.setBackgroundColor(context.getColor(R.color.red));
                break;
            default:
                break;
        }
        tvStatus.setText(status);

        return view;
    }
}


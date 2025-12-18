package com.agriberriesmx.agriberries.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.R;

import java.util.List;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.ViewHolder> {
    private final List<String> textList;

    public TextAdapter(List<String> textList) {
        this.textList = textList;
    }

    @NonNull
    @Override
    public TextAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return textList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            tvText = itemView.findViewById(R.id.tvText);
        }

        public void setup(int position) {
            // Get text and initialize it
            String text = textList.get(position);
            tvText.setText(text);
        }
    }

}

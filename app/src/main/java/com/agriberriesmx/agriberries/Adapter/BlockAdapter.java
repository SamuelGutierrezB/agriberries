package com.agriberriesmx.agriberries.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.R;

import java.util.List;

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.ViewHolder> {
    List<String> blocks;

    public BlockAdapter(List<String> blocks) {
        this.blocks = blocks;
    }

    @NonNull
    @Override
    public BlockAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_block, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBlock;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Link XML to Java
            tvBlock = itemView.findViewById(R.id.tvBlock);
        }

        public void setup(int position) {
            //Get text block and initialize it
            String block = blocks.get(position);
            tvBlock.setText(block);
        }

    }

}

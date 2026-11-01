package com.example.planta;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

public class PlantsAdapter extends RecyclerView.Adapter<PlantsAdapter.PlantViewHolder> {

    private final ArrayList<PlantItem> plantList;
    private final OnItemClickListener clickListener;
    private final OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(PlantItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(PlantItem item, int position);
    }

    public PlantsAdapter(ArrayList<PlantItem> plantList, OnItemClickListener clickListener, OnDeleteClickListener deleteListener) {
        this.plantList = plantList;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantItem item = plantList.get(position);
        holder.tvName.setText(item.getName().replace("_", " "));
        holder.tvDate.setText("Added " + item.getDateAdded());

        if (item.getImagePath() != null && new File(item.getImagePath()).exists()) {
            holder.imgPlant.setImageBitmap(BitmapFactory.decodeFile(item.getImagePath()));
        } else {
            holder.imgPlant.setImageResource(android.R.color.darker_gray);
        }

        // Set up row click listener to see full detailed layout screens
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(item));

        // Set up individual row element garbage data delete action callback handler
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(item, position));
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlant;
        TextView tvName, tvDate;
        ImageButton btnDelete;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlant = itemView.findViewById(R.id.itemImgPlant);
            tvName = itemView.findViewById(R.id.itemTvName);
            tvDate = itemView.findViewById(R.id.itemTvDate);
            btnDelete = itemView.findViewById(R.id.itemBtnDelete);
        }
    }
}
package com.example.planta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MyPlantsFragment extends Fragment {

    private RecyclerView rvMyPlants;
    private TextView tvEmptyState;
    private DatabaseHelper dbHelper;
    private PlantsAdapter adapter;
    private ArrayList<PlantItem> plantList;

    public MyPlantsFragment() {
        super(R.layout.fragment_my_plants);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMyPlants = view.findViewById(R.id.rvMyPlants);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        dbHelper = new DatabaseHelper(requireContext());

        if (rvMyPlants != null) {
            rvMyPlants.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        loadPlantsData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlantsData();
    }

    private void loadPlantsData() {
        // Safety Check: Avoid executing data setup sequences if components aren't ready yet
        if (dbHelper == null || rvMyPlants == null || tvEmptyState == null) {
            return;
        }

        plantList = dbHelper.getAllPlants();

        if (plantList == null || plantList.isEmpty()) {
            rvMyPlants.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvMyPlants.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);

            adapter = new PlantsAdapter(plantList,
                    item -> {
                        if (getContext() != null) {
                            Intent intent = new Intent(requireContext(), ResultActivity.class);
                            String processedKeyName = item.getName().replace(" ", "_");
                            intent.putExtra("plant_name", processedKeyName);
                            intent.putExtra("image_path", item.getImagePath());
                            intent.putExtra("confidence", 0.95f);
                            intent.putExtra("is_from_history", true);
                            startActivity(intent);
                        }
                    },
                    (item, position) -> {
                        dbHelper.deletePlant(item.getId());
                        if (position < plantList.size()) {
                            plantList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }

                        if (plantList.isEmpty()) {
                            rvMyPlants.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        }
                    }
            );
            rvMyPlants.setAdapter(adapter);
        }
    }
}
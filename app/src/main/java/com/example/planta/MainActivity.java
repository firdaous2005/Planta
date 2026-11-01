package com.example.planta;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navClassify, navDiagnose, navMyPlants;
    private ImageView iconClassify, iconDiagnose, iconMyPlants;
    private TextView labelClassify, labelDiagnose, labelMyPlants;
    private View indicatorClassify, indicatorDiagnose, indicatorMyPlants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navClassify  = findViewById(R.id.navClassify);
        navDiagnose  = findViewById(R.id.navDiagnose);
        navMyPlants  = findViewById(R.id.navMyPlants);

        iconClassify = findViewById(R.id.iconClassify);
        iconDiagnose = findViewById(R.id.iconDiagnose);
        iconMyPlants = findViewById(R.id.iconMyPlants);

        labelClassify = findViewById(R.id.labelClassify);
        labelDiagnose = findViewById(R.id.labelDiagnose);
        labelMyPlants = findViewById(R.id.labelMyPlants);

        indicatorClassify = findViewById(R.id.indicatorClassify);
        indicatorDiagnose = findViewById(R.id.indicatorDiagnose);
        indicatorMyPlants = findViewById(R.id.indicatorMyPlants);

        // Default tab
        loadFragment(new ClassificationFragment());
        selectTab(0);

        navClassify.setOnClickListener(v -> {
            loadFragment(new ClassificationFragment());
            selectTab(0);
        });

        navDiagnose.setOnClickListener(v -> {
            loadFragment(new DiagnoseFragment());
            selectTab(1);
        });

        navMyPlants.setOnClickListener(v -> {
            loadFragment(new MyPlantsFragment());
            selectTab(2);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }

    /** Highlights the selected tab, resets the other two — identical sizing/position for all */
    private void selectTab(int index) {
        int active   = ContextCompat.getColor(this, R.color.light_green_accent);
        int inactive = ContextCompat.getColor(this, R.color.nav_icon_inactive);
        int transparent = ContextCompat.getColor(this, android.R.color.transparent);

        // Reset all
        iconClassify.setColorFilter(inactive);
        iconDiagnose.setColorFilter(inactive);
        iconMyPlants.setColorFilter(inactive);

        labelClassify.setTextColor(inactive);
        labelDiagnose.setTextColor(inactive);
        labelMyPlants.setTextColor(inactive);

        labelClassify.setTypeface(null, android.graphics.Typeface.NORMAL);
        labelDiagnose.setTypeface(null, android.graphics.Typeface.NORMAL);
        labelMyPlants.setTypeface(null, android.graphics.Typeface.NORMAL);

        indicatorClassify.setBackgroundColor(transparent);
        indicatorDiagnose.setBackgroundColor(transparent);
        indicatorMyPlants.setBackgroundColor(transparent);

        // Apply active state
        switch (index) {
            case 0:
                iconClassify.setColorFilter(active);
                labelClassify.setTextColor(active);
                labelClassify.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorClassify.setBackgroundColor(active);
                break;
            case 1:
                iconDiagnose.setColorFilter(active);
                labelDiagnose.setTextColor(active);
                labelDiagnose.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorDiagnose.setBackgroundColor(active);
                break;
            case 2:
                iconMyPlants.setColorFilter(active);
                labelMyPlants.setTextColor(active);
                labelMyPlants.setTypeface(null, android.graphics.Typeface.BOLD);
                indicatorMyPlants.setBackgroundColor(active);
                break;
        }
    }
}
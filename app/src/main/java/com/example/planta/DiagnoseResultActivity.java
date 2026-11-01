package com.example.planta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class DiagnoseResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose_result);

        // Core Layout views structures
        View mainContentView = findViewById(R.id.mainContentView);
        View lowConfidenceView = findViewById(R.id.lowConfidenceView);

        // Fetch Bundle Extra Intent arguments
        String plantName   = getIntent().getStringExtra("plant_name");
        String diseaseName = getIntent().getStringExtra("disease_name");
        float  confidence  = getIntent().getFloatExtra("confidence", 0f);
        String rawLabel    = getIntent().getStringExtra("raw_label");
        String imagePath   = getIntent().getStringExtra("image_path");

        // ★ Check accuracy threshold limit (< 90%)
        if (confidence < 0.90f) {
            mainContentView.setVisibility(View.GONE);
            lowConfidenceView.setVisibility(View.VISIBLE);

            MaterialButton btnRetake = findViewById(R.id.btnRetake);
            MaterialButton btnCancelRetake = findViewById(R.id.btnCancelRetake);

            btnRetake.setOnClickListener(v -> finish());
            btnCancelRetake.setOnClickListener(v -> finish());
            return; // Terminate execution here
        }

        // Show standard healthy layout structure configuration
        mainContentView.setVisibility(View.VISIBLE);
        lowConfidenceView.setVisibility(View.GONE);

        // Load image from temp file safely
        Bitmap image = null;
        if (imagePath != null) {
            image = BitmapFactory.decodeFile(imagePath);
        }
        if (image == null && getIntent().hasExtra("image")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                image = getIntent().getParcelableExtra("image", Bitmap.class);
            } else {
                image = getIntent().getParcelableExtra("image");
            }
        }

        ImageView imgPlant         = findViewById(R.id.imgDiagnoseResult);
        TextView  tvPlant          = findViewById(R.id.tvDiagnosePlant);
        TextView  tvDisease        = findViewById(R.id.tvDiseaseResult);
        TextView  tvConfidence     = findViewById(R.id.tvDiagnoseConfidence);
        TextView  tvStatus         = findViewById(R.id.tvHealthStatus);
        TextView  tvRecommendation = findViewById(R.id.tvRecommendation);
        MaterialButton btnBack     = findViewById(R.id.btnDiagnoseBack);

        if (image != null) imgPlant.setImageBitmap(image);

        tvPlant.setText(plantName != null ? plantName.replace("_", " ") : "Unknown Plant");
        tvConfidence.setText(String.format("%.1f%% match", confidence * 100));

        boolean isHealthy = diseaseName != null && diseaseName.equalsIgnoreCase("healthy");

        if (isHealthy) {
            tvDisease.setText("No Disease Detected");
            tvStatus.setText("✓ Healthy");
            // Match structural accent brand elements color
            tvStatus.setTextColor(getColor(R.color.light_green_accent));
            tvRecommendation.setText(
                    "Your plant looks healthy! Keep up the good care:\n\n" +
                            "• Water regularly but avoid overwatering\n" +
                            "• Ensure adequate sunlight\n" +
                            "• Check for pests periodically\n" +
                            "• Fertilize during growing season");
        } else {
            tvDisease.setText(diseaseName != null ? diseaseName.replace("_", " ") : "Unknown Disease");
            tvStatus.setText("⚠ Disease Detected");
            tvStatus.setTextColor(0xFFFF6B6B); // Soft danger red highlight choice
            tvRecommendation.setText(getRecommendation(rawLabel));
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private String getRecommendation(String rawLabel) {
        if (rawLabel == null) return "• Consult a plant specialist for treatment advice.";
        if (rawLabel.contains("Apple_scab"))
            return "• Remove and destroy infected leaves\n• Apply fungicide (captan or myclobutanil)\n• Improve air circulation\n• Avoid overhead watering";
        if (rawLabel.contains("Black_rot"))
            return "• Prune and destroy infected parts\n• Apply copper-based fungicide\n• Remove mummified fruits\n• Ensure good drainage";
        if (rawLabel.contains("Cedar_apple_rust"))
            return "• Apply fungicide in early spring\n• Remove nearby cedar/juniper trees if possible\n• Use resistant apple varieties\n• Spray myclobutanil or triadimefon";
        if (rawLabel.contains("Powdery_mildew"))
            return "• Apply sulfur or potassium bicarbonate spray\n• Improve air circulation\n• Avoid overhead watering\n• Remove heavily infected plant parts";
        if (rawLabel.contains("Cercospora") || rawLabel.contains("Gray_leaf_spot"))
            return "• Apply foliar fungicide (strobulirin class)\n• Rotate crops annually\n• Use resistant hybrids\n• Reduce leaf wetness duration";
        if (rawLabel.contains("Common_rust"))
            return "• Apply fungicide at first sign of rust\n• Use resistant corn varieties\n• Plant early to avoid peak rust season\n• Monitor fields regularly";
        if (rawLabel.contains("Northern_Leaf_Blight"))
            return "• Apply fungicide (azoxystrobin or propiconazole)\n• Use resistant hybrids\n• Crop rotation\n• Bury or till infected crop debris";
        if (rawLabel.contains("Esca") || rawLabel.contains("Black_Measles"))
            return "• Prune infected wood and seal wounds\n• Apply fungicide to pruning cuts\n• Avoid water stress\n• Remove severely infected vines";
        if (rawLabel.contains("Leaf_blight"))
            return "• Apply copper-based fungicide\n• Remove infected leaves promptly\n• Improve canopy air circulation\n• Avoid wetting foliage";
        if (rawLabel.contains("Haunglongbing") || rawLabel.contains("Citrus_greening"))
            return "• No cure available — remove infected trees\n• Control Asian citrus psyllid vector\n• Use certified disease-free planting material\n• Maintain tree nutrition to slow progression";
        if (rawLabel.contains("Bacterial_spot"))
            return "• Apply copper-based bactericide\n• Avoid overhead irrigation\n• Use disease-free seeds/transplants\n• Remove and destroy infected plant material";
        if (rawLabel.contains("Early_blight"))
            return "• Apply fungicide (chlorothalonil or mancozeb)\n• Remove lower infected leaves\n• Mulch around plants\n• Avoid overhead watering";
        if (rawLabel.contains("Late_blight"))
            return "• Apply fungicide immediately (metalaxyl or chlorothalonil)\n• Remove and destroy all infected tissue\n• Avoid working in wet conditions\n• Improve drainage and air circulation";
        if (rawLabel.contains("Leaf_Mold"))
            return "• Improve greenhouse ventilation\n• Reduce humidity below 85%\n• Apply fungicide (chlorothalonil)\n• Remove infected leaves";
        if (rawLabel.contains("Septoria"))
            return "• Apply fungicide at first sign (mancozeb or chlorothalonil)\n• Remove infected lower leaves\n• Avoid overhead watering\n• Rotate crops";
        if (rawLabel.contains("Spider_mites"))
            return "• Apply miticide or insecticidal soap\n• Increase humidity around plants\n• Spray undersides of leaves with water\n• Introduce predatory mites as biological control";
        if (rawLabel.contains("Target_Spot"))
            return "• Apply fungicide (azoxystrobin)\n• Remove infected leaves\n• Improve air circulation\n• Avoid excessive nitrogen fertilization";
        if (rawLabel.contains("Yellow_Leaf_Curl"))
            return "• Control whitefly vector with insecticide\n• Use reflective mulches to deter whiteflies\n• Remove and destroy infected plants\n• Use virus-resistant tomato varieties";
        if (rawLabel.contains("mosaic_virus"))
            return "• No cure — remove infected plants\n• Control aphid and insect vectors\n• Wash hands and tools after handling plants\n• Use virus-resistant varieties";
        if (rawLabel.contains("Leaf_scorch"))
            return "• Apply fungicide (captan or myclobutanil)\n• Remove infected leaves\n• Avoid overhead irrigation\n• Ensure good air circulation";
        return "• Isolate the affected plant\n• Remove visibly infected parts\n• Consult a local plant specialist\n• Consider sending a sample to a plant disease lab";
    }
}
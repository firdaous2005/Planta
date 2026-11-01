package com.example.planta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResultActivity extends AppCompatActivity {

    private static final Map<String, String[]> PLANT_DATABASE = new HashMap<>();

    static {
        // ── Vegetables (lines 1–13 in labels.txt) ────────────────────────
        PLANT_DATABASE.put("Bean",         new String[]{"Legume high in protein and dietary fiber.", "Every 2 days", "Full sun", "50–60 days"});
        PLANT_DATABASE.put("Brinjal",      new String[]{"Also known as eggplant, rich in antioxidants.", "Twice a week", "Full sun", "70–85 days"});
        PLANT_DATABASE.put("Broccoli",     new String[]{"Cruciferous vegetable packed with fiber and vitamins.", "Twice a week", "Full sun", "80–100 days"});
        PLANT_DATABASE.put("Cabbage",      new String[]{"Leafy vegetable high in vitamin K and fiber.", "Twice a week", "Full sun", "80–180 days"});
        PLANT_DATABASE.put("Capsicum",     new String[]{"Bell pepper rich in vitamin C and antioxidants.", "Twice a week", "Full sun", "60–90 days"});
        PLANT_DATABASE.put("Carrot",       new String[]{"Root vegetable rich in beta-carotene and vitamins.", "Twice a week", "Full sun", "70–80 days"});
        PLANT_DATABASE.put("Cauliflower",  new String[]{"White cruciferous vegetable rich in vitamin C.", "Twice a week", "Full sun", "55–100 days"});
        PLANT_DATABASE.put("Cucumber",     new String[]{"Refreshing vegetable with high water content.", "Every 2 days", "Full sun", "50–70 days"});
        PLANT_DATABASE.put("Papaya",       new String[]{"Tropical fruit rich in digestive enzymes and vitamin C.", "Every 2 days", "Full sun", "9–11 months"});
        PLANT_DATABASE.put("Potato",       new String[]{"Starchy tuber, one of the world's most consumed crops.", "Twice a week", "Full sun", "70–120 days"});
        PLANT_DATABASE.put("Pumpkin",      new String[]{"Large squash rich in vitamin A and antioxidants.", "Twice a week", "Full sun", "75–100 days"});
        PLANT_DATABASE.put("Radish",       new String[]{"Fast-growing root vegetable with peppery flavor.", "Every 2 days", "Full sun", "22–70 days"});
        PLANT_DATABASE.put("Tomato",       new String[]{"Popular fruit used as vegetable, rich in lycopene.", "Daily", "Full sun", "60–85 days"});

        // ── Fruits (lines 14–21 in labels.txt, cleaned by CameraActivity) ─
        // "apple fruit"        → "Apple"
        // "banana fruit"       → "Banana"
        // "kiwi fruit"         → "Kiwi"
        // "mango fruit"        → "Mango"
        // "orange fruit"       → "Orange"
        // "peaches fruit"      → "Peaches"
        // "pomegranates fruit" → "Pomegranates"
        // "strawberry fruit"   → "Strawberry"
        PLANT_DATABASE.put("Apple",        new String[]{"Pomaceous fruit rich in fiber and vitamin C.", "Weekly", "Full sun", "2–5 years"});
        PLANT_DATABASE.put("Banana",       new String[]{"Tropical fruit rich in potassium and vitamin B6.", "Twice a week", "Full sun", "9–12 months"});
        PLANT_DATABASE.put("Kiwi",         new String[]{"Small fruit packed with vitamin C and dietary fiber.", "Every 2 days", "Full sun", "3–5 years"});
        PLANT_DATABASE.put("Mango",        new String[]{"Tropical drupe known as the king of fruits.", "Twice a week", "Full sun", "3–5 years"});
        PLANT_DATABASE.put("Orange",       new String[]{"Citrus fruit packed with vitamin C and antioxidants.", "Twice a week", "Full sun", "7–15 years"});
        PLANT_DATABASE.put("Peaches",      new String[]{"Stone fruit rich in vitamins A and C.", "Twice a week", "Full sun", "2–4 years"});
        PLANT_DATABASE.put("Pomegranates", new String[]{"Ancient fruit rich in antioxidants and punicalagins.", "Weekly", "Full sun", "3–5 years"});
        PLANT_DATABASE.put("Strawberry",   new String[]{"Sweet red berry high in vitamin C and antioxidants.", "Every 2 days", "Full sun", "4–6 weeks"});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        View mainContentView   = findViewById(R.id.mainContentView);
        View lowConfidenceView = findViewById(R.id.lowConfidenceView);

        String  plantName     = getIntent().getStringExtra("plant_name");
        float   confidence    = getIntent().getFloatExtra("confidence", 0f);
        String  imagePath     = getIntent().getStringExtra("image_path");
        boolean isFromHistory = getIntent().getBooleanExtra("is_from_history", false);

        if (confidence < 0.90f) {
            mainContentView.setVisibility(View.GONE);
            lowConfidenceView.setVisibility(View.VISIBLE);
            MaterialButton btnRetake       = findViewById(R.id.btnRetake);
            MaterialButton btnCancelRetake = findViewById(R.id.btnCancelRetake);
            btnRetake.setOnClickListener(v -> finish());
            btnCancelRetake.setOnClickListener(v -> finish());
            return;
        }

        mainContentView.setVisibility(View.VISIBLE);
        lowConfidenceView.setVisibility(View.GONE);

        // Load bitmap from file path
        Bitmap bitmap = null;
        if (imagePath != null) {
            bitmap = BitmapFactory.decodeFile(imagePath);
        }
        if (bitmap == null && getIntent().hasExtra("image")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bitmap = getIntent().getParcelableExtra("image", Bitmap.class);
            } else {
                bitmap = getIntent().getParcelableExtra("image");
            }
        }

        ImageView      imgPlant         = findViewById(R.id.imgPlant);
        TextView       tvName           = findViewById(R.id.tvName);
        TextView       tvConfidence     = findViewById(R.id.tvConfidence);
        TextView       tvDescription    = findViewById(R.id.tvDescription);
        TextView       tvWater          = findViewById(R.id.tvWater);
        TextView       tvSun            = findViewById(R.id.tvSun);
        TextView       tvHarvest        = findViewById(R.id.tvHarvest);
        MaterialButton btnBack          = findViewById(R.id.btnBack);
        MaterialButton btnAddToMyPlants = findViewById(R.id.btnAddToMyPlants);

        if (bitmap != null) imgPlant.setImageBitmap(bitmap);

        // plantName already cleaned by CameraActivity (e.g. "Apple", "Banana")
        final String finalPlantName = plantName != null
                ? plantName.replace("_", " ")
                : "Unknown Plant";

        tvName.setText(finalPlantName);
        tvConfidence.setText(String.format("%.1f%% match", confidence * 100));

        String[] info = PLANT_DATABASE.get(plantName);
        if (info != null) {
            tvDescription.setText(info[0]);
            tvWater.setText(info[1]);
            tvSun.setText(info[2]);
            tvHarvest.setText(info[3]);
        } else {
            tvDescription.setText("A healthy plant from your garden.");
            tvWater.setText("Regular");
            tvSun.setText("Full sun");
            tvHarvest.setText("Varies");
        }

        btnBack.setOnClickListener(v -> finish());

        if (isFromHistory) {
            btnAddToMyPlants.setVisibility(View.GONE);
        } else {
            btnAddToMyPlants.setVisibility(View.VISIBLE);
            DatabaseHelper dbHelper  = new DatabaseHelper(this);
            final Bitmap   finalBitmap = bitmap;

            btnAddToMyPlants.setOnClickListener(v -> {
                String savedImagePath = saveImagePermanently(imagePath, finalBitmap);
                boolean success = dbHelper.addPlant(finalPlantName, savedImagePath);
                if (success) {
                    Toast.makeText(ResultActivity.this,
                            "Plant added to My Plants!", Toast.LENGTH_SHORT).show();
                    btnAddToMyPlants.setEnabled(false);
                    btnAddToMyPlants.setText("✓ Added to Collection");
                } else {
                    Toast.makeText(ResultActivity.this,
                            "Failed to save plant.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String saveImagePermanently(String srcPath, Bitmap fallbackBitmap) {
        File internalDir = new File(getFilesDir(), "my_plants_images");
        if (!internalDir.exists()) internalDir.mkdirs();

        String uniqueFilename = "plant_" + UUID.randomUUID().toString() + ".jpg";
        File   destFile       = new File(internalDir, uniqueFilename);

        if (srcPath != null && new File(srcPath).exists()) {
            try (InputStream  in  = new java.io.FileInputStream(srcPath);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                return destFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fallbackBitmap != null) {
            try (FileOutputStream out = new FileOutputStream(destFile)) {
                fallbackBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                return destFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return srcPath;
    }
}
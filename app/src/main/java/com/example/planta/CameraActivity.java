package com.example.planta;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private static final int IMG_SIZE = 224;

    public static final String EXTRA_MODE    = "mode";
    public static final String MODE_CLASSIFY = "classify";
    public static final String MODE_DIAGNOSE = "diagnose";

    private String mode = MODE_CLASSIFY;

    private ImageCapture imageCapture;
    private Interpreter  tflite;
    private List<String> labels;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private PreviewView previewView;
    private ImageView   imgPreview;
    private View        layoutCamera;
    private View        layoutConfirm;
    private Bitmap      capturedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getIntent().hasExtra(EXTRA_MODE)) {
            mode = getIntent().getStringExtra(EXTRA_MODE);
        }

        try {
            labels = loadLabels();
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            options.setUseXNNPACK(true);
            tflite = new Interpreter(loadModelFile(), options);
            tflite.allocateTensors();
        } catch (Exception e) {
            android.util.Log.e("TFLite", "Error loading model", e);
            Toast.makeText(this, "Error loading model: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        previewView   = findViewById(R.id.previewView);
        imgPreview    = findViewById(R.id.imgPreview);
        layoutCamera  = findViewById(R.id.layoutCamera);
        layoutConfirm = findViewById(R.id.layoutConfirm);

        View                     btnCapture  = findViewById(R.id.btnCapture);
        FloatingActionButton     fabGallery  = findViewById(R.id.fabGallery);
        MaterialButton           btnRetake   = findViewById(R.id.btnRetake);
        MaterialButton           btnUsePhoto = findViewById(R.id.btnUsePhoto);

        startCamera();
        showCameraMode();

        btnCapture.setOnClickListener(v -> takePhoto());

        btnRetake.setOnClickListener(v -> {
            capturedBitmap = null;
            showCameraMode();
        });

        btnUsePhoto.setOnClickListener(v -> {
            if (capturedBitmap != null && tflite != null) {
                processImage(capturedBitmap);
            }
        });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media
                                        .getBitmap(getContentResolver(), uri);
                                capturedBitmap = bitmap;
                                showConfirmMode(bitmap);
                            } catch (IOException e) {
                                Toast.makeText(this, "Could not load image",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        fabGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            galleryLauncher.launch(intent);
        });
    }

    // ── UI modes ──────────────────────────────────────────────────────────

    private void showCameraMode() {
        previewView.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
        layoutCamera.setVisibility(View.VISIBLE);
        layoutConfirm.setVisibility(View.GONE);
    }

    private void showConfirmMode(Bitmap bitmap) {
        previewView.setVisibility(View.GONE);
        imgPreview.setVisibility(View.VISIBLE);
        imgPreview.setImageBitmap(bitmap);
        layoutCamera.setVisibility(View.GONE);
        layoutConfirm.setVisibility(View.VISIBLE);
    }

    // ── CameraX ───────────────────────────────────────────────────────────

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ── Capture ───────────────────────────────────────────────────────────

    private void takePhoto() {
        if (imageCapture == null) return;
        File photoFile = new File(getCacheDir(), "temp_capture.jpg");
        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        if (bitmap == null) return;
                        try {
                            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
                            int orientation = exif.getAttributeInt(
                                    ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_NORMAL);
                            int rot = 0;
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)  rot = 90;
                            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rot = 180;
                            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rot = 270;
                            if (rot != 0) {
                                Matrix m = new Matrix();
                                m.postRotate(rot);
                                bitmap = Bitmap.createBitmap(
                                        bitmap, 0, 0,
                                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                            }
                        } catch (IOException ignored) {}
                        capturedBitmap = bitmap;
                        showConfirmMode(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Toast.makeText(CameraActivity.this, "Capture failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Classification ────────────────────────────────────────────────────

    private void processImage(Bitmap bitmap) {
        try {
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);
            ByteBuffer input = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3);
            input.order(ByteOrder.nativeOrder());
            int[] pixels = new int[IMG_SIZE * IMG_SIZE];
            resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);

            for (int px : pixels) {
                // ★ MobileNetV2 preprocess_input → normalize to [-1, 1]
                input.putFloat((((px >> 16) & 0xFF) / 127.5f) - 1.0f);
                input.putFloat((((px >> 8)  & 0xFF) / 127.5f) - 1.0f);
                input.putFloat(((px         & 0xFF) / 127.5f) - 1.0f);
            }
            input.rewind();

            int outputSize = tflite.getOutputTensor(0).shape()[1];
            float[][] output = new float[1][outputSize];
            tflite.run(input, output);

            int bestIdx = 0;
            float bestConf = 0f;
            for (int i = 0; i < output[0].length; i++) {
                if (output[0][i] > bestConf) {
                    bestConf = output[0][i];
                    bestIdx  = i;
                }
            }

            // Save display image to file (avoids parcel blob crash)
            File imgFile = new File(getCacheDir(), "result_image.jpg");
            try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                Bitmap display = Bitmap.createScaledBitmap(
                        bitmap,
                        Math.min(bitmap.getWidth(), 800),
                        Math.min(bitmap.getHeight(), 800),
                        true);
                display.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
            String imagePath = imgFile.getAbsolutePath();

            if (MODE_DIAGNOSE.equals(mode)) {
                // Disease model — label format: "Plant___Disease_name"
                String rawLabel = labels.get(bestIdx);
                String[] parts  = rawLabel.split("___");
                String plant    = parts.length > 0 ? parts[0].replace("_", " ") : rawLabel;
                String disease  = parts.length > 1 ? parts[1].replace("_", " ") : "Unknown";

                Intent intent = new Intent(this, DiagnoseResultActivity.class);
                intent.putExtra("plant_name",   plant);
                intent.putExtra("disease_name", disease);
                intent.putExtra("confidence",   bestConf);
                intent.putExtra("raw_label",    rawLabel);
                intent.putExtra("image_path",   imagePath);
                startActivity(intent);

            } else {
                // ★ Clean dataset folder name suffixes then capitalize
                // "apple fruit" → "Apple"
                // "banana fruit" → "Banana"
                // "pomegranates fruit" → "Pomegranates"  etc.
                String rawLabel  = labels.get(bestIdx);
                String plantName = rawLabel
                        .replace(" fruit", "")
                        .replace(" test", "")
                        .replace(" validation", "")
                        .trim();

                if (!plantName.isEmpty()) {
                    plantName = plantName.substring(0, 1).toUpperCase()
                            + plantName.substring(1);
                }

                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("plant_name", plantName);
                intent.putExtra("confidence", bestConf);
                intent.putExtra("image_path", imagePath);
                startActivity(intent);
            }

            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Inference failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── Model + labels ────────────────────────────────────────────────────

    private MappedByteBuffer loadModelFile() throws IOException {
        // classify → plant_mobilenet_v2.tflite  |  diagnose → plant_disease_model.tflite
        String modelName = MODE_DIAGNOSE.equals(mode)
                ? "plant_disease_model.tflite"
                : "plant_mobilenet_v2.tflite";
        try (AssetFileDescriptor afd = getAssets().openFd(modelName);
             FileInputStream fis = new FileInputStream(afd.getFileDescriptor())) {
            FileChannel channel = fis.getChannel();
            return channel.map(FileChannel.MapMode.READ_ONLY,
                    afd.getStartOffset(), afd.getDeclaredLength());
        }
    }

    private List<String> loadLabels() throws IOException {
        String labelsName = MODE_DIAGNOSE.equals(mode)
                ? "disease_labels.txt"
                : "labels.txt";
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getAssets().open(labelsName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) list.add(line.trim());
            }
        }
        return list;
    }
}
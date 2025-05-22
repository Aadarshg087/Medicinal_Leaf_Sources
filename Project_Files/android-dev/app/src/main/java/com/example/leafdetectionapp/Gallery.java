package com.example.leafdetectionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Gallery extends AppCompatActivity {

    public static final int Gallery_REQUEST_CODE = 105;
    ImageView imageView;
    Button gallery;
    TextView resultText;
    Interpreter tflite;
    Bitmap selectedBitmap;

    Button detectButton,share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar_two);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menuu);

        // UI Components
        imageView = findViewById(R.id.imageView);
        gallery = findViewById(R.id.gallery);
        resultText = findViewById(R.id.resultText); // Make sure you have this in your XML
        detectButton = findViewById(R.id.detect);
        share = findViewById(R.id.share);

        // Load the ML model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Log.e("Gallery", "Error loading model: " + e.getMessage());
        }

        // Gallery button click event
        gallery.setOnClickListener(view -> {
            Intent gall = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gall, Gallery_REQUEST_CODE);
        });

        detectButton.setOnClickListener(view -> {
            if (selectedBitmap != null) {
                runModel(selectedBitmap);
            } else {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBitmap == null) {
                    Toast.makeText(Gallery.this, "No image selected!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the image and get its URI
                Uri imageUri = saveImageToCache(selectedBitmap);
                if (imageUri == null) {
                    Toast.makeText(Gallery.this, "Error saving image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get prediction text
                String predictionText = resultText.getText().toString();

                // Create share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*"); // Set type to image
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri); // Attach image
                shareIntent.putExtra(Intent.EXTRA_TEXT, predictionText); // Attach text
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant permission

                // Start share intent
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });
    }

    private Uri saveImageToCache(Bitmap bitmap) {
        try {
            // Create a file in the cache directory
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "leaf_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            // Get content URI using FileProvider
            return FileProvider.getUriForFile(this, "com.example.leafdetectionapp.fileprovider", file);
        } catch (IOException e) {
            Log.e("Gallery", "Error saving image: " + e.getMessage());
            return null;
        }
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Gallery_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri contentUri = data.getData();
            if (contentUri != null) {
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                    imageView.setImageBitmap(selectedBitmap);
                } catch (IOException e) {
                    Log.e("Gallery", "Error loading image: " + e.getMessage());
                }
            }
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model.tflite");
        FileInputStream inputStream = fileDescriptor.createInputStream();
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private void runModel(Bitmap bitmap) {
        try {
            if (bitmap == null) {
                Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Resize image to match the model input size
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            // Model output (assuming 6 classes)
            float[][] output = new float[1][6];
            tflite.run(inputBuffer, output);

            // Get the predicted class index
            int predictedIndex = getMaxIndex(output[0]);
            float confidence = output[0][predictedIndex] * 100;

            if(confidence < 70){
                resultText.setText("No Leaf Detected Try Again!");
                return ;
            }

            // Class labels
            String[] classNames = {
                    "Arjun Leaf", "Curry Leaf", "Marsh Pennywort Leaf",
                    "Mint Leaf", "Neem Leaf", "Rubber Leaf"
            };

            String predictedClass = classNames[predictedIndex];

            final String result = "Predicted Leaf Type: " + predictedClass + "\nConfidence: " + String.format("%.2f", confidence) + "%";
            runOnUiThread(() -> {
                resultText.setText(result);
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            Log.e("Gallery", "Error running model: ", e);
            runOnUiThread(() -> Toast.makeText(this, "Error running model: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private int getMaxIndex(float[] arr) {
        int maxIndex = 0;
        float maxVal = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > maxVal) {
                maxIndex = i;
                maxVal = arr[i];
            }
        }
        return maxIndex;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        buffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[224 * 224];
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224);

        for (int pixel : intValues) {
            buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f); // Red
            buffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);  // Green
            buffer.putFloat((pixel & 0xFF) / 255.0f);        // Blue
        }
        return buffer;
    }
}

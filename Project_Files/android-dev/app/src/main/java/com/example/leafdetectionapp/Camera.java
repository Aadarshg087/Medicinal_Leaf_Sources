package com.example.leafdetectionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yalantis.ucrop.UCrop;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Camera extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    public static final int REQ = 102;

    ImageView imageView;
    Button camera, detectButton,share;
    TextView resultText;
    String currentPhotoPath;
    Interpreter tflite;

    Bitmap selectedBitmap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar_one);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menuu);

        imageView = findViewById(R.id.image);
        camera = findViewById(R.id.camera);
        detectButton = findViewById(R.id.detect);
        resultText = findViewById(R.id.resultText);
        share = findViewById(R.id.share);

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.setOnClickListener(view -> askCameraPermissions());
        detectButton.setOnClickListener(view -> runModel());
        share.setOnClickListener(view -> shareImage());
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photoPath", currentPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPhotoPath = savedInstanceState.getString("photoPath");
    }


    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        Log.d("Camera", "Image file created: " + currentPhotoPath);
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("Camera", "Error creating file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.leafdetectionapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQ);
            }
        }
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ && resultCode == Activity.RESULT_OK) {
            File f = new File(currentPhotoPath);

            if (!f.exists()) {
                Toast.makeText(this, "Image file not found!", Toast.LENGTH_SHORT).show();
                Log.e("Camera", "Error: Image file does not exist at " + currentPhotoPath);
                return;
            }

            // Start the cropping process
            Uri sourceUri = Uri.fromFile(f);
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));

            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1) // Crop in a square shape
                    .withMaxResultSize(224, 224) // Resize to match the model's input size
                    .start(this);
        }
        else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            // Handle the cropped image
            Uri croppedImageUri = UCrop.getOutput(data);
            if (croppedImageUri != null) {
                currentPhotoPath = croppedImageUri.getPath();
                selectedBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                imageView.setImageURI(croppedImageUri);
            }
        }
        else if (requestCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Camera", "Crop error", cropError);
        }
    }




    private MappedByteBuffer loadModelFile() throws IOException {
        try {
            AssetFileDescriptor fileDescriptor = getAssets().openFd("model.tflite");
            FileInputStream inputStream = fileDescriptor.createInputStream();
            FileChannel fileChannel = inputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
        } catch (IOException e) {
            Log.e("Camera", "Error loading model file: " + e.getMessage());
            throw e;  // Re-throw to detect the issue
        }
    }




    private void runModel() {
        try {
            if (currentPhotoPath == null || currentPhotoPath.isEmpty()) {
                Toast.makeText(this, "No image found. Capture an image first!", Toast.LENGTH_SHORT).show();
                return;
            }

            File imgFile = new File(currentPhotoPath);
            if (!imgFile.exists()) {
                Toast.makeText(this, "Image file not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap == null) {
                Toast.makeText(this, "Failed to load image. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Resize image for model input
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            // Model output should match the number of classes
            float[][] output = new float[1][6];  // Assuming you have 6 classes

            tflite.run(inputBuffer, output);

            // Get the predicted class index
            int predictedIndex = getMaxIndex(output[0]);

            float confidence = output[0][predictedIndex] * 100; // Convert to percentage

            if(confidence < 70){
                resultText.setText("No Leaf Detected Try Again!");
                return ;
            }


            // Class labels (adjust based on your model's labels)
            String[] classNames = {
                    "Arjun Leaf", "Curry Leaf", "Marsh Pennywort Leaf",
                    "Mint Leaf", "Neem Leaf", "Rubber Leaf"
            };

            String predictedClass = classNames[predictedIndex];


//            final String result = "Predicted Class: " + classNames[predictedIndex];

            final String result = "Predicted Leaf Type: " + predictedClass + "\nConfidence: " + String.format("%.2f", confidence) + "%";


            runOnUiThread(() -> {
                resultText.setText(result);
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            });


        } catch (Exception e) {
            Log.e("Camera", "Error running model: ", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Error running model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Helper function to get index of max value
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

    private String getLabelFromOutput(float prediction) {
        if (prediction < 0.5) {
            return "Healthy Leaf";
        } else {
            return "Diseased Leaf";
        }
    }

    private void shareImage() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = saveImageToCache(selectedBitmap);
        if (imageUri == null) {
            Toast.makeText(this, "Error saving image!", Toast.LENGTH_SHORT).show();
            return;
        }

        String predictionText = resultText.getText().toString();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, predictionText);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private Uri saveImageToCache(Bitmap bitmap) {
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "leaf_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
            return FileProvider.getUriForFile(this, "com.example.leafdetectionapp.fileprovider", file);
        } catch (IOException e) {
            Log.e("Camera", "Error saving image: " + e.getMessage());
            return null;
        }
    }

}

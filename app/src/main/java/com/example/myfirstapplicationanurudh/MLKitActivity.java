package com.example.myfirstapplicationanurudh;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.List;

public class MLKitActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 3000;
    private Uri imageFileUri;
    private ImageView imageView;
    private TextView textViewOutput;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                if (result.getData() != null &&
                                        result.getData().getData() != null)
                                    imageFileUri = result.getData().getData();

                                if (imageFileUri != null) {
                                    imageView.setImageURI(imageFileUri);

                                    // Add code for ML Kit below this line
                                    textViewOutput.setText("");
                                    InputImage image = null;
                                    try {
                                        image = InputImage.fromFilePath(getBaseContext(), imageFileUri);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (image != null) {
                                        processImageFromBarcodeReader(image);
                                        processImageFromContentReader(image);
                                        processImageFromTextReader(image);
                                    }
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mlkit);

        View root = findViewById(R.id.mlkit_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        imageView = findViewById(R.id.imageViewMLKit);
        textViewOutput = findViewById(R.id.tvMLKitOutput);
    }

    private boolean checkPermission() {
        String permission = android.Manifest.permission.CAMERA;
        boolean grantCamera = ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED;
        if (!grantCamera) {
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    REQUEST_PERMISSION);
        }
        return grantCamera;
    }

    public void openCamera(View view) {
        if (!checkPermission())
            return;

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "New_Photo_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageFileUri != null) {
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
            activityResultLauncher.launch(takePhotoIntent);
        }
    }

    public void loadImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(galleryIntent);
    }

    public void processImageFromBarcodeReader(InputImage image) {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        textViewOutput.append(Html.fromHtml("<font color='navy'><b>Detected barcode:</b></font><br>", Html.FROM_HTML_MODE_LEGACY));
                        String result = "";
                        for (Barcode barcode : barcodes) {
                            result = barcode.getRawValue();
                            textViewOutput.append(result + "\n");
                        }
                        if (result.length() < 2) {
                            textViewOutput.append(" Barcode not found.\n");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textViewOutput.setText("Failed");
                    }
                });
    }

    public void processImageFromContentReader(InputImage image) {
        ImageLabeler labeler = ImageLabeling.getClient(
                ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<ImageLabel>>() {
                            @Override
                            public void onSuccess(List<ImageLabel> labels) {
                                if (labels.size() == 0) {
                                    textViewOutput.append("Nothing found in the image\n");
                                    return;
                                }
                                textViewOutput.append(Html.fromHtml("<font color='navy'><b>Detected image content:</b></font><br>",
                                        Html.FROM_HTML_MODE_LEGACY));
                                int counter = 1;
                                for (ImageLabel label : labels) {
                                    String result = label.getText();
                                    float confidence = label.getConfidence();
                                    textViewOutput.append(" " +
                                            counter + ". " + result +
                                            " (" + String.format("%.1f", confidence * 100.0f) +
                                            "% confidence)\n");
                                    counter++;
                                }
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textViewOutput.setText("Failed");
                    }
                });
    }

    public void processImageFromTextReader(InputImage image) {
        TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                textViewOutput.append(Html.fromHtml("<font color='navy'><b>Detected text:</b></font><br>", Html.FROM_HTML_MODE_LEGACY));
                                // Task completed successfully
                                String result = visionText.getText();
                                if (result.length() > 1)
                                    textViewOutput.append(" " + result + "\n");
                                else
                                    textViewOutput.append(" No text found.\n");
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        textViewOutput.setText("Failed");
                                    }
                                });
    }
}

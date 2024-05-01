package com.example.lowvisreading;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

public class flipped extends AppCompatActivity {
    FloatingActionButton picture_button;
    SeekBar textSize;
    SeekBar blindSpotSize;
    TextView text_data;
    ImageView blindSpot;
    Bitmap bitmap;
    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    ImageButton flip_screen;
    ImageButton draw_spot;

    private static final int extra_space = 50;
    private static final int REQUEST_CAMERA_CODE = 100;
    private static final int REQUEST_STORAGE_CODE = 101;
    private static final int DRAW_REQUEST_CODE = 102;

    private Uri imageURI = null;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private String custom_scotoma = null;
    private int max_scotoma_size = 900;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipped);
        // Initialize screen components
        picture_button = findViewById(R.id.takePicture);
        textSize = findViewById(R.id.textSize);
        blindSpotSize = findViewById(R.id.blindSpotSize);
        text_data = findViewById(R.id.textDisplay);
        blindSpot = findViewById(R.id.blindSpot);
        flip_screen = findViewById(R.id.flipDown);
        draw_spot = findViewById(R.id.draw_blind_spot);
        Bundle extras = getIntent().getExtras();

        // Set permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Ask Permissions when launch app
        if(ContextCompat.checkSelfPermission(flipped.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(flipped.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }

        Intent change_scotoma_intent = getIntent();
        if(change_scotoma_intent != null){
            String path = change_scotoma_intent.getStringExtra("blind_spot_path");
            if(path != null){
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ImageView blindSpotOverlay = findViewById(R.id.blindSpot);
                blindSpotOverlay.setImageBitmap(bitmap);
                float text_Size = extras.getFloat("text_size", 22);
                text_data.setTextSize(TypedValue.COMPLEX_UNIT_PX, text_Size);
                text_data.setText(extras.getString("text", ""));
                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                int blindSize = extras.getInt("blind_size", 100);
                params.width = blindSize;
                params.height = blindSize;
                blindSpot.setLayoutParams(params);
                int textSizeProgress = extras.getInt("text_size_prog", 0);
                textSize.setProgress(textSizeProgress);
                int blindSizeProgress = extras.getInt("blind_size_prog", 0);
                blindSpotSize.setProgress(blindSizeProgress);
                custom_scotoma = path;
                max_scotoma_size = 2000;
            }
        }

        // check if activity was given extras
        if(extras != null){
            float text_Size = extras.getFloat("text_size", 22);
            text_data.setTextSize(TypedValue.COMPLEX_UNIT_PX, text_Size);
            text_data.setText(extras.getString("text", ""));
            ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
            int blindSize = extras.getInt("blind_size", 100);
            params.width = blindSize;
            params.height = blindSize;
            blindSpot.setLayoutParams(params);
            int textSizeProgress = extras.getInt("text_size_prog", 0);
            textSize.setProgress(textSizeProgress);
            int blindSizeProgress = extras.getInt("blind_size_prog", 0);
            blindSpotSize.setProgress(blindSizeProgress);
            max_scotoma_size = extras.getInt("max_size", 900);
            String path = extras.getString("blind_spot_path","");
            if(!path.equals("")){
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                blindSpot.setImageBitmap(bitmap);
            }
        }

        // take picture
        picture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(flipped.this, camera_view.class);
                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                intent.putExtra("blind_size", params.width);
                intent.putExtra("text_size", text_data.getTextSize());
                intent.putExtra("text_size_prog", textSize.getProgress());
                intent.putExtra("blind_size_prog", blindSpotSize.getProgress());
                int[] loc = new int[2];
                blindSpot.getLocationOnScreen(loc);
                intent.putExtra("left", loc[0]);
                intent.putExtra("top", loc[1]);
                if(custom_scotoma != null){
                    intent.putExtra("blind_spot_path",custom_scotoma);
                }
                startActivity(intent);
            }
        });

        // Change Text size
        textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int min = 8;
                int max = 70;
                int text_size = min + (max - min) * i / seekBar.getMax();
                text_data.setTextSize(text_size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // change blind spot size
        blindSpotSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int min = 100;
                int max = max_scotoma_size;
                int blind_size = min + (max - min) * i / seekBar.getMax();
                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                params.width = blind_size;
                params.height = blind_size;
                blindSpot.setLayoutParams(params);

                int text_pad = blind_size + extra_space;
                text_data.setPadding(text_data.getPaddingLeft(), text_pad, text_data.getPaddingRight(), text_data.getPaddingBottom());
                text_data.requestLayout();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        flip_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(flipped.this, MainActivity.class);
                intent.putExtra("text",text_data.getText().toString());
                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                intent.putExtra("blind_size", params.width);
                intent.putExtra("text_size", text_data.getTextSize());
                intent.putExtra("text_size_prog", textSize.getProgress());
                intent.putExtra("blind_size_prog", blindSpotSize.getProgress());
                if(custom_scotoma != null){
                    intent.putExtra("blind_spot_path",custom_scotoma);
                    intent.putExtra("max_size",2000);
                }
                startActivity(intent);
            }
        });

        draw_spot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(flipped.this, draw_blindSpot.class);
                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                intent.putExtra("blind_size", params.width);
                max_scotoma_size = 2000;
                startActivityForResult(intent, DRAW_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DRAW_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String path = data.getStringExtra("blind_spot_path");
            int dim = data.getIntExtra("blind_size", 100);
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                blindSpot.setImageBitmap(bitmap);

                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                params.width = dim;
                params.height = dim;
                blindSpot.setLayoutParams(params);

                flip_screen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(flipped.this, MainActivity.class);
                        intent.putExtra("text", text_data.getText().toString());
                        ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                        intent.putExtra("blind_size", params.width);
                        intent.putExtra("text_size", text_data.getTextSize());
                        intent.putExtra("text_size_prog", textSize.getProgress());
                        intent.putExtra("blind_size_prog", blindSpotSize.getProgress());

                        intent.putExtra("blind_spot_path", path);

                        startActivity(intent);
                    }
                });
            }
        }
    }
    // Get text from Image
    private void recognizeText() {
        try {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            Task<Text> textTaskRes = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    String result = text.getText();
                    Log.i("TEXT", result);
                    text_data.setText(result.toUpperCase());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(flipped.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed image processing: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Picture Activity
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraActivityResultLaunch.launch(intent);
        }
    }
    // Get bitmap file of image taken, then call recognize text
    private ActivityResultLauncher<Intent> cameraActivityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Bundle extras = result.getData().getExtras();
                        bitmap = (Bitmap) extras.get("data");
                        recognizeText();
                    }
                    else{
                        Toast.makeText(flipped.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission(){
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, REQUEST_STORAGE_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean camRes = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageRes = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return camRes && storageRes;
    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, REQUEST_CAMERA_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CAMERA_CODE:{
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        takePicture();
                    }
                    else{
                        Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "Cancelled Permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
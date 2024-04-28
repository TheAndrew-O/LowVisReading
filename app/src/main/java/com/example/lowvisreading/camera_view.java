package com.example.lowvisreading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class camera_view extends AppCompatActivity {

    ImageButton capture;
    ImageButton text_rec;
    SeekBar textSize;
    SeekBar blindSpotSize;
    TextView textView;
    ScrollView scrollView;
    PreviewView previewView;
    ImageCapture imageCapture;
    Bitmap bitmap;
    ImageAnalysis imageAnalysis;
    ImageView blindSpot;
    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    final long[] lastAnalyzedTimeStamps = {System.currentTimeMillis()};
    private static final int REQUEST_CAMERA_CODE = 100;
    private boolean featureOn = false;
    private String custom_scotoma = null;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        capture = findViewById(R.id.capture);
        textSize = findViewById(R.id.textSize);
        blindSpotSize = findViewById(R.id.blindSpotSize);
        text_rec = findViewById(R.id.text_rec);
        textView = findViewById(R.id.textDisplay);
        scrollView = findViewById(R.id.scroll);
        previewView = findViewById(R.id.cameraPreView);
        blindSpot = findViewById(R.id.blindSpot);
        Bundle extras = getIntent().getExtras();
        // check if activity was given extras
        if(extras != null){
            float text_Size = extras.getFloat("text_size", 22);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, text_Size);
            textView.setText(extras.getString("text", ""));
            ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
            int blindSize = extras.getInt("blind_size", 100);
            params.width = blindSize;
            params.height = blindSize;
            blindSpot.setLayoutParams(params);
            int textSizeProgress = extras.getInt("text_size_prog", 0);
            textSize.setProgress(textSizeProgress);
            int blindSizeProgress = extras.getInt("blind_size_prog", 0);
            blindSpotSize.setProgress(blindSizeProgress);
            String path = extras.getString("blind_spot_path","");
            if(!path.equals("")){
                custom_scotoma = path;
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                blindSpot.setImageBitmap(bitmap);
            }
        }

        if(ContextCompat.checkSelfPermission(camera_view.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(camera_view.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try{
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, getMainExecutor());

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        text_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnTextRec();
            }
        });

        // Change Text size
        textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int min = 8;
                int max = 70;
                int text_size = min + (max - min) * i / seekBar.getMax();
                textView.setTextSize(text_size);
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
                int max = 900;
                int blind_size = min + (max - min) * i / seekBar.getMax();
                ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                params.width = blind_size;
                params.height = blind_size;
                blindSpot.setLayoutParams(params);

                int text_pad = blind_size + 40;
                textView.setPadding(textView.getPaddingLeft(), textView.getPaddingTop(), textView.getPaddingRight(), text_pad);
                textView.requestLayout();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
        imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setImageQueueDepth(10).build();

        try{
            cameraProvider.bindToLifecycle(this,cameraSelector,preview, imageCapture, imageAnalysis);
        } catch (Exception e) {
            Log.e("E", "USE CASE BINDING FAILED.", e);
        }
    }

    private void takePicture(){
        imageCapture.takePicture(getMainExecutor(),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        bitmap = toBitmap(image);
                        detectText();
                        image.close();
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e){
                        Toast.makeText(camera_view.this, "ERROR TAKING PHOTO", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void turnOnTextRec(){
        if(!featureOn){
            featureOn = true;
            text_rec.setBackgroundColor(Color.RED);
            imageAnalysis.setAnalyzer(getMainExecutor(), new ImageAnalysis.Analyzer() {
                @Override
                public void analyze(@NonNull ImageProxy image) {
                    @SuppressLint("UnsafeOptInUsageError") InputImage mediaImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                    recognizer.process(mediaImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            long currTime = System.currentTimeMillis();
                            String res = text.getText();
                            Spannable str = Spannable.Factory.getInstance().newSpannable(res.toUpperCase());
                            str.setSpan(new BackgroundColorSpan(Color.BLACK), 0, res.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            if(currTime - lastAnalyzedTimeStamps[0] >= 3000)
                            {
                                lastAnalyzedTimeStamps[0] = currTime;
                                runOnUiThread(() -> textView.setText(str));
                            }
                            image.close();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(camera_view.this, "IMAGE ANALYSIS FAILED!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            image.close();
                        }
                    });
                }
            });
        }
        else{
            text_rec.setBackgroundResource(0);
            featureOn = false;
            imageAnalysis.clearAnalyzer();
        }
    }

    private Bitmap toBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void recognizeText() {
        try {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            Task<Text> textTaskRes = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    String result = text.getText();
                    Log.i("TEXT", result);
                    Spannable str = Spannable.Factory.getInstance().newSpannable(result.toUpperCase());
                    str.setSpan(new BackgroundColorSpan(Color.BLACK), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    runOnUiThread(() -> textView.setText(str));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    runOnUiThread(() -> Toast.makeText(camera_view.this, "Text recognition error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed image processing: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void detectText() {
        try {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            Task<Text> textTaskRes = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    String result = text.getText().toUpperCase();
                    Intent intent = new Intent(camera_view.this, MainActivity.class);
                    intent.putExtra("text",result);
                    ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
                    intent.putExtra("blind_size", params.width);
                    intent.putExtra("text_size", textView.getTextSize());
                    if(custom_scotoma != null){
                        intent.putExtra("blind_spot_path",custom_scotoma);
                        intent.putExtra("max_size",1000);
                    }
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    runOnUiThread(() -> Toast.makeText(camera_view.this, "Text recognition error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed image processing: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
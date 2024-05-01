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
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
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

public class training extends AppCompatActivity {

    customTextView cs;
    ImageView blindSpot;
    String txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        Bundle extras = getIntent().getExtras();
        cs = findViewById(R.id.curvedTextView);
        blindSpot = findViewById(R.id.blindSpot);
        RectF rect = getBlindSpotBoundary();

        if(extras != null){
            txt = extras.getString("text", "FUCK  ET DOLORE MAGNA ALIQUA. UT ENIM AD MINIM VENIAM, QUIS NOSTRUD EXERCITATION ULLAMCO LABORIS NISI UT ALIQUIP EX EA COMMODO CONSEQUAT. DUIS AUTE IRURE DOLOR IN REPREHENDERIT IN VOLUPTATE VELIT ESSE CILLUM DOLORE EU FUGIAT NULLA PARIATUR. EXCEPTEUR SINT OCCAECAT CUPIDATAT NON PROIDENT, SUNT IN CULPA QUI OFFICIA DESERUNT MOLLIT ANIM ID EST LABORUM.");
            ViewGroup.LayoutParams params = blindSpot.getLayoutParams();
            int blindSize = extras.getInt("blind_size", 100);
            params.width = blindSize;
            params.height = blindSize;
            blindSpot.setLayoutParams(params);
            cs.setString(txt);
            int d = extras.getInt("left", 2);
            int c = extras.getInt("top",3);
            cs.setBoundary(new RectF((float)d, (float)c, (float)(d + blindSize), (float)(c + blindSize)));
//            Toast.makeText(this, d + "-" + c + "-"+ String.valueOf(d + blindSize)+ "-" + String.valueOf(c + blindSize), Toast.LENGTH_SHORT).show();
            String path = extras.getString("blind_spot_path","");
            if(!path.equals("")){
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                blindSpot.setImageBitmap(bitmap);
            }
        }
        else{
            txt = "";
        }
//        cs.setUp(rect.left, rect.top, rect.right, rect.bottom, txt);
    }

    private RectF getBlindSpotBoundary(){
        int[] loc = new int[2];
        blindSpot.getLocationOnScreen(loc);

        float left = loc[0];
        float top = loc[1];
        float right = left + blindSpot.getWidth();
        float bottom = top + blindSpot.getHeight();

        return new RectF(left, top, right, bottom);
    }

}
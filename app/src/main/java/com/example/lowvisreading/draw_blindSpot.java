package com.example.lowvisreading;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class draw_blindSpot extends AppCompatActivity {

    private draw_view drawing;
    private ImageButton undo;
    private ImageButton save;
    private ImageButton cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_blind_spot);

        drawing = findViewById(R.id.draw_view);
        undo = findViewById(R.id.undo_blind_spot);
        save = findViewById(R.id.save_blind_spot);
        cancel = findViewById(R.id.exit_draw);

        Bundle extras = getIntent().getExtras();
        int dim = 100;
        if(extras != null){
            dim = extras.getInt("blind_size", 100);
            // Log.w("ASDASDASDASDASD", Integer.toString(dim));
        }

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawing.onUndo();
            }
        });

        int finalDim = dim;
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap scotoma_bitmap = drawing.getCroppedBitmap();

                String file_name = "blind_spot_overlay.png";
                File file = new File(getExternalFilesDir(null), file_name);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    scotoma_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    // Pass the file path back to the main activity
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("blind_spot_path", file.getAbsolutePath());
                    returnIntent.putExtra("blind_size", finalDim);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }
}
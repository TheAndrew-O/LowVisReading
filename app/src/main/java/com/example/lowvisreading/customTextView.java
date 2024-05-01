package com.example.lowvisreading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class customTextView extends View {
    private Path textPath;
    private TextPaint paint;
    private Paint bg;
    private String[] words;
    private int currentIndex = 0;  // Index of the currently displayed word
    private float touchStartY;
    String text;
    RectF boundary = new RectF(100f, 800f, 900f, 1000f);
    private float textSize = 70f;

    public customTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPath = new Path();
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.RED);
        bg.setStyle(Paint.Style.FILL);

        // Define your text here
        text = "SHITE ULLAMCO LABORIS NISI UT ALIQUIP EX EA COMMODO CONSEQUAT. DUIS AUTE IRURE DOLOR IN REPREHENDERIT IN VOLUPTATE VELIT ESSE CILLUM DOLORE EU FUGIAT NULLA PARIATUR. EXCEPTEUR SINT OCCAECAT CUPIDATAT NON PROIDENT, SUNT IN CULPA QUI OFFICIA DESERUNT MOLLIT ANIM ID EST LABORUM.";
        words = text.split(" ");
    }
    public void setString(String str){
        text = str;
        words = text.split(" ");
        invalidate();
        requestLayout();
    }

    public void setBoundary(RectF rect){
        boundary = rect;
        invalidate();
    }

    public void setTextSize(float size){
        textSize = size;
        paint.setTextSize(textSize);
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        textPath.reset();

//        RectF enclosingRect = new RectF(100f, 800f, 900f, 1000f);
        textPath.addArc(boundary, 225f, 90f);

        if (words.length > 0) {
//            canvas.drawArc(boundary.left+30, boundary.top-150, boundary.right+30, boundary.bottom, 225f, 90f, false, bg);
            canvas.drawTextOnPath(words[currentIndex], textPath, 0, -100, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                float deltaY = event.getY() - touchStartY;
                if (deltaY > 0 && currentIndex > 0) {
                    currentIndex--;
                } else if (deltaY < 0 && currentIndex < words.length - 1) {
                    currentIndex++;
                }
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }
}

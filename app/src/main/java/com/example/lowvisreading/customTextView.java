package com.example.lowvisreading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class customTextView extends View {
    private String text = "LOREM IPSUM DOLOR SIT AMET, CONSECTETUR ADIPISCING ELIT, SED DO EIUSMOD TEMPOR INCIDIDUNT UT LABORE ET DOLORE MAGNA ALIQUA. UT ENIM AD MINIM VENIAM, QUIS NOSTRUD EXERCITATION ULLAMCO LABORIS NISI UT ALIQUIP EX EA COMMODO CONSEQUAT. DUIS AUTE IRURE DOLOR IN REPREHENDERIT IN VOLUPTATE VELIT ESSE CILLUM DOLORE EU FUGIAT NULLA PARIATUR. EXCEPTEUR SINT OCCAECAT CUPIDATAT NON PROIDENT, SUNT IN CULPA QUI OFFICIA DESERUNT MOLLIT ANIM ID EST LABORUM."; // Ideally, set this through XML or a setter method.
    private TextPaint textPaint;
    private Paint textPaint_;
    private RectF blindSpotRect;
    private int textSize = 22;


    public customTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint_.setColor(Color.BLACK);
        textPaint_.setTextSize(70);
        blindSpotRect = new RectF(100, 100, 300, 300);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTextAroundBlindSpot(canvas);
    }

    private void drawTextAroundBlindSpot(Canvas canvas) {
        int width = getWidth();
        StaticLayout layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int lineCount = layout.getLineCount();
        float textY = 0;

        for (int i = 0; i < lineCount; i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String lineText = text.substring(lineStart, lineEnd);

            float textWidth = textPaint.measureText(lineText);
            float textX = (getWidth() - textWidth) / 2; // Center text horizontally

            if (textY + textPaint.getTextSize() > blindSpotRect.top && textY < blindSpotRect.bottom) {
                // The line is within the vertical bounds of the blind spot
                // Calculate new positions to break the text and draw it on either side of the blind spot
                float rightTextWidth = textWidth - blindSpotRect.right;
                canvas.drawText(lineText, 0, (int) (lineStart + (blindSpotRect.left - textX) / textPaint.getTextSize()), textX, textY, textPaint);
                canvas.drawText(lineText, (int) (lineStart + (blindSpotRect.right - textX) / textPaint.getTextSize()), lineEnd, blindSpotRect.right, textY, textPaint);
            } else {
                // Draw normally
                canvas.drawText(lineText, textX, textY, textPaint);
            }

            textY += textPaint.getTextSize() + 10; // Move to next line, add line spacing
        }
    }
    public void setText(String text){
        this.text = text;
    }

    public void setTextSize(int size){
        textSize = size;
        textPaint.setTextSize(textSize);
    }

    public String getText(){
        return text;
    }

    public int getTextSize(){
        return textSize;
    }
}

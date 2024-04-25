package com.example.lowvisreading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class customTextView extends androidx.appcompat.widget.AppCompatTextView{

    private Path blindSpotPath;
    private float blindSpotRadius;
    private float blindSpotX;
    private float blindSpotY;

    public customTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBlindSpot(float x, float y, float radius) {
        this.blindSpotX = x;
        this.blindSpotY = y;
        this.blindSpotRadius = radius;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        textPaint.drawableState = getDrawableState();

        blindSpotPath = new Path();
        // Create a circle path that the text will wrap around
        blindSpotPath.addCircle(blindSpotX, blindSpotY, blindSpotRadius, Path.Direction.CW);

        String text = getText().toString();
        int width = getWidth() - getPaddingLeft() - getPaddingRight();

        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineCount = layout.getLineCount();
        float lineTop = 0;
        float lineBottom = layout.getLineBottom(0);

        for (int i = 0; i < lineCount; i++) {
            float lineStart = layout.getLineLeft(i);
            float lineEnd = layout.getLineRight(i);
            String lineText = text.substring(layout.getLineStart(i), layout.getLineEnd(i));

            if (lineBottom > blindSpotY - blindSpotRadius && lineTop < blindSpotY + blindSpotRadius) {
                // The line intersects the blind spot, break it
                String[] words = lineText.split("\\s");
                StringBuilder leftSide = new StringBuilder();
                StringBuilder rightSide = new StringBuilder();
                boolean pastBlindSpot = false;
                for (String word : words) {
                    float wordWidth = textPaint.measureText(word + " ");
                    if (!pastBlindSpot && lineStart + wordWidth < blindSpotX - blindSpotRadius) {
                        leftSide.append(word).append(" ");
                    } else {
                        pastBlindSpot = true;
                        rightSide.append(word).append(" ");
                    }
                    lineStart += wordWidth;
                }
                // Draw the left and right side of the line separately
                canvas.drawText(leftSide.toString(), getPaddingLeft(), lineTop, textPaint);
                canvas.drawText(rightSide.toString(), blindSpotX + blindSpotRadius, lineTop, textPaint);
            } else {
                // Draw the line normally
                canvas.drawText(lineText, getPaddingLeft(), lineTop, textPaint);
            }
            lineTop = lineBottom;
            lineBottom += layout.getLineBottom(i + 1) - layout.getLineBottom(i);
        }
    }
}

package com.example.lowvisreading;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.os.Bundle;

import java.util.Stack;

public class draw_view extends View {

    private Path draw_path;
    private Paint draw_paint, canvas_paint, fill_paint;
    private Canvas draw_canvas;
    private Bitmap canvas_bitmap;
    private Stack<Path> stack_paths = new Stack<>();
    private PointF start_point = new PointF();
    private PointF last_point =  new PointF();
    private float minX, maxX, minY, maxY;

    public draw_view(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setUpDraw();
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;

        maxX = Float.MIN_VALUE;
        maxY = Float.MIN_VALUE;
    }
    private void setUpDraw(){
        draw_paint = new Paint();
        draw_path = new Path();

        draw_paint.setColor(Color.RED);
        draw_paint.setAntiAlias(true);
        draw_paint.setStrokeWidth(5);
        draw_paint.setStyle(Paint.Style.STROKE);
        draw_paint.setStrokeJoin(Paint.Join.ROUND);
        draw_paint.setStrokeCap(Paint.Cap.ROUND);

        fill_paint = new Paint();
//        fill_paint.setColor(Color.BLACK);
        fill_paint.setAntiAlias(true);
        fill_paint.setStyle(Paint.Style.FILL);

        canvas_paint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvas_bitmap, 0, 0, canvas_paint);
        if (canvas_bitmap != null) {
            canvas.drawPath(draw_path, fill_paint); // Draw the path with the fill paint
            canvas.drawPath(draw_path, draw_paint); // Draw the path with the stroke paint
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
//        Log.w("daw", "DASD");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                draw_path = new Path();
                stack_paths.push(draw_path);
                draw_path.moveTo(touchX, touchY);
                start_point.set(touchX, touchY);
                last_point.set(touchX, touchY);
                updateBounds(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                draw_path.lineTo(touchX, touchY);
                last_point.set(touchX, touchY);
                updateBounds(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                closeShapeIfNeeded();
                float centerX = (minX + maxX) / 2;
                float centerY = (minY + maxY) / 2;
                float radius = Math.max(maxX - minX, maxY - minY) / 2; // Maximum distance from center to edges

                // Configure the RadialGradient
                RadialGradient gradient = new RadialGradient(centerX, centerY, radius, new int[] {Color.BLACK, Color.argb(175,0,0,0)}, null, Shader.TileMode.CLAMP);
                fill_paint.setShader(gradient);
                draw_canvas.drawPath(draw_path, fill_paint);
                draw_canvas.drawPath(draw_path, draw_paint);
                draw_path = new Path();
                minX = Float.MAX_VALUE;
                minY = Float.MAX_VALUE;
                maxX = Float.MIN_VALUE;
                maxY = Float.MIN_VALUE;
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvas_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        draw_canvas = new Canvas(canvas_bitmap);
    }

    private PointF getStartPoint(){
        return start_point;
    }

    private PointF getEndPoint(){
        return last_point;
    }
    private void closeShapeIfNeeded() {

        if(!last_point.equals(start_point)) {
            draw_path.lineTo(start_point.x, start_point.y);
        }
        draw_path.close(); // This ensures the path is closed for filling
        //draw_paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void onUndo(){
        if(!stack_paths.empty()){
            stack_paths.pop();
            fill_paint.setShader(null);
            redrawShapes();
        }
    }

    private void redrawShapes() {
        canvas_bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        draw_canvas = new Canvas(canvas_bitmap);

        for(Path path : stack_paths){
            draw_canvas.drawPath(path, draw_paint);
        }

        invalidate();
    }

    public Bitmap getBitmap(){
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public Bitmap getCroppedBitmap(){
        if (minX < maxX && minY < maxY) {
            return Bitmap.createBitmap(canvas_bitmap, (int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
        }
        return canvas_bitmap;
    }

    private void updateBounds(float x, float y){
        if(x < minX){
            minX = x;
        }
        if(x > maxX){
            maxX = x;
        }
        if(y < minY){
            minY = y;
        }
        if(y > maxY){
            maxY = y;
        }
    }

}
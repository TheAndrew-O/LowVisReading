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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.os.Bundle;

import java.util.Stack;

public class draw_view extends View {

    private Path draw_path;
    private Paint draw_paint, canvas_paint;
    private Canvas draw_canvas;
    private Bitmap canvas_bitmap;
    private Stack<Path> stack_paths = new Stack<>();
    private PointF start_point = new PointF();
    private PointF last_point =  new PointF();

    public draw_view(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setUpDraw();
    }
    private void setUpDraw(){
        draw_paint = new Paint();
        draw_path = new Path();

        draw_paint.setColor(Color.BLACK);
        draw_paint.setAntiAlias(true);
        draw_paint.setStrokeWidth(5);
        draw_paint.setStyle(Paint.Style.STROKE);
        draw_paint.setStrokeJoin(Paint.Join.ROUND);
        draw_paint.setStrokeCap(Paint.Cap.ROUND);

        canvas_paint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvas_bitmap, 0, 0, canvas_paint);
        canvas.drawPath(draw_path, draw_paint);
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
                break;
            case MotionEvent.ACTION_MOVE:
                draw_path.lineTo(touchX, touchY);
                last_point.set(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                closeShapeIfNeeded();
                draw_canvas.drawPath(draw_path, draw_paint);
                draw_path = new Path();
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
        draw_paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void onUndo(){
        if(!stack_paths.empty()){
            stack_paths.pop();
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
}
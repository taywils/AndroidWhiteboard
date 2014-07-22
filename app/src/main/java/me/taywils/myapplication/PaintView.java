package me.taywils.myapplication;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

public class PaintView extends View {
    private Bitmap bitmap;
    private Canvas canvas;
    private Path penPath;
    private Paint penPaint;
    private Paint canvasPaint;

    public PaintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupPainting();
    }

    public void clearCanvas() {
        this.canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    protected void setupPainting() {
        this.bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);

        this.penPath = new Path();

        this.penPaint = new Paint();
        this.penPaint.setColor(Color.BLUE);
        this.penPaint.setAntiAlias(true);
        this.penPaint.setStrokeWidth(20);
        this.penPaint.setStyle(Paint.Style.STROKE);
        this.penPaint.setStrokeJoin(Paint.Join.ROUND);
        this.penPaint.setStrokeCap(Paint.Cap.ROUND);

        this.canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas cvs) {
        cvs.drawBitmap(bitmap, 0, 0, canvasPaint);
        cvs.drawPath(penPath, penPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF touchPoint = new PointF();
        touchPoint.set(event.getX(), event.getY());

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.penPath.moveTo(touchPoint.x, touchPoint.y);
                break;

            case MotionEvent.ACTION_MOVE:
                this.penPath.lineTo(touchPoint.x, touchPoint.y);
                break;

            case MotionEvent.ACTION_UP:
                this.canvas.drawPath(penPath, penPaint);
                this.penPath.reset();
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }
}

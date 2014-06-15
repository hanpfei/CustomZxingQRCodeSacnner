package com.wolfcs.qrcodescanner.measurement;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class MeasuringPoint extends MeasuringObject {
    private static final int POINT_COLOR = Color.WHITE;
    private static final float POINT_RADIUS = 10.0f;
    private static final int POINT_SIZE = 50;

    private int mMeasuredPointIndex;
    public Point mPosition;

    private Paint mPointPaint;
    private Paint mSelectedPointPaint;

    public MeasuringPoint(Context context, int width, int height, int index) {
        super(context, width, height);
        mPosition = new Point();
        mMeasuredPointIndex = index;

        mPointPaint = new Paint();
        mPointPaint.setColor(POINT_COLOR);
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeWidth(2.0f);

        mSelectedPointPaint = new Paint();
        mSelectedPointPaint.setColor(Color.YELLOW);
        mSelectedPointPaint.setStyle(Paint.Style.STROKE);
        mSelectedPointPaint.setStrokeWidth(3.0f);
    }

    @Override
    public void move(float dx, float dy) {

    }

    private void drawCrosshair(Canvas canvas, Paint paint) {
        float startX = mPosition.x - 15;
        float startY = mPosition.y;
        float stopX = mPosition.x + 15;
        float stopY = mPosition.y;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = mPosition.x;
        startY = mPosition.y - 15;
        stopX = mPosition.x;
        stopY = mPosition.y + 15;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    @Override
    public void drawOnView(Canvas canvas) {
        canvas.drawCircle(mPosition.x, mPosition.y, POINT_RADIUS, mPointPaint);
        drawCrosshair(canvas, mPointPaint);
    }

    @Override
    public void drawSelectedOnView(Canvas canvas) {
        canvas.drawCircle(mPosition.x, mPosition.y, POINT_RADIUS, mSelectedPointPaint);
        drawCrosshair(canvas, mSelectedPointPaint);
    }

    @Override
    public void drawOperatedObjectOnView(Canvas canvas) {

    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {

    }

    @Override
    public boolean containOnViewPoint(float xPosition, float yPosition) {
        if (distanceTo((int) xPosition, (int) yPosition) < POINT_SIZE) {
            return true;
        }
        return false;
    }

    public void setPosition(int x, int y){
        mPosition.set(x, y);
    }

    public int distanceTo(int x, int y) {
        return (int) Math.sqrt((x - mPosition.x) * (x - mPosition.x)
                + (y - mPosition.y) * (y - mPosition.y));
    }

    public int getIndex() {
        return mMeasuredPointIndex;
    }
}

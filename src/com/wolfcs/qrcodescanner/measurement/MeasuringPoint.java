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

    private final int mMeasuredPointIndex;
    public Point mPosition;

    private Paint mPointPaint;
    private Paint mSelectedPointPaint;

    public MeasuringPoint(Context context, int width, int height, int index) {
        super(context, width, height);
        mPosition = new Point();
        mMeasuredPointIndex = index;

        mPointPaint = new Paint();
        mPointPaint.setColor(POINT_COLOR);
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
        Paint.Style style = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        float startX = mPosition.x - 17;
        float startY = mPosition.y;
        float stopX = mPosition.x - 10;
        float stopY = mPosition.y;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = mPosition.x + 10;
        startY = mPosition.y;
        stopX = mPosition.x + 17;
        stopY = mPosition.y;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = mPosition.x;
        startY = mPosition.y - 17;
        stopX = mPosition.x;
        stopY = mPosition.y - 10;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        startX = mPosition.x;
        startY = mPosition.y + 10;
        stopX = mPosition.x;
        stopY = mPosition.y + 17;
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        paint.setStyle(style);
    }

    private void drawCircle(Canvas canvas, Paint paint) {
        Paint.Style style = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mPosition.x, mPosition.y, POINT_RADIUS, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mPosition.x, mPosition.y, 2.0f, paint);
        paint.setStyle(style);
    }

    @Override
    public void drawOnView(Canvas canvas) {
        drawCircle(canvas, mPointPaint);
        drawCrosshair(canvas, mPointPaint);
    }

    @Override
    public void drawSelectedOnView(Canvas canvas) {
        drawCircle(canvas, mSelectedPointPaint);
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

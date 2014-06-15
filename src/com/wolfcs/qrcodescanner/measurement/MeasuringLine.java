package com.wolfcs.qrcodescanner.measurement;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class MeasuringLine extends MeasuringObject {
    private static final int LINE_DRAW_WIDTH = 10;
    private static final int LINE_SELECT_WIDTH = 50;
    private static final float BOUNDARY_POINT_CIRCLE_RADIUS = 10.0f;

    private static final int TEXT_COLOR = Color.GREEN;
    private static final float TEXT_SIZE = 40.0f;

    private final int mIndex;
    private float mStartX;
    private float mStartY;
    private float mStopX;
    private float mStopY;

    private Paint mTempTextPaint;
    private String mLowTempStr;
    private String mHighTempStr;
    private float mViewMinTempX;
    private float mViewMinTempY;
    private float mViewMaxTempX;
    private float mViewMaxTempY;

    public MeasuringLine(Context context, int index, int width, int height) {
        super(context, width, height);
        mIndex = index;
        init();
    }

    private void init() {
        mTempTextPaint = new Paint();
        mTempTextPaint.setColor(TEXT_COLOR);
        mTempTextPaint.setTextSize(TEXT_SIZE);
    }

    public void setPosition(float startX, float startY, float stopX, float stopY) {
        mStartX = startX;
        mStartY = startY;
        mStopX = stopX;
        mStopY = stopY;

        mViewMinTempX = mStartX;
        mViewMinTempY = mStartY;

        mViewMaxTempX = mStopX;
        mViewMaxTempY = mStopY;
    }

    private void drawLine(Canvas canvas, int color) {
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(LINE_DRAW_WIDTH);
        paint.setColor(color);

        canvas.drawLine(mStartX, mStartY, mStopX, mStopY, paint);

        mLowTempStr = "0" + getCeliusStr();
        mHighTempStr = "0" + getCeliusStr();
        canvas.drawText(mLowTempStr, mViewMinTempX + 5, mViewMinTempY, mTempTextPaint);
        canvas.drawText(mHighTempStr, mViewMaxTempX + 5, mViewMaxTempY, mTempTextPaint);
    }

    @Override
    public void drawOnView(Canvas canvas) {
        drawLine(canvas, Color.WHITE);
    }

    @Override
    public void drawSelectedOnView(Canvas canvas) {
        drawLine(canvas, Color.YELLOW);
    }

    @Override
    public void drawOperatedObjectOnView(Canvas canvas) {
        drawSelectedOnView(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(LINE_DRAW_WIDTH);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(mStartX, mStartY, BOUNDARY_POINT_CIRCLE_RADIUS, paint);
        canvas.drawCircle(mStopX, mStopY, BOUNDARY_POINT_CIRCLE_RADIUS, paint);
    }

    @Override
    public boolean containOnViewPoint(float xPosition, float yPosition) {
        float left = mStartX < mStopX ? mStartX : mStopX;
        float right = mStartX > mStopX ? mStartX : mStopX;
        float top = mStartY < mStopY ? mStartY : mStopY;
        float bottom = mStartY > mStopY ? mStartY : mStopY;
        if (xPosition < left - LINE_SELECT_WIDTH || xPosition > right + LINE_SELECT_WIDTH) {
            return false;
        }
        if (yPosition < top - LINE_SELECT_WIDTH || yPosition > bottom + LINE_SELECT_WIDTH) {
            return false;
        }
        if (mStartX == mStopX || mStartY == mStopY) {
            return true;
        }
        float k = (mStartY - mStopY) / (mStartX - mStopX);
        if ((yPosition - mStartY - k * (xPosition - LINE_SELECT_WIDTH - mStartX)) *
                (yPosition - mStartY - k * (xPosition + LINE_SELECT_WIDTH - mStartX)) < 0) {
            return true;
        }
        if ((yPosition - LINE_SELECT_WIDTH - mStartY - k * (xPosition - mStartX)) *
                (yPosition + LINE_SELECT_WIDTH - mStartY - k * (xPosition - mStartX)) < 0) {
            return true;
        }
        return false;
    }

    public boolean startPointContainOnViewPoint(float xPosition, float yPosition) {
        float dx = xPosition - mStartX;
        float dy = yPosition - mStartY;
        if (Math.sqrt(dx * dx + dy * dy) < LINE_SELECT_WIDTH * 2) {
            return true;
        }
        return false;
    }

    public boolean stopPointContainOnViewPoint(float xPosition, float yPosition) {
        float dx = xPosition - mStopX;
        float dy = yPosition - mStopY;
        if (Math.sqrt(dx * dx + dy * dy) < LINE_SELECT_WIDTH * 2) {
            return true;
        }
        return false;
    }

    @Override
    public void move(float dx, float dy) {
        mStartX = mStartX + dx;
        mStartY = mStartY + dy;
        mStopX = mStopX + dx;
        mStopY = mStopY + dy;

        mViewMinTempX = mStartX;
        mViewMinTempY = mStartY;

        mViewMaxTempX = mStopX;
        mViewMaxTempY = mStopY;
    }

    public void moveStartPoint(float dx, float dy) {
        mStartX = mStartX + dx;
        mStartY = mStartY + dy;

        mViewMinTempX = mStartX;
        mViewMinTempY = mStartY;
    }

    public void moveStopPoint(float dx, float dy) {
        mStopX = mStopX + dx;
        mStopY = mStopY + dy;

        mViewMaxTempX = mStopX;
        mViewMaxTempY = mStopY;
    }

    public static void drawLine(Canvas canvas, float startX, float startY,
            float endX, float endY) {
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(LINE_DRAW_WIDTH);
        paint.setColor(Color.WHITE);

        canvas.drawLine(startX, startY, endX, endY, paint);
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {

    }
}

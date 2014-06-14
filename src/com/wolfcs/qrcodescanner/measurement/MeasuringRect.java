package com.wolfcs.qrcodescanner.measurement;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.Log;

public class MeasuringRect extends MeasuringObject {
    private static final String TAG = "MeasuringRect";
    private static final boolean DEBUG = false;
    private static final int LINE_LENGTH = 10;
    private static final int SIDE_WIDTH = 30;
    private static final int LINE_WIDTH = 3;

    private Rect mOnViewRect;
    private Paint mPaint;
    private Paint mTextPaint;

    private int mIndex;
    private String mLowTempStr;
    private String mHighTempStr;
    private int mViewMaxTempX;
    private int mViewMaxTempY;
    private int mViewMinTempX;
    private int mViewMinTempY;

    private Paint mTempPaint;

    enum SelectedSide {
        TOP, BOTTOM, LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, NONE
    };

    public MeasuringRect(Context context, Paint paint, int index, int width,
            int height) {
        super(context, width, height);
        mIndex = index;

        mOnViewRect = new Rect();
        mPaint = paint;
        mTextPaint = new Paint();
        mTextPaint.setTextSize(25f);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Style.FILL_AND_STROKE);
        // mTextPaint.setStrokeWidth(1.5f);

        mTempPaint = new Paint();
        mTempPaint.setStyle(Style.STROKE);
    }

    public void set(int left, int top, int right, int bottom) {
        final int width = getWidth();
        final int height = getHeight();
        mOnViewRect.set(left, top, right, bottom);
    }

    @Override
    public void move(float dx, float dy) {

        Rect onViewRect = getOnViewRect();
        int left = (int) (onViewRect.left + dx);
        int top = (int) (onViewRect.top + dy);
        int right = (int) (onViewRect.right + dx);
        int bottom = (int) (onViewRect.bottom + dy);

        if (left < LINE_WIDTH) {
            left = LINE_WIDTH;
            right = onViewRect.right - onViewRect.left + LINE_WIDTH;
        }
        if (top < LINE_WIDTH) {
            top = LINE_WIDTH;
            bottom = onViewRect.bottom - onViewRect.top + LINE_WIDTH;
        }
        if (right + LINE_WIDTH > getWidth()) {
            right = getWidth() - LINE_WIDTH;
            left = getWidth() - onViewRect.right + onViewRect.left - LINE_WIDTH;
        }
        if (bottom + LINE_WIDTH > getHeight()) {
            bottom = getHeight() - LINE_WIDTH;
            top = getHeight() - onViewRect.bottom + onViewRect.top - LINE_WIDTH;
        }
        set(left, top, right, bottom);
        if (DEBUG) {
            Log.i(TAG, onViewRect.toShortString());
        }
    }

    private void drawViewRect(Canvas canvas, Paint paint) {
        canvas.drawRect(mOnViewRect, paint);
        if (mHighTempStr != null) {
            canvas.drawText(mHighTempStr, mOnViewRect.left + 10,
                    mOnViewRect.top + 30, mTextPaint);
        }
        if (mLowTempStr != null) {
            canvas.drawText(mLowTempStr, mOnViewRect.left + 10,
                    mOnViewRect.bottom - 15, mTextPaint);
        }

        if (mViewMaxTempX > mOnViewRect.left && mViewMaxTempX < mOnViewRect.right &&
                mViewMaxTempY > mOnViewRect.top && mViewMaxTempY < mOnViewRect.bottom) {
            mTempPaint.setColor(Color.RED);
            mTempPaint.setStrokeWidth(3);
            canvas.drawLine(mViewMaxTempX - LINE_LENGTH, mViewMaxTempY, mViewMaxTempX
                    + LINE_LENGTH, mViewMaxTempY, mTempPaint);
            canvas.drawLine(mViewMaxTempX, mViewMaxTempY - LINE_LENGTH, mViewMaxTempX,
                    mViewMaxTempY + LINE_LENGTH, mTempPaint);
            mTempPaint.setColor(Color.WHITE);
            mTempPaint.setStrokeWidth(1);
            canvas.drawCircle(mViewMaxTempX, mViewMaxTempY, LINE_LENGTH, mTempPaint);
        }

        if (mViewMinTempX > mOnViewRect.left && mViewMinTempX < mOnViewRect.right &&
                mViewMinTempY > mOnViewRect.top && mViewMinTempY < mOnViewRect.bottom) {
            mTempPaint.setColor(Color.BLUE);
            mTempPaint.setStrokeWidth(3);
            canvas.drawLine(mViewMinTempX - LINE_LENGTH, mViewMinTempY, mViewMinTempX
                    + LINE_LENGTH, mViewMinTempY, mTempPaint);
            canvas.drawLine(mViewMinTempX, mViewMinTempY - LINE_LENGTH, mViewMinTempX,
                    mViewMinTempY + LINE_LENGTH, mTempPaint);
            mTempPaint.setColor(Color.WHITE);
            mTempPaint.setStrokeWidth(1);
            canvas.drawCircle(mViewMinTempX, mViewMinTempY, LINE_LENGTH, mTempPaint);
        }
    }

    @Override
    public void drawOnView(Canvas canvas) {
        drawViewRect(canvas, mPaint);
    }

    @Override
    public void drawSelectedOnView(Canvas canvas) {
        Paint paint = new Paint(mPaint);
        paint.setColor(Color.YELLOW);
        drawViewRect(canvas, paint);
    }

    @Override
    public void drawOperatedObjectOnView(Canvas canvas) {
        drawSelectedOnView(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Style.FILL);
        float radius = 16.0f;
        canvas.drawCircle((mOnViewRect.left + mOnViewRect.right) / 2, mOnViewRect.top,
                radius, paint);
        canvas.drawCircle((mOnViewRect.left + mOnViewRect.right) / 2, mOnViewRect.bottom,
                radius, paint);
        canvas.drawCircle(mOnViewRect.left, (mOnViewRect.top + mOnViewRect.bottom) / 2,
                radius, paint);
        canvas.drawCircle(mOnViewRect.right, (mOnViewRect.top + mOnViewRect.bottom) / 2,
                radius, paint);
    }

    public Rect getOnViewRect() {
        return new Rect(mOnViewRect);
    }

    public int getIndex() {
        return mIndex;
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containOnViewPoint(float xPosition, float yPosition) {
        return mOnViewRect.contains((int)xPosition, (int)yPosition);
    }

    public boolean isDrop() {
        return false;
    }

    public SelectedSide selecteSide(Point touchDownPoint) {
        Rect onViewRect = getOnViewRect();
        if (Math.abs(onViewRect.left - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.top - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.TOP_LEFT;
        }
        if (Math.abs(onViewRect.right - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.top - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.TOP_RIGHT;
        }
        if (Math.abs(onViewRect.left - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.bottom - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.BOTTOM_LEFT;
        }
        if (Math.abs(onViewRect.right - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.bottom - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.BOTTOM_RIGHT;
        }
        if (Math.abs(onViewRect.top - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.TOP;
        }
        if (Math.abs(onViewRect.bottom - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.BOTTOM;
        }
        if (Math.abs(onViewRect.left - touchDownPoint.x) < SIDE_WIDTH) {
            return SelectedSide.LEFT;
        }
        if (Math.abs(onViewRect.right - touchDownPoint.x) < SIDE_WIDTH) {
            return SelectedSide.RIGHT;
        }
        return SelectedSide.NONE;
    }

    public void extendSide(SelectedSide selectedSide, int xPosition, int yPosition,
            Point mTrackPoint) {
        int dx = xPosition - mTrackPoint.x;
        int dy = yPosition - mTrackPoint.y;
        Rect onViewRect = getOnViewRect();
        int left = onViewRect.left;
        int top = onViewRect.top;
        int right = onViewRect.right;
        int bottom = onViewRect.bottom;

        switch(selectedSide) {
        case TOP:
            top = top + dy;
            break;

        case BOTTOM:
            bottom = bottom + dy;
            break;

        case LEFT:
            left = left + dx;
            break;

        case RIGHT:
            right = right + dx;
            break;

        case TOP_LEFT:
            left = left + dx;
            top = top + dy;
            break;

        case TOP_RIGHT:
            right = right + dx;
            top = top + dy;
            break;

        case BOTTOM_RIGHT:
            right = right + dx;
            bottom = bottom + dy;
            break;

        case BOTTOM_LEFT:
            left = left + dx;
            bottom = bottom + dy;
            break;

        case NONE:
            break;
        }
        set(left, top, right, bottom);
    }
}

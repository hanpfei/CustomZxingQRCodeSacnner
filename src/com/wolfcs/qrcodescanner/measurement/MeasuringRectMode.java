package com.wolfcs.qrcodescanner.measurement;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringRectMode extends MeasuringMode {
    private static final String TAG = "TempMeasureRectMode";
    private static final boolean DEBUG = true;

    public static interface MeasuredRectUpdateListener {
        void onMeasuredRectUpdate(MeasuredRect rect);
    }

    private static final int DRAG_THREASHOLD = 10;
    private static final int LINE_WIDTH = 3;
    private static final int SHAKE_RANGE = 5;
    private static final int RECT_MIN_SIZE = 60;
    private static final int SIDE_WIDTH = 30;

    private final int mMaxMeasuringRectNum;

    private Vector<MeasuredRect> mUsingMeasuredRects;
    private Vector<MeasuredRect> mUsableMeasuredRects;
    private Vector<MeasuredRect> mSelectedMeasuredRects;

    private Paint mPaint;
    private Paint mTextPaint;
    private MeasuredRectUpdateListener mListener;

    private Paint mRectPaint;

    private Rect mCurrentViewRect = null;
    private MeasuredRect mCurrentMeasuredRect = null;
    private MeasuredRect mOperatingMeasuredRect = null;
    private Point mTouchDownPoint = new Point();
    private Point mTrackPoint = new Point();

    enum SelectedSide {
        TOP, BOTTOM, LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, NONE
    };
    private SelectedSide mSelectedSide = SelectedSide.NONE;

    public MeasuringRectMode(Context context, int maxMeasuringRectNum) {
        super(context);
        mMaxMeasuringRectNum = maxMeasuringRectNum;
        init();
    }

    public void setOnMeasuredRectUpdateListener(MeasuredRectUpdateListener listener) {
        mListener = listener;
    }

    private final void init() {
        mUsableMeasuredRects = new Vector<MeasuringRectMode.MeasuredRect>();
        mUsingMeasuredRects = new Vector<MeasuringRectMode.MeasuredRect>();
        mSelectedMeasuredRects = new Vector<MeasuringRectMode.MeasuredRect>();

        mPaint = new Paint();
        // mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(LINE_WIDTH);
        // mPaint.setTextSize(25f);
        // Must manually scale the desired text size to match screen density
        mPaint.setColor(Color.WHITE);

        mTextPaint = new Paint();
        mTextPaint.setTextSize(22f);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Style.FILL_AND_STROKE);

        mRectPaint = new Paint();
        mRectPaint.setStyle(Style.FILL);
        mRectPaint.setColor(Color.GRAY);
    }

    public float getDistance(MotionEvent event) {
        float x = Math.abs(event.getX(0) - event.getX(1));
        float y = Math.abs(event.getY(0) - event.getY(1));
        return FloatMath.sqrt(x * x + y * y);
    }

    private MeasuredRect selectMeasuredRect(Point point) {
        MeasuredRect measuredRect = null;
        synchronized (mUsingMeasuredRects) {
            for (int i = 0; i < mUsingMeasuredRects.size(); i++) {
                MeasuredRect rect = mUsingMeasuredRects.get(i);
                if (rect.containPoint(point)) {
                    if(measuredRect == null || measuredRect.getIndex() < rect.getIndex()) {
                        measuredRect = rect;
                    }
                }
            }
        }
        return measuredRect;
    }

    @Override
    protected void onLongPress() {
        performMeasuringObjectSelected();
        mOperatingMeasuredRect = mCurrentMeasuredRect;
        mSelectedMeasuredRects.add(mOperatingMeasuredRect);
        mCurrentMeasuredRect = null;
    }

    private SelectedSide selecteSide(MeasuredRect rect, Point touchDownPoint) {
        Rect onViewRect = rect.getOnViewRect();
        if(Math.abs(onViewRect.left - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.top - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.TOP_LEFT;
        }
        if(Math.abs(onViewRect.right - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.top - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.TOP_RIGHT;
        }
        if(Math.abs(onViewRect.left - touchDownPoint.x) < SIDE_WIDTH
                && Math.abs(onViewRect.bottom - touchDownPoint.y) < SIDE_WIDTH) {
            return SelectedSide.BOTTOM_LEFT;
        }
        if(Math.abs(onViewRect.right - touchDownPoint.x) < SIDE_WIDTH
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
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        updateViewDimension(view);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            if (DEBUG) Log.i(TAG, "Down : " + event.getX() + " " + event.getY());
            if (DEBUG) Log.i(TAG, "Down raw: " + event.getRawX() + " " + event.getRawY());
            // int id = event.getPointerId(0);
            mTrackPoint.x = (int) event.getX();
            mTrackPoint.y = (int) event.getY();
            mTouchDownPoint.x = (int) event.getX();
            mTouchDownPoint.y = (int) event.getY();

            MeasuredRect rect = selectMeasuredRect(mTrackPoint);
            if (mOperatingMeasuredRect != null) {
                mSelectedSide = selecteSide(mOperatingMeasuredRect, mTouchDownPoint);
                if (mSelectedSide == SelectedSide.NONE) {
                    mCurrentMeasuredRect = mOperatingMeasuredRect;
                }
            } else {
                if (rect == null) {
                    synchronized (mUsingMeasuredRects) {
                        if (mUsingMeasuredRects.size() < mMaxMeasuringRectNum) {
                            mCurrentViewRect = new Rect();
                            mCurrentViewRect.left = mTrackPoint.x;
                            mCurrentViewRect.top = mTrackPoint.y;
                        }
                    }
                } else {
                    mCurrentMeasuredRect = rect;
                    startLongPressCheck(view);
                }
            }
            break;

        case MotionEvent.ACTION_MOVE:
            int xPostion = (int) event.getX();
            int yPosition = (int) event.getY();

            if (mOperatingMeasuredRect != null && mSelectedSide != SelectedSide.NONE) {
                int dx = xPostion - mTrackPoint.x;
                int dy = yPosition - mTrackPoint.y;
                Rect onViewRect = mOperatingMeasuredRect.getOnViewRect();
                int left = onViewRect.left;
                int top = onViewRect.top;
                int right = onViewRect.right;
                int bottom = onViewRect.bottom;

                switch(mSelectedSide) {
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
                mOperatingMeasuredRect.set(left, top, right, bottom);
                mTrackPoint.x = xPostion;
                mTrackPoint.y = yPosition;
            } else if (mCurrentMeasuredRect != null) { // For dragging a existed rectangle
                int dx = xPostion - mTrackPoint.x;
                int dy = yPosition - mTrackPoint.y;

                if (DEBUG) Log.i(TAG, "dx = " + dx + " dy = " + dy);
                // 消除抖动
                if (Math.abs(dx) > SHAKE_RANGE && Math.abs(dy) > SHAKE_RANGE) {
                    cancelLongPressCheck(view);
                }

                Rect onViewRect = mCurrentMeasuredRect.getOnViewRect();
                int left = onViewRect.left + dx;
                int top = onViewRect.top + dy;
                int right = onViewRect.right + dx;
                int bottom = onViewRect.bottom + dy;

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
                mCurrentMeasuredRect.set(left, top, right, bottom);

                if (DEBUG) {
                    Log.i(TAG, onViewRect.toShortString());
                }

                mTrackPoint.x = xPostion;
                mTrackPoint.y = yPosition;
            } else if (mCurrentViewRect != null) { // For creating new rectangle.
                if (xPostion < mCurrentViewRect.left) {
                    if (mCurrentViewRect.right == 0) {
                        mCurrentViewRect.right = mCurrentViewRect.left;
                    }
                    mCurrentViewRect.left = xPostion;
                } else {
                    mCurrentViewRect.right = xPostion;
                }

                if (yPosition < mCurrentViewRect.top) {
                    if (mCurrentViewRect.bottom == 0) {
                        mCurrentViewRect.bottom = mCurrentViewRect.top;
                    }
                    mCurrentViewRect.top = yPosition;
                } else {
                    mCurrentViewRect.bottom = yPosition;
                }

                if (mCurrentViewRect.right + LINE_WIDTH > getWidth()) {
                    mCurrentViewRect.right = getWidth() - LINE_WIDTH;
                }
                if (mCurrentViewRect.bottom + LINE_WIDTH > getHeight()) {
                    mCurrentViewRect.bottom = getHeight() - LINE_WIDTH;
                }
            }
            break;

        case MotionEvent.ACTION_UP:
            // For creating a new measured rectangle.
            if (mCurrentViewRect != null) {
                if (DEBUG) Log.i(TAG, "mCurrentViewRect: " + mCurrentViewRect.toShortString());
                if (mCurrentViewRect.right - mCurrentViewRect.left > RECT_MIN_SIZE
                        && mCurrentViewRect.bottom - mCurrentViewRect.top > RECT_MIN_SIZE) {
                    MeasuredRect measuredRect = getMeasuredRect(mPaint);
                    measuredRect.set(mCurrentViewRect.left, mCurrentViewRect.top,
                            mCurrentViewRect.right, mCurrentViewRect.bottom);
                    if (DEBUG) Log.i(TAG, "MeasureRect: " + measuredRect.toString());
                    if (mListener != null) {
                        mListener.onMeasuredRectUpdate(measuredRect);
                    }
                }
            }

            if (mCurrentMeasuredRect != null) {
                // For selecting a existing rectangle.
                int x = (int)event.getX();
                int y = (int)event.getY();
                if(Math.abs(x - mTouchDownPoint.x) < DRAG_THREASHOLD &&
                        Math.abs(y - mTouchDownPoint.y) < DRAG_THREASHOLD) {
                    if(mSelectedMeasuredRects.contains(mCurrentMeasuredRect)){
                        mSelectedMeasuredRects.remove(mCurrentMeasuredRect);
                    } else if (mOperatingMeasuredRect != mCurrentMeasuredRect){
                        mSelectedMeasuredRects.add(mCurrentMeasuredRect);
                    }

                    if(mOperatingMeasuredRect != null) {
                        mOperatingMeasuredRect = null;
                    }
                } else { // For dragging a existing rectangle.
                    if (mListener != null) {
                        mListener.onMeasuredRectUpdate(mCurrentMeasuredRect);
                    }
                }
//                mCurrentMeasuredRect.showTempDelay();
            }
            if(mOperatingMeasuredRect != null) {
                if (mListener != null) {
                    mListener.onMeasuredRectUpdate(mOperatingMeasuredRect);
                }
            }
            mCurrentViewRect = null;
            mCurrentMeasuredRect = null;
            cancelLongPressCheck(view);
            break;
        }
        view.postInvalidate();
        return true;
    }

    @Override
    public void drawOnView(Canvas canvas) {
        if (mCurrentViewRect != null) {
            canvas.drawRect(mCurrentViewRect, mPaint);
        }
        synchronized (mUsingMeasuredRects) {
            for (MeasuredRect rect : mUsingMeasuredRects) {
                if(mSelectedMeasuredRects.contains(rect)) {
                    rect.drawSelectedOnView(canvas);
                } else {
                    rect.drawOnView(canvas);
                }
            }
        }
        if (mOperatingMeasuredRect != null) {
            mOperatingMeasuredRect.drawOperatingOnView(canvas);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        synchronized (mUsingMeasuredRects) {
            for (MeasuredRect rect : mSelectedMeasuredRects) {
                rect.recycle();
                mUsingMeasuredRects.remove(rect);
            }
            mSelectedMeasuredRects.clear();
            if (mOperatingMeasuredRect != null) {
                mOperatingMeasuredRect = null;
            }
        }
    }

    @Override
    public void selectAllMeasuringObjects() {
        synchronized (mUsingMeasuredRects) {
            for (MeasuredRect rect : mUsingMeasuredRects) {
                if(!mSelectedMeasuredRects.contains(rect)) {
                    mSelectedMeasuredRects.add(rect);
                }
            }
        }
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        mOperatingMeasuredRect = null;
        mSelectedMeasuredRects.clear();
    }

    private MeasuredRect getMeasuredRect(Paint paint) {
        synchronized (mUsingMeasuredRects) {
            if(mUsingMeasuredRects.size() >= mMaxMeasuringRectNum) {
                return null;
            }
            MeasuredRect rect;
            if(!mUsableMeasuredRects.isEmpty()) {
                rect = mUsableMeasuredRects.get(0);
                mUsableMeasuredRects.remove(0);
            } else {
                rect = new MeasuredRect(paint, mUsingMeasuredRects.size());
            }

            mUsingMeasuredRects.add(rect);
            return rect;
        }
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        synchronized (mUsingMeasuredRects) {
            for (MeasuredRect rect : mUsingMeasuredRects) {
                rect.drawOnRealWorldObjectImage(canvas);
            }
        }
    }

    public class MeasuredRect {
        private static final int LINE_LENGTH = 10;
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

        MeasuredRect(Paint paint, int index) {
            mIndex = index;

            mOnViewRect = new Rect();
            this.mPaint = paint;
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

        public boolean containPoint(Point point) {
            return mOnViewRect.contains(point.x, point.y);
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

        public void drawOnView(Canvas canvas) {
            drawViewRect(canvas, mPaint);
        }

        public void drawSelectedOnView(Canvas canvas) {
            Paint paint = new Paint(mPaint);
            paint.setColor(Color.YELLOW);
            drawViewRect(canvas, paint);
        }

        public void drawOperatingOnView(Canvas canvas) {
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

        public void drawOnRealWorldObjectImage(Canvas canvas) {
        }

        public Rect getOnViewRect() {
            return new Rect(mOnViewRect);
        }

        public boolean isDrop() {
            return false;
        }

        public void recycle() {
            mUsableMeasuredRects.add(this);
        }

        public int getIndex() {
            return mIndex;
        }
    }
}

package com.wolfcs.qrcodescanner.measurement;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringRectMode extends MeasuringMode {
    private static final String TAG = "TempMeasureRectMode";
    private static final boolean DEBUG = true;

    public static interface MeasuredRectUpdateListener {
        void onMeasuredRectUpdate(MeasuringRect rect);
    }

    private static final int DRAG_THREASHOLD = 10;
    private static final int LINE_WIDTH = 3;
    private static final int SHAKE_RANGE = 5;
    private static final int RECT_MIN_SIZE = 60;

    private MeasuredRectUpdateListener mListener;

    private Rect mCurrentViewRect = null;
    private MeasuringRect mCurrentMeasuredRect = null;
    private MeasuringRect mOperatingMeasuredRect = null;
    private Point mTouchDownPoint = new Point();
    private Point mTrackPoint = new Point();

    private MeasuringObjectsManager mMeasuringObjectsManager;

    private MeasuringRect.SelectedSide mSelectedSide = MeasuringRect.SelectedSide.NONE;

    public MeasuringRectMode(Context context, int maxMeasuringRectNum) {
        super(context);
        mMeasuringObjectsManager = MeasuringObjectsManager.getInstance();
        mMeasuringObjectsManager.setMaxMeasuringRectNum(maxMeasuringRectNum);
    }

    public void setOnMeasuredRectUpdateListener(MeasuredRectUpdateListener listener) {
        mListener = listener;
    }

    public float getDistance(MotionEvent event) {
        float x = Math.abs(event.getX(0) - event.getX(1));
        float y = Math.abs(event.getY(0) - event.getY(1));
        return FloatMath.sqrt(x * x + y * y);
    }

    @Override
    protected void onLongPress() {
        performMeasuringObjectsSelected();
        mOperatingMeasuredRect = mCurrentMeasuredRect;
        mMeasuringObjectsManager.selectRect(mOperatingMeasuredRect);
        mCurrentMeasuredRect = null;
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

            MeasuringRect rect = mMeasuringObjectsManager.selectMeasuredRect(mTrackPoint);
            if (mOperatingMeasuredRect != null) {
                mSelectedSide = mOperatingMeasuredRect.selecteSide(mTouchDownPoint);
                if (mSelectedSide == MeasuringRect.SelectedSide.NONE) {
                    mCurrentMeasuredRect = mOperatingMeasuredRect;
                }
            } else {
                if (rect == null) {
                    if (mMeasuringObjectsManager.canCreateNewRect()) {
                        mCurrentViewRect = new Rect();
                        mCurrentViewRect.left = mTrackPoint.x;
                        mCurrentViewRect.top = mTrackPoint.y;
                    }
                } else {
                    mCurrentMeasuredRect = rect;
                    startLongPressCheck(view);
                }
            }
            break;

        case MotionEvent.ACTION_MOVE:
            int xPosition = (int) event.getX();
            int yPosition = (int) event.getY();

            if (mOperatingMeasuredRect != null
                    && mSelectedSide != MeasuringRect.SelectedSide.NONE) {
                mOperatingMeasuredRect.extendSide(mSelectedSide, xPosition,
                        yPosition, mTrackPoint);
                mTrackPoint.x = xPosition;
                mTrackPoint.y = yPosition;
            } else if (mCurrentMeasuredRect != null) { // For dragging a existed rectangle
                int dx = xPosition - mTrackPoint.x;
                int dy = yPosition - mTrackPoint.y;

                if (DEBUG) Log.i(TAG, "dx = " + dx + " dy = " + dy);
                // 消除抖动
                if (Math.abs(dx) > SHAKE_RANGE && Math.abs(dy) > SHAKE_RANGE) {
                    cancelLongPressCheck(view);
                }
                mCurrentMeasuredRect.move(dx, dy);

                mTrackPoint.x = xPosition;
                mTrackPoint.y = yPosition;
            } else if (mCurrentViewRect != null) { // For creating new rectangle.
                if (xPosition < mCurrentViewRect.left) {
                    if (mCurrentViewRect.right == 0) {
                        mCurrentViewRect.right = mCurrentViewRect.left;
                    }
                    mCurrentViewRect.left = xPosition;
                } else {
                    mCurrentViewRect.right = xPosition;
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
                    MeasuringRect measuredRect = mMeasuringObjectsManager
                            .createNewMeasuringRect(getContext(),
                                    getWidth(), getHeight());
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
                    if(mMeasuringObjectsManager.isRectSelected(mCurrentMeasuredRect)){
                        mMeasuringObjectsManager.deselectRect(mCurrentMeasuredRect);
                        if (mMeasuringObjectsManager.getSelectedMeasuringRectsSize() == 0) {
                            performMeasuringObjectsDeSelected();
                        }
                    } else if (mOperatingMeasuredRect != mCurrentMeasuredRect){
                        mMeasuringObjectsManager.selectRect(mCurrentMeasuredRect);
                        if (mMeasuringObjectsManager.getSelectedMeasuringRectsSize() == 1) {
                            performMeasuringObjectsSelected();
                        }
                    }

                    if(mOperatingMeasuredRect != null) {
                        mOperatingMeasuredRect = null;
                        mMeasuringObjectsManager.deselectRect(mCurrentMeasuredRect);
                    }
                } else { // For dragging a existing rectangle.
                    if (mListener != null) {
                        mListener.onMeasuredRectUpdate(mCurrentMeasuredRect);
                    }
                }
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
            MeasuringRect.drawRect(canvas, mCurrentViewRect);
        }
        mMeasuringObjectsManager.drawRectsOnView(canvas);
        if (mOperatingMeasuredRect != null) {
            mOperatingMeasuredRect.drawOperatedObjectOnView(canvas);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        mMeasuringObjectsManager.clearSelectedRects();
        if (mOperatingMeasuredRect != null) {
            mOperatingMeasuredRect = null;
        }
        performMeasuringObjectsDeSelected();
    }

    @Override
    public void selectAllMeasuringObjects() {
       mMeasuringObjectsManager.selectAllRects();
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        mOperatingMeasuredRect = null;
        mMeasuringObjectsManager.cancelOpOnRects();
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        mMeasuringObjectsManager.drawRectsOnRealWorldObjectImage(canvas);
    }
}

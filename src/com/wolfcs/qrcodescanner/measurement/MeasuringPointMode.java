package com.wolfcs.qrcodescanner.measurement;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringPointMode extends MeasuringMode {
    private static final String TAG = "TempMeasurePointMode";

    private static final int TOUCH_SLOP = 20;

    private final int mMaxMeasuringPointNum;

    private ArrayList<MeasuringPoint> mUsingPoints;
    private ArrayList<MeasuringPoint> mUsablePoints;
    private ArrayList<MeasuringPoint> mSelectedPoints;

    private int mLastMotionX, mLastMotionY; 
    private boolean isMoved;

    private MeasuringPoint mSelectedPoint;

    public MeasuringPointMode(Context context, int maxMeasuringPoint) {
        super(context);
        mMaxMeasuringPointNum = maxMeasuringPoint;

        mUsingPoints = new ArrayList<MeasuringPoint>();
        mUsablePoints = new ArrayList<MeasuringPoint>();
        mSelectedPoints = new ArrayList<MeasuringPoint>();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        updateViewDimension(view);

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mLastMotionX = x;
            mLastMotionY = y;
            if(mSelectedPoint == null) {
                mSelectedPoint = selectUsingPoint(x, y);
            }
            isMoved = false;
            break;

        case MotionEvent.ACTION_MOVE:
            if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                    || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                if(mSelectedPoint != null) {
                    mSelectedPoint.setPosition(x, y);
                }
                isMoved = true;
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mSelectedPoint != null) {
                if (!isMoved) {
                    if (mSelectedPoints.contains(mSelectedPoint)) {
                        mSelectedPoints.remove(mSelectedPoint);
                        if (mSelectedPoints.size() == 0) {
                            performMeasuringObjectsDeSelected();
                        }
                    } else {
                        mSelectedPoints.add(mSelectedPoint);
                        if (mSelectedPoints.size() == 1) {
                            performMeasuringObjectsSelected();
                        }
                    }
                } else {
                    notifyPointPositionChange(mSelectedPoint);
                }
                mSelectedPoint = null;
            } else if (!isMoved) {
                MeasuringPoint point = createNewMeasuringPoint();
                if (point != null) {
                    point.setPosition(x, y);
                    notifyPointPositionChange(point);
                }
            }
            break;
        }
        view.postInvalidate();
        return true;
    }

    private void notifyPointPositionChange(MeasuringPoint point) {
        int index = point.getIndex();
        Point thermalPosition = converToRealObjectImagePosition(point.mPosition);
    }

    @Override
    public void drawOnView(Canvas canvas) {
        for(MeasuringPoint point : mUsingPoints) {
            if(!mSelectedPoints.contains(point)){
                point.drawOnView(canvas);
            } else {
                point.drawSelectedOnView(canvas);
            }
        }
        if(mSelectedPoint != null) {
            mSelectedPoint.drawSelectedOnView(canvas);
        }
    }

    private MeasuringPoint selectUsingPoint(int x, int y) {
        for(MeasuringPoint point : mUsingPoints) {
            if (point.containOnViewPoint(x, y)) {
                return point;
            }
        }
        return null;
    }
    
    private MeasuringPoint createNewMeasuringPoint() {
        if(mUsingPoints.size() >= mMaxMeasuringPointNum) {
            return null;
        }
        MeasuringPoint point;
        if(!mUsablePoints.isEmpty()){
            point = mUsablePoints.get(0);
            mUsablePoints.remove(0);
        } else {
            point = new MeasuringPoint(getContext(), getWidth(), getHeight(),
                    mUsingPoints.size());
        }

        mUsingPoints.add(point);
        return point;
    }

    public void recycle(MeasuringPoint point) {
        mUsablePoints.remove(point);
        mUsablePoints.add(point);
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        Log.i(TAG, "clearSelectedMeasuringObjects");
        for(MeasuringPoint point : mSelectedPoints) {
            mUsingPoints.remove(point);
            recycle(point);
        }
        mSelectedPoints.clear();
        performMeasuringObjectsDeSelected();
    }

    @Override
    public void selectAllMeasuringObjects() {
        for(MeasuringPoint point : mUsingPoints) {
            if (!mSelectedPoints.contains(point)) {
                mSelectedPoints.add(point);
            }
        }
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        mSelectedPoints.clear();
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {

    }
}

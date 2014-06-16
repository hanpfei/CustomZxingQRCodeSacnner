package com.wolfcs.qrcodescanner.measurement;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringPointMode extends MeasuringMode {
    private static final int TOUCH_SLOP = 20;

    private int mLastMotionX, mLastMotionY; 
    private boolean isMoved;

    private MeasuringPoint mSelectedPoint;
    private MeasuringObjectsManager mMeasuringObjectsManager;

    public MeasuringPointMode(Context context, int maxMeasuringPoint) {
        super(context);
        mMeasuringObjectsManager = MeasuringObjectsManager.getInstance();
        mMeasuringObjectsManager.setMaxMeasuringPointNum(maxMeasuringPoint);
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
                mSelectedPoint = mMeasuringObjectsManager.selectUsingPoint(x, y);
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
                    if (mMeasuringObjectsManager.isPointSelected(mSelectedPoint)) {
                        mMeasuringObjectsManager.deselectPoint(mSelectedPoint);
                        if (mMeasuringObjectsManager.getSelectedMeasuringPointsSize() == 0) {
                            performMeasuringObjectsDeSelected();
                        }
                    } else {
                        mMeasuringObjectsManager.selectPoint(mSelectedPoint);
                        if (mMeasuringObjectsManager.getSelectedMeasuringPointsSize() == 1) {
                            performMeasuringObjectsSelected();
                        }
                    }
                } else {
                    notifyPointPositionChange(mSelectedPoint);
                }
                mSelectedPoint = null;
            } else if (!isMoved) {
                MeasuringPoint point = mMeasuringObjectsManager
                        .createNewMeasuringPoint(getContext(), getWidth(), getHeight());
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
        mMeasuringObjectsManager.drawPointsOnView(canvas);
        if(mSelectedPoint != null) {
            mSelectedPoint.drawSelectedOnView(canvas);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        mMeasuringObjectsManager.clearSelectedPoints();
        performMeasuringObjectsDeSelected();
    }

    @Override
    public void selectAllMeasuringObjects() {
        mMeasuringObjectsManager.selectAllUsingPoints();
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        mMeasuringObjectsManager.cancelOpOnPoints();
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        mMeasuringObjectsManager.drawPointsOnRealWorldObjectImage(canvas);
    }
}

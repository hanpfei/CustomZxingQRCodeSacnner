package com.wolfcs.qrcodescanner.measurement;

import com.wolfcs.qrcodescanner.measurement.MeasuringLine.EndpointType;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringLineMode extends MeasuringMode {
    private Point mTouchDownPoint = new Point();
    private Point mTrackPoint = new Point();

    private MeasuringLine mCurrentMeasuringLine;
    private MeasuringLine mOpMeasuringLine;

    private boolean mIsMoved;
    private boolean mCurrentCreatingLine;

    private EndpointType mEndpointType = EndpointType.NONE;

    public MeasuringLineMode(Context context,
            MeasuringObjectsManager objectManager, int maxMeasuringLineNum) {
        super(context, objectManager);
        mObjectsManager.setMaxMeasuringLineNum(maxMeasuringLineNum);
    }

    @Override
    protected void onLongPress() {
        performMeasuringObjectsSelected();
        mOpMeasuringLine = mCurrentMeasuringLine;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        updateViewDimension(view);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mTrackPoint.x = (int) event.getX();
            mTrackPoint.y = (int) event.getY();
            mTouchDownPoint.x = (int) event.getX();
            mTouchDownPoint.y = (int) event.getY();
            mIsMoved = false;
            mCurrentMeasuringLine = mObjectsManager
                    .selectMeasuringLine(event.getX(), event.getY());
            if (mOpMeasuringLine == null) {
                if (mCurrentMeasuringLine == null) {
                    mCurrentCreatingLine = true;
                } else {
                    startLongPressCheck(view);
                }
            } else {
                mEndpointType = mOpMeasuringLine.selectEndpoint(event.getX(),
                        event.getY());
            }
            break;

        case MotionEvent.ACTION_MOVE:
            float xPostion = event.getX();
            float yPosition = event.getY();
            float dx = xPostion - mTrackPoint.x;
            float dy = yPosition - mTrackPoint.y;
            if (Math.abs(dx) > MOVE_THREASHOLD || Math.abs(dy) > MOVE_THREASHOLD) {
                mTrackPoint.x = (int) xPostion;
                mTrackPoint.y = (int) yPosition;
                mIsMoved = true;
                cancelLongPressCheck(view);
                if (mEndpointType != EndpointType.NONE) {
                    if (mEndpointType == EndpointType.START_POINT) {
                        mOpMeasuringLine.moveStartPoint(dx, dy);
                    } else if (mEndpointType == EndpointType.STOP_POINT) {
                        mOpMeasuringLine.moveStopPoint(dx, dy);
                    }
                } else if (mCurrentMeasuringLine != null) {
                    mCurrentMeasuringLine.move(dx, dy);
                }
            }
            break;

        case MotionEvent.ACTION_UP:
            cancelLongPressCheck(view);
            if (mCurrentMeasuringLine == null) {
                mCurrentMeasuringLine = mObjectsManager
                        .createNewMeasuringLine(getContext(), getWidth(),getHeight());
                mCurrentCreatingLine = false;
                if (mCurrentMeasuringLine != null) {
                    mCurrentMeasuringLine.setPosition(mTouchDownPoint.x, mTouchDownPoint.y, 
                            event.getX(), event.getY());
                }
            } else {
                if (!mIsMoved) {
                    if (mObjectsManager.isLineSelected(mCurrentMeasuringLine)
                            && mOpMeasuringLine == null) {
                        mObjectsManager.deselectLine(mCurrentMeasuringLine);
                        if (mObjectsManager.getSelectedMeasuringLinesSize() == 0) {
                            performMeasuringObjectsDeSelected();
                        }
                    } else {
                        mObjectsManager.selectLine(mCurrentMeasuringLine);
                        if (mObjectsManager.getSelectedMeasuringLinesSize() == 1) {
                            performMeasuringObjectsSelected();
                        }
                    }
                }
            }
            mEndpointType = EndpointType.NONE;
            mCurrentMeasuringLine = null;
            break;
        }
        view.postInvalidate();
        return true;
    }

    @Override
    public void drawOnView(Canvas canvas) {
        if (mOpMeasuringLine != null) {
            mOpMeasuringLine.drawOperatedObjectOnView(canvas);
        }
        if (mObjectsManager.canCreateNewLine() && mCurrentCreatingLine) {
            MeasuringLine.drawLine(canvas, mTouchDownPoint.x,
                    mTouchDownPoint.y, mTrackPoint.x, mTrackPoint.y);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        mObjectsManager.clearSelectedLines();
        mOpMeasuringLine = null;
        performMeasuringObjectsDeSelected();
    }

    @Override
    public void selectAllMeasuringObjects() {
        mObjectsManager.selectAllLines();
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        mOpMeasuringLine = null;
        mObjectsManager.cancelOpOnLines();
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        mObjectsManager.drawLinesOnRealWorldObjectImage(canvas);
    }
}

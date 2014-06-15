package com.wolfcs.qrcodescanner.measurement;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringLineMode extends MeasuringMode {
    private final int mMaxMeasuringLineNum;

    private Point mTouchDownPoint = new Point();
    private Point mTrackPoint = new Point();

    private MeasuringLine mCurrentMeasuringLine;
    private MeasuringLine mOpMeasuringLine;

    private Vector<MeasuringLine> mUsingMeasuringLines;
    private Vector<MeasuringLine> mUsableMeasuringLines;
    private Vector<MeasuringLine> mSelectedMeasuringLines;

    private boolean mIsMoved;
    private boolean mCurrentCreatedLine;

    enum EndpointType {
        START_POINT, STOP_POINT, NONE
    };
    private EndpointType mEndpointType = EndpointType.NONE;

    public MeasuringLineMode(Context context, int maxMeasuringLineNum) {
        super(context);
        mMaxMeasuringLineNum = maxMeasuringLineNum;
        init();
    }

    private void init() {
        mUsingMeasuringLines = new Vector<MeasuringLine>();
        mUsableMeasuringLines = new Vector<MeasuringLine>();
        mSelectedMeasuringLines = new Vector<MeasuringLine>();
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
            mCurrentMeasuringLine = selectMeasuringLine(event.getX(), event.getY());
            if (mOpMeasuringLine == null) {
                if (mCurrentMeasuringLine == null) {
                    mCurrentCreatedLine = true;
                } else {
                    startLongPressCheck(view);
                }
            } else {
                mEndpointType = selectEndpoint(mOpMeasuringLine, event.getX(),
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
                mCurrentMeasuringLine = createNewMeasuringLine();
                mCurrentCreatedLine = false;
                if (mCurrentMeasuringLine != null) {
                    mCurrentMeasuringLine.setPosition(mTouchDownPoint.x, mTouchDownPoint.y, 
                            event.getX(), event.getY());
                    mUsingMeasuringLines.add(mCurrentMeasuringLine);
                }
            } else {
                if (!mIsMoved) {
                    if (mSelectedMeasuringLines.contains(mCurrentMeasuringLine)
                            && mOpMeasuringLine == null) {
                        mSelectedMeasuringLines.remove(mCurrentMeasuringLine);
                        if (mSelectedMeasuringLines.size() == 0) {
                            performMeasuringObjectsDeSelected();
                        }
                    } else {
                        mSelectedMeasuringLines.add(mCurrentMeasuringLine);
                        if (mSelectedMeasuringLines.size() == 1) {
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

    private EndpointType selectEndpoint(MeasuringLine line,
            float xPosition, float yPosition) {
        if (line.startPointContainOnViewPoint(xPosition, yPosition)) {
            return EndpointType.START_POINT;
        }
        if (line.stopPointContainOnViewPoint(xPosition, yPosition)) {
            return EndpointType.STOP_POINT;
        }

        return EndpointType.NONE;
    }

    private MeasuringLine selectMeasuringLine(float xPosition, float yPosition) {
        for (MeasuringLine line : mUsingMeasuringLines){
            if(line.containOnViewPoint(xPosition, yPosition)) {
                return line;
            }
        }
        return null;
    }

    private MeasuringLine createNewMeasuringLine() {
        if (mUsingMeasuringLines.size() >= mMaxMeasuringLineNum) {
            return null;
        }
        MeasuringLine line;
        if (!mUsableMeasuringLines.isEmpty()) {
            line = mUsableMeasuringLines.get(0);
        } else {
            line = new MeasuringLine(getContext(),
                    mUsingMeasuringLines.size(), getWidth(), getHeight());
        }
        return line;
    }

    @Override
    public void drawOnView(Canvas canvas) {
        for (MeasuringLine line : mUsingMeasuringLines) {
            if (!mSelectedMeasuringLines.contains(line)) {
                line.drawOnView(canvas);
            }
        }
        for (MeasuringLine line : mSelectedMeasuringLines) {
            line.drawSelectedOnView(canvas);
        }
        if (mOpMeasuringLine != null) {
            mOpMeasuringLine.drawOperatedObjectOnView(canvas);
        }
        if (mUsingMeasuringLines.size() < mMaxMeasuringLineNum
                && mCurrentCreatedLine) {
            MeasuringLine.drawLine(canvas, mTouchDownPoint.x,
                    mTouchDownPoint.y, mTrackPoint.x, mTrackPoint.y);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        mSelectedMeasuringLines.clear();
        for (MeasuringLine line : mUsingMeasuringLines) {
            recycle(line);
        }
        mUsingMeasuringLines.clear();
        mOpMeasuringLine = null;
        performMeasuringObjectsDeSelected();
    }

    @Override
    public void selectAllMeasuringObjects() {
        for (MeasuringLine line : mUsingMeasuringLines) {
            mSelectedMeasuringLines.add(line);
        }
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        mOpMeasuringLine = null;
        mSelectedMeasuringLines.clear();
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        for (MeasuringLine line : mUsingMeasuringLines) {
            line.drawOnRealWorldObjectImage(canvas);
        }
    }

    public void recycle(MeasuringLine line) {
        if (line != null && !mUsableMeasuringLines.contains(line)) {
            mUsableMeasuringLines.add(line);
        }
    }
}

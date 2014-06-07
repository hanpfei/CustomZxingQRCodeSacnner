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
        performMeasuringObjectSelected();
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

            mCurrentMeasuringLine = selectMeasuringLine(event.getX(), event.getY());
            if (mCurrentMeasuringLine != null && mOpMeasuringLine == null) {
                startLongPressCheck(view);
            }
            if (mOpMeasuringLine != null) {
                mEndpointType = selectEndpoint(mOpMeasuringLine,
                        event.getX(), event.getY());
            }
            break;

        case MotionEvent.ACTION_MOVE:
            float xPostion = event.getX();
            float yPosition = event.getY();
            float dx = xPostion - mTrackPoint.x;
            float dy = yPosition - mTrackPoint.y;
            if (mEndpointType != EndpointType.NONE) {
                if (mEndpointType == EndpointType.START_POINT) {
                    mOpMeasuringLine.moveStartPoint(dx, dy);
                } else if (mEndpointType == EndpointType.STOP_POINT) {
                    mOpMeasuringLine.moveStopPoint(dx, dy);
                }
                mTrackPoint.x = (int) xPostion;
                mTrackPoint.y = (int) yPosition;
            }else if (mCurrentMeasuringLine != null) {
                if (Math.abs(dx) > MOVE_THREASHOLD || Math.abs(dy) > MOVE_THREASHOLD) {
                    cancelLongPressCheck(view);
                    mCurrentMeasuringLine.move(dx, dy);
                    mTrackPoint.x = (int) xPostion;
                    mTrackPoint.y = (int) yPosition;
                }
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mCurrentMeasuringLine == null) {
                mCurrentMeasuringLine = createNewMeasuringLine();
                if (mCurrentMeasuringLine != null) {
                    mCurrentMeasuringLine.setPosition(mTouchDownPoint.x, mTouchDownPoint.y, 
                            event.getX(), event.getY());
                    mUsingMeasuringLines.add(mCurrentMeasuringLine);
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
                    mUsingMeasuringLines.size());
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
            mOpMeasuringLine.drawOperatingOnView(canvas);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        mSelectedMeasuringLines.clear();
        for (MeasuringLine line : mUsingMeasuringLines) {
            recycle(line);
        }
        mUsingMeasuringLines.clear();
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
            line.drawOnThermalImage(canvas);
        }
    }

    public void recycle(MeasuringLine line) {
        if (line != null && !mUsableMeasuringLines.contains(line)) {
            mUsableMeasuringLines.add(line);
        }
    }
}

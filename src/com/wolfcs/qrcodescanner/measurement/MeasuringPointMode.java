package com.wolfcs.qrcodescanner.measurement;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class MeasuringPointMode extends MeasuringMode {
    private static final String TAG = "TempMeasurePointMode";

    private static final int POINT_COLOR = Color.WHITE;
    private static final float POINT_RADIUS = 20.0f;
    private static final int POINT_SIZE = 50;
    private static final int TOUCH_SLOP = 20;

    private final int mMaxMeasuringPointNum;

    private Paint mPointPaint;
    private Paint mSelectedPointPaint;

    private ArrayList<MeasuredPoint> mUsingPoints;
    private ArrayList<MeasuredPoint> mUsablePoints;
    private ArrayList<MeasuredPoint> mSelectedPoints;

    private int mLastMotionX, mLastMotionY; 
    private boolean isMoved;
    private boolean isReleased;
    private int mCounter;
    private Runnable mLongPressRunnable;
    private Handler mHandler;
    private boolean mOperationMode;

    private MeasuredPoint mSelectedPoint;

    public MeasuringPointMode(Context context, int maxMeasuringPoint) {
        super(context);
        mMaxMeasuringPointNum = maxMeasuringPoint;

        mPointPaint = new Paint();
        mPointPaint.setColor(POINT_COLOR);
        mSelectedPointPaint = new Paint();
        mSelectedPointPaint.setColor(Color.YELLOW);

        mUsingPoints = new ArrayList<MeasuringPointMode.MeasuredPoint>();
        mUsablePoints = new ArrayList<MeasuringPointMode.MeasuredPoint>();
        mSelectedPoints = new ArrayList<MeasuringPointMode.MeasuredPoint>();

        mHandler = new Handler();
        mLongPressRunnable = new Runnable() {  

            @Override
            public void run() {
                --mCounter;
                if (mCounter > 0 || isReleased || isMoved) {
                    return;
                }
                performMeasuringObjectSelected();
                mOperationMode = true;
            }
        }; 
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
            if(mSelectedPoint != null) {
                mSelectedPoint.setPosition(x, y);

                isReleased = false;
                isMoved = false;
                if (!mOperationMode) {
                    mCounter++;
                    mHandler.postDelayed(mLongPressRunnable,
                            ViewConfiguration.getLongPressTimeout());
                }
            }
            break;

        case MotionEvent.ACTION_MOVE:
            if(mSelectedPoint != null) {
                mSelectedPoint.setPosition(x, y);
            }
            if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                    || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                isMoved = true;
            }
            break;

        case MotionEvent.ACTION_UP:
            if(mSelectedPoint != null) {
                if(!isMoved && mOperationMode) {
                    mSelectedPoints.add(mSelectedPoint);
                    mSelectedPoint.setPosition(mLastMotionX, mLastMotionY);
                } else if(isMoved){
                    notifyPointPositionChange(mSelectedPoint);
                }
                mSelectedPoint = null;
            } else {
                if(!mOperationMode) {
                    MeasuredPoint point = getMeasuredPoint();
                    if(point != null) {
                        point.setPosition(x, y);
                        notifyPointPositionChange(point);
                    }
                }
            }

            isReleased = true;
            break;
        }
        view.invalidate();
        return true;
    }

    private void notifyPointPositionChange(MeasuredPoint point) {
        int index = point.mMeasuredPointIndex;
        Point thermalPosition = converToRealObjectImagePosition(point.mPosition);
    }

    @Override
    public void drawOnView(Canvas canvas) {
        for(MeasuredPoint point : mUsingPoints) {
            if(!mSelectedPoints.contains(point)){
                point.draw(canvas);
            } else {
                point.drawSelected(canvas);
            }
        }
        if(mSelectedPoint != null) {
            mSelectedPoint.drawSelected(canvas);
        }
    }

    private MeasuredPoint selectUsingPoint(int x, int y) {
        MeasuredPoint selectedPoint = null;
        int distance = POINT_SIZE;
        for(MeasuredPoint point : mUsingPoints) {
            if(point.distanceTo(x, y) < POINT_SIZE && point.distanceTo(x, y) < distance) {
                distance = point.distanceTo(x, y);
                selectedPoint = point;
            }
        }
        return selectedPoint;
    }
    
    private MeasuredPoint getMeasuredPoint() {
        if(mUsingPoints.size() >= mMaxMeasuringPointNum) {
            return null;
        }
        MeasuredPoint point;
        if(!mUsablePoints.isEmpty()){
            point = mUsablePoints.get(0);
            mUsablePoints.remove(0);
        } else {
            point = new MeasuredPoint();
            point.mMeasuredPointIndex = mUsingPoints.size();
        }

        mUsingPoints.add(point);
        return point;
    }

    private class MeasuredPoint {
        public int mMeasuredPointIndex;
        public Point mPosition;

        public MeasuredPoint() {
            mPosition = new Point();
        }

        public void setPosition(int x, int y){
            mPosition.set(x, y);
        }

        public void draw(Canvas canvas) {
            canvas.drawCircle(mPosition.x, mPosition.y, POINT_RADIUS, mPointPaint);
        }

        public void drawSelected(Canvas canvas) {
            canvas.drawCircle(mPosition.x, mPosition.y, POINT_RADIUS, mSelectedPointPaint);
        }

        public int distanceTo(int x, int y) {
            return (int)Math.sqrt((x - mPosition.x) * (x - mPosition.x)
                    + (y - mPosition.y) * (y - mPosition.y));
        }

        public void recycle() {
            mUsablePoints.remove(this);
            mUsablePoints.add(this);
        }
    }

    @Override
    public void clearSelectedMeasuringObjects() {
        Log.i(TAG, "clearSelectedMeasuringObjects");
        for(MeasuredPoint point : mSelectedPoints) {
            mUsingPoints.remove(point);
            point.recycle();
        }
        int size = mSelectedPoints.size();
        for(int i = 0; i < size; ++ i) {
            mSelectedPoints.remove(0);
        }
    }

    @Override
    public void selectAllMeasuringObjects() {
        for(MeasuredPoint point : mUsingPoints) {
            if (!mSelectedPoints.contains(point)) {
                mSelectedPoints.add(point);
            }
        }
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        int size = mSelectedPoints.size();
        for(int i = 0; i < size; ++ i) {
            mSelectedPoints.remove(0);
        }
        mOperationMode = false;
    }

    @Override
    public void drawOnRealWorldObjectImage(Canvas canvas) {
        // TODO Auto-generated method stub
        
    }
}

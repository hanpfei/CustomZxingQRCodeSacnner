package com.wolfcs.qrcodescanner.measurement;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;

public abstract class MeasuringMode implements OnTouchListener{
    private static final String TAG = "TempMeasureMode";

    protected static final int MOVE_THREASHOLD = 5;

    protected final MeasuringObjectsManager mObjectsManager;

    private final Context mContext;
    private final String mCeliusStr;

    private int mRealWorldObjectImageWidth = 240;
    private int mRealWorldObjectImageHeight = 320;

    private int mViewWidth;
    private int mViewHeight;
    public MeasuringMode(Context context, MeasuringObjectsManager objectsManager){
        mContext = context;
        mCeliusStr = mContext.getResources().getString(R.string.celcius);
        mObjectsManager = objectsManager;
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getCeliusStr() {
        return mCeliusStr;
    }

    public abstract void drawOnView(Canvas canvas);

    public abstract void clearSelectedMeasuringObjects();

    public abstract void selectAllMeasuringObjects();

    public abstract void cancelOpOnMeasuringObjects();

    public interface MeasurementOperationListener {
        public void onMeasuredObjectsSelected();
        public void onMeasuredObjectsDeSelected();
    }

    private MeasurementOperationListener mOperationListener;

    public void registerOperationListener(MeasurementOperationListener listenner) {
        mOperationListener = listenner;
    }

    protected void performMeasuringObjectsSelected() {
        if(mOperationListener != null) {
            mOperationListener.onMeasuredObjectsSelected();
        }
    }

    protected void performMeasuringObjectsDeSelected() {
        if (mOperationListener != null) {
            mOperationListener.onMeasuredObjectsDeSelected();
        }
    }

    protected Point converToRealObjectImagePosition(Point viewPosition) {
        Point thermalPosition = new Point();
        int x = (int)(viewPosition.x * mRealWorldObjectImageWidth / mViewWidth);
        int y = (int)(viewPosition.y * mRealWorldObjectImageHeight / mViewHeight);
        thermalPosition.set(x, y);
        return thermalPosition;
    }

    protected void updateViewDimension(View view) {
        if (mViewWidth == 0 || mViewHeight == 0) {
            mViewWidth = view.getWidth();
            mViewHeight = view.getHeight();
        }
    }

    protected int getWidth() {
        return mViewWidth;
    }

    protected int getHeight() {
        return mViewHeight;
    }

    // Should be implemented in subclass if long click check is needed
    protected void onLongPress() {
    }

    private Runnable mCheckForLongPress = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mCheckForLongPress ");
            onLongPress();
        }
    };

    protected void startLongPressCheck(View view) {
        view.postDelayed(mCheckForLongPress, 
                ViewConfiguration.getLongPressTimeout());
        view.postInvalidateDelayed(ViewConfiguration.getLongPressTimeout() + 10);
    }
    
    protected void cancelLongPressCheck(View view) {
        view.removeCallbacks(mCheckForLongPress);
    }
}

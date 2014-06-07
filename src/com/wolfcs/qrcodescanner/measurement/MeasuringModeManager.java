package com.wolfcs.qrcodescanner.measurement;

import com.wolfcs.qrcodescanner.R;
import com.wolfcs.qrcodescanner.measurement.MeasuringMode.MeasurementOperationListener;
import com.wolfcs.qrcodescanner.measurement.MeasuringRectMode.MeasuredRect;
import com.wolfcs.qrcodescanner.measurement.MeasuringRectMode.MeasuredRectUpdateListener;
import com.wolfcs.qrcodescanner.widget.TouchEventReceiverView;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MeasuringModeManager implements OnClickListener,
        MeasuredRectUpdateListener {
    private static final String TAG = "MeasureTempManager";
    private static final boolean DEBG = true;

    private static final int MEASURE_TEMP_MODE_NULL_INDEX = 0;
    private static final int MEASURE_TEMP_MODE_POINT_INDEX = 1;
    private static final int MEASURE_TEMP_MODE_LINE_INDEX = 2;
    private static final int MEASURE_TEMP_MODE_RECT_INDEX = 3;

    private Context mContext;
    private MeasurementOperationListener mListener;
    private TouchEventReceiverView mRectView;

    private int[] mMeasureTempModeDrawableIds = {
    };

    private MeasuringNullMode mNullMode;
    private MeasuringPointMode mPointMode;
    private MeasuringLineMode mLineMode;
    private MeasuringRectMode mRectMode;

    private int mCurrentMeasuringModeIndex;
    private MeasuringMode mCurrentMeasuringMode;

    public MeasuringModeManager(Context context, MeasurementOperationListener listener){
        mContext = context;
        mListener = listener;
    }

    public void init(TouchEventReceiverView rectView, ImageView view) {
        mRectView = rectView;
        mRectView.setMeasureTempManager(this);

        mNullMode = new MeasuringNullMode(mContext);
        mPointMode = new MeasuringPointMode(mContext, 8);
        mPointMode.registerOperationListener(mListener);

        mLineMode = new MeasuringLineMode(mContext, 1);
        mLineMode.registerOperationListener(mListener);

        mRectMode = new MeasuringRectMode(mContext, 8);
        mRectMode.registerOperationListener(mListener);
        mRectMode.setOnMeasuredRectUpdateListener(this);

        mCurrentMeasuringModeIndex = MEASURE_TEMP_MODE_NULL_INDEX;
        selectMeasureTempMode(mCurrentMeasuringModeIndex, view);
    }

    private void onDeleteClick(View view) {
        if(mCurrentMeasuringMode != null) {
            mCurrentMeasuringMode.clearSelectedMeasuringObjects();
            mRectView.invalidate();
        }
    }
    
    private void onSelectAllClick(View view) {
        if(mCurrentMeasuringMode != null) {
            mCurrentMeasuringMode.selectAllMeasuringObjects();
            mRectView.invalidate();
        }
    }
    
    private void onTempMeasureSwitcherClick(View view) {
        if (!(view instanceof ImageView)) {
            return;
        }
        ++ mCurrentMeasuringModeIndex;
        if (mCurrentMeasuringModeIndex == mMeasureTempModeDrawableIds.length) {
            mCurrentMeasuringModeIndex = 0;
        }
        selectMeasureTempMode(mCurrentMeasuringModeIndex, view);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
        }
    }

    private void selectMeasureTempMode(int modeIndex, View view) {
        ImageView imageView = (ImageView)view;
        imageView.setImageResource(mMeasureTempModeDrawableIds[mCurrentMeasuringModeIndex]);

        MeasuringMode measureMode = null;
        switch (modeIndex) {
        case MEASURE_TEMP_MODE_NULL_INDEX:
            measureMode = mNullMode;
            break;

        case MEASURE_TEMP_MODE_POINT_INDEX:
            measureMode = mPointMode;
            break;

        case MEASURE_TEMP_MODE_LINE_INDEX:
            measureMode = mLineMode;
            break;

        case MEASURE_TEMP_MODE_RECT_INDEX:
            measureMode = mRectMode;
            break;

        default:
            break;
        }
        mCurrentMeasuringMode = measureMode;
        mRectView.setOnTouchListener(measureMode);
    }

    public void drawOnView(Canvas canvas) {
        mPointMode.drawOnView(canvas);
        mLineMode.drawOnView(canvas);
        mRectMode.drawOnView(canvas);
    }

    public void drawOnRealWorldObjectImage(Canvas canvas) {
        mPointMode.drawOnRealWorldObjectImage(canvas);
        mLineMode.drawOnRealWorldObjectImage(canvas);
        mRectMode.drawOnRealWorldObjectImage(canvas);
    }

    public void cancelOpOnMeasuringObjects() {
        mCurrentMeasuringMode.cancelOpOnMeasuringObjects();
        mRectView.invalidate();
    }


    @Override
    public void onMeasuredRectUpdate(MeasuredRect measuredRect) {
        if (DEBG) Log.i(TAG, "onMeasuredRectUpdate");
        if(!measuredRect.isDrop()) {
            // Do something.
        }
    }
}

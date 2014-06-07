package com.wolfcs.qrcodescanner.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.wolfcs.qrcodescanner.measurement.MeasuringModeManager;

public class TouchEventReceiverView extends View {
	public TouchEventReceiverView(Context context) {
        super(context);
    }

    public TouchEventReceiverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchEventReceiverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mManager != null) {
            mManager.drawOnView(canvas);
        }
    }


    private MeasuringModeManager mManager;
    public void setMeasureTempManager(MeasuringModeManager manager) {
        mManager = manager;
    }
}

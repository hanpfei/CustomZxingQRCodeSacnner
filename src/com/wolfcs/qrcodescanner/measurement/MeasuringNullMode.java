package com.wolfcs.qrcodescanner.measurement;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class MeasuringNullMode extends MeasuringMode {
    public MeasuringNullMode(Context context, MeasuringObjectsManager objectManager) {
        super(context, objectManager);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        updateViewDimension(view);
        return false;
    }

    @Override
    public void drawOnView(Canvas canvas) {

    }

    @Override
    public void clearSelectedMeasuringObjects() {
        
    }

    @Override
    public void selectAllMeasuringObjects() {
        
    }

    @Override
    public void cancelOpOnMeasuringObjects() {
        
    }
}

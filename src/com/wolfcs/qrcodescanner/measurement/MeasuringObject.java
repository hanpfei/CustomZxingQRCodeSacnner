package com.wolfcs.qrcodescanner.measurement;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.graphics.Canvas;

public abstract class MeasuringObject {
    private Context mContext;
    private final String mCeliusStr;

    public MeasuringObject(Context context) {
        mContext = context;
        mCeliusStr = mContext.getResources().getString(R.string.celcius);
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getCeliusStr() {
        return mCeliusStr;
    }

    public abstract void move(float dx, float dy);

    public abstract void drawOnView(Canvas canvas);

    public abstract void drawSelectedOnView(Canvas canvas);

    public abstract void drawOperatingOnView(Canvas canvas);

    public abstract void drawOnThermalImage(Canvas canvas);
    
    public abstract boolean containOnViewPoint(float xPosition, float yPosition);
}

package com.wolfcs.qrcodescanner.measurement;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;

public abstract class MeasuringObject {
    private Context mContext;
    private final String mCeliusStr;

    private final int mViewWidth;
    private final int mViewHeight;

    public MeasuringObject(Context context, int width, int height) {
        mContext = context;
        mCeliusStr = mContext.getResources().getString(R.string.celcius);
        mViewWidth = width;
        mViewHeight = height;
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getCeliusStr() {
        return mCeliusStr;
    }

    protected int getWidth() {
        return mViewWidth;
    }

    protected int getHeight() {
        return mViewHeight;
    }

    public abstract void move(float dx, float dy);

    public abstract void drawOnView(Canvas canvas);

    public abstract void drawSelectedOnView(Canvas canvas);

    public abstract void drawOperatedObjectOnView(Canvas canvas);

    public abstract void drawOnRealWorldObjectImage(Canvas canvas);

    public boolean containOnViewPoint(Point point) {
        return containOnViewPoint(point.x, point.y);
    }

    public abstract boolean containOnViewPoint(float xPosition, float yPosition);
}

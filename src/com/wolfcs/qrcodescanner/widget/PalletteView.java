package com.wolfcs.qrcodescanner.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class PalletteView extends View implements OnClickListener {
    private static final String TAG = "PalletteView";
    private static final boolean DEBUG = true;

    public static final int PALLETTE_LENGTH = 256;

    private Paint mPaint;
    public interface PalletteChangeListener {
        public void onPalletteChange();
    }

    private PalletteChangeListener mListener;
    private int[] mPallette;

    public PalletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PalletteView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        setOnClickListener(this);
        mPallette = new int[PALLETTE_LENGTH];
        for (int i = 0; i < PALLETTE_LENGTH; ++i) {
            mPallette[i] = ((PALLETTE_LENGTH - 1 - i) << 8) + i + 0xFF000000;
        }
    }

    public void setPallette(int[] pallette) {
        if (pallette == null || pallette.length != PALLETTE_LENGTH) {
            throw new IllegalArgumentException("Invalid pallette!");
        }
        mPallette = new int[PALLETTE_LENGTH];
        System.arraycopy(pallette, 0, mPallette, 0, PALLETTE_LENGTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (DEBUG) Log.i(TAG, "width = " + viewWidth + " height = " + viewHeight);

        int stepWidth = viewWidth / PALLETTE_LENGTH;
        int adjustSteps = 8;

        float left = 0;
        for (int i = 0; i < PALLETTE_LENGTH; ++i) {
            int color = mPallette[i];
            mPaint.setColor(color);

            float width = 0.0f;
            if ((i+1) % adjustSteps == 0) {
                width = viewWidth * (i + 1) / PALLETTE_LENGTH - left;
            } else {
                width = stepWidth;
            }

            canvas.drawRect(left, 0, left + width, viewHeight, mPaint);
            left += width;
        }
    }

    public void setPalletteChangeListener(PalletteChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onPalletteChange();
        }
        invalidate();
    }
}

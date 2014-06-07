package com.wolfcs.qrcodescanner.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.wolfcs.qrcodescanner.R;

public class ModeSwitcher extends View implements OnTouchListener {
    private static final String TAG = "ModeSwitcher";

    public static final int CAMERA_MODE_INDEX = 0;
    private static final int MIN_MODE_INDEX = CAMERA_MODE_INDEX;
    public static final int MEASURE_MODE_INDEX = 1;
    public static final int SCAN_MODE_INDEX = 2;
    private static final int MAX_MODE_INDEX = SCAN_MODE_INDEX;

    private static final int[] MODE_LABEL_IDS = { 
        R.string.camera_mode,
        R.string.scan_mode, 
    };

    public interface ModeSwitchListener {
        public void onModeSelected(int i);
    }

    private ModeSwitchListener mSwitchListener;
    private int mModeLabelWidths[] = new int[MAX_MODE_INDEX + 1];

    private int mLabelHoriInterval = 25;
    private int mLabelVertInterval = 1;
    private float mTextSize = 45;
    private Paint mTextPaint;
    private int mUnSelectedModeLabelColor = Color.WHITE;
    private int mSelectedModeLabelColor = Color.YELLOW;

    private int mCurrentMode = 0;
    private GestureDetector mGestureDetector;
    private boolean mSwitchEnabled = true;

    public ModeSwitcher(Context context) {
        super(context);
        init();
    }
    
    public ModeSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModeSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        for (int i = 0; i < MODE_LABEL_IDS.length; ++ i) {
            String text = getContext().getString(MODE_LABEL_IDS[i]);
            Rect rect = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), rect);
            mModeLabelWidths[i] = rect.width();
        }

        setLongClickable(true);
        setOnTouchListener(this);
        mGestureDetector = new GestureDetector(getContext(),
                new SwitcherGestureListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = mModeLabelWidths[mCurrentMode];
        int measuredHeight;
        int leftWidth = 0, rightWidth = 0;
        if (mCurrentMode > MIN_MODE_INDEX) {
            leftWidth = mModeLabelWidths[mCurrentMode - 1];
        }

        if (mCurrentMode < MAX_MODE_INDEX) {
            rightWidth = mModeLabelWidths[mCurrentMode + 1];
        }

        measuredWidth += Math.max(leftWidth, rightWidth) * 2;
        measuredWidth += mLabelHoriInterval * 2;

        int radius = 10;
        FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        measuredHeight = radius * 2 + mLabelVertInterval + (int)textHeight;

        measuredHeight = Math.max(measuredHeight, getMeasuredHeight());

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int viewWidth = getWidth();

        int radius = 10;
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(viewWidth / 2, radius, radius, mTextPaint);

        FontMetrics fontMetrics = mTextPaint.getFontMetrics();

        // Draw text of current mode.
        String text = getContext().getString(MODE_LABEL_IDS[mCurrentMode]);
        float startX = viewWidth / 2 - mModeLabelWidths[mCurrentMode] / 2;
        float startY = radius * 2 + mLabelVertInterval - fontMetrics.top;
        mTextPaint.setColor(mSelectedModeLabelColor);
        canvas.drawText(text, startX, startY, mTextPaint);

        // Draw left text.
        if (mCurrentMode > MIN_MODE_INDEX) {
            text = getContext().getString(MODE_LABEL_IDS[mCurrentMode - 1]);
            startX = viewWidth / 2 - mModeLabelWidths[mCurrentMode] / 2;
            startX -= mLabelHoriInterval;
            startX -= mModeLabelWidths[mCurrentMode - 1];
            mTextPaint.setColor(mUnSelectedModeLabelColor);
            canvas.drawText(text, startX, startY, mTextPaint);
        }

        // Draw right text.
        if (mCurrentMode < SCAN_MODE_INDEX) {
            text = getContext().getString(MODE_LABEL_IDS[mCurrentMode + 1]);
            startX = viewWidth / 2 + mModeLabelWidths[mCurrentMode] / 2;
            startX += mLabelHoriInterval;
            mTextPaint.setColor(mUnSelectedModeLabelColor);
            canvas.drawText(text, startX, startY, mTextPaint);
        }
    }

    public void setModeSwitchListener(ModeSwitchListener switchListener) {
        mSwitchListener = switchListener;
    }

    private void switchToPreviousMode() {
        if (mCurrentMode == MIN_MODE_INDEX) {
            return;
        }

        --mCurrentMode;
        requestLayout();
        if (mSwitchListener != null) {
            mSwitchListener.onModeSelected(mCurrentMode);
        }

    }

    private void switchToNextMode() {
        if (mCurrentMode == MAX_MODE_INDEX) {
            return;
        }
        ++ mCurrentMode;
        requestLayout();
        if (mSwitchListener != null) {
            mSwitchListener.onModeSelected(mCurrentMode);
        }
    }

    private class SwitcherGestureListener implements OnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                float velocityX, float velocityY) {
            Log.i(TAG, "velocityX = " + velocityX + ", velocityY = " + velocityY);
            if (!mSwitchEnabled){
                return false;
            }
            if (velocityX > 0) {
                switchToPreviousMode();
            } else if (velocityX < 0) {
                switchToNextMode();
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {

        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2,
                float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent event) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return false;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void setSwitchEnabled(boolean enabled) {
        mSwitchEnabled = enabled;
    }
}

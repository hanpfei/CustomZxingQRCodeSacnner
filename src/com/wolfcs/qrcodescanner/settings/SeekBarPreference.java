package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements
        SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "SeekBarPreference";
    private static final int SEEK_BAR_MAX_VALUE = 100;

    private SeekBar mSeekbar;
    private TextView mSeekbarLabel;
    private TextView mSeekbarValue;
    private String mUnit;

    private float mDefault;
    private float mMinValue;
    private float mMaxValue;

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.wolfcs);
        mDefault = arr.getFloat(R.styleable.wolfcs_defaultValue, 0);
        mMaxValue = arr.getFloat(R.styleable.wolfcs_maxValue, 100);
        mMinValue = arr.getFloat(R.styleable.wolfcs_minValue, 100);
        mUnit = arr.getString(R.styleable.wolfcs_unit);

        arr.recycle();
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateDialogView() {
        return View.inflate(getContext(), R.layout.preference_dialog_seek, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekbar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekbar.setMax(SEEK_BAR_MAX_VALUE);
        mSeekbar.setProgress(getSeekbarProgress());
        mSeekbar.setOnSeekBarChangeListener(this);

        mSeekbarLabel = (TextView) view.findViewById(R.id.seekvalue_label);
        mSeekbarLabel.setText(getTitle() + ": ");

        mSeekbarValue = (TextView) view.findViewById(R.id.pref_seekvalue);
        mSeekbarValue.setText(getValueLabel(getValue()));
    }

    private int getSeekbarProgress() {
        float value = getValue();
        int progress = (int) (value / (mMaxValue - mMinValue)
                * SEEK_BAR_MAX_VALUE + mMinValue);
        return progress;
    }

    private float getValue() {
        return getSharedPreferences().getFloat(getKey(), mDefault);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            saveValue(mSeekbar.getProgress());
        } else {
            Log.i(TAG, "You click negative button");
        }
    }

    private float getValueFromProgress(int progress) {
        float value = ((float) progress) / SEEK_BAR_MAX_VALUE
                * (mMaxValue - mMinValue);
        value += mMinValue;
        return value;
    }

    private void saveValue(int progress) {
        float value = getValueFromProgress(progress);
        getEditor().putFloat(getKey(), value).commit();
        notifyChanged();
    }

    private String getValueLabel(float value) {
        String str = String.valueOf(value);
        if (mUnit != null) {
            str = str + mUnit;
        }
        return str;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        float value = getValueFromProgress(progress);
        String str = getValueLabel(value);
        mSeekbarValue.setText(str);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        
    }
}

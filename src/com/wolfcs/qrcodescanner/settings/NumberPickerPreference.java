package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {
    private static final int DEFAULT_MINIMUM_VALUE = 0;
    private static final int DEFAULT_MAXIMUM_VALUE = 0;

    private NumberPicker mNumberPicker;
    private int mMinimumValue;
    private int mMaximumValue;
    private int mDefaultValue;
    private String mUnit;

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.wolfcs);
        mMinimumValue = arr.getInt(R.styleable.wolfcs_MinValue, DEFAULT_MINIMUM_VALUE);
        mMaximumValue = arr.getInt(R.styleable.wolfcs_MaxValue, DEFAULT_MAXIMUM_VALUE);
        mDefaultValue = arr.getInt(R.styleable.wolfcs_DefaultValue, mMinimumValue);
        mUnit = arr.getString(R.styleable.wolfcs_unit);

        arr.recycle();
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public NumberPickerPreference(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateDialogView() {
        return View.inflate(getContext(), R.layout.preference_dialog_numberpicker, null);
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinimumValue);
        mNumberPicker.setMaxValue(mMaximumValue);
        int value = getSharedPreferences().getInt(getKey(), mDefaultValue);
        getEditor().putInt(getKey(), value).commit();
        mNumberPicker.setValue(value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int value = mNumberPicker.getValue();
            if (callChangeListener(value)) {
                // To save value;
                saveValue(value);
            }
        }
    }
    
    private void saveValue(int value) {
        getEditor().putInt(getKey(), value).commit();
        notifyChanged();
    }
    
    public int getValue() {
        return getSharedPreferences().getInt(getKey(), mDefaultValue);
    }
    
    public String getUnit() {
        return mUnit;
    }
}

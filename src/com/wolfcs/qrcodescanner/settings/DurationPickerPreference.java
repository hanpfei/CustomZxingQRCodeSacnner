package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class DurationPickerPreference extends DialogPreference {
    private NumberPicker mMinPicker;
    private NumberPicker mSecondPicker;
    private NumberPicker mMsPicker;

    public DurationPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DurationPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public DurationPickerPreference(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateDialogView() {
        return View.inflate(getContext(), R.layout.preference_dialog_durationpicker, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        mMinPicker = (NumberPicker) view.findViewById(R.id.minutes_picker);
        mMinPicker.setMinValue(0);
        mMinPicker.setMaxValue(500);
        mSecondPicker = (NumberPicker) view.findViewById(R.id.seconds_picker);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mMsPicker = (NumberPicker) view.findViewById(R.id.milliseconds_picker);
        mMsPicker.setMinValue(200);
        mMsPicker.setMaxValue(999);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int minutes = mMinPicker.getValue();
            int seconds = mSecondPicker.getValue();
            int milliseconds = mMsPicker.getValue();
            int value = (minutes * 60 + seconds) * 1000 + milliseconds;
            if (callChangeListener(value)) {
                saveValue(value);
            }
        }
    }

    private void saveValue(int value) {
        getEditor().putInt(getKey(), value).commit();
        notifyChanged();
    }
}

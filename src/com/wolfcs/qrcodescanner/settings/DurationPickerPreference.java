package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class DurationPickerPreference extends DialogPreference implements OnValueChangeListener{
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 0;

    private static final int MINUTES_NATURAL_MAX = 999;
    private static final int SECONDS_NATURAL_MAX = 59;
    private static final int MILLIS_NATURAL_MAX = 999;

    private static final int NATURAL_MIN_VALUE = DEFAULT_MIN_VALUE;
    private static final int NATURAL_MAX_VALUE = ((MINUTES_NATURAL_MAX + 1) * 60 
            + SECONDS_NATURAL_MAX) * 1000 + MILLIS_NATURAL_MAX;

    private NumberPicker mMinPicker;
    private NumberPicker mSecondPicker;
    private NumberPicker mMillisPicker;

    private final int mMinimumValue;
    private final int mMaximumValue;
    private final int mDefaultValue;

    public DurationPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.wolfcs);
        int minValue = arr.getInt(R.styleable.wolfcs_MinValue, DEFAULT_MIN_VALUE);
        if (minValue == DEFAULT_MIN_VALUE) {
            minValue = NATURAL_MIN_VALUE;
        }
        int maxValue = arr.getInt(R.styleable.wolfcs_MaxValue, DEFAULT_MAX_VALUE);
        if (maxValue == DEFAULT_MAX_VALUE) {
            minValue = NATURAL_MAX_VALUE;
        }
        mMinimumValue = minValue;
        mMaximumValue = maxValue;
        mDefaultValue = arr.getInt(R.styleable.wolfcs_DefaultValue, mMinimumValue);

        arr.recycle();
    }

    public DurationPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public DurationPickerPreference(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateDialogView() {
        return View.inflate(getContext(), R.layout.preference_dialog_durationpicker, null);
    }

    public int getMinutesInMillis(long millis) {
        return (int) (millis / (60 * 1000));
    }

    public int getSecondsInMillis(long millis) {
        return (int) ((millis % (60 * 1000)) / 1000);
    }

    public int getMillisInMillis(long millis) {
        return (int) (millis % 1000);
    }

    @Override
    protected void onBindDialogView(View view) {
        mMinPicker = (NumberPicker) view.findViewById(R.id.minutes_picker);
        mMinPicker.setOnValueChangedListener(this);
        mMinPicker.setMinValue(0);
        mSecondPicker = (NumberPicker) view.findViewById(R.id.seconds_picker);
        mSecondPicker.setOnValueChangedListener(this);
        mSecondPicker.setMinValue(0);
        mMillisPicker = (NumberPicker) view.findViewById(R.id.milliseconds_picker);
        mMillisPicker.setOnValueChangedListener(this);
        mMillisPicker.setMinValue(0);
        mMinPicker.setMaxValue(MINUTES_NATURAL_MAX);
        mSecondPicker.setMaxValue(SECONDS_NATURAL_MAX);
        mMillisPicker.setMaxValue(MILLIS_NATURAL_MAX);
        if (mMaximumValue != 0) {
            int maxMinutes = getMinutesInMillis(mMaximumValue);
            int maxSeconds = getSecondsInMillis(mMaximumValue);
            int maxMillis = getMillisInMillis(mMaximumValue);

            if (maxMinutes != 0) {
                mMinPicker.setMaxValue(maxMinutes);
            }else if (maxSeconds != 0) {
                mMinPicker.setMaxValue(0);
                mSecondPicker.setMaxValue(maxSeconds);
            } else {
                mMinPicker.setMaxValue(0);
                mSecondPicker.setMaxValue(0);
                mMillisPicker.setMaxValue(maxMillis);
                int minMillis = getMillisInMillis(mMinimumValue);
                mMillisPicker.setMinValue(minMillis);
            }
        }

        int value = getSharedPreferences().getInt(getKey(), mDefaultValue);
        int minutes = getMinutesInMillis(value);
        int seconds = getSecondsInMillis(value);
        int millis = getMillisInMillis(value);
        mMinPicker.setValue(minutes);
        mSecondPicker.setValue(seconds);
        mMillisPicker.setValue(millis);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int minutes = mMinPicker.getValue();
            int seconds = mSecondPicker.getValue();
            int milliseconds = mMillisPicker.getValue();
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

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (mMaximumValue == 0 && mMinimumValue == 0) return;
        if (picker == mSecondPicker) {
            int minutes = mMinPicker.getValue();
            int leftMillis = mMaximumValue - minutes * 60 * 1000;
            int maxSeconds = leftMillis / 1000;
            if (maxSeconds > SECONDS_NATURAL_MAX) {
                maxSeconds = SECONDS_NATURAL_MAX;
            }

            int minSeconds;
            leftMillis = mMinimumValue - minutes * 60 * 1000;
            if (leftMillis <= 0) {
                minSeconds = 0;
            } else {
                minSeconds = leftMillis / 1000;
            }
            if (minSeconds == maxSeconds) {
                minSeconds = minSeconds == 0 ? minSeconds : minSeconds - 1;
                maxSeconds = maxSeconds == SECONDS_NATURAL_MAX ? maxSeconds : maxSeconds + 1;
            }

            mSecondPicker.setMaxValue(maxSeconds);
            mSecondPicker.setMinValue(minSeconds);
            if (newVal >= minSeconds && newVal <= maxSeconds) {
                return;
            } else {
                mSecondPicker.setValue(oldVal);
            }
        } else if(picker == mMillisPicker) {
            int minutes = mMinPicker.getValue();
            int seconds = mSecondPicker.getValue();
            int leftMillis = mMaximumValue - (minutes * 60 + seconds) * 1000;
            int maxMillis = leftMillis;
            if (maxMillis > SECONDS_NATURAL_MAX) {
                maxMillis = MILLIS_NATURAL_MAX;
            }

            leftMillis = mMinimumValue - (minutes * 60 + seconds) * 1000;
            int minMillis;
            if (leftMillis < 0) {
                minMillis = 0;
            } else {
                minMillis = leftMillis;
            }
            if (minMillis == maxMillis) {
                minMillis = minMillis == 0 ? minMillis : minMillis - 1;
                maxMillis = maxMillis == MILLIS_NATURAL_MAX ? maxMillis : maxMillis + 1;
            }
            mMillisPicker.setMinValue(minMillis);
            mMillisPicker.setMaxValue(maxMillis);
            if (newVal >= minMillis && newVal <= maxMillis) {
                return;
            } else {
                mMillisPicker.setValue(oldVal);
            }
        }
    }

    public int getValue() {
        return getSharedPreferences().getInt(getKey(), mDefaultValue);
    }
}

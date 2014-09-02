package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class FloatNumberEditorPreference extends DialogPreference {
    private static final String TAG = "";
    private static final float DEFAULT_MINIMUM_VALUE = 0.0f;
    private static final float DEFAULT_MAXIMUM_VALUE = 0.0f;

    private EditText mEditText;
    private float mMinimumValue;
    private float mMaximumValue;
    private float mDefaultValue;
    private String mUnit;

    public FloatNumberEditorPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.wolfcs);
        mMinimumValue = arr.getFloat(R.styleable.wolfcs_minValue, DEFAULT_MINIMUM_VALUE);
        mMaximumValue = arr.getFloat(R.styleable.wolfcs_maxValue, DEFAULT_MAXIMUM_VALUE);
        mDefaultValue = arr.getFloat(R.styleable.wolfcs_defaultValue, mMinimumValue);
        mUnit = arr.getString(R.styleable.wolfcs_unit);
    }

    public FloatNumberEditorPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public FloatNumberEditorPreference(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateDialogView() {
        return View.inflate(getContext(), R.layout.preference_dialog_numbereditor, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEditText = (EditText)view.findViewById(R.id.number_editor);
        float value = getSharedPreferences().getFloat(getKey(), mDefaultValue);
        getEditor().putFloat(getKey(), value).commit();
        mEditText.setText(String.valueOf(value));
        mEditText.setSelection(String.valueOf(value).length());

        mEditText.setFilters(new InputFilter[] {mInputFilter});
        mEditText.addTextChangedListener(mTextWatcher);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String valueStr = s.toString();
            int dotIndex = valueStr.lastIndexOf('.');
            int stringLen = valueStr.length();
            if (dotIndex > 0 && dotIndex < stringLen - 3) {
                s.replace(dotIndex + 3, valueStr.length(), "");
            }
        }
    };

    private InputFilter mInputFilter = new InputFilter () {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            float value = mMinimumValue - 1;

            boolean number = source.toString().matches("^[0-9.]*$");
            if (!number) {
                return "";
            }
            try {
                if (dest.toString().contains(".") && source.subSequence(start, end).equals(".")){
                    return "";
                }
                StringBuilder valueStr = new StringBuilder(dest);
                valueStr.replace(dstart, dend, source.toString());
                value = Float.parseFloat(valueStr.toString());
            } catch (Exception e) {
                Log.w(TAG, "parseInt failed: string = " + dest.toString() + source.toString());
            }

            if (value < mMinimumValue) {
                return "";
            }

            if (value > mMaximumValue) {
                return "";
            }

            return source;
        }
        
    };

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        String text = mEditText.getText().toString();
        if (text.equals("")) {
            return;
        }
        if (positiveResult) {
            float value = Float.valueOf(text);
            if (callChangeListener(value)) {
                // To save value;
                saveValue(value);
            }
        }
    }

    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    private void saveValue(float value) {
        getEditor().putFloat(getKey(), value).commit();
        notifyChanged();
    }

    public float getValue() {
        return getSharedPreferences().getFloat(getKey(), mDefaultValue);
    }

    public String getUnit() {
        return mUnit;
    }
}

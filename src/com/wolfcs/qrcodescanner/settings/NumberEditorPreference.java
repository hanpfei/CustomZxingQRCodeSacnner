package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class NumberEditorPreference extends DialogPreference {
    private static final String TAG = "";
    private static final int DEFAULT_MINIMUM_VALUE = 0;
    private static final int DEFAULT_MAXIMUM_VALUE = 0;

    private EditText mEditText;
    private int mMinimumValue;
    private int mMaximumValue;
    private int mDefaultValue;
    private String mUnit;

    public NumberEditorPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.wolfcs);
        mMinimumValue = arr.getInt(R.styleable.wolfcs_MinValue, DEFAULT_MINIMUM_VALUE);
        mMaximumValue = arr.getInt(R.styleable.wolfcs_MaxValue, DEFAULT_MAXIMUM_VALUE);
        mDefaultValue = arr.getInt(R.styleable.wolfcs_DefaultValue, mMinimumValue);
        mUnit = arr.getString(R.styleable.wolfcs_unit);
    }

    public NumberEditorPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public NumberEditorPreference(Context context) {
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
        int value = getSharedPreferences().getInt(getKey(), mDefaultValue);
        getEditor().putInt(getKey(), value).commit();
        mEditText.setText(String.valueOf(value));
        mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER
                | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
        mEditText.setFilters(new InputFilter[] { mInputFilter });
        mEditText.setSelection(String.valueOf(value).length());
    }

    private InputFilter mInputFilter = new InputFilter () {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            String text = mEditText.getText().toString();
            int value = mMinimumValue - 1;

            StringBuilder string = new StringBuilder(dest);
            string.replace(mEditText.getSelectionStart(),
                    mEditText.getSelectionEnd(), source.toString());
            try {
                if (!text.equals("")) {
                    value = Integer.parseInt(text);
                }
                value = Integer.parseInt(string.toString());
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
        if (positiveResult) {
            String text = mEditText.getText().toString();
            int value = Integer.valueOf(text);
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

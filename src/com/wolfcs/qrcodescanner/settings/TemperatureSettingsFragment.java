package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class TemperatureSettingsFragment extends PreferenceFragment 
    implements OnPreferenceChangeListener {

    private static final String TEMP_RANGE_KEY = "temp_range";
    private static final String RANGE_MODE_KEY = "range_model";
    private static final String ABOVE_ALARM_KEY = "above_alarm";
    private static final String ABOVE_THRESHOLD_KEY = "above_threshold";
    private static final String BELOW_ALARM_KEY = "below_alarm";
    private static final String BELOW_THRESHOLD_KEY = "below_threshold";

    private ListPreference mTempRangePreference;
    private ListPreference mRangeModePreference;
    private SwitchPreference mAboveAlarmPreference;
    private FloatNumberEditorPreference mAboveThresholdPreference;
    private SwitchPreference mBelowAlarmPreference;
    private FloatNumberEditorPreference mBelowThresholdPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tempreture_settings);

        mTempRangePreference = (ListPreference)findPreference(TEMP_RANGE_KEY);
        mTempRangePreference.setOnPreferenceChangeListener(this);
        String value = mTempRangePreference.getValue();
        int  valueIndex = Integer.parseInt(value);
        updateListPreference(mTempRangePreference, valueIndex);

        mRangeModePreference = (ListPreference)findPreference(RANGE_MODE_KEY);
        mRangeModePreference.setOnPreferenceChangeListener(this);
        value = mRangeModePreference.getValue();
        valueIndex = Integer.parseInt(value);
        updateListPreference(mRangeModePreference, valueIndex);
        mRangeModePreference.setValue(value);

        mAboveAlarmPreference = (SwitchPreference) findPreference(ABOVE_ALARM_KEY);
        mAboveAlarmPreference.setOnPreferenceChangeListener(this);
        mAboveThresholdPreference = (FloatNumberEditorPreference)
                findPreference(ABOVE_THRESHOLD_KEY);
        mAboveThresholdPreference.setOnPreferenceChangeListener(this);
        updateAboveAlarmPreference();

        mBelowAlarmPreference = (SwitchPreference) findPreference(BELOW_ALARM_KEY);
        mBelowAlarmPreference.setOnPreferenceChangeListener(this);
        mBelowThresholdPreference = (FloatNumberEditorPreference)
                findPreference(BELOW_THRESHOLD_KEY);
        mBelowThresholdPreference.setOnPreferenceChangeListener(this);
    }

    private void updateListPreference(ListPreference preference, int value) {
        preference.setSummary(preference.getEntries()[value]);
    }

    private void updateAboveAlarmPreference() {
        if (mAboveAlarmPreference.isChecked()) {
            mAboveThresholdPreference.setEnabled(true);
        } else {
            mAboveThresholdPreference.setEnabled(false);
        }

        String unit = mAboveThresholdPreference.getUnit();
        CharSequence title = mAboveThresholdPreference.getDialogTitle();
        mAboveThresholdPreference.setDialogTitle(title + "(" + unit + ")");

        float value = mAboveThresholdPreference.getValue();
        mAboveThresholdPreference.setSummary(value + unit);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == mTempRangePreference) {
            int  valueIndex = Integer.parseInt((String)newValue);
            updateListPreference(mTempRangePreference, valueIndex);
            return true;
        } else if (preference == mRangeModePreference) {
            int  valueIndex = Integer.parseInt((String)newValue);
            updateListPreference(mRangeModePreference, valueIndex);
            return true;
        } else if(preference == mAboveAlarmPreference) {
            return true;
        } else if(preference == mBelowAlarmPreference) {
            return true;
        }
        return false;
    }
}

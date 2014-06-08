package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.util.Log;

public class TemperatureSettingsFragment extends PreferenceFragment 
    implements OnPreferenceChangeListener {
    private static final String TAG = "TemperatureSettingsFragment";
    private static final boolean DEBUG = true;

    private static final String TEMP_RANGE_KEY = "temp_range";
    private static final String RANGE_MODE_KEY = "range_model";
    
    private ListPreference mTempRangePreference;
    private ListPreference mRangeModePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tempreture_settings);

        mTempRangePreference = (ListPreference)findPreference(TEMP_RANGE_KEY);
        mTempRangePreference.setOnPreferenceChangeListener(this);
        String value = mTempRangePreference.getValue();
        int  valueIndex = Integer.parseInt(value);
        mTempRangePreference.setSummary(mTempRangePreference.getEntries()[valueIndex]);
        mTempRangePreference.setValue(value);

        mRangeModePreference = (ListPreference)findPreference(RANGE_MODE_KEY);
        mRangeModePreference.setOnPreferenceChangeListener(this);
        value = mRangeModePreference.getValue();
        valueIndex = Integer.parseInt(value);
        mRangeModePreference.setSummary(mRangeModePreference.getEntries()[valueIndex]);
        mRangeModePreference.setValue(value);

//        mThermalSensorController = ThermalSensorController.getInstance();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (DEBUG) Log.i(TAG, "onPreferenceChange: Preference " + preference + " Value " + newValue);
        if(preference == mTempRangePreference) {
            int  valueIndex = Integer.parseInt((String)newValue);
            mTempRangePreference.setSummary(mTempRangePreference.getEntries()[valueIndex]);
            mTempRangePreference.setValue((String)newValue);
        } else if (preference == mRangeModePreference) {
            int  valueIndex = Integer.parseInt((String)newValue);
//            mThermalSensorController.setRangeMode(valueIndex);
            mRangeModePreference.setSummary(mRangeModePreference.getEntries()[valueIndex]);
            mRangeModePreference.setValue((String)newValue);
        }
        return false;
    }
}

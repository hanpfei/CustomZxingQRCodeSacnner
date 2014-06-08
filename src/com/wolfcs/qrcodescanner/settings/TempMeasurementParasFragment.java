package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class TempMeasurementParasFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String EMISSION_FREQ_KEY = "emission_freq";
    private static final String RTC_KEY = "rtc";
    private static final String OPTICAL_TRANSMITTANCE_KEY = "optical_transmittance";
    private static final String ENV_TEMP_KEY = "env_temp";
    private static final String MEASURING_DISTANCE_KEY = "measuring_distance";

    private SeekBarPreference mEmissionFreqPreference;
    private EditTextPreference mRTCPreference;
    private SeekBarPreference mOpticalTransmittance;
    private EditTextPreference mEnvTempPreference;
    private EditTextPreference mMeasuringDistance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.temp_measurement_paras_settings);

        String str;
        mEmissionFreqPreference = (SeekBarPreference) findPreference(EMISSION_FREQ_KEY);
        mEmissionFreqPreference.setOnPreferenceChangeListener(this);

        mRTCPreference = (EditTextPreference) findPreference(RTC_KEY);
        mRTCPreference.setOnPreferenceChangeListener(this);
        String title = mRTCPreference.getTitle().toString();
        title = title + " (" + getString(R.string.celcius) + ")";
        mRTCPreference.setDialogTitle(title);
        str = mRTCPreference.getText();
        mRTCPreference.setSummary(str + " " + getString(R.string.celcius));

        mOpticalTransmittance = (SeekBarPreference) findPreference(OPTICAL_TRANSMITTANCE_KEY);
        mOpticalTransmittance.setOnPreferenceChangeListener(this);

        mEnvTempPreference = (EditTextPreference) findPreference(ENV_TEMP_KEY);
        mEnvTempPreference.setOnPreferenceChangeListener(this);
        title = mEnvTempPreference.getTitle().toString();
        title = title + " (" + getString(R.string.celcius) + ")";
        mEnvTempPreference.setDialogTitle(title);
        str = mEnvTempPreference.getText();
        mEnvTempPreference.setSummary(str + " " + getString(R.string.celcius));

        mMeasuringDistance = (EditTextPreference) findPreference(MEASURING_DISTANCE_KEY);
        mMeasuringDistance.setOnPreferenceChangeListener(this);
        title = mMeasuringDistance.getTitle().toString();
        title = title + " (" + "m" + ")";
        mMeasuringDistance.setDialogTitle(title);
        str = mMeasuringDistance.getText();
        mMeasuringDistance.setSummary(str + " m");

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEmissionFreqPreference) {
            String string = (String) newValue;
            float value = Float.valueOf(string);
            if (value < 0 || value > 1) {
                return true;
            }
            mEmissionFreqPreference.setSummary(string);
        } else if (preference == mRTCPreference) {
            String string = (String)newValue;
            int value = Integer.valueOf(string);
            if (value < 0 || value > 2000) {
                return true;
            }
            mRTCPreference.setText(string);
            String celiusStr = getString(R.string.celcius);
            String summary = value + " " + celiusStr;
            mRTCPreference.setSummary(summary);
            return true;
        } else if (preference == mEnvTempPreference) {
            String string = (String)newValue;
            int value = Integer.valueOf(string);
            if (value < -50 || value > 100) {
                return true;
            }
            mEnvTempPreference.setText(string);
            String celiusStr = getString(R.string.celcius);
            String summary = value + " " + celiusStr;
            mEnvTempPreference.setSummary(summary);
            return true;
        } else if (preference == mMeasuringDistance) {
            String string = (String)newValue;
            int value = Integer.valueOf(string);
            if (value < 1 || value > 1000) {
                return true;
            }
            mMeasuringDistance.setText(string);
            String unit = "m";
            String summary = value + " " + unit;
            mMeasuringDistance.setSummary(summary);
            return true;
        }
        return false;
    }
}

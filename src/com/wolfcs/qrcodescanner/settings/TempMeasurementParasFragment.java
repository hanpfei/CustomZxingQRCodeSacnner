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
    private NumberPickerPreference mRTCPreference;
    private SeekBarPreference mOpticalTransmittance;
    private EditTextPreference mEnvTempPreference;
    private NumberPickerPreference mMeasuringDistance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.temp_measurement_paras_settings);

        String str;
        mEmissionFreqPreference = (SeekBarPreference) findPreference(EMISSION_FREQ_KEY);
        mEmissionFreqPreference.setOnPreferenceChangeListener(this);
        float value = mEmissionFreqPreference.getValue();
        mEmissionFreqPreference.setSummary(String.valueOf(value));

        mRTCPreference = (NumberPickerPreference) findPreference(RTC_KEY);
        mRTCPreference.setOnPreferenceChangeListener(this);
        String title = mRTCPreference.getTitle().toString();
        title = title + " (" + getString(R.string.celcius) + ")";
        mRTCPreference.setDialogTitle(title);
        int rtc = mRTCPreference.getValue();
        String unit = mRTCPreference.getUnit();
        mRTCPreference.setSummary(rtc + unit);

        mOpticalTransmittance = (SeekBarPreference) findPreference(OPTICAL_TRANSMITTANCE_KEY);
        mOpticalTransmittance.setOnPreferenceChangeListener(this);
        value = mOpticalTransmittance.getValue();
        mOpticalTransmittance.setSummary(String.valueOf(value) + "%");

        mEnvTempPreference = (EditTextPreference) findPreference(ENV_TEMP_KEY);
        mEnvTempPreference.setOnPreferenceChangeListener(this);
        title = mEnvTempPreference.getTitle().toString();
        title = title + " (" + getString(R.string.celcius) + ")";
        mEnvTempPreference.setDialogTitle(title);
        str = mEnvTempPreference.getText();
        mEnvTempPreference.setSummary(str + " " + getString(R.string.celcius));

        mMeasuringDistance = (NumberPickerPreference) findPreference(MEASURING_DISTANCE_KEY);
        mMeasuringDistance.setOnPreferenceChangeListener(this);
        title = mMeasuringDistance.getTitle().toString();
        unit = mMeasuringDistance.getUnit();
        title = title + " (" + unit + ")";
        mMeasuringDistance.setDialogTitle(title);
        int distance = mMeasuringDistance.getValue();
        mMeasuringDistance.setSummary(distance + unit);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEmissionFreqPreference) {
            Float value = (Float) newValue;
            mEmissionFreqPreference.setSummary(String.valueOf(value));
            return true;
        } else if (preference == mRTCPreference) {
            Integer rtc = (Integer)newValue;
            String unit = mRTCPreference.getUnit();
            String summary = rtc + " " + unit;
            mRTCPreference.setSummary(summary);
            return true;
        } else if (preference == mOpticalTransmittance){
            Float value = (Float) newValue;
            mOpticalTransmittance.setSummary(String.valueOf(value) + "%");
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
            Integer distance = (Integer) newValue;
            String unit = mMeasuringDistance.getUnit();
            String summary = distance + " " + unit;
            mMeasuringDistance.setSummary(summary);
            return true;
        }
        return false;
    }
}

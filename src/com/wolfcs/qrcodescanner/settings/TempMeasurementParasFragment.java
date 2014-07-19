package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.os.Bundle;
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

    private static final String DURATION_KEY = "duration";

    private SeekBarPreference mEmissionFreqPreference;
    private NumberPickerPreference mRTCPreference;
    private SeekBarPreference mOpticalTransmittance;
    private NumberEditorPreference mEnvTempPreference;
    private NumberPickerPreference mMeasuringDistance;

    private DurationPickerPreference mDurationPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.temp_measurement_paras_settings);

        mEmissionFreqPreference = (SeekBarPreference) findPreference(EMISSION_FREQ_KEY);
        mEmissionFreqPreference.setOnPreferenceChangeListener(this);
        updateSeekBarPreference(mEmissionFreqPreference, mEmissionFreqPreference.getValue());

        mRTCPreference = (NumberPickerPreference) findPreference(RTC_KEY);
        mRTCPreference.setOnPreferenceChangeListener(this);
        String title = mRTCPreference.getTitle().toString();
        mRTCPreference.setDialogTitle(title);
        updateNumberPickerPreference(mRTCPreference, mRTCPreference.getValue());

        mOpticalTransmittance = (SeekBarPreference) findPreference(OPTICAL_TRANSMITTANCE_KEY);
        mOpticalTransmittance.setOnPreferenceChangeListener(this);
        updateSeekBarPreference(mOpticalTransmittance, mOpticalTransmittance.getValue());

        mEnvTempPreference = (NumberEditorPreference) findPreference(ENV_TEMP_KEY);
        mEnvTempPreference.setOnPreferenceChangeListener(this);
        title = mEnvTempPreference.getTitle().toString();
        mEnvTempPreference.setDialogTitle(title);
        int envTemp = mEnvTempPreference.getValue();
        mEnvTempPreference.setSummary(envTemp + " " + mEnvTempPreference.getUnit());

        mMeasuringDistance = (NumberPickerPreference) findPreference(MEASURING_DISTANCE_KEY);
        mMeasuringDistance.setOnPreferenceChangeListener(this);
        title = mMeasuringDistance.getTitle().toString();
        mMeasuringDistance.setDialogTitle(title);
        updateNumberPickerPreference(mMeasuringDistance, mMeasuringDistance.getValue());

        mDurationPreference = (DurationPickerPreference) findPreference(DURATION_KEY);
        mDurationPreference.setOnPreferenceChangeListener(this);
        updateDurationPickerPreference(mDurationPreference, null);
    }

    private void updateDurationPickerPreference(
            DurationPickerPreference preference, Integer value) {
        int millis;
        if (value == null) {
            millis = preference.getValue();
        } else {
            millis = value.intValue();
        }
        int minutes = preference.getMinutesInMillis(millis);
        int seconds = preference.getSecondsInMillis(millis);
        int leftMillis = preference.getMillisInMillis(millis);
        String summary = leftMillis + "ms";
        if (minutes != 0) {
            summary = minutes + "min " + seconds + "s " + summary;
        } else {
            if (seconds != 0) {
                summary = seconds + "s " + summary;
            }
        }
        preference.setSummary(summary);
    }

    private void updateSeekBarPreference(SeekBarPreference preference, Float value) {
        String summary = String.valueOf(value);
        String unit = preference.getUnit();
        if (unit != null) {
            summary = summary + unit;
        }
        preference.setSummary(summary);
    }

    private void updateNumberPickerPreference(
            NumberPickerPreference preference, Integer value) {
        String unit = preference.getUnit();
        String summary = value + " " + unit;
        preference.setSummary(summary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof SeekBarPreference) {
            updateSeekBarPreference((SeekBarPreference) preference, (Float) newValue);
            return true;
        } else if (preference instanceof NumberPickerPreference) {
            updateNumberPickerPreference((NumberPickerPreference) preference,
                    (Integer) newValue);
            return true;
        } else if (preference == mEnvTempPreference) {
            Integer value = (Integer) newValue;
            String unit = mEnvTempPreference.getUnit();
            String summary = value + " " + unit;
            mEnvTempPreference.setSummary(summary);
            return true;
        } else if (preference == mDurationPreference) {
            updateDurationPickerPreference(mDurationPreference, (Integer)newValue);
            return true;
        }
        return false;
    }
}

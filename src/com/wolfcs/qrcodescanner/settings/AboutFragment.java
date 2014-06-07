package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AboutFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_settings);
    }
}

package com.wolfcs.qrcodescanner.settings;

import java.util.List;

import com.wolfcs.qrcodescanner.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onBuildHeaders(List<Header> headers) {
        if (!onIsHidingHeaders()) {
            loadHeadersFromResource(R.xml.settings_headers, headers);
        }
    }

    private static final String[] ENTRY_FRAGMENTS = { 
        AboutFragment.class.getName(),
    };

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // Almost all fragments are wrapped in this,
        // except for a few that have their own activities.
        for (int i = 0; i < ENTRY_FRAGMENTS.length; i++) {
            if (ENTRY_FRAGMENTS[i].equals(fragmentName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int action = item.getItemId();
        switch (action) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

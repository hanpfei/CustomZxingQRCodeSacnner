package com.wolfcs.qrcodescanner.settings;

import com.wolfcs.qrcodescanner.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
    private SeekBar seekBar;
    private TextView textView;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        return View.inflate(getContext(), R.layout.preference_dialog_seek, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.i("Dialog closed", "You click positive button");
        } else {
            Log.i("Dialog closed", "You click negative button");
        }
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        
    }

}

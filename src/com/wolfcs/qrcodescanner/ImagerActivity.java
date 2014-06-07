package com.wolfcs.qrcodescanner;

import com.wolfcs.qrcodescanner.widget.ModeSwitcher.ModeSwitchListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class ImagerActivity extends Activity implements OnClickListener,
        ModeSwitchListener {
    private static final String TAG = "ThermalImagerActivity";

    public static final String REGION_TAG_KEY = "region_tag";

    private Dialog mExitDialog;
    
    private PreviewModule mPreviewModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imager);

        setRotationAnimation();
        Log.i(TAG, "onCreate");

        Intent intent = getIntent();
        if (intent == null) {
            // finish();
            return;
        }
        View rootView = findViewById(R.id.app_root);

        mPreviewModule = new PreviewModule(this, rootView, "tag");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPreviewModule.onActivityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPreviewModule.onActivityStopped();
    }

    private void setRotationAnimation() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
        rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onClick(View view) {
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mExitDialog == null) {
                mExitDialog = createExitDialog();
            }
            mExitDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    };
    
    private Dialog createExitDialog(){
        Dialog dialog =  new AlertDialog.Builder(this)
        .setTitle(R.string.exit)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        })
        .setNegativeButton(android.R.string.no, null)
        .create();
        return dialog;
    }

    @Override
    public void onModeSelected(int i) {
        
    }
}

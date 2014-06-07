package com.wolfcs.qrcodescanner;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class CameraModule implements ImagerModule, OnClickListener {
    private static final String TAG = "CameraModule";

    private View mCameraRootView;
    private View mCameraPreviewView;
    private TextView mFlash;
    private TextView mSettings;

    private ImagerActivity mActivity;
    private CameraController mCameraController;

    private int mCurrentOrientation;

    public CameraModule(ImagerActivity activity, View rootView) {
        mCameraController = CameraController.getInstance();
        mActivity = activity;
        mCameraRootView = rootView.findViewById(R.id.camera_root);
        mCameraPreviewView = rootView.findViewById(R.id.camera_preview);

        mFlash = (TextView) mCameraRootView.findViewById(R.id.flash);
        mSettings = (TextView) mCameraRootView.findViewById(R.id.c_settings);
        mSettings.setOnClickListener(this);
    }

    private void rotateIfNeed(int orientation){
        if (mCurrentOrientation == orientation) {
            return;
        }
        mCurrentOrientation = orientation;
        orientation = 360 - orientation;

        mSettings.setRotation(orientation);
        mFlash.setRotation(orientation);
    }

    @Override
    public void display(int orientation) {
        mCameraRootView.setVisibility(View.VISIBLE);
        mCameraPreviewView.setVisibility(View.VISIBLE);
        mCameraController.startPreview();

        rotateIfNeed(orientation);
    }

    @Override
    public void disappear() {
        mCameraRootView.setVisibility(View.INVISIBLE);
        mCameraPreviewView.setVisibility(View.INVISIBLE);
        mCameraController.stopPreview();
    }

    @Override
    public void onOrientationChanged(int orientation) {
        rotateIfNeed(orientation);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.c_settings:
                ImagerUtils.startSettingActivity(mActivity);
                break;
            default:
                break;
        }
    }
}

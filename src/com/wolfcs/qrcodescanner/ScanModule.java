package com.wolfcs.qrcodescanner;

import java.util.Collection;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.ViewfinderView;
import com.google.zxing.client.android.camera.CameraManager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ScanModule implements ImagerModule, OnClickListener {
    private static final int TRY_DECODE_DELAY = 50;
    private Activity mActivity;
    private CameraController mCameraController;
    
    private View mScanRootView;
    private View mCameraPreviewView;
    private ViewfinderView mViewfinderView;
    private TextView mScanResult;
    private TextView mSettings;

    private CaptureActivityHandler mHandler;

    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType,?> decodeHints;
    private String characterSet;
    private boolean mStartScan;

    private int mCurrentOrientation;

    public ScanModule(Activity activity, View rootView) {
        mActivity = activity;
        mCameraController = CameraController.getInstance();
        mScanRootView = rootView.findViewById(R.id.scan_root);
        mCameraPreviewView = rootView.findViewById(R.id.camera_preview);
        mViewfinderView = (ViewfinderView) rootView.findViewById(R.id.viewfinder_view);
        mScanResult = (TextView) rootView.findViewById(R.id.scan_result);
        mSettings = (TextView) rootView.findViewById(R.id.s_settings);
        mSettings.setOnClickListener(this);
    }

    private void rotateIfNeed(int orientation){
        if (mCurrentOrientation == orientation) {
            return;
        }
        mCurrentOrientation = orientation;
        orientation = 360 - orientation;

        mSettings.setRotation(orientation);
    }

    @Override
    public void display(int orientation) {
        mStartScan = true;
        if (mHandler == null) {
            mHandler = new CaptureActivityHandler(mActivity, this,
                    decodeFormats, decodeHints, characterSet, mCameraController.getCameraManager());
        }
        mViewfinderView.setCameraManager(mCameraController.getCameraManager());
        mScanRootView.setVisibility(View.VISIBLE);
        mCameraPreviewView.setVisibility(View.VISIBLE);
        mCameraController.startPreview();

        if(mCameraController.isCameraOpen()) {
            mHandler.sendEmptyMessage(R.id.decode_failed);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mStartScan) {
                        if(mCameraController.isCameraOpen()) {
                            mHandler.sendEmptyMessage(R.id.decode_failed);
                        } else {
                            mHandler.postDelayed(this, TRY_DECODE_DELAY);
                        }
                    }
                }
            }, TRY_DECODE_DELAY);
        }

        drawViewfinder();

        rotateIfNeed(orientation);
    }

    @Override
    public void disappear() {
        mStartScan = false;
        mScanRootView.setVisibility(View.INVISIBLE);
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
            case R.id.s_settings:
                ImagerUtils.startSettingActivity(mActivity);
                break;
            default:
                break;
        }
    }

    Activity getActivity() {
        return mActivity;
    }

    ViewfinderView getViewfinderView() {
        return mViewfinderView;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public CameraManager getCameraManager() {
        return mCameraController.getCameraManager();
    }

    public void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        mScanResult.setText(rawResult.getText());
    }
}

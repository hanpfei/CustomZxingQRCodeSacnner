package com.wolfcs.qrcodescanner;

import com.wolfcs.qrcodescanner.widget.ModeSwitcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class PreviewModule implements OnClickListener, 
    CameraController.CameraActionListener, ModeSwitcher.ModeSwitchListener {
    private static final String TAG = PreviewModule.class.getName();
    private ImagerActivity mActivity;
    private View mRootView;

    private String mTag;

    private ImageView mPreviewButton;

    private CameraModule mCameraModule;
    private ScanModule mScanModule;
    private ImagerModule mCurrentImageModule;

    private CameraController mCameraController;

    private OrientationEventListener mOrientationEventListener;
    private int mCurrentOrientation;

    public PreviewModule(ImagerActivity activity, View parent, String tag) {
        mActivity = activity;
        mRootView = parent;
        mTag = tag;

        mActivity.getLayoutInflater().inflate(R.layout.preview_module,
                (ViewGroup) mRootView, true);

        mCameraController = CameraController.getInstance();
        mCameraController.init(activity, (TextureView) mRootView.findViewById(R.id.camera_preview));

        ModeSwitcher modeSwitcher = (ModeSwitcher) mRootView.findViewById(R.id.mode_switcher);
        modeSwitcher.setModeSwitchListener(this);

        mPreviewButton = (ImageView) mRootView.findViewById(R.id.preview_thumb);
        mPreviewButton.setOnClickListener(this);

        ImageView shutterButton = (ImageView) mRootView.findViewById(R.id.shutter_button);
        shutterButton.setOnClickListener(this);

        mCameraModule = new CameraModule(activity, mRootView);
        mScanModule = new ScanModule(mActivity, mRootView);

        mCurrentImageModule = mCameraModule;
        mOrientationEventListener = new MyOrientationEventListener(activity);
        mOrientationEventListener.enable();
    }

    public void onActivityStarted() {
        mCameraController.registerCameraActionListener(this);

        mCurrentImageModule.display(mCurrentOrientation);
    }

    public void onActivityStopped() {
        mCurrentImageModule.disappear();
        mCameraController.unregisterCameraActionListener();
    }

    private void selectMode(int index) {
        ImagerModule newImageModule;
        switch (index) {
        case ModeSwitcher.CAMERA_MODE_INDEX:
            newImageModule = mCameraModule;
            break;
        case ModeSwitcher.SCAN_MODE_INDEX:
            newImageModule = mScanModule;
            break;
        default:
            newImageModule = mCameraModule;
            break;
        }

        if (mCurrentImageModule != null) {
            mCurrentImageModule.disappear();
        }
        newImageModule.display(mCurrentOrientation);

        mCurrentImageModule = newImageModule;
    }

    public void onPictureTackenDone(Uri uri) {
        Bitmap bitmap = PhotoManager.getBitmap(mActivity.getContentResolver(), uri);
        int width = mPreviewButton.getWidth();
        int height = mPreviewButton.getHeight();
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, width, height, false);
        bitmap.recycle();
        mPreviewButton.setImageBitmap(thumbnail);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.shutter_button:
            mCameraController.capture(mTag);
            break;

        case R.id.preview_thumb:
            break;
        }
    }

    public void onShutterDone (long captureStartTime) {
    }

    @Override
    public void onModeSelected(int i) {
        switch (i) {
        case ModeSwitcher.CAMERA_MODE_INDEX:
            selectMode(ModeSwitcher.CAMERA_MODE_INDEX);
            break;
        case ModeSwitcher.SCAN_MODE_INDEX:
            selectMode(ModeSwitcher.SCAN_MODE_INDEX);
            break;
        default:
            Log.w(TAG, "Unknown mode");
        }
    }

    private void onOrientationChanged(int orientation) {
        if (mCurrentOrientation == orientation) {
            return;
        }
        orientation = 360 - orientation;
        mPreviewButton.setRotation(orientation);
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }
            orientation = (orientation + 45) / 90 * 90;
            if (orientation == 360) {
                orientation = 0;
            }
            if (orientation == mCurrentOrientation) {
                return;
            }
            PreviewModule.this.onOrientationChanged(orientation);
            mCurrentImageModule.onOrientationChanged(orientation);
            mCurrentOrientation = orientation;
        }
    }
}

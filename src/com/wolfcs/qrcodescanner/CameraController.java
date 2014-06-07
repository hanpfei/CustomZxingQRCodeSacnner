package com.wolfcs.qrcodescanner;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.util.Log;
import android.view.TextureView;

import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import android.os.Handler;

/**
 * Created by nova on 5/28/14.
 */
public class CameraController {
    private static final String TAG = "CameraController";
    private static final boolean DEBUG = true;
    private static final int TRY_START_PREVIEW_DELAY = 50;

    private final static CameraController INSTANCE = new CameraController();

    private long mShutterCallbackTime;
    private long mJpegPictureCallbackTime;
    private long mRawPictureCallbackTime;
    private boolean mStartPreview;
    public long mShutterLag;
    public long mJpegCallbackFinishTime;

    private Activity mActivity;
    private TextureView mPreviewTexture;
    private CameraManager mCameraManager;
    private Handler mHandler;

    public interface CameraActionListener {
        public void onShutterDone(long captureStartTime);

        public void onPictureTackenDone(Uri uri);
    }

    private CameraActionListener mActionListener;

    private CameraController() {}

    public static CameraController getInstance() { return INSTANCE; }

    public void init(Activity activity, TextureView textureView) {
        mActivity = activity;
        mCameraManager = new CameraManager(activity);
        mPreviewTexture = textureView;
        mPreviewTexture.setSurfaceTextureListener(mTextureListener);
        mHandler = new Handler();
    }

    public void registerCameraActionListener(CameraActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void unregisterCameraActionListener() {
        mActionListener = null;
    }


    public void startPreview() {
        if (DEBUG) Log.i(TAG, "startPreview");
        mStartPreview = true;
        if (mCameraManager == null) {
            Log.e(TAG, "startPreview error! cameraManger is null");
            return;
        }
        if (mCameraManager.isOpen()) {
            mCameraManager.startPreview();
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.i(TAG, "retry start preview");
                    if (mStartPreview) {
                       if (mCameraManager.isOpen()) {
                           mCameraManager.startPreview();
                       } else {
                           mHandler.postDelayed(this, TRY_START_PREVIEW_DELAY);
                       }
                    }
                }
            }, TRY_START_PREVIEW_DELAY);
        }
    }

    public void stopPreview() {
        if (DEBUG) Log.i(TAG, "stopPreview");
        mStartPreview = false;
        if (mCameraManager != null) {
            mCameraManager.stopPreview();
        }
    }

    public boolean capture(String regionTag) {
        long captureStartTime = System.currentTimeMillis();
        startPreview();

        if (mCameraManager != null) {
            IRSAutoFocusCallback autoFocusCallback = new IRSAutoFocusCallback(
                    captureStartTime, regionTag);
            mCameraManager.autoFocus(autoFocusCallback);
        }
        return false;
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    public boolean isCameraOpen() {
        if (mCameraManager == null)
            return false;
        return mCameraManager.isOpen();
    }

    private TextureView.SurfaceTextureListener mTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    if (DEBUG) Log.i(TAG, "onSurfaceTextureAvailable");
                    try {
                        if (mCameraManager != null)
                            mCameraManager.openDriver(surfaceTexture, width, height);
                    } catch (IOException ioe) {
                        Log.e(TAG, "Open Camera error: " + ioe);
                    }
                    if (mStartPreview)
                        startPreview();
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
                    if (DEBUG) Log.i(TAG, "onSurfaceTextureDestroyed");
                    if (mCameraManager != null) {
                        mCameraManager.closeDriver();
                    }
                    return true;
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
                }
            };

    private final class IRSShutterCallback implements Camera.ShutterCallback {
        private final long mCaptureStartTime;

        public IRSShutterCallback(long captureStartTime) {
            mCaptureStartTime = captureStartTime;
        }

        @Override
        public void onShutter() {
            mShutterCallbackTime = System.currentTimeMillis();
            mShutterLag = mShutterCallbackTime - mCaptureStartTime;

            Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
            if (mActionListener != null) {
                mActionListener.onShutterDone(mCaptureStartTime);
            }
        }
    }

    private final class IRSRawPictureCallback implements Camera.PictureCallback {
        private final long mCaptureStartTime;

        public IRSRawPictureCallback(long captureStartTime) {
            mCaptureStartTime = captureStartTime;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mRawPictureCallbackTime = System.currentTimeMillis();
            Log.v(TAG, "mShutterToRawCallbackTime = "
                    + (mRawPictureCallbackTime - mShutterCallbackTime) + "ms");
        }
    }

    private final class IRSJpegPictureCallback implements Camera.PictureCallback {
        private final long mCaptureStartTime;
        private  String mRegionTag;

        public IRSJpegPictureCallback(long captureStartTime, final String regionTag) {
            mCaptureStartTime = captureStartTime;
            mRegionTag = regionTag;
        }

        @Override
        public void onPictureTaken(byte[] jpegData, Camera camera) {
            mJpegPictureCallbackTime = System.currentTimeMillis();

            long now = System.currentTimeMillis();
            mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
            Log.v(TAG, "mJpegCallbackFinishTime = " + mJpegCallbackFinishTime + "ms");
            mJpegPictureCallbackTime = 0;

            Uri uri = PhotoManager.saveCameraJpegImage(mActivity, mRegionTag,
                    jpegData, mCaptureStartTime);

            if (mStartPreview)
                startPreview();

            if (mActionListener != null) {
                mActionListener.onPictureTackenDone(uri);
            }
        }
    }

    private final class IRSAutoFocusCallback implements Camera.AutoFocusCallback {
        private final long mCaptureStartTime;
        private String mRegionTag;

        public IRSAutoFocusCallback(long captureStartTime, String regionTag) {
            mCaptureStartTime = captureStartTime;
            mRegionTag = regionTag;
        }

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            IRSShutterCallback shutterCallback =
                    new IRSShutterCallback(mCaptureStartTime);
            IRSRawPictureCallback rawPictureCallback =
                    new IRSRawPictureCallback(mCaptureStartTime);
            IRSJpegPictureCallback jpegPictureCallback =
                    new IRSJpegPictureCallback(mCaptureStartTime, mRegionTag);

            int cameraId = ThermalImagerUtils.getOptimalCameraId();
            int displayRotation = ThermalImagerUtils.getDisplayRotation(mActivity);
            int rotation = ThermalImagerUtils.getJpegRotation(cameraId, displayRotation);
            if (mCameraManager != null) {
                mCameraManager.setRotation(rotation);
                mCameraManager.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
            }
        }
    }
}

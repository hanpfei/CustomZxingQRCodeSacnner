package com.wolfcs.qrcodescanner;

import java.util.List;

import com.wolfcs.qrcodescanner.settings.SettingsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

public class ThermalImagerUtils {

    /**
     * Calculate the default orientation of the device based on the width and
     * height of the display when rotation = 0 (i.e. natural width and height)
     * @param activity the activity context
     * @return whether the default orientation of the device is portrait
     */
    public static boolean isDefaultToPortrait(Activity activity) {
        Display currentDisplay = activity.getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        currentDisplay.getSize(displaySize);
        int orientation = currentDisplay.getRotation();
        int naturalWidth, naturalHeight;
        if (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
            naturalWidth = displaySize.x;
            naturalHeight = displaySize.y;
        } else {
            naturalWidth = displaySize.y;
            naturalHeight = displaySize.x;
        }
        return naturalWidth < naturalHeight;
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: return 0;
            case Surface.ROTATION_90: return 90;
            case Surface.ROTATION_180: return 180;
            case Surface.ROTATION_270: return 270;
        }
        return 0;
    }

    public static int getOptimalCameraId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return 0;
    }

    public static Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double)width / height;
        if (sizes == null) {
            return null;
        }

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static int getJpegRotation(int cameraId, int orientation) {
        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        int rotation = 0;

        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (info.orientation + orientation) % 360;
            }
        }
        if (rotation % 180 == 0) {
            rotation = rotation + 180;
            rotation = rotation % 360;
        }
        return rotation;
    }

    public static void startSettingActivity(Context ctx) {
        Intent intent = new Intent(ctx, SettingsActivity.class);
        ctx.startActivity(intent);
    }
}

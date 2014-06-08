package com.wolfcs.qrcodescanner;

import com.wolfcs.qrcodescanner.measurement.MeasuringMode.MeasurementOperationListener;
import com.wolfcs.qrcodescanner.measurement.MeasuringModeManager;
import com.wolfcs.qrcodescanner.widget.PalletteView;
import com.wolfcs.qrcodescanner.widget.TouchEventReceiverView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MeasureModule implements ImagerModule, OnClickListener, 
        MeasurementOperationListener {
    private static final String MIN_TEMP_KEY = "min_temp";
    private static final String MAX_TEMP_KEY = "max_temp";

    private ImagerActivity mActivity;
    private View mMeasureRootView;
    private View mThermalPreviewView;

    private View mMeasureOpBar;
    private MeasuringModeManager mMeasureManager;

    private ImageView mActThermometry;
    private ImageView mActSettings;

    private TouchEventReceiverView mRectView;
    private ImageView mAlertImageView;
    private PalletteView mPalletteView;
    private TextView mMinTLabel;
    private TextView mMaxTLabel;

    private int mCurrentOrientation;

    public MeasureModule(ImagerActivity activity, View rootView) {
        mActivity= activity;

        mMeasureRootView = rootView.findViewById(R.id.measure_root);
        mThermalPreviewView = rootView.findViewById(R.id.thermal_preview);

        mMeasureManager = new MeasuringModeManager(activity, this);

        mMeasureOpBar = mMeasureRootView.findViewById(R.id.measure_temp_op_bar);
        Button btnCancel = (Button)mMeasureRootView.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(this);
        ImageView imageDelete = (ImageView)mMeasureRootView.findViewById(R.id.delete);
        imageDelete.setOnClickListener(mMeasureManager);
        Button btnSelectAll = (Button)mMeasureRootView.findViewById(R.id.select_all);
        btnSelectAll.setOnClickListener(mMeasureManager);

        mActThermometry = (ImageView) mMeasureRootView.findViewById(R.id.act_measure);
        mActThermometry.setOnClickListener(mMeasureManager);

        mActSettings = (ImageView) mMeasureRootView.findViewById(R.id.t_settings);
        mActSettings.setOnClickListener(this);

        mRectView = (TouchEventReceiverView)rootView.findViewById(R.id.control_rect);
        mMeasureManager.init(mRectView, mActThermometry);

        mAlertImageView = (ImageView) rootView.findViewById(R.id.alert_imgview);
        mAlertImageView.setVisibility(View.INVISIBLE);

        mPalletteView = (PalletteView) rootView.findViewById(R.id.pallette);

        mMinTLabel = (TextView) rootView.findViewById(R.id.min_t);
        mMaxTLabel = (TextView) rootView.findViewById(R.id.max_t);
    }

    private void rotateIfNeed(int orientation){
        if (mCurrentOrientation == orientation) {
            return;
        }
        mCurrentOrientation = orientation;
        orientation = 360 - orientation;

        mActThermometry.setRotation(orientation);
        mActSettings.setRotation(orientation);
    }

    @Override
    public void display(int orientation) {
        mMinTLabel.setVisibility(View.VISIBLE);
        mMaxTLabel.setVisibility(View.VISIBLE);
        mPalletteView.setVisibility(View.VISIBLE);
        mMeasureRootView.setVisibility(View.VISIBLE);
        mThermalPreviewView.setVisibility(View.VISIBLE);

        rotateIfNeed(orientation);
    }

    @Override
    public void disappear() {
        mMeasureRootView.setVisibility(View.INVISIBLE);
        mPalletteView.setVisibility(View.INVISIBLE);
        mMaxTLabel.setVisibility(View.INVISIBLE);
        mMinTLabel.setVisibility(View.INVISIBLE);
        mThermalPreviewView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        rotateIfNeed(orientation);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.t_settings:
            ImagerUtils.startSettingActivity(mActivity);
            break;

        case R.id.cancel:
            if(mMeasureOpBar.getVisibility() == View.VISIBLE) {
                mMeasureOpBar.setVisibility(View.INVISIBLE);
                mMeasureManager.cancelOpOnMeasuringObjects();
            }
            break;
        default:
            break;
        }
    }

    private static final int SET_TEMP_LABEL = 0;
    private static final int SET_ALERT_IMAGE = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_TEMP_LABEL:
                    Bundle data = (Bundle) msg.obj;
                    mMinTLabel.setText(data.getString(MIN_TEMP_KEY));
                    mMaxTLabel.setText(data.getString(MAX_TEMP_KEY));
                    break;
                case SET_ALERT_IMAGE:
//                    if (mControlEnable)
//                        mAlertImageView.setVisibility(View.INVISIBLE);
//                    else
//                        mAlertImageView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onTempMeasuredObjectSelected() {
        mMeasureOpBar.setVisibility(View.VISIBLE);
    }
}

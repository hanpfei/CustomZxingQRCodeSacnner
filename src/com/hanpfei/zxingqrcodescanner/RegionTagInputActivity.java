package com.hanpfei.zxingqrcodescanner;

import com.google.zxing.client.android.Intents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class RegionTagInputActivity extends Activity implements OnClickListener {
    private static final String TAG = "NFCActivity";

    private EditText mTagEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_tag_input);

        mTagEditText = (EditText)findViewById(R.id.tag_edit);
    }

    private void startMainActivity(String regionTag) {
//        Intent intent = new Intent(this, MainActivity.class);
//        boolean isConnected = ThermalSensor.isConnected();;
//        intent.putExtra(MainActivity.IS_CONNECTED_KEY, isConnected);
//        Log.i(TAG, "deviceTag = " + regionTag);
//        if (regionTag != null && !regionTag.equals("")) {
//            intent.putExtra(MainActivity.REGION_TAG_KEY, regionTag);
//        }
//
//        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.img_barcode_input:
            Intent intent = new Intent(Intents.Scan.ACTION);
            startActivityForResult(intent, 0);

            break;
        case R.id.btn_next:
            startMainActivity(mTagEditText.getText().toString());
            finish();
            break;

        default:
            break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Bundle extras = data.getExtras();
            String regionTag = extras.getString(Intents.Scan.RESULT);
            mTagEditText.setText(regionTag);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

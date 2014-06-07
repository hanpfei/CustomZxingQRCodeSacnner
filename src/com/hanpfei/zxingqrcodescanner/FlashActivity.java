package com.hanpfei.zxingqrcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class FlashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startMainScreen();
    }
    
    private void startMainScreen() {
        boolean connected = isThermalSendorConnected();

        if (connected) {
            Intent intent = new Intent(this, RegionTagInputActivity.class);
//            intent.putExtra(MainActivity.IS_CONNECTED_KEY, connected);
            startActivity(intent);
        } else {
            startMainActivity(connected);
        }
        finish();
    }

    private void startMainActivity(boolean connected) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(MainActivity.IS_CONNECTED_KEY, connected);
//        startActivity(intent);
    }

    private boolean isThermalSendorConnected() {
        return true;
    }
}

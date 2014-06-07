package com.wolfcs.qrcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FlashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startMainScreen();
    }

    private void startMainScreen() {
        Intent intent = new Intent(this, ImagerActivity.class);
        startActivity(intent);
        finish();
    }
}

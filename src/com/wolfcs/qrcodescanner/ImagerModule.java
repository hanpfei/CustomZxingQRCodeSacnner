package com.wolfcs.qrcodescanner;

public interface ImagerModule {
    public void display(int orientation);

    public void disappear();

    public void onOrientationChanged(int orientation);
}

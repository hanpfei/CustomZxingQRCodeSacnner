/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wolfcs.qrcodescanner;

import android.graphics.SurfaceTexture;
import android.opengl.GLUtils;
import android.util.Log;

import java.lang.Thread;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

public class GLProducerThread extends Thread {
    private static final String TAG = "GLProducerThread";
    private static final boolean DEBUG = true;

    private final SurfaceTexture mSurfaceTexture;
    private final GLRenderer mRenderer;
    private volatile boolean mRunning;
    private volatile boolean mFinished;

    private EGL10 mEgl;
    private EGLDisplay mEglDisplay = EGL10.EGL_NO_DISPLAY;
    private EGLContext mEglContext = EGL10.EGL_NO_CONTEXT;
    private EGLSurface mEglSurface = EGL10.EGL_NO_SURFACE;
    private GL mGl;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int EGL_OPENGL_ES2_BIT = 4;

    public interface GLRenderer {
        public boolean drawFrame();

        public void release();
    }

    public GLProducerThread(SurfaceTexture surfaceTexture, GLRenderer renderer) {
        mSurfaceTexture = surfaceTexture;
        mRenderer = renderer;
    }

    private void initGL() {
        mEgl = (EGL10) EGLContext.getEGL();

        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay() failed "
                                       + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize() failed " +
                                       GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int[] configAttribs = {
            EGL10.EGL_BUFFER_SIZE, 32,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_NONE
        };

        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, configAttribs, configs, 1, numConfigs) || numConfigs[0] == 0) {
            throw new RuntimeException("eglChooseConfig() failed");
        }

        int[] contextAttribs = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE };

        mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs);

        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], mSurfaceTexture, null);

        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEgl.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                throw new RuntimeException("eglCreateWindowSurface() returned EGL_BAD_NATIVE_WINDOW.");
            }
            throw new RuntimeException("eglCreateWindowSurface() failed "
                                       + GLUtils.getEGLErrorString(error));
        }

        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent() failed "
                                       + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        mGl = mEglContext.getGL();
    }

    void destroyGL() {
        if (DEBUG) Log.i(TAG, "destoryGL");
        mRenderer.release();

        mEgl.eglDestroyContext(mEglDisplay, mEglContext);
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
        mEglContext = EGL10.EGL_NO_CONTEXT;
        mEglSurface = EGL10.EGL_NO_SURFACE;
    }

    @Override
    public void run() {
        initGL();

        while (true) {
            try {
                while (!mRunning) {
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException ie) {

            }

            if (mFinished) {
                break;
            }

            boolean drawResult = false;
            if (mRenderer != null) {
                drawResult = mRenderer.drawFrame();
            }
            if (!mRunning || !drawResult) {
                continue;
            }

            mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
            //Assert.assertEquals(EGL10.EGL_SUCCESS, mEgl.eglGetError());
        }

        destroyGL();
    }

    public void stopRendering() {
        mRunning = false;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void startRendering() {
        if (!mRunning) {
            synchronized (this) {
                mRunning = true;
                notify();
            }
        }
    }

    public void finish() {
        if (!mFinished) {
            synchronized (this) {
                mFinished = true;
            }
        }
    }
}

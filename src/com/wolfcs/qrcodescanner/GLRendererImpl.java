package com.wolfcs.qrcodescanner;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.wolfcs.qrcodescanner.GLProducerThread.GLRenderer;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

public class GLRendererImpl implements GLRenderer {
    private static final String TAG = "GLRendererImpl";

    private static final String sSimpleVS =
            "attribute vec4 position;\n" +
            "attribute vec2 texCoords;\n" +
            "varying vec2 outTexCoords;\n" +
            "\nvoid main(void) {\n" +
            "    outTexCoords = texCoords;\n" +
            "    gl_Position = position;\n" +
            "}\n\n";
    private static final String sSimpleFS =
            "precision mediump float;\n\n" +
            "varying vec2 outTexCoords;\n" +
            "uniform sampler2D texture;\n" +
            "\nvoid main(void) {\n" +
            "    gl_FragColor = texture2D(texture, outTexCoords);\n" +
            "}\n\n";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
             1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
            -1.0f,  1.0f, 0.0f, 0.0f, 0.0f,
             1.0f,  1.0f, 0.0f, 1.0f, 0.0f,
    };

    private int mTextureId = -1;
    private int mProgram = -1;
    private int mAttribPosition;
    private int mAttribTexCoords;
    private int mUniformTexture;

    GLRendererImpl() {
//        mThermalSensor = thermalSensor;
    }

    private void createProgram() {
        long createProgramStartTime = System.currentTimeMillis();
        mProgram = buildProgram(sSimpleVS, sSimpleFS);

        mAttribPosition = glGetAttribLocation(mProgram, "position");
        checkGlError();

        mAttribTexCoords = glGetAttribLocation(mProgram, "texCoords");
        checkGlError();

        mUniformTexture = glGetUniformLocation(mProgram, "texture");
        checkGlError();

        Log.i(TAG, "Time to create program: "
            + ((System.currentTimeMillis() - createProgramStartTime) / 1000.0f)
            + " seconds");
    }

    private Bitmap getBitmap() {
        return null;
    }

    public boolean drawFrame() {
        if (mProgram == -1) {
            createProgram();
        }

        if (mTextureId == -1) {
            mTextureId = loadTexture();
        }

        FloatBuffer triangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        triangleVertices.put(mTriangleVerticesData).position(0);

        glBindTexture(GL_TEXTURE_2D, mTextureId);
        checkGlError();

        Bitmap bitmap = getBitmap();
        if (bitmap == null) {
            return false;
        }

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap, GL_UNSIGNED_BYTE, 0);
        checkGlError();

        bitmap.recycle();

        glUseProgram(mProgram);
        checkGlError();

        glEnableVertexAttribArray(mAttribPosition);
        checkGlError();

        glEnableVertexAttribArray(mAttribTexCoords);
        checkGlError();

        glUniform1i(mUniformTexture, 0);
        checkGlError();

        // drawQuad
        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        glVertexAttribPointer(mAttribPosition, 3, GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        checkGlError();

        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        glVertexAttribPointer(mAttribTexCoords, 3, GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        checkGlError();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        checkGlError();

        {
            glClear(GL_COLOR_BUFFER_BIT);
            checkGlError();

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            checkGlError();
        }

       // Log.i(TAG, "Time to display texture: "
       //     + ((System.currentTimeMillis() - displayStartTime) / 1000.0f)
       //     + " seconds");

        return true;
    }

    public int loadTexture() {
        int textureIds[] = new int[1];

        glActiveTexture(GL_TEXTURE0);
        glGenTextures(1, textureIds, 0);
        checkGlError();

        int textureId = textureIds[0];
        glBindTexture(GL_TEXTURE_2D, textureId);
        checkGlError();

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureId;
    }

    private static int buildProgram(String vertex, String fragment) {
        int vertexShader = buildShader(vertex, GL_VERTEX_SHADER);
        if (vertexShader == 0) return 0;

        int fragmentShader = buildShader(fragment, GL_FRAGMENT_SHADER);
        if (fragmentShader == 0) return 0;

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        checkGlError();

        glAttachShader(program, fragmentShader);
        checkGlError();

        glLinkProgram(program);
        checkGlError();

        int[] status = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, status, 0);
        if (status[0] != GL_TRUE) {
            String error = glGetProgramInfoLog(program);
            Log.w(TAG, "Error while linking program:\n" + error);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            glDeleteProgram(program);
            return 0;
        }

        return program;
    }
    
    private static int buildShader(String source, int type) {
        int shader = glCreateShader(type);

        glShaderSource(shader, source);
        checkGlError();

        glCompileShader(shader);
        checkGlError();

        int[] status = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, status, 0);
        if (status[0] != GL_TRUE) {
            String error = glGetShaderInfoLog(shader);
            Log.w(TAG, "Error while compiling shader:\n" + error);
            glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private static void checkGlError() {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            Log.w(TAG, "GL error = 0x" + Integer.toHexString(error));
        }
    }

    public void release () {
        if (mProgram != -1) {
            glDeleteProgram(mProgram);
            mProgram = -1;
        }

        if (mTextureId == -1) {
            int textureIds[] = {mTextureId};
            glDeleteTextures(1, textureIds, 0);
            mTextureId = -1;
        }
    }
}

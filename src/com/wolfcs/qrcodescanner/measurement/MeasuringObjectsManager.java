package com.wolfcs.qrcodescanner.measurement;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;

public class MeasuringObjectsManager {
    private static volatile MeasuringObjectsManager mInstance;

    private MeasuringObjectsManager() {
        initMeasuringPoints();
        initMeasuringLine();
        initMeasuringRect();
    }

    public static MeasuringObjectsManager getInstance() {
        if (mInstance == null) {
            synchronized (MeasuringObjectsManager.class) {
                if (mInstance == null) {
                    mInstance = new MeasuringObjectsManager();
                }
            }
        }
        return mInstance;
    }

    private int mMaxMeasuringPointNum;
    private ArrayList<MeasuringPoint> mUsingPoints;
    private ArrayList<MeasuringPoint> mUsablePoints;
    private ArrayList<MeasuringPoint> mSelectedPoints;

    private void initMeasuringPoints() {
        mUsingPoints = new ArrayList<MeasuringPoint>();
        mUsablePoints = new ArrayList<MeasuringPoint>();
        mSelectedPoints = new ArrayList<MeasuringPoint>();
    }

    public void setMaxMeasuringPointNum(int maxMeasuringPoint) {
        mMaxMeasuringPointNum = maxMeasuringPoint;
    }

    public MeasuringPoint createNewMeasuringPoint(Context context, int width,
            int height) {
        if (mUsingPoints.size() >= mMaxMeasuringPointNum) {
            return null;
        }
        MeasuringPoint point;
        if (!mUsablePoints.isEmpty()) {
            point = mUsablePoints.get(0);
            mUsablePoints.remove(0);
        } else {
            point = new MeasuringPoint(context, width, height, mUsingPoints.size());
        }

        mUsingPoints.add(point);
        return point;
    }

    public MeasuringPoint selectUsingPoint(int x, int y) {
        for (MeasuringPoint point : mUsingPoints) {
            if (point.containOnViewPoint(x, y)) {
                return point;
            }
        }
        return null;
    }

    public boolean isPointSelected(MeasuringPoint point) {
        return mSelectedPoints.contains(point);
    }

    public void selectPoint(MeasuringPoint point) {
        mSelectedPoints.add(point);
    }

    public void deselectPoint(MeasuringPoint point) {
        mSelectedPoints.remove(point);
    }

    public int getSelectedMeasuringPointsSize() {
        return mSelectedPoints.size();
    }

    public void drawPointsOnView(Canvas canvas) {
        for (MeasuringPoint point : mUsingPoints) {
            if (!mSelectedPoints.contains(point)) {
                point.drawOnView(canvas);
            } else {
                point.drawSelectedOnView(canvas);
            }
        }
    }

    public void clearSelectedPoints() {
        for (MeasuringPoint point : mSelectedPoints) {
            mUsingPoints.remove(point);
            recycle(point);
        }
        mSelectedPoints.clear();
    }

    public void selectAllUsingPoints() {
        for (MeasuringPoint point : mUsingPoints) {
            if (!mSelectedPoints.contains(point)) {
                mSelectedPoints.add(point);
            }
        }
    }

    public void cancelOpOnPoints() {
        mSelectedPoints.clear();
    }

    public void drawPointsOnRealWorldObjectImage(Canvas canvas) {

    }

    public void recycle(MeasuringPoint point) {
        mUsingPoints.remove(point);
        mUsablePoints.add(point);
    }

    // Line management

    private int mMaxMeasuringLineNum;
    private Vector<MeasuringLine> mUsingMeasuringLines;
    private Vector<MeasuringLine> mUsableMeasuringLines;
    private Vector<MeasuringLine> mSelectedMeasuringLines;

    private void initMeasuringLine() {
        mUsingMeasuringLines = new Vector<MeasuringLine>();
        mUsableMeasuringLines = new Vector<MeasuringLine>();
        mSelectedMeasuringLines = new Vector<MeasuringLine>();
    }

    public void setMaxMeasuringLineNum(int maxMeasuringLineNum) {
        mMaxMeasuringLineNum = maxMeasuringLineNum;
    }

    public MeasuringLine createNewMeasuringLine(Context context, int width,
            int height) {
        if (mUsingMeasuringLines.size() >= mMaxMeasuringLineNum) {
            return null;
        }
        MeasuringLine line;
        if (!mUsableMeasuringLines.isEmpty()) {
            line = mUsableMeasuringLines.get(0);
        } else {
            line = new MeasuringLine(context, mUsingMeasuringLines.size(),
                    width, height);
            mUsingMeasuringLines.add(line);
        }
        return line;
    }

    public MeasuringLine selectMeasuringLine(float xPosition, float yPosition) {
        for (MeasuringLine line : mUsingMeasuringLines) {
            if (line.containOnViewPoint(xPosition, yPosition)) {
                return line;
            }
        }
        return null;
    }

    public boolean isLineSelected(MeasuringLine line) {
        return mSelectedMeasuringLines.contains(line);
    }

    public void selectLine(MeasuringLine line) {
        mSelectedMeasuringLines.add(line);
    }

    public void deselectLine(MeasuringLine line) {
        mSelectedMeasuringLines.remove(line);
    }

    public int getSelectedMeasuringLinesSize() {
        return mSelectedMeasuringLines.size();
    }

    public void drawLinesOnView(Canvas canvas) {
        for (MeasuringLine line : mUsingMeasuringLines) {
            if (!mSelectedMeasuringLines.contains(line)) {
                line.drawOnView(canvas);
            }
        }
        for (MeasuringLine line : mSelectedMeasuringLines) {
            line.drawSelectedOnView(canvas);
        }
    }

    public boolean canCreateNewLine() {
        return mUsingMeasuringLines.size() < mMaxMeasuringLineNum;
    }

    public void clearSelectedLines() {
        mSelectedMeasuringLines.clear();
        for (MeasuringLine line : mUsingMeasuringLines) {
            recycle(line);
        }
        mUsingMeasuringLines.clear();
    }

    public void selectAllLines() {
        for (MeasuringLine line : mUsingMeasuringLines) {
            mSelectedMeasuringLines.add(line);
        }
    }

    public void cancelOpOnLines() {
        mSelectedMeasuringLines.clear();
    }

    public void drawLinesOnRealWorldObjectImage(Canvas canvas) {
        for (MeasuringLine line : mUsingMeasuringLines) {
            line.drawOnRealWorldObjectImage(canvas);
        }
    }

    public void recycle(MeasuringLine line) {
        if (line != null && !mUsableMeasuringLines.contains(line)) {
            mUsableMeasuringLines.add(line);
        }
    }

    // Rect management
    private static final int LINE_WIDTH = 3;

    private int mMaxMeasuringRectNum;

    private Vector<MeasuringRect> mUsingMeasuredRects;
    private Vector<MeasuringRect> mUsableMeasuredRects;
    private Vector<MeasuringRect> mSelectedMeasuredRects;

    private Paint mPaint;

    private void initMeasuringRect() {
        mUsableMeasuredRects = new Vector<MeasuringRect>();
        mUsingMeasuredRects = new Vector<MeasuringRect>();
        mSelectedMeasuredRects = new Vector<MeasuringRect>();

        mPaint = new Paint();
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(LINE_WIDTH);
        // Must manually scale the desired text size to match screen density
        mPaint.setColor(Color.WHITE);
    }

    public void setMaxMeasuringRectNum(int maxMeasuringRectNum) {
        mMaxMeasuringRectNum = maxMeasuringRectNum;
    }

    public MeasuringRect createNewMeasuringRect(Context context,
            int width, int height) {
        synchronized (mUsingMeasuredRects) {
            if(mUsingMeasuredRects.size() >= mMaxMeasuringRectNum) {
                return null;
            }
            MeasuringRect rect;
            if(!mUsableMeasuredRects.isEmpty()) {
                rect = mUsableMeasuredRects.get(0);
                mUsableMeasuredRects.remove(0);
            } else {
                rect = new MeasuringRect(context, mPaint, mUsingMeasuredRects.size(),
                        width, height);
            }
            mUsingMeasuredRects.add(rect);
            return rect;
        }
    }

    public MeasuringRect selectMeasuredRect(Point point) {
        MeasuringRect measuredRect = null;
        synchronized (mUsingMeasuredRects) {
            for (int i = 0; i < mUsingMeasuredRects.size(); i++) {
                MeasuringRect rect = mUsingMeasuredRects.get(i);
                if (rect.containOnViewPoint(point)) {
                    if(measuredRect == null || measuredRect.getIndex() < rect.getIndex()) {
                        measuredRect = rect;
                    }
                }
            }
        }
        return measuredRect;
    }

    public boolean isRectSelected(MeasuringRect rect) {
        return mSelectedMeasuredRects.contains(rect);
    }

    public void selectRect(MeasuringRect rect) {
        mSelectedMeasuredRects.add(rect);
    }

    public void deselectRect(MeasuringRect rect) {
        mSelectedMeasuredRects.remove(rect);
    }

    public int getSelectedMeasuringRectsSize() {
        return mSelectedMeasuredRects.size();
    }

    public void drawRectsOnView(Canvas canvas) {
        synchronized (mUsingMeasuredRects) {
            for (MeasuringRect rect : mUsingMeasuredRects) {
                if(mSelectedMeasuredRects.contains(rect)) {
                    rect.drawSelectedOnView(canvas);
                } else {
                    rect.drawOnView(canvas);
                }
            }
        }
    }

    public boolean canCreateNewRect() {
        return mUsingMeasuredRects.size() < mMaxMeasuringRectNum;
    }

    public void clearSelectedRects() {
        synchronized (mUsingMeasuredRects) {
            for (MeasuringRect rect : mSelectedMeasuredRects) {
                recycle(rect);
                mUsingMeasuredRects.remove(rect);
            }
            mSelectedMeasuredRects.clear();
        }
    }

    public void selectAllRects() {
        synchronized (mUsingMeasuredRects) {
            for (MeasuringRect rect : mUsingMeasuredRects) {
                if(!mSelectedMeasuredRects.contains(rect)) {
                    mSelectedMeasuredRects.add(rect);
                }
            }
        }
    }

    public void cancelOpOnRects() {
        mSelectedMeasuredRects.clear();
    }

    public void drawRectsOnRealWorldObjectImage(Canvas canvas) {
        synchronized (mUsingMeasuredRects) {
            for (MeasuringRect rect : mUsingMeasuredRects) {
                rect.drawOnRealWorldObjectImage(canvas);
            }
        }
    }

    public void recycle(MeasuringRect rect) {
        mUsableMeasuredRects.add(rect);
    }

    public void drawOnView(Canvas canvas) {
        drawPointsOnView(canvas);
        drawLinesOnView(canvas);
        drawRectsOnView(canvas);
    }

    public void drawOnRealWorldObjectImage(Canvas canvas) {
        drawPointsOnRealWorldObjectImage(canvas);
        drawLinesOnRealWorldObjectImage(canvas);
        drawRectsOnRealWorldObjectImage(canvas);
    }
}

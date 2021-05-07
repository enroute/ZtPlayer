package com.ztfun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.ztfun.math.Point3;
import com.ztfun.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ZtPlotView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = ZtPlotView.class.getSimpleName();

    public ZtPlotView(Context context) {
        super(context);
        setup();
    }

    public ZtPlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ZtPlotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public ZtPlotView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    private void setup() {
        // getHolder().addCallback(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw");
        drawView(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        Log.d(TAG, "dispatchDraw");
        drawView(canvas);
    }

    public void drawView(Canvas canvas) {
        canvas.save();
        for (DataSetProperty dsp : dataSets) {
            if (dsp.dataSet.data.size() <= 0) {
                continue;
            } else if (dsp.dataSet.data.size() == 1) {
                Point3 point = dsp.dataSet.data.get(0);
                canvas.drawPoint((float) point.x, (float) point.y, dsp.paint);
                continue;
            }

            int i = 0;
            Point3 point1 = toCanvasCoordinate(dsp.dataSet.data.get(i));
            for (i = 1; i < dsp.dataSet.data.size(); i++) {
                Point3 point2 = toCanvasCoordinate(dsp.dataSet.data.get(i));
                canvas.drawLine((float) point1.x, (float) point1.y, (float) point2.x, (float) point2.y, dsp.paint);
                Log.d(TAG, "draw line from " + point1 + " to " + point2);
                point1 = point2;
            }
        }

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // x & y range
    private double x1, x2, y1, y2, dx, dy;

    public void setRange(double x1, double x2, double y1, double y2) {
        this.x1 = x1; this.x2 = x2; dx = this.x2 - this.x1;
        this.y1 = y1; this.y2 = y2; dy = this.y2 - this.y1;
    }

    // padding, in pixels
    private int paddingTop, paddingBottom, paddingLeft, paddingRight, drawWidth, drawHeight;

    public void setPadding(int padding) {
        setPadding(padding, padding, padding, padding);
    }

    public void setPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    private Point3 toCanvasCoordinate(Point3 dataPoint) {
        Point3 canvasPoint = new Point3();
        // x1 <-> paddingLeft, x2 <-> canvas.width - paddingRight,  (x-x1)/(x2-x1) = (X-left)/(right-left)
        // y1 <-> paddingTop, y2 <-> canvas.height - paddingBottom
        canvasPoint.x = paddingLeft + drawWidth * (dataPoint.x - x1) / dx;
        canvasPoint.y = paddingTop + drawHeight * (y2 - dataPoint.y) / dy;

        return canvasPoint;
    }

    private Point3 toDataCoordinate(double canvasX, double canvasY) {
        Point3 dataPoint = new Point3();
        // x1 <-> paddingLeft, x2 <-> canvas.width - paddingRight,  (x-x1)/(x2-x1) = (X-left)/(right-left)
        // y1 <-> paddingTop, y2 <-> canvas.height - paddingBottom
        dataPoint.x = x1 + dx * (canvasX - paddingLeft) / drawWidth;
        dataPoint.y = y2 - dy * (canvasY - paddingTop) / drawHeight;

        return dataPoint;
    }

    private final List<DataSetProperty> dataSets = new ArrayList<>();

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        holder.addCallback(this);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Width=" + width + ", height=" + height);
        drawWidth = width - paddingLeft - paddingRight;
        drawHeight = height - paddingTop - paddingBottom;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public enum AxisType {
        AXIS_LEFT,
        AXIS_RIGHT
    }

    public void addDataSet(DataSet dataSet, Paint paint, AxisType axisType) {
        dataSets.add(new DataSetProperty(dataSet, paint, axisType));

        Log.d(TAG, "invalidate after data set changed.");
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // if ui thread
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public static class DataSetProperty {
        public DataSet dataSet;
        public Paint paint;
        public AxisType axisType;

        public DataSetProperty(DataSet dataSet, Paint paint, AxisType axisType) {
            this.dataSet = dataSet;
            this.paint = paint;
            this.axisType = axisType;
        }
    }

    public static class DataSet {
        public List<Point3> data = new ArrayList<>();

        public void addData(double[] val) { addData(new Point3(val)); }
        public void addData(Point3 point3) { data.add(point3); }
    }

    abstract public static class Marker {
        protected int size = 1;
        protected Paint paint;
        public Marker(int size, Paint paint) {
            this.size = size;
            this.paint = paint;
        }

        abstract public void draw(Canvas canvas, Point pt);
    }

    public static class MarkerDot extends Marker {
        public MarkerDot(int size, Paint paint) {
            super(size, paint);
        }

        @Override
        public void draw(Canvas canvas, Point pt) {
            canvas.drawCircle(pt.x, pt.y, size, paint);
        }
    }
}

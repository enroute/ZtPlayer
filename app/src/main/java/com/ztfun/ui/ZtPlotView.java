package com.ztfun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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
        getHolder().addCallback(this);
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

    // font size, which could be obtained by
    // getResources().getDimensionPixelSize(R.dimen.myFontSize);
    private int fontScaledSize = -1;
    public void setFontScaledSize(int fontScaledSize) {
        this.fontScaledSize = fontScaledSize;
    }

    private void drawGrids(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(fontScaledSize <= 0 ? (float) (12 * Math.sqrt(canvas.getWidth() * canvas.getHeight()) / 250)
                : fontScaledSize);

        if (xTicks != null) {
            for (int i = 0; i < xTicks.length; i ++) {
                Point3 pt = toCanvasCoordinate(new Point3(xTicks[i], 0, 0));
                canvas.drawLine((float)pt.x, height - paddingBottom, (float)pt.x, paddingTop, paint);

                // xlabel
                if (xLabels != null && xLabels.length >= i) {
                    //canvas.drawText(xLabels[i], (float)pt.x, (float)pt.y, paint);
                    drawText(canvas, xLabels[i], (float)pt.x, (float)pt.y, paint, TEXT_ALIGN_CENTER_HORIZONTAL | TEXT_ALIGN_TOP);
                }
            }
        }

        if (yTicks != null) {
            paint.setTextAlign(Paint.Align.RIGHT);
            for (int i = 0; i < yTicks.length; i ++) {
                Point3 pt = toCanvasCoordinate(new Point3(0, yTicks[i], 0));
                canvas.drawLine(paddingLeft, (float)pt.y, width - paddingRight, (float)pt.y, paint);

                if (yLabels != null && yLabels.length >= i) {
                    // canvas.drawText(yLabels[i], (float)pt.x, (float)pt.y, paint);
                    drawText(canvas, yLabels[i], (float)pt.x, (float)pt.y, paint, TEXT_ALIGN_CENTER_VERTICAL | TEXT_ALIGN_RIGHT);
                }
            }
        }
    }

    public void drawView(Canvas canvas) {
        canvas.save();

        drawGrids(canvas);

        // clip with an extra pixel to include the border
        canvas.clipRect(paddingLeft - 1, paddingTop - 1,
                getWidth() - paddingRight + 1, getHeight() - paddingBottom + 1);

        for (DataSetProperty dsp : dataSets) {
            if (dsp.dataSet.data.size() <= 0) {
                continue;
            } else if (dsp.dataSet.data.size() == 1) {
                Point3 point = dsp.dataSet.data.get(0);
                dsp.marker.draw(canvas, point.x, point.y);
                continue;
            }

            int i = 0;
            Point3 point1 = toCanvasCoordinate(dsp.dataSet.data.get(i));
            dsp.marker.draw(canvas, point1.x, point1.y);
            for (i = 1; i < dsp.dataSet.data.size(); i++) {
                Point3 point2 = toCanvasCoordinate(dsp.dataSet.data.get(i));
                canvas.drawLine((float) point1.x, (float) point1.y, (float) point2.x, (float) point2.y, dsp.paint);
                dsp.marker.draw(canvas, point2.x, point2.y);
                // Log.d(TAG, "draw line from " + point1 + " to " + point2 + ", origin=" + dsp.dataSet.data.get(i));
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
    private int paddingTop, paddingBottom, paddingLeft, paddingRight, drawWidth, drawHeight,
            width, height;

    public void setPadding(int padding) {
        setPadding(padding, padding, padding, padding);
    }

    public void setPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    private double[] xTicks, yTicks, yTicksRight;
    private String[] xLabels, yLabels, yLabelsRight;
    public void setXTicks(double... xTicks) {
        this.xTicks = xTicks;
    }
    public void setXLabels(String... xLabels) {
        this.xLabels = xLabels;
    }
    public void setYTicks(double... yTicks) {
        this.yTicks = yTicks;
    }
    public void setYTicksRight(double... yTicksRight) {
        this.yTicksRight = yTicksRight;
    }
    public void setYLabels(String... yLabels) {
        this.yLabels = yLabels;
    }
    public void setYLabelsRight(String... yLabelsRight) {
        this.yLabelsRight = yLabelsRight;
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
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Width=" + width + ", height=" + height);
        this.width = width; this.height = height;

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

    public void addDataSet(DataSet dataSet, Paint paint, AxisType axisType, Marker marker) {
        dataSets.add(new DataSetProperty(dataSet, paint, axisType, marker));

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
        public Marker marker;

        public DataSetProperty(DataSet dataSet, Paint paint, AxisType axisType, Marker marker) {
            this.dataSet = dataSet;
            this.paint = paint;
            this.axisType = axisType;
            // if null, make a marker which does nothing
            this.marker = marker != null ? marker : new Marker(0, null);
        }
    }

    public static class DataSet {
        public List<Point3> data = new ArrayList<>();

        public void addData(double[] val) { addData(new Point3(val)); }
        public void addData(Point3 point3) { data.add(point3); }
    }

    public static class Marker {
        protected int size = 1;
        protected Paint paint;
        public Marker(int size, Paint paint) {
            this.size = size;
            this.paint = paint;
        }

        public void draw(Canvas canvas, double x, double y) {}
    }

    public static class MarkerDot extends Marker {
        public MarkerDot(int size, Paint paint) {
            super(size, paint);
        }

        @Override
        public void draw(Canvas canvas, double x, double y) {
            canvas.drawCircle((float)x, (float)y, size, paint);
        }
    }

    public static class MarkerPolygon extends Marker {
        protected Point3[] vertices = null;
        public MarkerPolygon(int size, Paint paint) {
            super(size, paint);
        }

        @Override
        public void draw(Canvas canvas, double x, double y) {
            canvas.drawPath(pathByVertices(vertices, new Point3(x, y, 0)), paint);
        }
    }

    // square marker
    public static class MarkerSquare extends Marker {
        public MarkerSquare(int size, Paint paint) {
            super(size, paint);
        }

        @Override
        public void draw(Canvas canvas, double x, double y) {
            canvas.drawRect((float)(x - size), (float)(y - size), (float)(x + size), (float)(y + size), paint);
        }
    }

    // triangle marker
    public static class MarkerTriangle extends MarkerPolygon {
        public MarkerTriangle(int size, Paint paint) {
            super(size, paint);
            vertices = new Point3[3];
            vertices[0] = new Point3(0, -1 * size, 0);
            vertices[1] = new Point3( 0.86602540378443864676372317075294 * size, 0.5 * size, 0);
            vertices[2] = new Point3(-0.86602540378443864676372317075294 * size, 0.5 * size, 0);
        }
    }

    // 10-points five star marker
    public static class MarkerStar extends MarkerPolygon {
        public MarkerStar(int size, Paint paint) {
            super(size, paint);
            vertices = new Point3[10];
            vertices[0] = new Point3(0, -1 * size, 0);
            vertices[1] = new Point3( 0.22451398828979268622097257589876 * size, -0.30901699437494742410229341718282 * size, 0);
            vertices[2] = new Point3( 0.95105651629515357211643933337938 * size, -0.30901699437494742410229341718282 * size, 0);
            vertices[3] = new Point3( 0.36327126400268044294773337874031 * size,  0.0020600717455570722112217196956 * size, 0);
            vertices[4] = new Point3( 0.58778525229247312916870595463907 * size,  0.80901699437494742410229341718282 * size, 0);
            vertices[5] = new Point3( 0, 0.38196601125010515179541316563436 * size, 0);
            vertices[6] = new Point3(-0.58778525229247312916870595463907 * size,  0.80901699437494742410229341718282 * size, 0);
            vertices[7] = new Point3(-0.36327126400268044294773337874031 * size,  0.0020600717455570722112217196956 * size, 0);
            vertices[8] = new Point3(-0.95105651629515357211643933337938 * size, -0.30901699437494742410229341718282 * size, 0);
            vertices[9] = new Point3(-0.22451398828979268622097257589876 * size, -0.30901699437494742410229341718282 * size, 0);
        }
    }

    private static Path pathByVertices(Point3[] vertices, Point3 offset) throws IndexOutOfBoundsException {
        Path path = new Path();

        path.moveTo((float)(offset.x + vertices[0].x), (float)(offset.y + vertices[0].y));
        for (int i = 1; i < vertices.length; i ++) {
            path.lineTo((float) (offset.x + vertices[i].x), (float) (offset.y + vertices[i].y));
        }
        path.close();

        return path;
    }

    public static final int TEXT_ALIGN_CENTER_HORIZONTAL = 1;
    public static final int TEXT_ALIGN_LEFT = 2;
    public static final int TEXT_ALIGN_RIGHT = 4;
    public static final int TEXT_ALIGN_CENTER_VERTICAL = 8;
    public static final int TEXT_ALIGN_TOP = 16;
    public static final int TEXT_ALIGN_BOTTOM = 32;


    private static void drawText(Canvas canvas, String text, float x, float y, Paint paint, int align) {
        Paint.Align oldAlign = paint.getTextAlign();

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);

        if ((align & TEXT_ALIGN_CENTER_HORIZONTAL) != 0) {
            paint.setTextAlign(Paint.Align.CENTER);
        }
        if ((align & TEXT_ALIGN_LEFT) != 0) {
            paint.setTextAlign(Paint.Align.LEFT);
        }
        if ((align & TEXT_ALIGN_RIGHT) != 0) {
            paint.setTextAlign(Paint.Align.RIGHT);
        }
        if ((align & TEXT_ALIGN_CENTER_VERTICAL) != 0) {
            y += rect.height() / 2.0;
        }
        if ((align & TEXT_ALIGN_TOP) != 0) {
            y += rect.height();
        }
        if ((align & TEXT_ALIGN_BOTTOM) != 0) {
            // pass
        }

        canvas.drawText(text, x, y, paint);
        // restore text alignment
        paint.setTextAlign(oldAlign);
    }
}

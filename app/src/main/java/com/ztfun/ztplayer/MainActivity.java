package com.ztfun.ztplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ztfun.retrofit.cryptonator.Crypto;
import com.ztfun.retrofit.cryptonator.Cryptonator;
import com.ztfun.retrofit.cryptonator.IResultCallback;
import com.ztfun.ui.ZtPlotView;
import com.ztfun.util.Log;

import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        // startActivity(new Intent(this, CameraActivity.class));
        test();

        Glide.with(this).load("https://img-home.csdnimg.cn/images/20201124032511.png")
                .into((ImageView) findViewById(R.id.logo));

        ZtPlotView osillo = findViewById(R.id.oscillo);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);

        ZtPlotView.DataSet dataSet = new ZtPlotView.DataSet();
//        dataSet.addData(new double[]{0, 0});
//        dataSet.addData(new double[]{5, 2});
//        dataSet.addData(new double[]{10, 8});
//        dataSet.addData(new double[]{15, 6});
        osillo.setRange(0, 100, 0, 10);
        osillo.setPadding(50);

        ZtPlotView.DataSet dataSetSin = new ZtPlotView.DataSet();
        ZtPlotView.DataSet dataSetCos = new ZtPlotView.DataSet();

        Paint paintMarker = new Paint();
        paintMarker.setColor(Color.WHITE);
        paintMarker.setStyle(Paint.Style.FILL);

        Paint paint2 = new Paint();
        paint2.setColor(Color.MAGENTA);
        paint2.setStrokeWidth(8);

        Paint paint3 = new Paint();
        paint3.setColor(Color.CYAN);
        paint3.setStrokeWidth(2);

        //osillo.setXTicks(0,10,20,40,60,80,100);
        //osillo.setXLabels("0", "10", "20", "40", "60", "80", "100");
        osillo.setXLabelsFixed("-50", "-40", "-30", "-20", "-10", "0");

        osillo.setYTicks(0,2,4,6,8,10);
        osillo.setYLabels("", "2", "4", "6", "8", "10");
        osillo.setYLabelsRight("Apple", "Berry", "Carrot", "Dumplings", "Eggplant", "Fruits");

        osillo.addDataSet(dataSet, paint, ZtPlotView.AxisType.AXIS_LEFT, new ZtPlotView.MarkerStar(8, paintMarker));
        osillo.addDataSet(dataSetSin, paint2, ZtPlotView.AxisType.AXIS_LEFT, null);
        osillo.addDataSet(dataSetCos, null, ZtPlotView.AxisType.AXIS_LEFT, new ZtPlotView.MarkerBar(10, paintMarker, osillo));

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.plot_font_size));
        osillo.setTextPaint(textPaint);

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        osillo.setGridPaint(gridPaint);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int x = 0;
                Random random = new Random();
                while (true) {
                    dataSet.addData(new double[]{x, random.nextInt(9)});
                    dataSetSin.addData(new double[]{x, Math.sin(x * 2 * Math.PI / 100) * 4 + 4});
                    dataSetCos.addData(new double[]{x, Math.cos(x * 2 * Math.PI / 100) * 4 + 4});
                    x++;
//                    if (x > 20) {
//                        osillo.setRange(Math.max(x - 100, 0), x, 0, 10);
//                    }
                    osillo.setRange(x - 100, x, 0, 10);
                    osillo.postInvalidate();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void test() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        Cryptonator cryptonator = new Cryptonator(client);
        cryptonator.call(new IResultCallback() {
            @Override
            public void onNext(List<Crypto.Market> marketList) {
                for(Crypto.Market market : marketList) {
                    Log.d(market.toString());
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
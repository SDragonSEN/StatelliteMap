package com.example.satellitemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static android.location.GnssStatus.*;

public class MainActivity extends AppCompatActivity {
    private LocationManager mLocationManager = null;
    private TextView mTextView;
    private Handler mHandler;
    private MyView myView;
    private ArrayList<MyStatellite> myStatellites = new ArrayList<MyStatellite>();
    private SensorManager mSensorManager;
    private float mRotateDegree;//弧度制

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        DisplayMetrics dm =getResources().getDisplayMetrics();

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLyout);

        myView = new MyView(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dm.widthPixels, dm.widthPixels);
        myView.setBackgroundColor(Color.parseColor(MyView.BACKGROUND_COLOR1));
        myView.setLayoutParams(lp);

        linearLayout.addView(myView, 0);

        mTextView = (TextView)findViewById(R.id.test);
        mHandler = new Handler(getMainLooper());

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
         || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        //每隔1S请求一次,(0为距离)
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 0, mLocationListener);
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL );
    }
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    GnssStatus.Callback mGnssStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onStarted() {
            super.onStarted();
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }

        @Override
        public void onFirstFix(int ttffMillis) {
            super.onFirstFix(ttffMillis);
        }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            super.onSatelliteStatusChanged(status);

            int numb = status.getSatelliteCount();

            if ( numb == 0)
                return;

            final StringBuffer sb = new StringBuffer();

            myStatellites.clear();

            sb.append("卫星状态：\n");
            for (int i = 0; i < numb; i++){
                sb.append(Integer.toString(i));

                sb.append(":\n");

                sb.append("ID:\t");
                sb.append(Integer.toString(status.getSvid(i)));
                sb.append("\n");

                sb.append("方位角:\t");
                sb.append(Float.toString(status.getAzimuthDegrees(i)));
                sb.append("\n");

                sb.append("仰角:\t");
                sb.append(Float.toString(status.getElevationDegrees(i)));
                sb.append("\n");

                sb.append("卫星类型:\t");
                switch (status.getConstellationType(i)){
                    case CONSTELLATION_BEIDOU:
                        sb.append("北斗(中国)");
                        break;
                    case CONSTELLATION_GALILEO:
                        sb.append("伽利略(欧洲)");
                        break;
                    case CONSTELLATION_GLONASS:
                        sb.append("格洛纳斯(俄罗斯)（原名:GLONASS,GLOBAL NAVIGATION SATELLITE SYSTEM,全球卫星导航系统）");
                        break;
                    case CONSTELLATION_GPS:
                        sb.append("GPS(美国)(Global Positioning System,全球定位系统)");
                        break;
                    case CONSTELLATION_QZSS:
                        sb.append("准天顶(日本)(原名:じゅんてんちょうえいせいシステム,准天顶卫星系统)");
                        break;
                    case CONSTELLATION_SBAS:
                        sb.append("SBAS(无国别)(原名:Satellite-Based Augmentation System,星基增强系统)");
                        break;
                    case CONSTELLATION_UNKNOWN:
                    default:
                        sb.append("未知");
                        break;
                }
                sb.append("\n");
                MyPosition myPosition = new MyPosition(status.getConstellationType(i), status.getElevationDegrees(i), status.getAzimuthDegrees(i));
                sb.append("radium:\t");
                sb.append(myPosition.radium);
                sb.append("\n");

                myStatellites.add(new MyStatellite(status.getConstellationType(i),status.getSvid(i),status.getAzimuthDegrees(i),myPosition.radium));

                myView.invalidate();
            }
            sb.append("\n");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(sb.toString());
                }
            };
            mHandler.post(r);
        }
    };
    SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] geomagnetic = new float[3];//用来保存地磁传感器的值
        float[] gravity = new float[3];
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values;

                float[] r = new float[9];
                float[] values = new float[3];

                SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);

                SensorManager.getOrientation(r, values);

                mRotateDegree = (float)Math.toDegrees(values[0]) * -1;

                myView.invalidate();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };
    class MyView extends View{
        public static final String BACKGROUND_COLOR1 = "#004400";
        public static final String BACKGROUND_COLOR2 = "#00aa00";

        public static final String LINE_COLOR1 = "#002200";
        public static final String LINE_COLOR2 = "#208020";

        public static final String SATELLITE_COLOR_BEIDU = "#cc0000";
        public static final String SATELLITE_COLOR_GPS = "#0000cc";
        public static final String SATELLITE_COLOR_GLONASS = "#00cccc";
        public static final String SATELLITE_COLOR_GALILEO = "#f19cc2";
        public static final String SATELLITE_COLOR_QZSS = "#ffffff";
        public static final String SATELLITE_COLOR_OTHER = "#999999";
        public static final int MAX_DISTANCE = 30000;

        private int mapRadium;
        private int mapCenterX;
        private int mapCenterY;
        public MyView(Context context){
            super(context);

            DisplayMetrics dm =getResources().getDisplayMetrics();
            mapRadium = dm.widthPixels / 2 - 75;
            mapCenterX = dm.widthPixels / 2;
            mapCenterY = dm.widthPixels / 2;
        }
        @Override
        public void onDraw(Canvas canvas){
            canvas.save();
            canvas.rotate(mRotateDegree, mapCenterX, mapCenterY);

            Paint paint = new Paint();

            paint.setColor(Color.YELLOW);
            paint.setTextSize(60);

            canvas.drawText("N",mapCenterX - 20, mapCenterY - mapRadium - 10, paint);
            canvas.drawText("S",mapCenterX - 20, mapCenterY + mapRadium + 50, paint);
            canvas.drawText("E",mapCenterX - mapRadium - 50, mapCenterY + 20, paint);
            canvas.drawText("W",mapCenterX + mapRadium + 10, mapCenterY + 20, paint);

            // 显示仪背景
            paint.setColor(Color.parseColor(BACKGROUND_COLOR2));
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mapCenterX, mapCenterY, mapRadium, paint);

            //内圆
            paint.setColor(Color.parseColor(LINE_COLOR2));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            canvas.drawCircle(mapCenterX, mapCenterY, (int)(mapRadium * 0.2), paint);
            canvas.drawCircle(mapCenterX, mapCenterY, (int)(mapRadium * 0.4), paint);
            canvas.drawCircle(mapCenterX, mapCenterY, (int)(mapRadium * 0.6), paint);
            canvas.drawCircle(mapCenterX, mapCenterY, (int)(mapRadium * 0.8), paint);

            //斜线
            canvas.drawLine(mapCenterX + mapRadium, mapCenterY, mapCenterX - mapRadium, mapCenterY, paint);
            canvas.drawLine(mapCenterX, mapCenterY + mapRadium, mapCenterX, mapCenterY - mapRadium, paint);
            float d = (float)(mapRadium * Math.sin(Math.PI / 4));
            canvas.drawLine(mapCenterX - d , mapCenterY - d, mapCenterX + d, mapCenterY + d, paint);
            canvas.drawLine(mapCenterX - d , mapCenterY + d, mapCenterX + d, mapCenterY - d, paint);

            //画中心点
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor(LINE_COLOR1));
            canvas.drawCircle(mapCenterX, mapCenterY, 10, paint);

            //外圆;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            canvas.drawCircle(mapCenterX, mapCenterY, mapRadium, paint);

            for (MyStatellite myStatellite : myStatellites){
                paint.setStrokeWidth(1);
                paint.setStyle(Paint.Style.FILL);
                switch (myStatellite.type) {
                    case CONSTELLATION_BEIDOU:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_BEIDU));
                        break;
                    case CONSTELLATION_GALILEO:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_GALILEO));
                        break;
                    case CONSTELLATION_GLONASS:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_GLONASS));
                        break;
                    case CONSTELLATION_GPS:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_GPS));
                        break;
                    case CONSTELLATION_QZSS:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_QZSS));
                        break;
                    case CONSTELLATION_SBAS:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_OTHER));
                        break;
                    case CONSTELLATION_UNKNOWN:
                    default:
                        paint.setColor(Color.parseColor(SATELLITE_COLOR_OTHER));
                        break;
                }
                float x = (float)(mapCenterX + Math.sin(myStatellite.azimuthDegrees * Math.PI / 180) * myStatellite.radium / MAX_DISTANCE * this.mapRadium);
                float y = (float)(mapCenterY - Math.cos(myStatellite.azimuthDegrees * Math.PI / 180) * myStatellite.radium / MAX_DISTANCE * this.mapRadium);
                canvas.drawCircle(x, y, 10, paint);
            }

            canvas.restore();

            super.onDraw(canvas);
        }
    }
}
class MyPosition{
    double  radium;
    float  azimuthDegrees;//方位角,角度制
    final int earthRadium = 6371;
    MyPosition(int gnssType, float elevationDegrees, float azimuthDegrees){
        this.azimuthDegrees = azimuthDegrees;

        int r1 = earthRadium;
        int r2 = earthRadium + getHeight(gnssType);
        double alpha = (elevationDegrees/180)*Math.PI;//转成弧度制

        double cosA_2_r1 = 2 * r1 * Math.cos(alpha + Math.PI / 2);
        double theta = Math.pow(cosA_2_r1,2) - 4 * (Math.pow(r1,2 ) - Math.pow(r2, 2));
        this.radium = (( cosA_2_r1 + Math.sqrt(theta) ) / 2) * Math.cos(alpha);
    }
    static int getHeight(int gnssType){
        switch (gnssType){
            case CONSTELLATION_BEIDOU:
                return 21500;
            case CONSTELLATION_GALILEO:
                return 23222;
            case CONSTELLATION_GLONASS:
                return 19100;
            case CONSTELLATION_GPS:
                return 20200;
            case CONSTELLATION_QZSS:
                //return 36786;
            case CONSTELLATION_SBAS:
            case CONSTELLATION_UNKNOWN:
            default:
                return 20000;
        }
    }
}
class MyStatellite{
    public int type;
    public int id;
    public float azimuthDegrees;//方位角,以北为正方向,角度制
    public double radium;
    public MyStatellite(int type, int id, float azimuthDegrees, double radium){
        this.type = type;
        this.id = id;
        this.azimuthDegrees = azimuthDegrees;
        this.radium = radium;
    }
}
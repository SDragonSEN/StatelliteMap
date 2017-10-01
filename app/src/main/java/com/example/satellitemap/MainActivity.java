package com.example.satellitemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import static android.location.GnssStatus.*;

public class MainActivity extends AppCompatActivity {
    private LocationManager mLocationManager = null;
    private TextView mTextView;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}

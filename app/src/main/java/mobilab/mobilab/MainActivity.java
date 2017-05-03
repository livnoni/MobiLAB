package mobilab.mobilab;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // sensors
    private static final String GPS = "gps";
    private static final String CAMERA = "camera";
    private static final String SMS = "sms";
    private static final String TEMPERATURE = "temperature";
    private static final String BATTERY = "battery";
    private static final String SOUND = "sound";
    private static final String BAROMETER = "barometer";
    private static final String EXTERNAL_SENSOR = "external sensor";
    private static final String RECEIVE = "receive data: ";
    private static final String LOCATION = "location: ";
    private static final String INTERVAL = "interval";
    private static final String RESOLUTION = "resolution";


    private HashMap<String, Object> _camera, _sms, _sound;
    private Boolean _barometer = false, _externalSensors = false, _temperature = false, _battery = false, _gps = false;

    //GPS:
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int GPS_minTime = 10000; //min time in milliseconds to show new gps single
    private static final int GPS_minDistance = 0; //min distance (in meters), to show new gps single
    private double altitude = 0, latitude = 0, longitude = 0;
    private TextView locationText;

    //Camera:

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private int PICtimeOut = 30000; // 30 sec
    private int widthResulution = 640; //default
    private int heightResulution = 480; //default
    //private int compressQuality = 10;    //3-100, 80 gives pic on 4 kb, its the best compress without loose high quality
    private String picPath;


    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Logger.append("take pic !");
                mCamera.startPreview(); //important! it allow to take multi pics!
                mCamera.takePicture(null, null, mPicture);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private boolean send = false;

    Runnable takePicRunnable = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (send) {
                        try {
                            wait(PICtimeOut);
                            handler.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationText = (TextView) (findViewById(R.id.locationText));

        incomingIntentData();
        initSensors();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private android.hardware.Camera getCameraInstance() {
        android.hardware.Camera camera = null;
        try {
            camera = android.hardware.Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                //Upload to server:
                //UpdateNewBitMap(pictureFile.getPath());
                //uploadImage();

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory("MobiLAB"), "Pictures");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Logger.append("failed to create directory for saving pictures!");
                return null;
            }
        }
        // Create a media file name

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + getNewPicName());
        Toast.makeText(getApplicationContext(),
                "new Pic created!, " + mediaFile.getName() + "Location: MobiLAB/Pictures", Toast.LENGTH_LONG).show();
        return mediaFile;
    }

    public String getNewPicName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return timeStamp + "|p=" + latitude + " " + longitude + "|al=" + (int) altitude+".jpg";
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void initSensors() {
        initGPS();
        initCAMERA();
    }

    private void initCAMERA() {
        if (_camera != null) {
            Toast.makeText(getApplicationContext(), "_camera != null", Toast.LENGTH_SHORT).show();
            int cameraInerval = Integer.parseInt(_camera.get(INTERVAL).toString());
            String cameraResolution = _camera.get(RESOLUTION).toString();


            PICtimeOut = cameraInerval * 1000; //30000 = 30 sec
            widthResulution = Integer.parseInt(cameraResolution.split("x")[0]);
            heightResulution = Integer.parseInt(cameraResolution.split("x")[1]);



            //Logger.append("PICtimeOut"+PICtimeOut+" widthResulution= " +widthResulution+" heightResulution="+heightResulution);

            mCamera = getCameraInstance();
            mCameraPreview = new CameraPreview(this, mCamera);
            final FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
            preview.addView(mCameraPreview);


            //We most use params after getCameraInstance().
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureSize(widthResulution, heightResulution);
            mCamera.setParameters(params);


            //Start thread:

            send = true;
            Thread takePicThread = new Thread(takePicRunnable);
            takePicThread.start();



        }
    }

    private void initGPS() {
        if (_gps) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    altitude = location.getAltitude();
                    String locationStr = "Altitude: " + altitude + "\n Latitude: " + latitude + "\n Longitude: " + longitude;
                    locationText.setText(locationStr);
                    Logger.append(LOCATION + altitude + ", " + latitude + ", " + longitude);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            };
            configure_button();
        }
    }

    void configure_button() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}, 10);
            }
            return;
        }
        locationManager.requestLocationUpdates(GPS, GPS_minTime, GPS_minDistance, locationListener);
    }


    private void incomingIntentData() {
        Intent intent = getIntent();
        HashMap<String, Object> tmp = null;
        String moving_data = RECEIVE;
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(GPS)) != null) {
            _gps = true;
            moving_data += GPS + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(BAROMETER)) != null) {
            _barometer = true;
            moving_data += BAROMETER + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(TEMPERATURE)) != null) {
            _temperature = true;
            moving_data += TEMPERATURE + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(BATTERY)) != null) {
            _battery = true;
            moving_data += BATTERY + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(SOUND)) != null) {
            _sound = new HashMap<>(tmp);
            moving_data += SOUND + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(CAMERA)) != null) {
            _camera = new HashMap<>(tmp);
            moving_data += CAMERA + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(SMS)) != null) {
            _sms = new HashMap<>(tmp);
            moving_data += SMS + ",";
        }
        if ((tmp = (HashMap<String, Object>) intent.getSerializableExtra(EXTERNAL_SENSOR)) != null) {
            _externalSensors = true;
            moving_data += EXTERNAL_SENSOR + ",";
        }
        Logger.append(moving_data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

}

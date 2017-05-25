package mobilab.mobilab;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.security.AccessController.getContext;

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
    private static final String TELEPHONE = "telephone";
    private static final String CLOUD = "cloud";

    private static boolean CloudSwitchData = false;
    private static final String CLOUD_SYNC = "cloudData";

    private static final int BAT_TEMP_INTERVAL = 120;// in sec
    private float current_temperature;
    private float current_battery_level;
    private HashMap<String, Object> _camera, _sms, _sound;
    private Boolean _barometer = false, _externalSensors = false, _temperature = false, _battery = false, _gps = false;
    private long dataId = 0;
    private int updateCloudInterval = 5;
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
    private int PICTimeOut = 30000; // 30 sec
    private int widthResolution = 640; //default
    private int heightResolution = 480; //default

    //private int compressQuality = 10;    //3-100, 80 gives pic on 4 kb, its the best compress without loose high quality
    private String picPath;
    private boolean runPic = false;

    //Sms:
    private boolean sendSMS = false;
    private String destinationNumber;
    private int SMSTimeOut = 30000;

    //Barometer
    private boolean barometerOn = false;
    private float barometerData;
    private int barometerTimeOut = 10;//in sec



    /////////////////////////////////////////////////////////////////////////CloudUpload//////////////////////////////////////////////////////////////
    com.android.volley.RequestQueue requestQueue;
    private static String AndroidId;
    String insertUrl = "http://mobilab.000webhostapp.com/telemetry/insertData.php";
    String MODEL = Build.MANUFACTURER + " " + Build.MODEL;

    Handler uploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO: change 0.0 values to actual values
            dataId++;
            String time = new SimpleDateFormat("dd.MM.yy--HH:mm:ss").format(new Date());
            sendToServer(dataId, time,latitude, longitude, altitude, current_temperature, current_battery_level,barometerData, 0.0,MODEL,AndroidId);
        }
    };
    Runnable updateCloudRunnable = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (CloudSwitchData) {
                        try {
                            //wait(updateCloudInterval * 1000);
                            wait(5000);
                            uploadHandler.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };

    public void sendToServer(final long ID, final String TIME ,final double latitude, final double longitude, final double altitude, final float Temperature, final float Battery, final double Barometer, final double EXT_Sensors,final String MODEL ,final String AndroidId) {
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Logger.append("SERVER: " + response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.append("SERVER: " + error.getCause());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("ID", String.valueOf(ID));
                parameters.put("TIME", TIME);
                parameters.put("LatitudeLongitude", String.valueOf(latitude) + "," + String.valueOf(longitude));
                parameters.put("Altitude", String.valueOf(altitude));
                parameters.put("Temperature", String.valueOf(Temperature));
                parameters.put("Battery", String.valueOf(Battery));
                parameters.put("Barometer", String.valueOf(Barometer));
                parameters.put("EXT_Sensors", String.valueOf(EXT_Sensors));
                parameters.put("MODEL", MODEL);
                parameters.put("AndroidId", AndroidId);
                return parameters;
            }
        };
        Logger.append("cloud updated -> ID: " + ID + ";LatitudeLongitude: " + latitude + "," + longitude + ";Altitude: " + altitude + ";Temperature: " + Temperature + ";Battery: " + Battery + ";Barometer: " + Barometer + ";EXT_Sensors: " + EXT_Sensors);
        requestQueue.add(request);
        Toast.makeText(getApplicationContext(),"Location Sent !", Toast.LENGTH_LONG).show();

    }
    /////////////////////////////////////////////////////////////////////////TakePicThread/////////////////////////////////////////////////////////////////

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                mCamera.startPreview(); //important! it allow to take multi pics!
                mCamera.takePicture(null, null, mPicture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    Runnable takePicRunnable = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (runPic) {
                        try {
                            wait(PICTimeOut);
                            handler.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };

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
            Logger.append("cannot access camera or does not exist");
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
                Logger.append("picture taken: " + pictureFile.getName());
                fos.close();
                //Upload to server:
                //UpdateNewBitMap(pictureFile.getPath());
                //uploadImage();

            } catch (FileNotFoundException e) {
                Logger.append("can't create picture file" + e.getStackTrace());
            } catch (IOException e) {
                Logger.append("taking picture failed: " + e.getStackTrace());
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
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + getStringData() + ".jpg");
        Toast.makeText(getApplicationContext(), "new Pic created!, " + mediaFile.getName() + "Location: MobiLAB/Pictures", Toast.LENGTH_LONG).show();
        Logger.append("created picture directory");
        return mediaFile;
    }


    /////////////////////////////////////////////////////////////////////////TakeSMSThread/////////////////////////////////////////////////////////////////

    Handler handlerSMS = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            sendSms();
        }
    };


    Runnable runnableSMS = new Runnable() {
        @Override
        public void run() {
            //while(true)
            {
                synchronized (this) {
                    while (sendSMS) {
                        try {
                            wait(SMSTimeOut);
                            handlerSMS.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };

    /////////////////////////////////////////////////////////////////////////////BATTERY and TEMPERATURE Thread//////////////////////////////

    private void batteryAndTemperatureSample() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        current_temperature = ((float) batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10);
        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            current_battery_level = 50.0f;
        }
        current_battery_level = ((float) level / (float) scale) * 100.0f;
        Logger.append("current battery level -> " + current_battery_level);
        Logger.append("current temperature -> " + current_temperature);
    }

    Runnable runnableBT = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (_battery || _temperature) {
                        try {
                            wait(BAT_TEMP_INTERVAL * 1000);
                            batteryAndTemperatureSample();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Logger.append(e.getStackTrace().toString());
                        }
                    }

                }
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////Barometric Thread////////////////////////////////////////
    Handler HandlerBarometric = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SensorManager sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
            final Sensor pS = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            barometerData =pS.getPower();
            Logger.append("Barometer Pressure:= "+barometerData);
        }
    };


    Runnable runnableBarometric = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (barometerOn) {
                        try {
                            wait(barometerTimeOut *1000);
                            HandlerBarometric.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////onCreate//////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationText = (TextView) (findViewById(R.id.locationText));
        incomingIntentData();
        initSensors();

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        AndroidId =telephonyManager.getDeviceId();
        Logger.append("AndroidId= "+AndroidId);

    }


    public String getStringData() {
        String msg = "<" + dataId++ + ">" + new SimpleDateFormat("dd-MM_HH:mm:ss").format(new Date());
        if (_gps) {
            msg += ";GPS:" + latitude + "," + longitude + "," + (int) altitude;
        }
        if (_temperature) {
            msg += ";TMP:" + current_temperature + "c";
        }
        if (_battery) {
            msg += ";BT:" + current_battery_level + "%";
        }
        if (_barometer) {
            // TODO: implement
        }
        if (_externalSensors) {
            // TODO: implement
        }

        return msg;
    }


    private void initSensors() {
        initGPS();
        initCAMERA();
        initSMS();
        initBATTERYandTEMPERATURE();
        initAutomateSync();
        initBarometer();
    }

    private void initBATTERYandTEMPERATURE() {
        if (_battery || _temperature) {
            batteryAndTemperatureSample();
            Thread tBT = new Thread(runnableBT);
            tBT.start();
        }
    }

    private void initCAMERA() {
        if (_camera != null) {
            int cameraInterval = Integer.parseInt(_camera.get(INTERVAL).toString());
            String cameraResolution = _camera.get(RESOLUTION).toString();

            PICTimeOut = cameraInterval * 1000; //30000 = 30 sec
            widthResolution = Integer.parseInt(cameraResolution.split("x")[0]);
            heightResolution = Integer.parseInt(cameraResolution.split("x")[1]);

            mCamera = getCameraInstance();
            mCameraPreview = new CameraPreview(this, mCamera);
            final FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
            preview.addView(mCameraPreview);

            //We most use params after getCameraInstance().
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureSize(widthResolution, heightResolution);
            mCamera.setParameters(params);

            //Start thread:
            runPic = true;
            Thread takePicThread = new Thread(takePicRunnable);
            takePicThread.start();

            boolean s = Boolean.parseBoolean(_camera.get(CLOUD).toString());
            if (s) {
                Logger.append("cloud state " + s);
                initCloud();
            } else {
                Logger.append("cloud state " + s);
            }
        }
    }

    private void initCloud()
    {
        //ToDo: implement pic upload!
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

    private void initSMS() {
        if (_sms != null) {
            destinationNumber = _sms.get(TELEPHONE).toString();
            int SMSInterval = Integer.parseInt(_sms.get(INTERVAL).toString());
            SMSTimeOut = SMSInterval * 1000; //1000 = 1 sec

            Logger.append("got sms data! " + destinationNumber + " " + SMSTimeOut);

            sendSMS = true;
            Thread smsThread = new Thread(runnableSMS);
            smsThread.start();
        }
    }

    public void sendSms() {
        SmsManager smsManager = SmsManager.getDefault();
        String SmsData = getStringData();
        smsManager.sendTextMessage(destinationNumber, null, SmsData, null, null);
        Logger.append("SMS sent to: " + destinationNumber + " Data sent = " + SmsData);
        Toast.makeText(getApplicationContext(), "SMS set to: " + destinationNumber, Toast.LENGTH_SHORT).show();
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

    public void initAutomateSync()
    {
        if(CloudSwitchData)
        {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Thread automateSyncDBThread = new Thread(updateCloudRunnable);
            automateSyncDBThread.start();
        }
    }

    private void initBarometer()
    {
        if(_barometer!=null)
        {

            barometerOn = true;
            Thread BarometricThread = new Thread(runnableBarometric);
            BarometricThread.start();





        }
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

        if(intent.getBooleanExtra(CLOUD_SYNC,false))
        {
            moving_data += CLOUD_SYNC + ".";
            CloudSwitchData = true;
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

    @Override
    public void onBackPressed() {
        Logger.append("event: onBackPressed");
        if (runPic) {
            runPic = false;
        }
        if (sendSMS) {
            sendSMS = false;
        }
        if(CloudSwitchData)
        {
            CloudSwitchData=false;
        }
        if(barometerOn)
        {
            barometerOn=false;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.append("event: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.append("event: onStop()");
    }
}

package mobilab.mobilab;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
    private static final String DRAGON_LINK = "DragonLink";


    private static boolean CloudSwitchData = false;
    private static final String CLOUD_SYNC = "cloudData";

    private static final int BAT_TEMP_INTERVAL = 120;// in sec
    private float current_temperature;
    private float current_battery_level;
    private HashMap<String, Object> _camera, _sms, _sound;
    private Boolean _barometer = false, _externalSensors = false, _temperature = false, _battery = false, _gps = false, _dragonLink = false;
    private String dataId;
    private String currentTime;
    private static long idCounter = 0;
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

    private boolean runPic = false;

    //Camera Upload pics:
    private boolean uploadCameraPic = false;
    private String UPLOAD_URL = "https://mobilab.000webhostapp.com/picture/upload.php";
    private Bitmap bitmap;
    private String picPath;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private int compressQuality = 10;    //3-100, 80 gives pic on 4 kb, its the best compress without loose high quality


    //Sms:
    private boolean sendSMS = false;
    private String destinationNumber;
    private int SMSTimeOut = 30000;

    //Barometer
    private boolean barometerOn = false;
    private float barometerData = -1;
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
            dataId = createID();
            currentTime = new SimpleDateFormat("dd.MM.yy--HH:mm:ss").format(new Date());
            sendToServer(dataId, currentTime, latitude, longitude, altitude, current_temperature, current_battery_level, barometerData, 0.0, MODEL, AndroidId);
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
                            wait(120000); //2 min
                            uploadHandler.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };

    public void sendToServer(final String ID, final String TIME, final double latitude, final double longitude, final double altitude, final float Temperature, final float Battery, final double Barometer, final double EXT_Sensors, final String MODEL, final String AndroidId) {
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
                parameters.put("ID", dataId);
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
        Toast.makeText(getApplicationContext(), "Location Sent !", Toast.LENGTH_SHORT).show();

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
                if (uploadCameraPic) {
                    //Upload to server:
                    UpdateNewBitMap(pictureFile.getPath());
                    uploadImage();
                }


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
        Toast.makeText(getApplicationContext(), "new Pic created!, " + mediaFile.getName() + "Location: MobiLAB/Pictures", Toast.LENGTH_SHORT).show();
        Logger.append("New Pic: " + getStringData() + ".jpg");
        return mediaFile;
    }


    public void UpdateNewBitMap(String path) {
        try {
            picPath = path;
            File f = new File(picPath);
            Uri imageUri = Uri.fromFile(f);
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadImage() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        Toast.makeText(MainActivity.this, "Cant upload pic!", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);
                //Getting Image Name

                String name = getNewPicName().trim();

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("ID", dataId);
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };


        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, compressQuality, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public String getNewPicName() {
        currentTime = new SimpleDateFormat("dd.MM.yy-HH:mm:ss").format(new Date());
        if (CloudSwitchData == false) {
            dataId = createID();
        }
        String picName = currentTime + "|" + dataId + "|" + latitude + "," + longitude + "|" + altitude + ".jpg";
        Logger.append("picName= " + picName);
        return picName;
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
            if (pS == null) {
                barometerData = -1;
            } else {
                barometerData = pS.getPower();
            }
            Logger.append("Barometer Pressure:= " + barometerData);
        }
    };


    Runnable runnableBarometric = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (barometerOn) {
                        try {
                            wait(barometerTimeOut * 1000);
                            HandlerBarometric.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    };

    ////////////////////////////////////////////////////////Dragon Link Thread //////////////////////////////////////
    android.os.Handler DragonHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (UsbService.class != null) { // if UsbService was correctly binded, Send data
                    UsbService.write((getStringDataShort() + "---").getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    Runnable sendToDragonMsg = new Runnable() {
        @Override
        public void run() {
            {
                synchronized (this) {
                    while (true) {
                        try {
                            wait(2000);
                            DragonHandler.sendEmptyMessage(0);
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

        currentTime = new SimpleDateFormat("dd.MM.yy--HH:mm:ss").format(new Date());
        incomingIntentData();
        initSensors();

        int permissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.READ_PHONE_STATE");

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_PHONE_STATE"}, 1);
        } else {
            //TODO
        }


        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        AndroidId = telephonyManager.getDeviceId();
        Logger.append("AndroidId= " + AndroidId);

    }


    public String getStringData() {
        String msg = "<" + dataId + ">" + new SimpleDateFormat("dd-MM_HH:mm:ss").format(new Date());
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
            msg += ";BAR:" + barometerData + "p";
        }
        if (_externalSensors) {
            // TODO: implement
        }
        return msg;
    }

    public String getStringDataShort() {
        String msg = new SimpleDateFormat("dd-MM_HH:mm:ss").format(new Date());
        if (_gps) {
            msg += ";GPS:" + latitude + "," + longitude + "," + (int) altitude;
        }
        if (_temperature) {
            msg += ";TMP:" + current_temperature + "c";
        }
        if (_battery) {
            msg += ";BT:" + current_battery_level + "%";
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
        initDragonLink();
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

    private void initCloud() {
        uploadCameraPic = true;
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

    public void initAutomateSync() {
        if (CloudSwitchData) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Thread automateSyncDBThread = new Thread(updateCloudRunnable);
            automateSyncDBThread.start();
        }
    }

    private void initBarometer() {
        if (_barometer != null) {
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

        if (intent.getBooleanExtra(CLOUD_SYNC, false)) {
            moving_data += CLOUD_SYNC + ",";
            CloudSwitchData = true;
        }

        if (intent.getBooleanExtra(DRAGON_LINK, false)) {
            moving_data += DRAGON_LINK + ".";
            _dragonLink = true;
        }
        Logger.append(moving_data);
    }

    private void initDragonLink() {
        if (_dragonLink) {
            Thread dragonLinkThread = new Thread(sendToDragonMsg);
            dragonLinkThread.start();
            Toast.makeText(getApplicationContext(), "Dragon thread start!", Toast.LENGTH_SHORT).show();
        }
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
        if (CloudSwitchData) {
            CloudSwitchData = false;
        }
        if (barometerOn) {
            barometerOn = false;
        }
        if (uploadCameraPic) {
            uploadCameraPic = false;
        }
        super.onBackPressed();
    }

    public static synchronized String createID() {
        //return UUID.randomUUID().toString().substring(0, 7);
        Random rand = new Random();
        int  num = rand.nextInt(99999999) + 10000000;
        return Integer.toString(num);
    }
//    @Override
//    protected void onPause() {
//        Logger.append("event: onPause()");
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        Logger.append("event: onStop()");
//        super.onStop();
//    }
}

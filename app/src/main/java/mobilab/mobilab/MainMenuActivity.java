package mobilab.mobilab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

public class MainMenuActivity extends AppCompatActivity {
    /**
     * constants
     */
    //camera
    private static final int CAMERA_10_INTERVAL = R.id.camera10sec;
    private static final int CAMERA_30_INTERVAL = R.id.camera30sec;
    private static final int CAMERA_60_INTERVAL = R.id.camera60sec;
    private static final int CAMERA_640x480_RESOLUTION = R.id.r640x480;
    private static final int CAMERA_800x600_RESOLUTION = R.id.r800x600;
    private static final int CAMERA_1024x768_RESOLUTION = R.id.r1024x768;
    //sms
    private static final int SMS_10_INTERVAL = R.id.sms10sec;
    private static final int SMS_30_INTERVAL = R.id.sms30sec;
    private static final int SMS_60_INTERVAL = R.id.sms60sec;
    //sound interval
    private static final int SOUND_30_INTERVAL = R.id.sDuration30sec;
    private static final int SOUND_60_INTERVAL = R.id.sDuration60sec;
    private static final int SOUND_120_INTERVAL = R.id.sDuration120sec;
    //sound duration
    private static final int SOUND_30_DURATION = R.id.iDuration30sec;
    private static final int SOUND_60_DURATION = R.id.iDuration60sec;
    private static final int SOUND_120_DURATION = R.id.iDuration120sec;

    private static final String GPS = "gps";
    private static final String CAMERA = "camera";
    private static final String SMS = "sms";
    private static final String TEMPERATURE = "temperature";
    private static final String BATTERY = "battery";
    private static final String SOUND = "sound";
    private static final String BAROMETER = "barometer";
    private static final String EXTERNAL_SENSOR = "external sensor";
    private static final String ID = "id";
    private static final String INTERVAL = "interval";
    private static final String TELEPHONE = "telephone";
    private static final String RESOLUTION = "resolution";
    private static final String DURATION = "duration";
    private static final String DEFAULT_TEL_NUMBER = "0000000000";
    private static final String OK = "OK";
    private static final String SEND = "sent data: ";
    private static final String START = "START";
    private static Button startButton;
    //camera radio buttons
    private static RadioButton rbCamera10sec;
    private static RadioButton rbCamera30sec;
    private static RadioButton rbCamera60sec;
    private static RadioButton rbCamera640sec;
    private static RadioButton rbCamera800sec;
    private static RadioButton rbCamera1024sec;
    //sound interval radio buttons
    private static RadioButton rbiSound10sec;
    private static RadioButton rbiSound30sec;
    private static RadioButton rbiSound60sec;
    //sound duration radio buttons
    private static RadioButton rbdSound30sec;
    private static RadioButton rbdSound60sec;
    private static RadioButton rbdSound120sec;
    //sms radio buttons
    private static RadioButton rbSMS10sec;
    private static RadioButton rbSMS30sec;
    private static RadioButton rbSMS60sec;

    class config {
        private CheckBox checkBox;
        private Boolean active;
        private HashMap<String, Object> data;
        private String id;
        private Boolean alertDialog = false;

        private config(CheckBox cb, Boolean bl, int dataCapacity, String name) {
            this.id = name;
            this.checkBox = cb;
            this.active = bl;
            this.data = new HashMap<String, Object>();
            if (dataCapacity > -1) {
                alertDialog = true;
            }
            if (name.equals(CAMERA)) {  //set default data for camera
                this.data.put(INTERVAL, 30);
                this.data.put(RESOLUTION, "640x480");
            }
            if (name.equals(SMS)) {//set default data for SMS
                this.data.put(TELEPHONE, DEFAULT_TEL_NUMBER);
                this.data.put(INTERVAL, 30);
            }
            if (name.equals(SOUND)) {//set default data for sound
                this.data.put(DURATION, 60);
                this.data.put(INTERVAL, 30);
            }
        }

        public Boolean getState() {
            return this.active;
        }

        public void changeState() {
            active = !active;
        }

        public void changeData(String key, Object data) {
            this.data.put(key, data);
        }

        public HashMap<String, Object> getData() {
            return this.data;
        }
    }

    private config _gps, _camera, _sms, _temperature, _battery, _sound, _barometer, _externalSensors;

    /**
     * onCreate method initial all global variables and set onClickListeners
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        startButton = (Button) findViewById(R.id.button);

        initConfigurations();
        setConfigurationListeners();
        setStartListener();
    }

    private void setStartListener() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                String moving_data = SEND;
                if (_gps.getState()) {
                    intent.putExtra(GPS, _gps.getData());
                    moving_data += GPS + ",";
                }
                if (_barometer.getState()) {
                    intent.putExtra(BAROMETER, _barometer.getData());
                    moving_data += BAROMETER + ",";
                }
                if (_temperature.getState()) {
                    intent.putExtra(TEMPERATURE, _temperature.getData());
                    moving_data += TEMPERATURE + ",";
                }
                if (_battery.getState()) {
                    intent.putExtra(BATTERY, _battery.getData());
                    moving_data += BATTERY + ",";
                }
                if (_sound.getState()) {
                    intent.putExtra(SOUND, _sound.getData());
                    moving_data += SOUND + ",";
                }
                if (_camera.getState()) {
                    intent.putExtra(CAMERA, _camera.getData());
                    moving_data += CAMERA + ",";
                }
                if (_sms.getState()) {
                    intent.putExtra(SMS, _sms.getData());
                    moving_data += SMS + ",";
                }
                if (_externalSensors.getState()) {
                    intent.putExtra(EXTERNAL_SENSOR, _externalSensors.getData());
                    moving_data += EXTERNAL_SENSOR + ",";
                }
                Logger.append(moving_data);
                startActivity(intent);
                Logger.append(START);
            }
        });
    }

    /**
     * set the on click listener to specific sensor
     *
     * @param conf which is the sensor
     */
    private void setListener(final config conf) {
        conf.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conf.changeState();
                if (conf.alertDialog && conf.getState()) {
                    showPopup(conf);
                }
                Logger.append(conf.id + " changed -> " + conf.getState());
            }
        });
    }

    /**
     * initialize all configuration objects to initial values (used only in onCreate method
     */
    public void initConfigurations() {
        _gps = new config((CheckBox) findViewById(R.id.gpsCB), false, -1, GPS);                                      // no additional data
        _camera = new config((CheckBox) findViewById(R.id.cameraCB), false, 2, CAMERA);                              // quality, interval
        _sms = new config((CheckBox) findViewById(R.id.smsCB), false, 2, SMS);                                       // telephone number, interval
        _temperature = new config((CheckBox) findViewById(R.id.temperatureCB), false, -1, TEMPERATURE);              // no additional data
        _battery = new config((CheckBox) findViewById(R.id.batteryLevelCB), false, -1, BATTERY);                     // no additional data
        _sound = new config((CheckBox) findViewById(R.id.soundCB), false, 2, SOUND);                                 // interval, duration
        _barometer = new config((CheckBox) findViewById(R.id.barometerCB), false, -1, BAROMETER);                    // no additional data
        _externalSensors = new config((CheckBox) findViewById(R.id.ExternalSensorsCB), false, -1, EXTERNAL_SENSOR);  // no additional data
    }

    /**
     * set onClickListeners to all configuration objects (checkboxes)
     */

    public void setConfigurationListeners() {
        // set listeners
        setListener(_gps);
        setListener(_barometer);
        setListener(_camera);
        setListener(_sms);
        setListener(_temperature);
        setListener(_battery);
        setListener(_sound);
        setListener(_barometer);
        setListener(_externalSensors);
    }

    /**
     * this function receive configuration object as parameter and shows a popup with his configuration options
     * (if more sensors or data is added make sure to add relevant popups here!)
     *
     * @param conf
     */
    public void showPopup(final config conf) {
        switch (conf.id) {
            case CAMERA:
                showCameraPropertiesPopUp(conf);
                break;
            case SMS:
                showSMSPropertiesPopUp(conf);
                break;
            case SOUND:
                showSoundPropertiesPopUp(conf);
                break;
        }
    }

    /**
     * initialize the sms popup radio buttons with their id's  in order to catch the answer from popup dialog
     *
     * @param dialog
     */

    private void initSMSRadioButtons(Dialog dialog) {
        rbSMS10sec = (RadioButton) dialog.findViewById(SMS_10_INTERVAL);
        rbSMS30sec = (RadioButton) dialog.findViewById(SMS_30_INTERVAL);
        rbSMS60sec = (RadioButton) dialog.findViewById(SMS_60_INTERVAL);

        rbSMS10sec.setId(SMS_10_INTERVAL);
        rbSMS30sec.setId(SMS_30_INTERVAL);
        rbSMS60sec.setId(SMS_60_INTERVAL);
    }

    /**
     * initialize the camera popup radio buttons with their id's  in order to catch the answer from popup dialog
     *
     * @param dialog
     */
    private void initCameraRadioButtons(Dialog dialog) {
        rbCamera10sec = (RadioButton) dialog.findViewById(CAMERA_10_INTERVAL);
        rbCamera30sec = (RadioButton) dialog.findViewById(CAMERA_30_INTERVAL);
        rbCamera60sec = (RadioButton) dialog.findViewById(CAMERA_60_INTERVAL);
        rbCamera640sec = (RadioButton) dialog.findViewById(CAMERA_640x480_RESOLUTION);
        rbCamera800sec = (RadioButton) dialog.findViewById(CAMERA_800x600_RESOLUTION);
        rbCamera1024sec = (RadioButton) dialog.findViewById(CAMERA_1024x768_RESOLUTION);

        rbCamera10sec.setId(CAMERA_10_INTERVAL);
        rbCamera30sec.setId(CAMERA_30_INTERVAL);
        rbCamera60sec.setId(CAMERA_60_INTERVAL);
        rbCamera640sec.setId(CAMERA_640x480_RESOLUTION);
        rbCamera800sec.setId(CAMERA_800x600_RESOLUTION);
        rbCamera1024sec.setId(CAMERA_1024x768_RESOLUTION);
    }

    /**
     * initialize the sound popup radio buttons with their id's  in order to catch the answer from popup dialog
     *
     * @param dialog
     */

    private void initSoundRadioButtons(Dialog dialog) {
        rbiSound10sec = (RadioButton) dialog.findViewById(SMS_10_INTERVAL);
        rbiSound30sec = (RadioButton) dialog.findViewById(SMS_30_INTERVAL);
        rbiSound60sec = (RadioButton) dialog.findViewById(SMS_60_INTERVAL);
        rbdSound30sec = (RadioButton) dialog.findViewById(SOUND_30_DURATION);
        rbdSound60sec = (RadioButton) dialog.findViewById(SOUND_60_DURATION);
        rbdSound120sec = (RadioButton) dialog.findViewById(SOUND_120_DURATION);

        rbiSound10sec.setId(CAMERA_10_INTERVAL);
        rbiSound30sec.setId(CAMERA_30_INTERVAL);
        rbiSound60sec.setId(CAMERA_60_INTERVAL);
        rbdSound30sec.setId(SOUND_30_DURATION);
        rbdSound60sec.setId(SOUND_60_DURATION);
        rbdSound120sec.setId(SOUND_120_DURATION);
    }

    /**
     * define the configuration popup to show for the camera
     *
     * @param conf
     */
    public void showCameraPropertiesPopUp(final config conf) {
        final Dialog dialog = new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.camera_popup);
        dialog.setTitle(R.string.camera_properties);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.CameraApprove);
        final RadioGroup intervalRadioGroup = (RadioGroup) dialog.findViewById(R.id.cameraIntervalGroup);
        final RadioGroup resolutionRadioGroup = (RadioGroup) dialog.findViewById(R.id.resolutionGroup);

        // assign id's for resolution and interval radio buttons
        initCameraRadioButtons(dialog);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier(CAMERA + conf.data.get(INTERVAL) + "sec", ID, getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make radio resolution button clicked:
        int lastResolutionId = getResources().getIdentifier("r" + conf.data.get(RESOLUTION), ID, getPackageName());
        RadioButton LastResolutionButton = (RadioButton) dialog.findViewById(lastResolutionId);
        LastResolutionButton.setChecked(true);


        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalSelectedId = intervalRadioGroup.getCheckedRadioButtonId();
                int resolutionSelectedId = resolutionRadioGroup.getCheckedRadioButtonId();

                switch (intervalSelectedId) {
                    case CAMERA_10_INTERVAL:
                        conf.changeData(INTERVAL, 10);
                        Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_10));
                        break;
                    case CAMERA_30_INTERVAL:
                        conf.changeData(INTERVAL, 30);
                        Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_30));
                        break;
                    case CAMERA_60_INTERVAL:
                        conf.changeData(INTERVAL, 60);
                        Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_60));
                        break;
                }
                switch (resolutionSelectedId) {

                    case CAMERA_640x480_RESOLUTION:
                        conf.changeData(RESOLUTION, "640x480");
                        Logger.append(getResources().getString(R.string.resolution_changed_640));
                        break;
                    case CAMERA_800x600_RESOLUTION:
                        conf.changeData(RESOLUTION, "800x600");
                        Logger.append(getResources().getString(R.string.resolution_changed_800));
                        break;
                    case CAMERA_1024x768_RESOLUTION:
                        conf.changeData(RESOLUTION, "1024x768");
                        Logger.append(getResources().getString(R.string.resolution_changed_1024));
                        break;
                }
                Toast.makeText(getApplicationContext(), "Camera set to: " + conf.data.get(RESOLUTION) + " and " + conf.data.get(INTERVAL) + " sec", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    /**
     * define the configuration popup to show for the sms
     *
     * @param conf
     */
    public void showSMSPropertiesPopUp(final config conf) {
        final Dialog dialog = new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.sms_popup);
        dialog.setTitle(R.string.sms_properties);
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.SMSApprove);
        final RadioGroup SMSRadioGroup = (RadioGroup) dialog.findViewById(R.id.smsIntervalGroup);
        final EditText telephoneText = (EditText) dialog.findViewById(R.id.telNo);

        // assign id's for resolution and interval radio buttons
        initSMSRadioButtons(dialog);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier(SMS + conf.data.get(INTERVAL) + "sec", ID, getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make telephoneText show the last data:
        String lastTelephoneVal = (String) conf.data.get(TELEPHONE);
        if (!lastTelephoneVal.equals(DEFAULT_TEL_NUMBER)) {
            telephoneText.setText(lastTelephoneVal, TextView.BufferType.EDITABLE);
        }
        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalSelectedId = SMSRadioGroup.getCheckedRadioButtonId();

                switch (intervalSelectedId) {
                    case SMS_10_INTERVAL:
                        conf.changeData(INTERVAL, 10);
                        Logger.append(SMS + " " + getResources().getString(R.string.interval_changed_10));
                        break;
                    case SMS_30_INTERVAL:
                        conf.changeData(INTERVAL, 30);
                        Logger.append(SMS + " " + getResources().getString(R.string.interval_changed_30));
                        break;
                    case SMS_60_INTERVAL:
                        conf.changeData(INTERVAL, 60);
                        Logger.append(SMS + " " + getResources().getString(R.string.interval_changed_60));
                        break;
                }

                String telephoneNumber = telephoneText.getText().toString();
                if (telephoneNumber.isEmpty()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                    alertDialog.setTitle(R.string.enter_phone_number);
                    alertDialog.setMessage(getResources().getString(R.string.enter_phone_number_description));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, OK,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else if (telephoneNumber.length() != 10) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                    alertDialog.setTitle(R.string.enter_phone_number);
                    alertDialog.setMessage(getResources().getString(R.string.enter_phone_number_error));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, OK,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else {
                    conf.changeData(TELEPHONE, telephoneNumber);
                    Toast.makeText(getApplicationContext(), "SMS will set to: " + conf.data.get(TELEPHONE) + " every " + conf.data.get(INTERVAL)
                            + " sec", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * define the configuration popup to show for the sound
     *
     * @param conf
     */
    public void showSoundPropertiesPopUp(final config conf) {
        final Dialog dialog = new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.sound_popup);
        dialog.setTitle(R.string.sound_properties);
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.soundApprove);
        final RadioGroup intervalRadioGroup = (RadioGroup) dialog.findViewById(R.id.soundIntervalGroup);
        final RadioGroup durationRadioGroup = (RadioGroup) dialog.findViewById(R.id.durationGroup);

        // assign id's for resolution and interval radio buttons
        initSoundRadioButtons(dialog);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier("iDuration" + conf.data.get(INTERVAL) + "sec", ID, getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make radio duration button clicked:
        int lastDurationId = getResources().getIdentifier("sDuration" + conf.data.get(DURATION) + "sec", ID, getPackageName());
        RadioButton LastDurationButton = (RadioButton) dialog.findViewById(lastDurationId);
        LastDurationButton.setChecked(true);

        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalSelectedId = intervalRadioGroup.getCheckedRadioButtonId();
                int DurationSelectedId = durationRadioGroup.getCheckedRadioButtonId();

                Logger.append("intervalSelectedId= " + intervalSelectedId);
                Logger.append("DurationSelectedId= " + DurationSelectedId);


                switch (intervalSelectedId) {
                    case SOUND_30_INTERVAL:
                        conf.changeData(INTERVAL, 30);
                        Logger.append(SOUND + " " + getResources().getString(R.string.interval_changed_30));
                        break;
                    case SOUND_60_INTERVAL:
                        conf.changeData(INTERVAL, 60);
                        Logger.append(SOUND + " " + getResources().getString(R.string.interval_changed_60));
                        break;
                    case SOUND_120_INTERVAL:
                        conf.changeData(INTERVAL, 120);
                        Logger.append(SOUND + " " + getResources().getString(R.string.interval_changed_120));
                        break;
                }
                switch (DurationSelectedId) {
                    case SOUND_30_DURATION:
                        conf.changeData(DURATION, 30);
                        Logger.append(SOUND + " " + getResources().getString(R.string.duration_changed_30));
                        break;
                    case SOUND_60_DURATION:
                        conf.changeData(DURATION, 60);
                        Logger.append(SOUND + " " + getResources().getString(R.string.duration_changed_60));
                        break;
                    case SOUND_120_DURATION:
                        conf.changeData(DURATION, 120);
                        Logger.append(SOUND + " " + getResources().getString(R.string.duration_changed_120));
                        break;
                }
                Toast.makeText(getApplicationContext(), "Sound set to duration: " + conf.data.get(DURATION) + "sec, every " + conf.data.get(INTERVAL)
                        + " sec", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Logger.onDestroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
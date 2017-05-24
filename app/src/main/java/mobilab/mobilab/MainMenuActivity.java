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
import java.io.Serializable;
import java.util.HashMap;

public class MainMenuActivity extends AppCompatActivity {
    /**
     * constants
     */
    //1
    private static final int CAMERA_10_INTERVAL = R.id.camera10sec;
    private static final int CAMERA_30_INTERVAL = R.id.camera30sec;
    private static final int CAMERA_60_INTERVAL = R.id.camera60sec;
    private static final int CAMERA_640x480_RESOLUTION = R.id.r640x480;
    private static final int CAMERA_800x600_RESOLUTION = R.id.r800x600;
    private static final int CAMERA_1024x768_RESOLUTION = R.id.r1024x768;
    //2
    private static final int SMS_10_INTERVAL = 200010;
    private static final int SMS_30_INTERVAL = 200030;
    private static final int SMS_60_INTERVAL = 200060;
    //3
    private static final int SOUND_30_INTERVAL = 300030;
    private static final int SOUND_60_INTERVAL = 300060;
    private static final int SOUND_120_INTERVAL = 3000120;
    //4
    private static final int SOUND_30_DURATION = 400030;
    private static final int SOUND_60_DURATION = 400060;
    private static final int SOUND_120_DURATION = 4000120;
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

    /**
     * check box configuration object to hold configuration for each sensor
     */

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

    private void initRadioButtons(Dialog dialog) {
        final RadioButton rb10sec = (RadioButton) dialog.findViewById(R.id.camera10sec);
        final RadioButton rb30sec = (RadioButton) dialog.findViewById(R.id.camera30sec);
        final RadioButton rb60sec = (RadioButton) dialog.findViewById(R.id.camera60sec);
        final RadioButton rb640sec = (RadioButton) dialog.findViewById(R.id.r640x480);
        final RadioButton rb800sec = (RadioButton) dialog.findViewById(R.id.r800x600);
        final RadioButton rb1024sec = (RadioButton) dialog.findViewById(R.id.r1024x768);

        rb10sec.setId(R.id.camera10sec);
        rb30sec.setId(R.id.camera30sec);
        rb60sec.setId(R.id.camera60sec);
        rb640sec.setId(R.id.r640x480);
        rb800sec.setId(R.id.r800x600);
        rb1024sec.setId(R.id.r1024x768);

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
        initRadioButtons(dialog);

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

                Logger.append("intervalSelectedId=" + intervalSelectedId);
                Logger.append("resolutionSelectedId=" + resolutionSelectedId);


                switch (intervalSelectedId) {
                    case R.id.camera10sec:
                        conf.changeData(INTERVAL, 10);
                        Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_10));
                        break;
                    case R.id.camera30sec:
                        conf.changeData(INTERVAL, 30);
                        Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_30));
                        break;
                    case R.id.camera60sec:
                        conf.changeData(INTERVAL, 60);
                        Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_60));
                        break;
                    case R.id.r640x480:
                        conf.changeData(RESOLUTION, "640x480");
                        Logger.append(getResources().getString(R.string.resolution_changed_640));
                        break;
                    case R.id.r800x600:
                        conf.changeData(RESOLUTION, "800x600");
                        Logger.append(getResources().getString(R.string.resolution_changed_800));
                        break;
                    case R.id.r1024x768:
                        conf.changeData(RESOLUTION, "1024x768");
                        Logger.append(getResources().getString(R.string.resolution_changed_1024));
                        break;
                }

//                if (intervalSelectedId == 0) {
//                    conf.changeData(INTERVAL, 10);
//                    Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_10));
//                }
//                if (intervalSelectedId == CAMERA_30_INTERVAL) {
//                    conf.changeData(INTERVAL, 30);
//                    Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_30));
//                }
//                if (intervalSelectedId == CAMERA_60_INTERVAL) {
//                    conf.changeData(INTERVAL, 60);
//                    Logger.append(CAMERA + " " + getResources().getString(R.string.interval_changed_60));
//                }
//                if (resolutionSelectedId == CAMERA_640x480_RESOLUTION) {
//                    conf.changeData(RESOLUTION, "640x480");
//                    Logger.append(getResources().getString(R.string.resolution_changed_640));
//                }
//                if (resolutionSelectedId == CAMERA_800x600_RESOLUTION) {
//                    conf.changeData(RESOLUTION, "800x600");
//                    Logger.append(getResources().getString(R.string.resolution_changed_800));
//                }
//                if (resolutionSelectedId == CAMERA_1024x768_RESOLUTION) {
//                    conf.changeData(RESOLUTION, "1024x768");
//                    Logger.append(getResources().getString(R.string.resolution_changed_1024));
//                }
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

                if (intervalSelectedId == SMS_10_INTERVAL) {
                    conf.changeData(INTERVAL, 10);
                    Logger.append(SMS + " " + getResources().getString(R.string.interval_changed_10));
                }
                if (intervalSelectedId == SMS_30_INTERVAL) {
                    conf.changeData(INTERVAL, 30);
                    Logger.append(SMS + " " + getResources().getString(R.string.interval_changed_30));
                }
                if (intervalSelectedId == SMS_60_INTERVAL) {
                    conf.changeData(INTERVAL, 60);
                    Logger.append(SMS + " " + getResources().getString(R.string.interval_changed_60));
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

                if (intervalSelectedId == SOUND_30_INTERVAL) {
                    conf.changeData(INTERVAL, 30);
                    Logger.append(SOUND + " " + getResources().getString(R.string.interval_changed_30));
                }
                if (intervalSelectedId == SOUND_60_INTERVAL) {
                    conf.changeData(INTERVAL, 60);
                    Logger.append(SOUND + " " + getResources().getString(R.string.interval_changed_60));
                }
                if (intervalSelectedId == SOUND_120_INTERVAL) {
                    conf.changeData(INTERVAL, 120);
                    Logger.append(SOUND + " " + getResources().getString(R.string.interval_changed_120));
                }
                if (DurationSelectedId == SOUND_30_DURATION) {
                    conf.changeData(DURATION, 30);
                    Logger.append(SOUND + " " + getResources().getString(R.string.duration_changed_30));
                }
                if (DurationSelectedId == SOUND_60_DURATION) {
                    conf.changeData(DURATION, 60);
                    Logger.append(SOUND + " " + getResources().getString(R.string.duration_changed_60));
                }
                if (DurationSelectedId == SOUND_120_DURATION) {
                    conf.changeData(DURATION, 120);
                    Logger.append(SOUND + " " + getResources().getString(R.string.duration_changed_120));
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
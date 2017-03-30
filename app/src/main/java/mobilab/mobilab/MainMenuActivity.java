package mobilab.mobilab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
    //const
    public static final int CAMERA_10_INTERVAL = 2131493000;
    public static final int CAMERA_30_INTERVAL = 2131493001;
    public static final int CAMERA_60_INTERVAL = 2131493002;
    public static final int CAMERA_640x480_RESOLUTION = 2131492995;
    public static final int CAMERA_800x600_RESOLUTION = 2131492996;
    public static final int CAMERA_1024x768_RESOLUTION = 2131492997;
    public static final int SMS_10_INTERVAL = 2131493041;
    public static final int SMS_30_INTERVAL = 2131493042;
    public static final int SMS_60_INTERVAL = 2131493043;
    public static final int SOUND_10_INTERVAL = 2131493050;
    public static final int SOUND_30_INTERVAL = 2131493051;
    public static final int SOUND_60_INTERVAL = 2131493052;
    public static final int SOUND_30_DURATION = 2131493046;
    public static final int SOUND_60_DURATION = 2131493047;
    public static final int SOUND_120_DURATION = 2131493048;
    public static final String GPS = "gps";
    public static final String CAMERA = "camera";
    public static final String SMS = "sms";
    public static final String TEMPERATURE = "temperature";
    public static final String BATTERY = "battery";
    public static final String SOUND = "sound";
    public static final String BAROMETER = "barometer";
    public static final String EXTERNAL_SENSOR = "external sensor";

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
            if (dataCapacity > -1) {
                this.data = new HashMap<String, Object>();
                alertDialog = true;
            }
            if (name.equals(CAMERA))  //set default data for camera
            {
                this.data.put("interval", 30);
                this.data.put("resolution", "640x480");
            }
            if (name.equals(SMS))    //set default data for SMS
            {
                this.data.put("telephone", "0000000000");
                this.data.put("interval", "30");
            }
            if (name.equals(SOUND))    //set default data for sound
            {
                this.data.put("duration", "60");
                this.data.put("interval", "30");
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

    }

    Button button;

    private static Logger logger;
    private config _gps, _camera, _sms, _temperature, _battery, _sound, _barometer, _externalSensors;

    /**
     * onCreate method initial all global variables and set onClickListeners
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConfigurations();
        setAllListeners();

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.append("this is start button.");
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
                logger.append(conf.id + " changed --> " + conf.getState());
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

    public void setAllListeners() {
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
     * define the configuration popup to show for the camera
     *
     * @param conf
     */
    public void showCameraPropertiesPopUp(final config conf) {
        //TODO: implement dialog.setCanceledOnTouchOutside(); function
        final Dialog dialog = new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.camera_popup);
        dialog.setTitle(R.string.camera_properties);
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.CameraApprove);
        final RadioGroup intervalRadioGroup = (RadioGroup) dialog.findViewById(R.id.cameraIntervalGroup);
        final RadioGroup resolutionRadioGroup = (RadioGroup) dialog.findViewById(R.id.resolutionGroup);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier("" + R.string.CAMERA + conf.data.get("interval") + "sec", "id", getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make radio resolution button clicked:
        int lastResolutionId = getResources().getIdentifier("r" + conf.data.get(R.string.resolution2), "id", getPackageName());
        RadioButton LastResolutionButton = (RadioButton) dialog.findViewById(lastResolutionId);
        LastResolutionButton.setChecked(true);

        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalSelectedId = intervalRadioGroup.getCheckedRadioButtonId();
                int resolutionSelectedId = resolutionRadioGroup.getCheckedRadioButtonId();

                if (intervalSelectedId == CAMERA_10_INTERVAL) {
                    conf.changeData("interval", 10);
                    Logger.append(CAMERA + R.string.interval_changed_10);
                }
                if (intervalSelectedId == CAMERA_30_INTERVAL) {
                    conf.changeData("interval", 30);
                    Logger.append(CAMERA + R.string.interval_changed_30);
                }
                if (intervalSelectedId == CAMERA_60_INTERVAL) {
                    conf.changeData("interval", 60);
                    Logger.append(CAMERA + R.string.interval_changed_60);
                }

                if (resolutionSelectedId == CAMERA_640x480_RESOLUTION) {
                    conf.changeData("resolution", "640x480");
                    Logger.append(R.string.resolution_changed_640 + "");
                }
                if (resolutionSelectedId == CAMERA_800x600_RESOLUTION) {
                    conf.changeData("resolution", "800x600");
                    Logger.append(R.string.resolution_changed_800 + "");
                }
                if (resolutionSelectedId == CAMERA_1024x768_RESOLUTION) {
                    conf.changeData("resolution", "1024x768");
                    Logger.append(R.string.resolution_changed_1024 + "");
                }
                Toast.makeText(getApplicationContext(), "Camera set to: " + conf.data.get("resolution") + " and " + conf.data.get("interval")
                        + " sec", Toast.LENGTH_SHORT).show();

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
        int lastIntervalId = getResources().getIdentifier("sms" + conf.data.get("interval") + "sec", "id", getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make telephoneText show the last data:
        String lastTelephoneVal = (String) conf.data.get(R.string.telNo);
        if (lastTelephoneVal != R.string.default_tel_number + "") {
            telephoneText.setText(lastTelephoneVal, TextView.BufferType.EDITABLE);
        }


        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalSelectedId = SMSRadioGroup.getCheckedRadioButtonId();

                if (intervalSelectedId == SMS_10_INTERVAL) {
                    conf.changeData("interval", 10);
                    Logger.append(SMS + R.string.interval_changed_10);
                }
                if (intervalSelectedId == SMS_30_INTERVAL) {
                    conf.changeData("interval", 30);
                    Logger.append(SMS + R.string.interval_changed_30);
                }
                if (intervalSelectedId == SMS_60_INTERVAL) {
                    conf.changeData("interval", 60);
                    Logger.append(SMS + R.string.interval_changed_60);
                }

                String telephoneNumber = telephoneText.getText().toString();

                if (telephoneNumber.isEmpty()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                    alertDialog.setTitle(R.string.enter_phone_number);
                    alertDialog.setMessage(R.string.enter_phone_number_description + "");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else if (telephoneNumber.length() != 10) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                    alertDialog.setTitle(R.string.enter_phone_number);
                    alertDialog.setMessage(R.string.enter_phone_number_error + "");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else {
                    conf.changeData("telephone", telephoneNumber);
                    Toast.makeText(getApplicationContext(), "SMS will set to: " + conf.data.get("telephone") + " every " + conf.data.get("interval")
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
        int lastIntervalId = getResources().getIdentifier("iDuration" + conf.data.get("interval") + "sec", "id", getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make radio duration button clicked:
        int lastDurationId = getResources().getIdentifier("sDuration" + conf.data.get("duration") + "sec", "id", getPackageName());
        RadioButton LastDurationButton = (RadioButton) dialog.findViewById(lastDurationId);
        LastDurationButton.setChecked(true);

        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intervalSelectedId = intervalRadioGroup.getCheckedRadioButtonId();
                int DurationSelectedId = durationRadioGroup.getCheckedRadioButtonId();

                if (intervalSelectedId == SOUND_10_INTERVAL) {
                    conf.changeData("interval", 30);
                    Logger.append(SOUND + R.string.interval_changed_10);
                }
                if (intervalSelectedId == SOUND_30_INTERVAL) {
                    conf.changeData("interval", 60);
                    Logger.append(SOUND + R.string.interval_changed_30);
                }
                if (intervalSelectedId == SOUND_60_INTERVAL) {
                    conf.changeData("interval", 120);
                    Logger.append(SOUND + R.string.interval_changed_60);
                }

                if (DurationSelectedId == SOUND_30_DURATION) {
                    conf.changeData("duration", "30");
                    Logger.append(SOUND + R.string.duration_changed_30);
                }
                if (DurationSelectedId == SOUND_60_DURATION) {
                    conf.changeData("duration", "60");
                    Logger.append(SOUND + R.string.duration_changed_60);
                }
                if (DurationSelectedId == SOUND_120_DURATION) {
                    conf.changeData("duration", "120");
                    Logger.append(SOUND + R.string.duration_changed_120);
                }
                Toast.makeText(getApplicationContext(), "Sound set to duration: " + conf.data.get("duration") + "sec, every " + conf.data.get("interval")
                        + " sec", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            logger.onDestroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




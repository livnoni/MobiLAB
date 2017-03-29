package mobilab.mobilab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import java.util.Objects;

public class MainMenuActivity extends AppCompatActivity {

    /**
     * check box configuration object to hold configuration for each sensor
     */

    class config {
        private CheckBox checkBox;
        private Boolean active;
        private HashMap<String, Object> data;
        private String id;
        private Boolean alertDialog = false;

        public config(CheckBox cb, Boolean bl, int dataCapacity, String name) {
            this.id = name;
            this.checkBox = cb;
            this.active = bl;
            if (dataCapacity > -1) {
                this.data =  new HashMap<String, Object>();
                alertDialog = true;
            }
            if(name=="camera")  //set default data for camera
            {
                this.data.put("interval",30);
                this.data.put("resolution","640x480");
            }
            if(name=="sms")    //set default data for SMS
            {
                this.data.put("telephone","0000000000");
                this.data.put("interval","30");
            }
            if(name=="sound")    //set default data for sound
            {
                this.data.put("duration","60");
                this.data.put("interval","30");
            }

        }

        public Boolean getState() {
            return this.active;
        }

        public void changeState() {
            active = !active;
        }

        public void changeData(String key,Object data)
        {
            this.data.put(key,data);
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
                logger.append("this is check.");
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
        _gps = new config((CheckBox) findViewById(R.id.gpsCB), false, -1, "gps");                                      // no additional data
        _camera = new config((CheckBox) findViewById(R.id.cameraCB), false, 2, "camera");                              // quality, interval
        _sms = new config((CheckBox) findViewById(R.id.smsCB), false, 2, "sms");                                       // telephone number, interval
        _temperature = new config((CheckBox) findViewById(R.id.temperatureCB), false, -1, "temperature");              // no additional data
        _battery = new config((CheckBox) findViewById(R.id.batteryLevelCB), false, -1, "battery");                     // no additional data
        _sound = new config((CheckBox) findViewById(R.id.soundCB), false, 2, "sound");                                 // interval, duration
        _barometer = new config((CheckBox) findViewById(R.id.barometerCB), false, -1, "barometer");                    // no additional data
        _externalSensors = new config((CheckBox) findViewById(R.id.ExternalSensorsCB), false, -1, "external sensor");  // no additional data
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
            case "camera":
                showCameraPropertiesPopUp(conf);
                break;
            case "sms":
                showSMSPropertiesPopUp(conf);
                break;
            case "sound":
                showSoundPropertiesPopUp(conf);
                break;
        }
    }

    public void showCameraPropertiesPopUp(final config conf)
    {
        final Dialog dialog=new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.camera_popup);
        dialog.setTitle("Camera Properties");
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.CameraApprove);
        final RadioGroup intervalRadioGroup = (RadioGroup) dialog.findViewById(R.id.cameraIntervalGroup);
        final RadioGroup resolutionRadioGroup = (RadioGroup) dialog.findViewById(R.id.resolutionGroup);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier("camera"+conf.data.get("interval")+"sec", "id", getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make radio resolution button clicked:
        int lastResolutionId = getResources().getIdentifier("r"+conf.data.get("resolution"), "id", getPackageName());
        RadioButton LastResolutionButton = (RadioButton) dialog.findViewById(lastResolutionId);
        LastResolutionButton.setChecked(true);

        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int intervalSelectedId = intervalRadioGroup.getCheckedRadioButtonId();
                int resolutionSelectedId = resolutionRadioGroup.getCheckedRadioButtonId();

                if(intervalSelectedId ==2131493000)
                {
                    conf.changeData("interval",10);
                    Logger.append("Camera interval changed to 10 sec");
                }
                if(intervalSelectedId ==2131493001)
                {
                    conf.changeData("interval",30);
                    Logger.append("Camera interval changed to 30 sec");
                }
                if(intervalSelectedId ==2131493002)
                {
                    conf.changeData("interval",60);
                    Logger.append("Camera interval changed to 60 sec");
                }

                if(resolutionSelectedId ==2131492995)
                {
                    conf.changeData("resolution","640x480");
                    Logger.append("Camera resolution changed to 640x480");
                }
                if(resolutionSelectedId ==2131492996)
                {
                    conf.changeData("resolution","800x600");
                    Logger.append("Camera resolution changed to 800x600");
                }
                if(resolutionSelectedId ==2131492997)
                {
                    conf.changeData("resolution","1024x768");
                    Logger.append("Camera resolution changed to 1024x768");
                }
                Toast.makeText(getApplicationContext(),"Camera set to: "+conf.data.get("resolution")+" and "+conf.data.get("interval")
                       +" sec" , Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });
    }

    public void showSMSPropertiesPopUp(final config conf)
    {
        final Dialog dialog=new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.sms_popup);
        dialog.setTitle("SMS Properties");
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.SMSApprove);
        final RadioGroup SMSRadioGroup = (RadioGroup) dialog.findViewById(R.id.smsIntervalGroup);

        final EditText telephoneText = (EditText) dialog.findViewById(R.id.telNo);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier("sms"+conf.data.get("interval")+"sec", "id", getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make telephoneText show the last data:
        String lastTelephoneVal = (String)conf.data.get("telephone");
        if(lastTelephoneVal!="0000000000")
        {
            telephoneText.setText(lastTelephoneVal, TextView.BufferType.EDITABLE);
        }


        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int intervalSelectedId = SMSRadioGroup.getCheckedRadioButtonId();

                if(intervalSelectedId ==2131493041)
                {
                    conf.changeData("interval",10);
                    Logger.append("SMS interval changed to 10 sec");
                }
                if(intervalSelectedId ==2131493042)
                {
                    conf.changeData("interval",30);
                    Logger.append("SMS interval changed to 30 sec");
                }
                if(intervalSelectedId ==2131493043)
                {
                    conf.changeData("interval",60);
                    Logger.append("SMS interval changed to 60 sec");
                }

                String telephoneNumber = telephoneText.getText().toString();

                if(telephoneNumber.isEmpty())
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                    alertDialog.setTitle("Enter Phone number");
                    alertDialog.setMessage("Please write a phone destination number for sending SMS");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if(telephoneNumber.length()!=10)
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainMenuActivity.this).create();
                    alertDialog.setTitle("Enter Phone number");
                    alertDialog.setMessage("Please write a valid phone number !\nfor example: 0556699919 ");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else
                {
                    conf.changeData("telephone",telephoneNumber);
                    Toast.makeText(getApplicationContext(),"SMS will set to: "+conf.data.get("telephone")+" every "+conf.data.get("interval")
                            +" sec" , Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            }
        });
    }

    public void showSoundPropertiesPopUp(final config conf)
    {
        final Dialog dialog=new Dialog(MainMenuActivity.this);
        dialog.setContentView(R.layout.sound_popup);
        dialog.setTitle("Sound Properties");
        dialog.show();

        Button bApprove = (Button) dialog.findViewById(R.id.soundApprove);
        final RadioGroup intervalRadioGroup = (RadioGroup) dialog.findViewById(R.id.soundIntervalGroup);
        final RadioGroup durationRadioGroup = (RadioGroup) dialog.findViewById(R.id.durationGroup);

        //Make radio interval button clicked:
        int lastIntervalId = getResources().getIdentifier("iDuration"+conf.data.get("interval")+"sec", "id", getPackageName());
        RadioButton LastRadioButton = (RadioButton) dialog.findViewById(lastIntervalId);
        LastRadioButton.setChecked(true);

        //Make radio duration button clicked:
        int lastDurationId = getResources().getIdentifier("sDuration"+conf.data.get("duration")+"sec", "id", getPackageName());
        RadioButton LastDurationButton = (RadioButton) dialog.findViewById(lastDurationId);
        LastDurationButton.setChecked(true);

        bApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int intervalSelectedId = intervalRadioGroup.getCheckedRadioButtonId();
                int DurationSelectedId = durationRadioGroup.getCheckedRadioButtonId();

                if(intervalSelectedId ==2131493050)
                {
                    conf.changeData("interval",30);
                    Logger.append("Sound interval changed to 30 sec");
                }
                if(intervalSelectedId ==2131493051)
                {
                    conf.changeData("interval",60);
                    Logger.append("Sound interval changed to 60 sec");
                }
                if(intervalSelectedId ==2131493052)
                {
                    conf.changeData("interval",120);
                    Logger.append("Sound interval changed to 120 sec");
                }

                if(DurationSelectedId ==2131493046)
                {
                    conf.changeData("duration","30");
                    Logger.append("Sound duration changed to 30 sec");
                }
                if(DurationSelectedId ==2131493047)
                {
                    conf.changeData("duration","60");
                    Logger.append("Sound duration changed to 60 sec");
                }
                if(DurationSelectedId ==2131493048)
                {
                    conf.changeData("duration","120");
                    Logger.append("Sound duration changed to 120 sec");
                }
                Toast.makeText(getApplicationContext(),"Sound set to duration: "+conf.data.get("duration")+"sec, every "+conf.data.get("interval")
                        +" sec" , Toast.LENGTH_SHORT).show();
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




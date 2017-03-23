package mobilab.mobilab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;

public class MainMenuActivity extends AppCompatActivity {

    /**
     * check box configuration object to hold configuration for each sensor
     */

    class config {
        private CheckBox checkBox;
        private Boolean active;
        private Object data[];
        private String id;
        private Boolean alertDialog = false;

        public config(CheckBox cb, Boolean bl, int dataCapacity, String name) {
            this.id = name;
            this.checkBox = cb;
            this.active = bl;
            if (dataCapacity > -1) {
                this.data = new Object[dataCapacity];
                alertDialog = true;
            }
        }

        public Boolean getState() {
            return this.active;
        }

        public void changeState() {
            active = !active;
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
    public void showPopup(config conf) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = null;
        switch (conf.id) {
            case "camera":
                dialogView = inflater.inflate(R.layout.camera_popup, null);
                break;
            case "sms":
                dialogView = inflater.inflate(R.layout.sms_popup, null);
                break;
            case "sound":
                dialogView = inflater.inflate(R.layout.sound_popup, null);
                break;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                    
//                take data from popup
//                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.resolutionGroup);
//                get selected radio button from radioGroup
//                int selectedId = radioGroup.getCheckedRadioButtonId();
//                find the radio button by returned id
//                RadioButton radioButton = (RadioButton) findViewById(whichButton);
//                Toast.makeText(MainMenuActivity.this, selectedId + "", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });
        builder.show();
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




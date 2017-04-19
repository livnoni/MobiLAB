package mobilab.mobilab;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashMap;

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
    private static final String ID = "id";
    private static final String INTERVAL = "interval";
    private static final String TELEPHONE = "telephone";

    private HashMap<String, Object> _gps, _camera, _sms, _temperature, _battery, _sound, _barometer, _externalSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        Logger.append("THIS IS TEST " + intent.getSerializableExtra(GPS));

        HashMap<String, Object> tmp = null;
        if ((intent.getSerializableExtra(GPS)) != null) {
            //_gps = new HashMap<>(tmp);
            Logger.append("GOT\t" + tmp.toString());
        }

    }
}

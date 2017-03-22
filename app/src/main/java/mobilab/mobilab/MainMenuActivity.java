package mobilab.mobilab;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainMenuActivity extends AppCompatActivity {

    Button button;
    Logger logger;

    CheckBox gpsCB,cameraCB,smsCB,temperatureCB,batteryLevelCB,soundCB,barometerCB,ExternalSensorsCB;
    boolean gps=false,camera=false,sms=false,temperature=false,batteryLevel=false,sound=false,barometer=false,ExternalSensors=false;

    CheckBox [] checkBoxes = new CheckBox[8];
    boolean [] boolCheckBoxes = new boolean[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsCB = (CheckBox) findViewById(R.id.gpsCB);
        cameraCB = (CheckBox) findViewById(R.id.cameraCB);
        smsCB = (CheckBox) findViewById(R.id.smsCB);
        temperatureCB = (CheckBox) findViewById(R.id.temperatureCB);
        temperatureCB = (CheckBox) findViewById(R.id.temperatureCB);
        batteryLevelCB = (CheckBox) findViewById(R.id.batteryLevelCB);
        soundCB = (CheckBox) findViewById(R.id.soundCB);
        barometerCB = (CheckBox) findViewById(R.id.barometerCB);
        ExternalSensorsCB = (CheckBox) findViewById(R.id.ExternalSensorsCB);
        
//        for (int i=0;i<8;i++)
//        {
//            checkBoxes[i].setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    boolCheckBoxes[i] = !boolCheckBoxes;
//
//                }
//            });
//        }

        gpsCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gps =!gps;
                logger.writeLog("gps set to: "+gps);
            }
        });


        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                logger.writeLog("this is check.");
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

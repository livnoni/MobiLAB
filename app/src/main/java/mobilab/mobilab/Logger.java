package mobilab.mobilab;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger extends Application {
    private static final String NAME = "MobiLAB";
    private static final String FILE_NAME = "mainlog";
    private static final String SHUTDOWN = "logger shut down.";
    private static final String ERROR = "failed to create directory";
    private static final String TAG = "DEBUG";
    private static final String _logger = "Logger: ";
    private static File logger;
    private static FileOutputStream fos;

    public Logger(Activity activity) throws IOException {
        logger = getOutputMediaFile();
        verifyStoragePermissions(activity);
        fos = new FileOutputStream(logger);

    }
///////////////////////////////

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    /////////////////////////////
    public static void append(String message) {
        if (fos != null) {
            try {
                fos.write(("[" + (new Date(System.currentTimeMillis())) + "]:\t" + message + "\n").getBytes());
                Log.i(_logger, message + "");
            } catch (IOException e) {
                Logger.append(e.getMessage());
                e.printStackTrace();

            }
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                Log.e(TAG, ERROR);
                return null;
            }
        }
        // Create a media file name
        return new File(mediaStorageDir.getPath() + File.separator + FILE_NAME+"_"+System.currentTimeMillis()+".txt");
    }

    public static void onDestroy() throws IOException {
        append(SHUTDOWN);
        fos.close();
    }
}

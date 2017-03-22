package mobilab.mobilab;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger extends Application {
    private final String TAG = "DEBUG";
    private static File logger;
    private static FileOutputStream fos;

    public Logger() throws IOException {
        logger = getOutputMediaFile();
        fos = new FileOutputStream(logger);
    }

    public static void append(String message) {
        try {
            fos.write(("[" + (new Date(System.currentTimeMillis())) + "]:\t" + message + "\n").getBytes());
            Log.i("Logger:", message + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MobiLAB");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        return new File(mediaStorageDir.getPath() + File.separator + "mainlog.txt");
    }

    public static void onDestroy() throws IOException {
        append("logger shut down.");
        fos.close();
    }


}

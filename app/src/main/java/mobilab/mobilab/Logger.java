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
    private static final String NAME = "MobiLAB";
    private static final String FILE_NAME = "mainlog.txt";
    private static final String SHUTDOWN = "logger shut down.";
    private static final String ERROR = "failed to create directory";
    private static final String TAG = "DEBUG";
    private static final String _logger = "Logger: ";
    private static File logger;
    private static FileOutputStream fos;

    public Logger() throws IOException {
        logger = getOutputMediaFile();
        fos = new FileOutputStream(logger);
    }

    public static void append(String message) {
        try {
            fos.write(("[" + (new Date(System.currentTimeMillis())) + "]:\t" + message + "\n").getBytes());
            Log.i(_logger, message + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, ERROR);
                return null;
            }
        }
        // Create a media file name
        return new File(mediaStorageDir.getPath() + File.separator + FILE_NAME);
    }

    public static void onDestroy() throws IOException {
        append(SHUTDOWN);
        fos.close();
    }
}
